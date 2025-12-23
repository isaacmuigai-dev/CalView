package com.example.calview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.calview.ui.theme.CalViewTheme
import com.example.calview.ui.screens.DashboardScreen
import com.example.calview.feature.dashboard.DashboardViewModel
import com.example.calview.ui.screens.SettingsScreen
import com.example.calview.feature.onboarding.OnboardingNavHost
import com.example.calview.ui.screens.ScannerScreen
import com.example.calview.feature.scanner.ScannerViewModel
import com.example.calview.ui.screens.ProgressScreen
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
                onScanClick = { navController.navigate("scanner") }
            )
        }
        
        composable("scanner") {
            val scannerViewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                viewModel = scannerViewModel,
                onClose = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainTabs(onScanClick: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
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
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Progress") },
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
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
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
                onClick = onScanClick,
                containerColor = Color.Black,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .offset(y = 48.dp) // Move FAB down to sit on bottom bar or near it
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Food", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> {
                    val dashboardViewModel: DashboardViewModel = hiltViewModel()
                    DashboardScreen(
                        viewModel = dashboardViewModel
                    )
                }
                1 -> ProgressScreen()
                2 -> SettingsScreen()
            }
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
