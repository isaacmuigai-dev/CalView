package com.example.calview.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calview.core.ui.components.CalAICard

@Composable
fun SettingsScreen() {
    SettingsContent(
        userName = "Isaac muigai",
        ageStr = "27 years old"
    )
}

@Composable
fun SettingsContent(
    userName: String,
    ageStr: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9F9F9))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileHeader(name = userName, age = ageStr)
        
        InviteFriendsCard()
        
        SettingsList()
        
        PreferencesSection()
        
        WidgetsSection()
        
        LegalSection()
        
        LogoutButton()
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsContent(
        userName = "Isaac muigai",
        ageStr = "27 years old"
    )
}

@Composable
fun ProfileHeader(name: String, age: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                // Mock avatar
                Icon(Icons.Default.Person, null, modifier = Modifier.fillMaxSize().padding(12.dp), tint = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(age, fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun InviteFriendsCard() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Group, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invite friends", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
            ) {
                // Mock image background
                Column(
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterStart)
                ) {
                    Text(
                        "The journey\nis easier together.",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text("Refer a friend to earn $10", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsList() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingsItem(Icons.Default.Badge, "Personal details")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Refresh, "Adjust macronutrients")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Flag, "Goal & current weight")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.History, "Weight history")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Translate, "Language")
        }
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PreferencesSection() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Preferences", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            PreferenceToggle("Appearance", "Choose light, dark, or system appearance", true)
            PreferenceToggle("Add Burned Calories", "Add burned calories to daily goal", true)
            PreferenceToggle("Rollover calories", "Add up to 200 left over calories from yesterday", true)
            PreferenceToggle("Badge Celebrations", "Show celebrations when you unlock new badges", true)
        }
    }
}

@Composable
fun PreferenceToggle(title: String, subtitle: String, initialValue: Boolean) {
    var checked by remember { mutableStateOf(initialValue) }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = { checked = it },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Black,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun WidgetsSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Widgets", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("How to add?", fontSize = 14.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            item { WidgetMockup1() }
            item { WidgetMockup2() }
            item { WidgetMockup3() }
            item { WidgetMockup4() }
        }
    }
}

@Composable
fun WidgetMockup1() {
    Surface(
        modifier = Modifier.size(width = 160.dp, height = 180.dp),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(progress = 0.5f, color = Color.Black, strokeWidth = 8.dp)
                Text("1705", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("+ Log your food", color = Color.White, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun WidgetMockup2() {
    Surface(
        modifier = Modifier.size(width = 240.dp, height = 180.dp),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(progress = 0.5f, color = Color.LightGray, strokeWidth = 8.dp)
                Text("1705", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                WidgetMacroItem(Color(0xFFD64D50), Icons.Default.Favorite, "117g", "Protein left")
                WidgetMacroItem(Color(0xFFE5A87B), Icons.Default.Grass, "203g", "Carbs left")
                WidgetMacroItem(Color(0xFF6A8FB3), Icons.Default.Opacity, "47g", "Fats left")
            }
        }
    }
}

@Composable
fun WidgetMockup3() {
    Surface(
        modifier = Modifier.size(width = 160.dp, height = 180.dp),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
           Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
               Box(modifier = Modifier.size(60.dp).background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                   Column(horizontalAlignment = Alignment.CenterHorizontally) {
                       Icon(Icons.Default.FilterCenterFocus, null, modifier = Modifier.size(16.dp))
                       Text("Scan Food", fontSize = 8.sp)
                   }
               }
                Box(modifier = Modifier.size(60.dp).background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                   Column(horizontalAlignment = Alignment.CenterHorizontally) {
                       Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(16.dp))
                       Text("Barcode", fontSize = 8.sp)
                   }
               }
           }
        }
    }
}

@Composable
fun WidgetMockup4() {
    Surface(
        modifier = Modifier.size(width = 160.dp, height = 180.dp),
        color = Color.White,
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.LocalFireDepartment, null, tint = Color(0xFFE5A87B), modifier = Modifier.size(100.dp))
            Text("0", fontWeight = FontWeight.Bold, fontSize = 24.sp, modifier = Modifier.padding(top = 24.dp))
        }
    }
}

@Composable
fun WidgetMacroItem(color: Color, icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 8.sp, color = Color.Gray)
        }
    }
}

@Composable
fun LegalSection() {
    CalAICard(modifier = Modifier.fillMaxWidth()) {
        Column {
            SettingsItem(Icons.Default.Description, "Terms and Conditions")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Security, "Privacy Policy")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Email, "Support Email")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.Campaign, "Feature Request")
            Divider(color = Color(0xFFF3F3F3), modifier = Modifier.padding(horizontal = 16.dp))
            SettingsItem(Icons.Default.PersonRemove, "Delete Account?")
        }
    }
}

@Composable
fun LogoutButton() {
    Button(
        onClick = { },
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Logout, null, tint = Color.Black)
            Text(" Logout", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
    Text(
        "VERSION 1.0.184",
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        fontSize = 12.sp,
        color = Color.LightGray
    )
}
