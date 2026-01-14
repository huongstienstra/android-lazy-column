package com.example.myapplication.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlin.random.Random

sealed class ListItem {
    abstract val id: String
    data class Header(override val id: String, val title: String) : ListItem()
    data class Content(override val id: String, val text: String, val value: Int) : ListItem()
    data class Footer(override val id: String, val summary: String) : ListItem()
    data class Advertisement(override val id: String, val adText: String) : ListItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTypeDemo(navController: NavController) {
    val listState = rememberLazyListState()
    var useContentType by remember { mutableStateOf(true) }

    // Track recycling by content type
    val recyclingStats = remember { mutableStateMapOf<String, Int>() }
    val compositionsByType = remember { mutableStateMapOf<String, Int>() }

    // Generate mixed content list
    val items = remember {
        buildList {
            for (section in 0..20) {
                add(ListItem.Header("header_$section", "Section $section"))
                for (item in 0..Random.nextInt(3, 8)) {
                    add(ListItem.Content("content_${section}_$item", "Item $item", Random.nextInt(100)))
                }
                if (section % 3 == 0) {
                    add(ListItem.Advertisement("ad_$section", "Special Offer #$section"))
                }
                add(ListItem.Footer("footer_$section", "End of Section $section"))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Content Type Demo") },
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
                    Text(
                        "Content Type Recycling",
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TypeStats("Headers", compositionsByType["header"] ?: 0, Color.Blue)
                        TypeStats("Content", compositionsByType["content"] ?: 0, Color.Green)
                        TypeStats("Footers", compositionsByType["footer"] ?: 0, Color.Red)
                        TypeStats("Ads", compositionsByType["ad"] ?: 0, Color.Yellow)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Use contentType parameter")
                        Switch(
                            checked = useContentType,
                            onCheckedChange = {
                                useContentType = it
                                compositionsByType.clear()
                                recyclingStats.clear()
                            }
                        )
                    }

                    if (useContentType) {
                        Text(
                            "✅ Items recycled within same type only",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Green
                        )
                    } else {
                        Text(
                            "⚠️ Any item can be recycled as any type",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Red
                        )
                    }
                }
            }

            // LazyColumn with content types
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(
                    items = items,
                    key = { it.id },
                    contentType = { item ->
                        if (useContentType) {
                            when (item) {
                                is ListItem.Header -> "header"
                                is ListItem.Content -> "content"
                                is ListItem.Footer -> "footer"
                                is ListItem.Advertisement -> "ad"
                            }
                        } else {
                            // No content type - all items treated the same
                            "default"
                        }
                    }
                ) { item ->
                    val contentType = when (item) {
                        is ListItem.Header -> "header"
                        is ListItem.Content -> "content"
                        is ListItem.Footer -> "footer"
                        is ListItem.Advertisement -> "ad"
                    }

                    LaunchedEffect(item.id) {
                        compositionsByType[contentType] =
                            (compositionsByType[contentType] ?: 0) + 1
                    }

                    when (item) {
                        is ListItem.Header -> HeaderItemView(item)
                        is ListItem.Content -> ContentItemView(item)
                        is ListItem.Footer -> FooterItemView(item)
                        is ListItem.Advertisement -> AdItemView(item)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderItemView(item: ListItem.Header) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = item.title,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ContentItemView(item: ListItem.Content) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.text)
            Badge {
                Text(item.value.toString())
            }
        }
    }
}

@Composable
fun FooterItemView(item: ListItem.Footer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = item.summary,
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AdItemView(item: ListItem.Advertisement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Yellow.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "📢 ${item.adText}",
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF6B00)
            )
        }
    }
}

@Composable
fun TypeStats(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}