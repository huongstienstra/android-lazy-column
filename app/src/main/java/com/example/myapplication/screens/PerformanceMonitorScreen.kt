package com.example.myapplication.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.system.measureNanoTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceMonitorScreen(navController: NavController) {
    val listState = rememberLazyListState()
    var itemCount by remember { mutableStateOf(100) }
    var enableHeavyItems by remember { mutableStateOf(false) }
    var showPerformanceOverlay by remember { mutableStateOf(true) }

    // Performance metrics
    val frameTimeHistory = remember { mutableStateListOf<Long>() }
    val recompositionCounts = remember { mutableStateMapOf<Int, Int>() }
    var totalRecompositions by remember { mutableStateOf(0) }
    var averageFrameTime by remember { mutableStateOf(0L) }
    var maxFrameTime by remember { mutableStateOf(0L) }

    // Track frame time
    LaunchedEffect(Unit) {
        while (isActive) {
            val frameTime = measureNanoTime {
                delay(16) // Simulate 60 FPS
            } / 1_000_000 // Convert to milliseconds

            frameTimeHistory.add(frameTime)
            if (frameTimeHistory.size > 60) {
                frameTimeHistory.removeAt(0)
            }

            averageFrameTime = frameTimeHistory.average().toLong()
            maxFrameTime = frameTimeHistory.maxOrNull() ?: 0L
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Monitor") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
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
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Performance Metrics",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Frame time graph
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                        ) {
                            FrameTimeGraph(
                                frameTimeHistory = frameTimeHistory,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MetricCard(
                                label = "Avg Frame",
                                value = "${averageFrameTime}ms",
                                icon = Icons.Default.Add,
                                color = when {
                                    averageFrameTime < 16 -> Color.Green
                                    averageFrameTime < 33 -> Color.Yellow
                                    else -> Color.Red
                                }
                            )
                            MetricCard(
                                label = "Max Frame",
                                value = "${maxFrameTime}ms",
                                icon = Icons.Default.Warning,
                                color = when {
                                    maxFrameTime < 16 -> Color.Green
                                    maxFrameTime < 33 -> Color.Yellow
                                    else -> Color.Red
                                }
                            )
                            MetricCard(
                                label = "Recompositions",
                                value = totalRecompositions.toString(),
                                icon = Icons.Default.Refresh,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Divider()

                        // Controls
                        Column {
                            Text("Item Count: $itemCount")
                            Slider(
                                value = itemCount.toFloat(),
                                onValueChange = { itemCount = it.toInt() },
                                valueRange = 50f..500f,
                                steps = 8
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Heavy Items (simulate load)")
                            Switch(
                                checked = enableHeavyItems,
                                onCheckedChange = { enableHeavyItems = it }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Performance Overlay")
                            Switch(
                                checked = showPerformanceOverlay,
                                onCheckedChange = { showPerformanceOverlay = it }
                            )
                        }
                    }
                }

                // Test LazyColumn
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(itemCount) { index ->
                        PerformanceTestItem(
                            index = index,
                            isHeavy = enableHeavyItems,
                            onRecompose = {
                                recompositionCounts[index] = (recompositionCounts[index] ?: 0) + 1
                                totalRecompositions++
                            },
                            recomposeCount = recompositionCounts[index] ?: 0
                        )
                    }
                }
            }

            // Performance Overlay
            if (showPerformanceOverlay) {
                PerformanceOverlay(
                    modifier = Modifier.align(Alignment.TopEnd),
                    fps = if (averageFrameTime > 0) (1000 / averageFrameTime).toInt() else 60,
                    frameTime = averageFrameTime,
                    itemsVisible = listState.layoutInfo.visibleItemsInfo.size
                )
            }
        }
    }
}

@Composable
fun PerformanceTestItem(
    index: Int,
    isHeavy: Boolean,
    onRecompose: () -> Unit,
    recomposeCount: Int
) {
    LaunchedEffect(Unit) {
        onRecompose()
    }

    // Simulate heavy computation if enabled
    if (isHeavy) {
        remember {
            // Simulate expensive calculation
            (0..1000).map { it * it }.sum()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                recomposeCount == 0 -> MaterialTheme.colorScheme.surface
                recomposeCount < 3 -> Color.Green.copy(alpha = 0.1f)
                recomposeCount < 10 -> Color.Yellow.copy(alpha = 0.1f)
                else -> Color.Red.copy(alpha = 0.1f)
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
                Text(
                    "Recomposed: $recomposeCount times",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (recomposeCount > 0) {
                Badge(
                    containerColor = when {
                        recomposeCount < 3 -> Color.Green
                        recomposeCount < 10 -> Color.Yellow
                        else -> Color.Red
                    }
                ) {
                    Text(recomposeCount.toString())
                }
            }
        }
    }
}

@Composable
fun FrameTimeGraph(
    frameTimeHistory: List<Long>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier.background(surfaceColor)) {
        if (frameTimeHistory.isEmpty()) return@Canvas

        val maxValue = (frameTimeHistory.maxOrNull() ?: 33L).coerceAtLeast(33L)
        val width = size.width
        val height = size.height
        val stepX = width / 60f
        val centerY = height / 2

        // Draw threshold lines
        val threshold16Y = height - (16f / maxValue * height)
        val threshold33Y = height - (33f / maxValue * height)

        drawLine(
            color = Color.Green.copy(alpha = 0.3f),
            start = Offset(0f, threshold16Y),
            end = Offset(width, threshold16Y),
            strokeWidth = 1.dp.toPx()
        )

        drawLine(
            color = Color.Yellow.copy(alpha = 0.3f),
            start = Offset(0f, threshold33Y),
            end = Offset(width, threshold33Y),
            strokeWidth = 1.dp.toPx()
        )

        // Draw frame time line
        val path = Path()
        frameTimeHistory.forEachIndexed { index, frameTime ->
            val x = index * stepX
            val y = height - (frameTime.toFloat() / maxValue * height)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

@Composable
fun PerformanceOverlay(
    modifier: Modifier = Modifier,
    fps: Int,
    frameTime: Long,
    itemsVisible: Int
) {
    Card(
        modifier = modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.8f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                "FPS: $fps",
                color = when {
                    fps >= 55 -> Color.Green
                    fps >= 30 -> Color.Yellow
                    else -> Color.Red
                },
                fontWeight = FontWeight.Bold
            )
            Text(
                "Frame: ${frameTime}ms",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "Visible: $itemsVisible",
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MetricCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = color
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
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