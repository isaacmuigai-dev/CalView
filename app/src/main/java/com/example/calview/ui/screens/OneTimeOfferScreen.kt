package com.example.calview.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun OneTimeOfferScreen(
    onStartTrial: () -> Unit,
    onClose: () -> Unit
) {
    var freeTrialEnabled by remember { mutableStateOf(true) }

    Scaffold(containerColor = Color.White) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.Default.Close, null)
                }
                Text(
                    "Your one-time offer",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                color = Color.Black,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "80% OFF\nFOREVER",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "KES4,500.00",
                fontSize = 24.sp,
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = TextDecoration.LineThrough
                )
            )
            Text(
                text = "KES250.00\n/mo",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD64D50),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            HighlightItem("☕", "Less than a coffee.")
            HighlightItem("⚠️", "Close this screen? This price is gone")
            HighlightItem("🙋", "What are you waiting for?")

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Free Trial Enabled", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Switch(
                    checked = freeTrialEnabled,
                    onCheckedChange = { freeTrialEnabled = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = Color.Black)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Yearly Plan", fontWeight = FontWeight.Bold)
                        Text("KES250.00 /mo", fontWeight = FontWeight.Bold)
                    }
                    Text("12mo • KES3,000.00", color = Color.Gray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartTrial,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Free Trial", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                Text(" No Commitment - Cancel Anytime", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HighlightItem(emoji: String, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true)
@Composable
fun OneTimeOfferScreenPreview() {
    OneTimeOfferScreen(
        onStartTrial = {},
        onClose = {}
    )
}
