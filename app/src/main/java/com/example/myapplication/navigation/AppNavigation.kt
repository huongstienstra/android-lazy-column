package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("virtualization") {
            VirtualizationDemoScreen(navController = navController)
        }
        composable("recycling") {
            RecyclingVisualizerScreen(navController = navController)
        }
        composable("performance") {
            PerformanceMonitorScreen(navController = navController)
        }
        composable("viewport") {
            ViewportVisualizationScreen(navController = navController)
        }
        composable("comparison") {
            ComparisonDemoScreen(navController = navController)
        }
        composable("scroll_mechanics") {
            ScrollMechanicsScreen(navController = navController)
        }
        composable("content_type") {
            ContentTypeDemo(navController = navController)
        }
    }
}