package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonDemoScreen(navController: NavController) {
    var itemCount by remember { mutableStateOf(100) }
    var useLazyColumn by remember { mutableStateOf(true) }
    var showPerformanceMetrics by remember { mutableStateOf(true) }

    // Performance metrics
    var lazyColumnLoadTime by remember { mutableStateOf(0L) }
    var regularColumnLoadTime by remember { mutableStateOf(0L) }
    var lazyColumnMemory by remember { mutableStateOf(0L) }
    var regularColumnMemory by remember { mutableStateOf(0L) }

    // Recomposition tracking
    val lazyRecompositions = remember { mutableStateMapOf<Int, Int>() }
    val regularRecompositions = remember { mutableStateMapOf<Int, Int>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LazyColumn vs Column") },
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
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Performance Comparison",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Comparison metrics
                    if (showPerformanceMetrics) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ComparisonCard(
                                title = "LazyColumn",
                                loadTime = lazyColumnLoadTime,
                                memory = lazyColumnMemory,
                                recompositions = lazyRecompositions.size,
                                color = Color.Green
                            )
                            ComparisonCard(
                                title = "Column",
                                loadTime = regularColumnLoadTime,
                                memory = regularColumnMemory,
                                recompositions = regularRecompositions.size,
                                color = Color.Red
                            )
                        }
                    }

                    Divider()

                    // Item count slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Item Count: $itemCount")
                            if (itemCount > 500 && !useLazyColumn) {
                                Badge(
                                    containerColor = Color.Red
                                ) {
                                    Text("⚠️ High Memory")
                                }
                            }
                        }
                        Slider(
                            value = itemCount.toFloat(),
                            onValueChange = { itemCount = it.toInt() },
                            valueRange = 10f..1000f,
                            steps = 19
                        )
                    }

                    // Column type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = useLazyColumn,
                            onClick = {
                                useLazyColumn = true
                                lazyRecompositions.clear()
                            },
                            label = { Text("LazyColumn") },
                            leadingIcon = if (useLazyColumn) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Green.copy(alpha = 0.2f)
                            )
                        )
                        FilterChip(
                            selected = !useLazyColumn,
                            onClick = {
                                if (itemCount <= 500) {
                                    useLazyColumn = false
                                    regularRecompositions.clear()
                                }
                            },
                            label = { Text("Regular Column") },
                            leadingIcon = if (!useLazyColumn) {
                                { Icon(Icons.Default.Check, contentDescription = null) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Red.copy(alpha = 0.2f)
                            ),
                            enabled = itemCount <= 500
                        )
                    }

                    if (itemCount > 500 && !useLazyColumn) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Red.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.Red
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Regular Column disabled for >500 items to prevent memory issues",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    // Measure button
                    Button(
                        onClick = {
                            if (useLazyColumn) {
                                lazyColumnLoadTime = measureTimeMillis {
                                    // Simulate load
                                }
                                lazyColumnMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024
                            } else {
                                regularColumnLoadTime = measureTimeMillis {
                                    // Simulate load
                                }
                                regularColumnMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Measure Performance")
                    }
                }
            }

            // Content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (useLazyColumn) {
                    LazyColumnContent(
                        itemCount = itemCount,
                        onItemComposed = { index ->
                            lazyRecompositions[index] = (lazyRecompositions[index] ?: 0) + 1
                        }
                    )
                } else {
                    RegularColumnContent(
                        itemCount = itemCount,
                        onItemComposed = { index ->
                            regularRecompositions[index] = (regularRecompositions[index] ?: 0) + 1
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LazyColumnContent(
    itemCount: Int,
    onItemComposed: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(itemCount) { index ->
            ComparisonItem(
                index = index,
                type = "Lazy",
                onComposed = { onItemComposed(index) }
            )
        }
    }
}

@Composable
fun RegularColumnContent(
    itemCount: Int,
    onItemComposed: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(itemCount) { index ->
            ComparisonItem(
                index = index,
                type = "Regular",
                onComposed = { onItemComposed(index) }
            )
        }
    }
}

@Composable
fun ComparisonItem(
    index: Int,
    type: String,
    onComposed: () -> Unit
) {
    LaunchedEffect(Unit) {
        onComposed()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (type == "Lazy") {
                Color.Green.copy(alpha = 0.1f)
            } else {
                Color.Red.copy(alpha = 0.1f)
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
                    "$type Item #$index",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Type: $type Column",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (type == "Lazy") Icons.Default.Add else Icons.Default.Add,
                contentDescription = null,
                tint = if (type == "Lazy") Color.Green else Color.Red
            )
        }
    }
}

@Composable
fun ComparisonCard(
    title: String,
    loadTime: Long,
    memory: Long,
    recompositions: Int,
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
                title,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricBadge(
                    label = "Load",
                    value = "${loadTime}ms"
                )
                MetricBadge(
                    label = "Memory",
                    value = "${memory}MB"
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Compositions: $recompositions",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun MetricBadge(
    label: String,
    value: String
) {
    Column(
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