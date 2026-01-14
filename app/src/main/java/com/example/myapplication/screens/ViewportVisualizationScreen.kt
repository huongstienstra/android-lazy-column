package com.example.myapplication.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewportVisualizationScreen(navController: NavController) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    var showViewportBounds by remember { mutableStateOf(true) }
    var showBufferZones by remember { mutableStateOf(true) }
    var showItemBounds by remember { mutableStateOf(true) }
    var bufferSize by remember { mutableStateOf(2) }

    // Track viewport information
    val visibleItemsInfo = listState.layoutInfo.visibleItemsInfo
    val viewportStartOffset = listState.layoutInfo.viewportStartOffset
    val viewportEndOffset = listState.layoutInfo.viewportEndOffset
    val totalItemsCount = listState.layoutInfo.totalItemsCount

    var lazyColumnSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viewport Visualization") },
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
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content
            Column(
                modifier = Modifier.weight(1f)
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
                                "Viewport Information",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoCard(
                                label = "Visible Items",
                                value = "${visibleItemsInfo.size}"
                            )
                            InfoCard(
                                label = "First Index",
                                value = "${visibleItemsInfo.firstOrNull()?.index ?: 0}"
                            )
                            InfoCard(
                                label = "Last Index",
                                value = "${visibleItemsInfo.lastOrNull()?.index ?: 0}"
                            )
                        }

                        Divider()

                        // Toggle controls
                        ToggleRow(
                            label = "Show Viewport Bounds",
                            checked = showViewportBounds,
                            onCheckedChange = { showViewportBounds = it }
                        )

                        ToggleRow(
                            label = "Show Buffer Zones",
                            checked = showBufferZones,
                            onCheckedChange = { showBufferZones = it }
                        )

                        ToggleRow(
                            label = "Show Item Bounds",
                            checked = showItemBounds,
                            onCheckedChange = { showItemBounds = it }
                        )

                        // Buffer size control
                        Column {
                            Text("Buffer Size: $bufferSize items")
                            Slider(
                                value = bufferSize.toFloat(),
                                onValueChange = { bufferSize = it.toInt() },
                                valueRange = 0f..5f,
                                steps = 5
                            )
                        }
                    }
                }

                // LazyColumn with viewport visualization
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .onGloballyPositioned { coordinates ->
                                lazyColumnSize = coordinates.size.toSize()
                            }
                            .drawBehind {
                                if (showViewportBounds) {
                                    drawViewportBounds()
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(100) { index ->
                            ViewportItem(
                                index = index,
                                isVisible = visibleItemsInfo.any { it.index == index },
                                isInBuffer = index >= (visibleItemsInfo.firstOrNull()?.index ?: 0) - bufferSize &&
                                        index <= (visibleItemsInfo.lastOrNull()?.index ?: 0) + bufferSize,
                                showItemBounds = showItemBounds
                            )
                        }
                    }

                    // Viewport overlay indicators
                    if (showViewportBounds) {
                        // Top viewport boundary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Red)
                                .align(Alignment.TopCenter)
                        )
                        // Bottom viewport boundary
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color.Red)
                                .align(Alignment.BottomCenter)
                        )
                    }

                    if (showBufferZones && bufferSize > 0) {
                        // Buffer zone indicators
                        val itemHeight = with(density) { 88.dp.toPx() } // 80dp card + 8dp spacing
                        val bufferHeight = itemHeight * bufferSize

                        // Top buffer zone
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(with(density) { bufferHeight.toDp() })
                                .background(Color.Blue.copy(alpha = 0.1f))
                                .border(1.dp, Color.Blue.copy(alpha = 0.3f))
                                .align(Alignment.TopCenter)
                        )

                        // Bottom buffer zone
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(with(density) { bufferHeight.toDp() })
                                .background(Color.Blue.copy(alpha = 0.1f))
                                .border(1.dp, Color.Blue.copy(alpha = 0.3f))
                                .align(Alignment.BottomCenter)
                        )
                    }
                }
            }

            // Minimap
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        "Minimap",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        drawMinimap(
                            totalItems = 100,
                            visibleRange = visibleItemsInfo.firstOrNull()?.index to
                                          visibleItemsInfo.lastOrNull()?.index,
                            bufferSize = bufferSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ViewportItem(
    index: Int,
    isVisible: Boolean,
    isInBuffer: Boolean,
    showItemBounds: Boolean
) {
    val backgroundColor = when {
        isVisible -> Color.Green.copy(alpha = 0.2f)
        isInBuffer -> Color.Blue.copy(alpha = 0.1f)
        else -> MaterialTheme.colorScheme.surface
    }

    val borderColor = when {
        isVisible && showItemBounds -> Color.Green
        isInBuffer && showItemBounds -> Color.Blue
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(
                width = if (showItemBounds && (isVisible || isInBuffer)) 2.dp else 0.dp,
                color = borderColor
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
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
                    when {
                        isVisible -> "📍 In Viewport"
                        isInBuffer -> "🔵 In Buffer Zone"
                        else -> "⚪ Outside View"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isVisible) {
                Badge(
                    containerColor = Color.Green
                ) {
                    Text("VISIBLE")
                }
            } else if (isInBuffer) {
                Badge(
                    containerColor = Color.Blue
                ) {
                    Text("BUFFER")
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    label: String,
    value: String
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
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
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
fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

fun DrawScope.drawViewportBounds() {
    // Draw subtle viewport background
    drawRect(
        color = Color.Green.copy(alpha = 0.05f),
        topLeft = Offset.Zero,
        size = size
    )
}

fun DrawScope.drawMinimap(
    totalItems: Int,
    visibleRange: Pair<Int?, Int?>,
    bufferSize: Int
) {
    val itemHeight = size.height / totalItems

    // Draw all items as background
    drawRect(
        color = Color.Gray.copy(alpha = 0.2f),
        topLeft = Offset.Zero,
        size = size
    )

    // Draw buffer zones
    if (bufferSize > 0 && visibleRange.first != null && visibleRange.second != null) {
        val bufferStart = (visibleRange.first!! - bufferSize).coerceAtLeast(0)
        val bufferEnd = (visibleRange.second!! + bufferSize).coerceAtMost(totalItems - 1)

        drawRect(
            color = Color.Blue.copy(alpha = 0.3f),
            topLeft = Offset(0f, bufferStart * itemHeight),
            size = Size(size.width, (bufferEnd - bufferStart + 1) * itemHeight)
        )
    }

    // Draw visible items
    if (visibleRange.first != null && visibleRange.second != null) {
        val visibleStart = visibleRange.first!! * itemHeight
        val visibleHeight = (visibleRange.second!! - visibleRange.first!! + 1) * itemHeight

        drawRect(
            color = Color.Green.copy(alpha = 0.5f),
            topLeft = Offset(0f, visibleStart),
            size = Size(size.width, visibleHeight)
        )

        // Draw viewport border
        drawRect(
            color = Color.Green,
            topLeft = Offset(0f, visibleStart),
            size = Size(size.width, visibleHeight),
            style = Stroke(width = 2f)
        )
    }
}