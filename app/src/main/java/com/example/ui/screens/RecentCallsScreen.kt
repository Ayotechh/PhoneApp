package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallType
import com.example.data.model.CallWithContact
import com.example.ui.components.BulkCallDialog
import com.example.ui.components.CallDirectionIcon
import com.example.ui.components.ContactAvatar
import com.example.ui.theme.CallMissedRed
import com.example.ui.viewmodel.RecentsFilter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentCallsScreen(
    recentCalls: List<CallWithContact>,
    searchQuery: String,
    currentFilter: RecentsFilter,
    onSearchQueryChange: (String) -> Unit,
    onFilterChange: (RecentsFilter) -> Unit,
    onCallClick: (String) -> Unit,
    onCallDetailsClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Search & Filter Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search recents...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recents_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentFilter == RecentsFilter.ALL,
                    onClick = { onFilterChange(RecentsFilter.ALL) },
                    label = { Text("All") },
                    modifier = Modifier.testTag("filter_all_chip")
                )
                FilterChip(
                    selected = currentFilter == RecentsFilter.MISSED,
                    onClick = { onFilterChange(RecentsFilter.MISSED) },
                    label = { Text("Missed") },
                    modifier = Modifier.testTag("filter_missed_chip")
                )
            }
        }

        if (recentCalls.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No call history",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calls made or received will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = recentCalls,
                    key = { it.callRecord.id }
                ) { item ->
                    RecentCallItem(
                        item = item,
                        onCallClick = { onCallClick(item.displayPhoneNumber) },
                        onDetailsClick = { onCallDetailsClick(item.callRecord.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RecentCallItem(
    item: CallWithContact,
    onCallClick: () -> Unit,
    onDetailsClick: () -> Unit
) {
    val dateStr = rememberFormattedDate(item.callRecord.timestamp)
    val isMissed = item.callRecord.callType == CallType.MISSED

    Card(
        onClick = onDetailsClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_call_item_${item.callRecord.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            ContactAvatar(
                name = item.contact?.name ?: item.displayName,
                colorHex = item.contact?.avatarColorHex ?: "#2196F3",
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Info Column
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isMissed) CallMissedRed else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val callDirectionText = when (item.callRecord.callType) {
                        CallType.INCOMING -> "Incoming"
                        CallType.OUTGOING -> "Outgoing"
                        CallType.MISSED -> "Missed"
                    }

                    Text(
                        text = "$callDirectionText • $dateStr",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (isMissed) "Duration: 00:00" else "Duration: ${item.formattedDuration}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            // Quick Call Icon & Info Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCallClick) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call back",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDetailsClick) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Call details",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

fun rememberFormattedDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val sdfDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault())

    return if (sdfDay.format(date) == sdfDay.format(now)) {
        val sdfTime = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdfTime.format(date)
    } else {
        val sdfFull = SimpleDateFormat("MMM d • h:mm a", Locale.getDefault())
        sdfFull.format(date)
    }
}
