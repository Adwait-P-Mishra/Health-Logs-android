package com.adprmi.healthLogs.ui.components

import android.graphics.drawable.shapes.RoundRectShape
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import com.adprmi.healthLogs.model.SearchKind
import com.adprmi.healthLogs.model.SearchResult
import com.adprmi.healthLogs.ui.theme.MyApplicationTheme
import com.adprmi.healthLogs.ui.theme.ThemePreviews
import com.adprmi.healthLogs.util.DateUtils
import java.util.*

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(placeholder) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search icon", tint = MaterialTheme.colorScheme.outline)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.testTag("clear_search_button")) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_input"),
            shape = CircleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            singleLine = true
        )
    }
}

@Composable
fun SearchResultsList(
    results: List<SearchResult>,
    onResultClick: (SearchResult) -> Unit,
    onAddClick: (SearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        if (results.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No matches found",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                results.forEach { result ->
                    SearchResultRow(
                        result = result,
                        onClick = { onResultClick(result) },
                        onAddClick = { onAddClick(result) }
                    )
                }
            }
        }
    }
}

@Composable
fun SearchResultRow(
    result: SearchResult,
    onClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_result_item_${result.title.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (result.kind == SearchKind.EXERCISE) Icons.Default.FitnessCenter else Icons.Default.Fastfood,
                    contentDescription = null,
                    tint = if (result.kind == SearchKind.EXERCISE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = result.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (result.kind == SearchKind.EXERCISE) "Exercise" else "Meal",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Last: ${DateUtils.formatDate(Date(result.latestDate), "MMM d")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                
                VerticalDivider(
                    modifier = Modifier.height(24.dp).padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                IconButton(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Quick add",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun SearchBarPreview() {
    MyApplicationTheme {
        Column {
            SearchBar(query = "", onQueryChange = {}, placeholder = "Search exercises or meals...", onClear = {})
            SearchBar(query = "Bench Press", onQueryChange = {}, placeholder = "Search exercises or meals...", onClear = {})
        }
    }
}

@ThemePreviews
@Composable
fun SearchResultRowPreview() {
    MyApplicationTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchResultRow(
                result = SearchResult(kind = SearchKind.EXERCISE, title = "Squat", latestDate = System.currentTimeMillis()),
                onClick = {},
                onAddClick = {}
            )
            SearchResultRow(
                result = SearchResult(kind = SearchKind.MEAL, title = "Pasta", latestDate = System.currentTimeMillis()),
                onClick = {},
                onAddClick = {}
            )
        }
    }
}

@ThemePreviews
@Composable
fun SearchResultsListPreview() {
    MyApplicationTheme {
        val results = listOf(
            SearchResult(kind = SearchKind.EXERCISE, title = "Deadlift", latestDate = System.currentTimeMillis()),
            SearchResult(kind = SearchKind.MEAL, title = "Chicken Salad", latestDate = System.currentTimeMillis())
        )
        Column {
            SearchResultsList(results = results, onResultClick = {}, onAddClick = {})
            Spacer(modifier = Modifier.height(20.dp))
            SearchResultsList(results = emptyList(), onResultClick = {}, onAddClick = {})
        }
    }
}
