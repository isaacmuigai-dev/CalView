package com.example.calview.core.data.prediction

import com.example.calview.core.data.local.WeightHistoryEntity
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class WeightPredictionEngine @Inject constructor() {

    data class PredictionResult(
        val predictedWeight30Days: Float,
        val weeklyChangeKg: Float,
        val daysToGoal: Int?,
        val projectedDate: Long?,
        val trend: Trend
    )

    enum class Trend {
        LOSING, GAINING, STABLE, INSUFFICIENT_DATA
    }

    /**
     * Calculates weight prediction based on history using linear regression.
     */
    fun predictWeight(
        history: List<WeightHistoryEntity>,
        goalWeight: Float
    ): PredictionResult {
        // 1. Need at least 3 data points over at least 3 days for a minimal trend
        if (history.size < 3) {
            return PredictionResult(0f, 0f, null, null, Trend.INSUFFICIENT_DATA)
        }

        // 2. Sort by date
        val sortedHistory = history.sortedBy { it.timestamp }
        
        // 3. Filter to last 60 days to keep trend relevant
        val sixtyDaysAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -60) }.timeInMillis
        val recentHistory = sortedHistory.filter { it.timestamp >= sixtyDaysAgo }
        
        if (recentHistory.size < 3) {
             // Fallback to all history if recent is sparse, or return insufficient
             if (sortedHistory.size >= 3) {
                 return calculateRegression(sortedHistory, goalWeight)
             }
             return PredictionResult(0f, 0f, null, null, Trend.INSUFFICIENT_DATA)
        }

        return calculateRegression(recentHistory, goalWeight)
    }

    private fun calculateRegression(data: List<WeightHistoryEntity>, goalWeight: Float): PredictionResult {
        // Normalize time: Start day = 0
        val startTime = data.first().timestamp
        val points = data.map { 
            val daysFromStart = TimeUnit.MILLISECONDS.toDays(it.timestamp - startTime).toDouble()
            Pair(daysFromStart, it.weight.toDouble())
        }

        val n = points.size.toDouble()
        val sumX = points.sumOf { it.first }
        val sumY = points.sumOf { it.second }
        val sumXY = points.sumOf { it.first * it.second }
        val sumX2 = points.sumOf { it.first * it.first }

        // Linear Regression: y = mx + c
        // m = (n*sumXY - sumX*sumY) / (n*sumX2 - sumX*sumX)
        val denominator = (n * sumX2 - sumX * sumX)
        
        if (denominator == 0.0) {
            return PredictionResult(0f, 0f, null, null, Trend.STABLE)
        }

        var slope = (n * sumXY - sumX * sumY) / denominator
        val intercept = (sumY - slope * sumX) / n

        // Dampen slope if data span is short (less than 14 days)
        val dataSpanDays = points.last().first - points.first().first
        if (dataSpanDays < 14) {
            val dampingFactor = (dataSpanDays / 14.0).coerceIn(0.2, 1.0)
            slope *= dampingFactor
        }

        // Cap weekly change to +/- 2kg/week for realism
        var weeklyChange = slope * 7
        if (weeklyChange > 2.0) {
            weeklyChange = 2.0
            slope = weeklyChange / 7.0
        } else if (weeklyChange < -2.0) {
            weeklyChange = -2.0
            slope = weeklyChange / 7.0
        }

        // Current status (approx from regression at today)
        val today = System.currentTimeMillis()
        val daysUntilToday = TimeUnit.MILLISECONDS.toDays(today - startTime).toDouble()
        
        // Use most recent weight as anchor if regression deviates too much
        val lastWeight = data.last().weight.toDouble()
        val regressionToday = slope * daysUntilToday + intercept
        
        // If regression is way off from last actual weight, anchor it closer
        val anchoredIntercept = if (Math.abs(regressionToday - lastWeight) > 2.0) {
            lastWeight - (slope * daysUntilToday)
        } else {
            intercept
        }

        // Predict 30 days from NOW
        val daysIn30Days = daysUntilToday + 30
        var predicted30 = (slope * daysIn30Days + anchoredIntercept).toFloat()
        
        // Sanity check: Don't predict negative weight
        predicted30 = predicted30.coerceAtLeast(10f) // Minimum 10kg for humans

        // Trend
        val trend = when {
            weeklyChange < -0.1 -> Trend.LOSING
            weeklyChange > 0.1 -> Trend.GAINING
            else -> Trend.STABLE
        }

        // Days to goal
        var daysToGoal: Int? = null
        var projectedDate: Long? = null

        // Only calculate goal projection if trending towards it
        val currentEffectiveWeight = regressionToday.coerceAtLeast(anchoredIntercept + slope * daysUntilToday) // Use anchored version for consistency
        val isTrendingTowardsGoal = (goalWeight < regressionToday && trend == Trend.LOSING) ||
                                    (goalWeight > regressionToday && trend == Trend.GAINING)

        if (isTrendingTowardsGoal && slope != 0.0) {
            // goal = m * days + c  =>  days = (goal - c) / m
            val daysTargetFromStart = (goalWeight - anchoredIntercept) / slope
            val daysFromToday = daysTargetFromStart - daysUntilToday
            
            if (daysFromToday > 0 && daysFromToday < 365 * 2) { // Cap at 2 years
                daysToGoal = daysFromToday.roundToInt()
                projectedDate = today + TimeUnit.DAYS.toMillis(daysToGoal.toLong())
            }
        }

        return PredictionResult(
            predictedWeight30Days = predicted30.toFloat(),
            weeklyChangeKg = weeklyChange.toFloat(),
            daysToGoal = daysToGoal,
            projectedDate = projectedDate,
            trend = trend
        )
    }
}
