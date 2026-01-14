package com.example.myapplication.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.random.Random

data class RecyclableItem(
    val id: Int,
    val color: Color,
    val createdAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecyclingVisualizerScreen(navController: NavController) {
    val listState = rememberLazyListState()
    var animateRecycling by remember { mutableStateOf(true) }
    var showDebugInfo by remember { mutableStateOf(true) }

    // Track recycled items
    val recycledPool = remember { mutableStateListOf<RecyclableItem>() }
    val activeItems = remember { mutableStateMapOf<Int, RecyclableItem>() }
    val itemHistory = remember { mutableStateMapOf<Int, MutableList<Long>>() }

    // Generate items with unique colors
    val items = remember {
        List(500) { index ->
            RecyclableItem(
                id = index,
                color = Color(
                    Random.nextFloat(),
                    Random.nextFloat(),
                    Random.nextFloat(),
                    1f
                )
            )
        }
    }

    // Track composition lifecycle
    val compositionTracker = remember { mutableStateMapOf<Int, Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recycling Visualizer") },
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
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color.Green
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Recycling Statistics",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        RecyclingStatCard(
                            label = "Pool Size",
                            value = recycledPool.size.toString(),
                            icon = Icons.Default.Add
                        )
                        RecyclingStatCard(
                            label = "Active Items",
                            value = activeItems.size.toString(),
                            icon = Icons.Default.PlayArrow
                        )
                        RecyclingStatCard(
                            label = "Total Reuses",
                            value = itemHistory.values.sumOf { it.size - 1 }.toString(),
                            icon = Icons.Default.Refresh
                        )
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Animate Recycling")
                        Switch(
                            checked = animateRecycling,
                            onCheckedChange = { animateRecycling = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Show Debug Info")
                        Switch(
                            checked = showDebugInfo,
                            onCheckedChange = { showDebugInfo = it }
                        )
                    }

                    // Legend
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Legend:", fontWeight = FontWeight.SemiBold)
                            LegendItem(color = Color.Green, text = "New composition")
                            LegendItem(color = Color.Yellow, text = "Recycled from pool")
                            LegendItem(color = Color.Red, text = "Leaving viewport")
                        }
                    }
                }
            }

            // LazyColumn with recycling visualization
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = items,
                    key = { _, item -> item.id }
                ) { index, item ->
                    RecyclableItemView(
                        item = item,
                        index = index,
                        animateRecycling = animateRecycling,
                        showDebugInfo = showDebugInfo,
                        onComposed = {
                            activeItems[index] = item
                            compositionTracker[index] = (compositionTracker[index] ?: 0) + 1

                            // Track item history
                            if (!itemHistory.containsKey(index)) {
                                itemHistory[index] = mutableListOf()
                            }
                            itemHistory[index]?.add(System.currentTimeMillis())
                        },
                        onDisposed = {
                            activeItems.remove(index)
                            recycledPool.add(item)
                        },
                        recycleCount = compositionTracker[index] ?: 0
                    )
                }
            }
        }
    }
}

@Composable
fun RecyclableItemView(
    item: RecyclableItem,
    index: Int,
    animateRecycling: Boolean,
    showDebugInfo: Boolean,
    onComposed: () -> Unit,
    onDisposed: () -> Unit,
    recycleCount: Int
) {
    var isNew by remember { mutableStateOf(true) }
    val animatedAlpha = animateFloatAsState(
        targetValue = if (isNew && animateRecycling) 0f else 1f,
        animationSpec = tween(500),
        label = "alpha"
    )

    val animatedScale = animateFloatAsState(
        targetValue = if (isNew && animateRecycling) 0.8f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        onComposed()
        isNew = false
    }

    DisposableEffect(Unit) {
        onDispose {
            onDisposed()
        }
    }

    val borderColor = when {
        recycleCount == 1 -> Color.Green
        recycleCount > 1 -> Color.Yellow
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .alpha(animatedAlpha.value)
            .scale(animatedScale.value)
            .border(
                width = if (recycleCount > 0) 2.dp else 0.dp,
                color = borderColor
            ),
        colors = CardDefaults.cardColors(
            containerColor = item.color.copy(alpha = 0.2f)
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
                    fontWeight = FontWeight.Bold,
                    color = item.color.copy(alpha = 1f)
                )
                if (showDebugInfo) {
                    Text(
                        "ID: ${item.id}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        when {
                            recycleCount == 1 -> "🆕 First composition"
                            recycleCount > 1 -> "♻️ Recycled ${recycleCount - 1} times"
                            else -> "⏳ Initializing"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (recycleCount > 1) {
                Badge(
                    containerColor = Color.Yellow
                ) {
                    Text("♻️ ${recycleCount - 1}")
                }
            }
        }
    }
}

@Composable
fun RecyclingStatCard(
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
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
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
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}