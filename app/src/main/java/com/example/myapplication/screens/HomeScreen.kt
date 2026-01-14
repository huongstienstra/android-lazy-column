package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

data class DemoItem(
    val title: String,
    val description: String,
    val route: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val demos = listOf(
        DemoItem(
            title = "Virtualization Demo",
            description = "See how LazyColumn only composes visible items",
            route = "virtualization",
            icon = Icons.Default.Add
        ),
        DemoItem(
            title = "Recycling Visualizer",
            description = "Watch items being recycled as you scroll",
            route = "recycling",
            icon = Icons.Default.Add
        ),
        DemoItem(
            title = "Performance Monitor",
            description = "Track recompositions and memory usage",
            route = "performance",
            icon = Icons.Default.Add
        ),
        DemoItem(
            title = "Viewport Visualization",
            description = "See viewport bounds and buffer zones",
            route = "viewport",
            icon = Icons.Default.Add
        ),
        DemoItem(
            title = "LazyColumn vs Column",
            description = "Compare performance between lazy and regular columns",
            route = "comparison",
            icon = Icons.Default.Add
        ),
        DemoItem(
            title = "Scroll Mechanics",
            description = "Understand fling velocity and scroll states",
            route = "scroll_mechanics",
            icon = Icons.Default.Add
        ),
        DemoItem(
            title = "Content Type Demo",
            description = "See how contentType improves recycling efficiency",
            route = "content_type",
            icon = Icons.Default.Add
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "LazyColumn Internals",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Welcome to LazyColumn Internals",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Explore how LazyColumn works under the hood through interactive visualizations and demos.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(demos) { demo ->
                Card(
                    onClick = { navController.navigate(demo.route) },
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = demo.icon,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                demo.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                demo.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}