package com.example.myapplication.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrollMechanicsScreen(navController: NavController) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll state tracking
    var scrollVelocity by remember { mutableStateOf(0f) }
    var scrollDirection by remember { mutableStateOf("IDLE") }
    var isScrollInProgress by remember { mutableStateOf(false) }
    val velocityHistory = remember { mutableStateListOf<Float>() }

    // Track scroll metrics
    var totalScrollDistance by remember { mutableStateOf(0f) }
    var flingCount by remember { mutableStateOf(0) }
    var dragCount by remember { mutableStateOf(0) }
    var lastScrollPosition by remember { mutableStateOf(0) }

    // Monitor scroll state
    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { scrolling ->
                isScrollInProgress = scrolling
                if (!scrolling) {
                    scrollDirection = "IDLE"
                    scrollVelocity = 0f
                }
            }
    }

    // Track scroll position and velocity
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset }
            .collect { offset ->
                val delta = offset - lastScrollPosition
                lastScrollPosition = offset

                if (delta != 0) {
                    totalScrollDistance += abs(delta)
                    scrollVelocity = delta.toFloat()
                    scrollDirection = if (delta > 0) "DOWN ↓" else "UP ↑"

                    velocityHistory.add(scrollVelocity)
                    if (velocityHistory.size > 50) {
                        velocityHistory.removeAt(0)
                    }

                    // Detect fling vs drag
                    if (abs(scrollVelocity) > 20) {
                        flingCount++
                    } else if (abs(scrollVelocity) > 0) {
                        dragCount++
                    }
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scroll Mechanics") },
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
            // Scroll Metrics Dashboard
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
                            "Scroll Mechanics",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Real-time scroll status
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isScrollInProgress -> Color.Green.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surface
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ScrollStatusIndicator(
                                label = "State",
                                value = if (isScrollInProgress) "SCROLLING" else "IDLE",
                                color = if (isScrollInProgress) Color.Green else Color.Gray
                            )
                            ScrollStatusIndicator(
                                label = "Direction",
                                value = scrollDirection,
                                color = when (scrollDirection) {
                                    "UP ↑" -> Color.Blue
                                    "DOWN ↓" -> Color.Red
                                    else -> Color.Gray
                                }
                            )
                            ScrollStatusIndicator(
                                label = "Velocity",
                                value = "${abs(scrollVelocity).toInt()}px",
                                color = when {
                                    abs(scrollVelocity) > 20 -> Color.Red
                                    abs(scrollVelocity) > 10 -> Color.Yellow
                                    else -> Color.Green
                                }
                            )
                        }
                    }

                    // Velocity graph
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        VelocityGraph(
                            velocityHistory = velocityHistory,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Scroll statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard(
                            label = "Total Distance",
                            value = "${(totalScrollDistance / 1000).toInt()}k px",
                            icon = Icons.Default.Add
                        )
                        StatCard(
                            label = "Flings",
                            value = flingCount.toString(),
                            icon = Icons.Default.Add
                        )
                        StatCard(
                            label = "Drags",
                            value = dragCount.toString(),
                            icon = Icons.Default.Add
                        )
                    }

                    Divider()

                    // Scroll position info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoChip(
                            label = "First Visible",
                            value = listState.firstVisibleItemIndex.toString()
                        )
                        InfoChip(
                            label = "Offset",
                            value = "${listState.firstVisibleItemScrollOffset}px"
                        )
                        InfoChip(
                            label = "Interactive",
                            value = if (listState.interactionSource.collectIsDraggedAsState().value) "YES" else "NO"
                        )
                    }

                    // Scroll control buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // Smooth scroll to top
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Top")
                        }

                        Button(
                            onClick = {
                                // Smooth scroll to middle
                                coroutineScope.launch {
                                    listState.animateScrollToItem(50)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Middle")
                        }

                        Button(
                            onClick = {
                                // Smooth scroll to bottom
                                coroutineScope.launch {
                                    listState.animateScrollToItem(99)
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Bottom")
                        }
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
                items(100) { index ->
                    ScrollTestItem(index = index)
                }
            }
        }
    }
}

@Composable
fun ScrollTestItem(index: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Item #$index",
                fontWeight = FontWeight.Bold
            )

            Text(
                "Scroll to see mechanics",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun VelocityGraph(
    velocityHistory: List<Float>,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = modifier
            .background(surfaceColor)
            .padding(8.dp)
    ) {
        if (velocityHistory.isEmpty()) return@Canvas

        val maxVelocity = velocityHistory.maxOfOrNull { abs(it) } ?: 1f
        val centerY = size.height / 2
        val stepX = size.width / 50f

        // Draw center line
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1.dp.toPx()
        )

        // Draw velocity line
        val path = Path()
        velocityHistory.forEachIndexed { index, velocity ->
            val x = index * stepX
            val y = centerY - (velocity / maxVelocity * centerY * 0.8f)

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

        // Draw velocity points
        velocityHistory.forEachIndexed { index, velocity ->
            val x = index * stepX
            val y = centerY - (velocity / maxVelocity * centerY * 0.8f)

            drawCircle(
                color = when {
                    abs(velocity) > 20 -> Color.Red
                    abs(velocity) > 10 -> Color.Yellow
                    else -> Color.Green
                },
                radius = 2.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun ScrollStatusIndicator(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun InfoChip(
    label: String,
    value: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}