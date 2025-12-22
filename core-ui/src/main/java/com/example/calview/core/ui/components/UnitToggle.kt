package com.example.calview.core.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.theme.Inter

@Composable
fun UnitToggle(
    isMetric: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .width(200.dp)
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Imperial",
            fontSize = 18.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            color = if (!isMetric) Color.Black else Color.LightGray,
            modifier = Modifier.clickable { onToggle(false) }
        )

        Switch(
            checked = isMetric,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFE5E5E5),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E5E5),
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent
            )
        )

        Text(
            text = "Metric",
            fontSize = 18.sp,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            color = if (isMetric) Color.Black else Color.LightGray,
            modifier = Modifier.clickable { onToggle(true) }
        )
    }
}
