package com.example.calview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calview.core.ui.theme.CalViewTheme
import com.example.calview.feature.dashboard.DashboardScreen
import com.example.calview.feature.dashboard.DashboardViewModel
import com.example.calview.feature.dashboard.SettingsScreen
import com.example.calview.feature.onboarding.OnboardingNavHost
import com.example.calview.feature.scanner.LogFoodScreen
import com.example.calview.feature.scanner.ScannerScreen
import com.example.calview.feature.scanner.ScannerViewModel
import com.example.calview.feature.trends.ProgressScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.tooling.preview.Preview


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalViewTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingNavHost(
                onOnboardingComplete = {
                    navController.navigate("main") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                },
                onSignIn = { /* Sign in logic */ }
            )
        }
        
        composable("main") {
            MainTabs(
                onScanClick = { navController.navigate("scanner") },
                onFoodDatabaseClick = { navController.navigate("logFood") }
            )
        }
        
        composable("scanner") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                viewModel = scannerViewModel,
                onClose = { navController.popBackStack() }
            )
        }
        
        composable("logFood") {
            LogFoodScreen(
                onBack = { navController.popBackStack() },
                onScanFood = { navController.navigate("scanner") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabs(
    onScanClick: () -> Unit,
    onFoodDatabaseClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showCameraMenu by remember { mutableStateOf(false) }
    
    // Camera menu bottom sheet
    if (showCameraMenu) {
        ModalBottomSheet(
            onDismissRequest = { showCameraMenu = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Food Database option
                CameraMenuItem(
                    icon = Icons.Filled.Search,
                    label = "Food Database",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showCameraMenu = false
                        onFoodDatabaseClick()
                    }
                )
                
                // Scan Food option
                CameraMenuItem(
                    icon = Icons.Filled.CameraAlt,
                    label = "Scan Food",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        showCameraMenu = false
                        onScanClick()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.BarChart, contentDescription = "Progress") },
                    label = { Text("Progress") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = Color.Black,
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                // Offset for FAB
                Spacer(modifier = Modifier.width(72.dp))
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCameraMenu = true },
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 48.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Log Food", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> {
                    val dashboardViewModel: DashboardViewModel = hiltViewModel()
                    DashboardScreen(viewModel = dashboardViewModel)
                }
                1 -> ProgressScreen()
                2 -> SettingsScreen()
            }
        }
    }
}

@Composable
fun CameraMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CalViewTheme {
        Greeting("Android")
    }
}
