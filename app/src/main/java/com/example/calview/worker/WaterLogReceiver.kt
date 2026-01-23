package com.example.calview.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.calview.core.data.local.DailyLogDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * BroadcastReceiver for handling quick water log actions from notifications.
 */
@AndroidEntryPoint
class WaterLogReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var dailyLogDao: DailyLogDao
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != WaterReminderWorker.ACTION_LOG_WATER) return
        
        val amountMl = intent.getIntExtra(WaterReminderWorker.EXTRA_AMOUNT_ML, 250)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                dailyLogDao.addWater(today, amountMl)
                
                // Show confirmation toast on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "💧 +${amountMl}ml logged!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    Toast.makeText(context, "Failed to log water", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
