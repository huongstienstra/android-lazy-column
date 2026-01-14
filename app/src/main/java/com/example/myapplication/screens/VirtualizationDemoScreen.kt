package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualizationDemoScreen(navController: NavController) {
    val listState = rememberLazyListState()
    var itemCount by remember { mutableStateOf(100) }
    var showVisualization by remember { mutableStateOf(true) }

    // Track which items are currently composed
    val composedItems = remember { mutableStateMapOf<Int, Boolean>() }
    val compositionCounts = remember { mutableStateMapOf<Int, Int>() }

    // Calculate visible items
    val visibleItems = remember(listState.layoutInfo) {
        listState.layoutInfo.visibleItemsInfo.map { it.index }
    }

    val firstVisibleIndex = listState.firstVisibleItemIndex
    val lastVisibleIndex = remember(listState.layoutInfo) {
        listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: firstVisibleIndex
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Virtualization Demo") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Control Panel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Virtualization Status",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatusCard(
                            label = "Total Items",
                            value = itemCount.toString(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatusCard(
                            label = "Visible Range",
                            value = "$firstVisibleIndex-$lastVisibleIndex",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        StatusCard(
                            label = "Composed",
                            value = composedItems.size.toString(),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Item count slider
                    Column {
                        Text("Item Count: $itemCount", style = MaterialTheme.typography.labelMedium)
                        Slider(
                            value = itemCount.toFloat(),
                            onValueChange = {
                                itemCount = it.toInt()
                                composedItems.clear()
                                compositionCounts.clear()
                            },
                            valueRange = 50f..1000f,
                            steps = 18
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = showVisualization,
                            onCheckedChange = { showVisualization = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show Composition Visualization")
                    }
                }
            }

            // LazyColumn with visualization
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = List(itemCount) { it },
                        key = { index, _ -> index }
                    ) { index, item ->
                        VirtualizedItem(
                            index = index,
                            isVisible = index in visibleItems,
                            showVisualization = showVisualization,
                            onComposed = {
                                composedItems[index] = true
                                compositionCounts[index] = (compositionCounts[index] ?: 0) + 1
                            },
                            compositionCount = compositionCounts[index] ?: 0
                        )
                    }
                }

                // Viewport indicator
                if (showVisualization) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Red.copy(alpha = 0.5f))
                            .align(Alignment.TopCenter)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Red.copy(alpha = 0.5f))
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun VirtualizedItem(
    index: Int,
    isVisible: Boolean,
    showVisualization: Boolean,
    onComposed: () -> Unit,
    compositionCount: Int
) {
    LaunchedEffect(Unit) {
        onComposed()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .then(
                if (showVisualization && isVisible) {
                    Modifier.border(2.dp, Color.Green)
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                showVisualization && isVisible -> Color.Green.copy(alpha = 0.1f)
                showVisualization && compositionCount > 0 -> Color.Yellow.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Item #$index",
                    fontWeight = FontWeight.Bold
                )
                if (showVisualization) {
                    Text(
                        when {
                            isVisible -> "✅ Currently Visible"
                            compositionCount > 0 -> "♻️ Recycled (composed $compositionCount times)"
                            else -> "⏳ Not yet composed"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (showVisualization && compositionCount > 0) {
                Badge {
                    Text(compositionCount.toString())
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    label: String,
    value: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}