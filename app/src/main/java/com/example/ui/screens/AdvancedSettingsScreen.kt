package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.CallWithContact
import com.example.data.model.Contact
import com.example.ui.components.BulkCallDialog
import com.example.ui.components.CallDirectionIcon
import com.example.ui.components.ContactAvatar
import com.example.ui.components.EditCallRecordDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSettingsScreen(
    recentCalls: List<CallWithContact>,
    allContacts: List<Contact>,
    onBackClick: () -> Unit,
    onSaveCallRecord: (
        recordId: Long,
        phoneNumber: String,
        contactId: Long?,
        callType: CallType,
        timestamp: Long,
        durationSeconds: Int
    ) -> Unit,
    onSaveBulkCalls: (rawNumbersText: String, callType: CallType) -> Unit,
    onDeleteCallRecord: (CallRecord) -> Unit,
    simulatedVersionCode: Int = 2,
    onSetSimulatedVersionCode: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {

    var recordToEdit by remember { mutableStateOf<CallRecord?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showBulkDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("advanced_settings_back")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showBulkDialog = true }, modifier = Modifier.testTag("bulk_call_icon_btn")) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Bulk Add Calls")
                    }
                    IconButton(onClick = { showAddDialog = true }, modifier = Modifier.testTag("add_call_record_icon")) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Add Single Record")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header card explaining call management & bulk calls
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Call Record & Time Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Manually adjust call parameters, timestamps, or bulk log multiple phone numbers separated by commas or semicolons with staggered timestamps.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showBulkDialog = true },
                            modifier = Modifier.testTag("bulk_add_calls_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bulk Add Calls")
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.testTag("new_record_management_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Add Single")
                        }
                    }
                }
            }

            // Update Server Simulation Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Update Server Simulation",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Simulate what version the remote API / APKPure server reports when 'Check for Updates' is tapped in Settings:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = simulatedVersionCode == 2,
                            onClick = { onSetSimulatedVersionCode(2) },
                            label = { Text("v1.1.0 (New Available)") },
                            modifier = Modifier.testTag("sim_version_v1_1")
                        )
                        FilterChip(
                            selected = simulatedVersionCode == 1,
                            onClick = { onSetSimulatedVersionCode(1) },
                            label = { Text("v1.0.0 (Up To Date)") },
                            modifier = Modifier.testTag("sim_version_v1_0")
                        )
                    }
                }
            }

            if (recentCalls.isEmpty()) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No records available to manage.")
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
                        AdvancedRecordItem(
                            item = item,
                            onEditClick = { recordToEdit = item.callRecord },
                            onDeleteClick = { onDeleteCallRecord(item.callRecord) }
                        )
                    }
                }
            }
        }

        if (showBulkDialog) {
            BulkCallDialog(
                onDismiss = { showBulkDialog = false },
                onSaveBulk = { rawNumbersText, callType ->
                    onSaveBulkCalls(rawNumbersText, callType)
                    showBulkDialog = false
                }
            )
        }

        if (showAddDialog) {
            EditCallRecordDialog(
                initialRecord = null,
                contacts = allContacts,
                onDismiss = { showAddDialog = false },
                onSave = { recordId, phoneNumber, contactId, callType, timestamp, durationSeconds ->
                    onSaveCallRecord(recordId, phoneNumber, contactId, callType, timestamp, durationSeconds)
                    showAddDialog = false
                }
            )
        }

        recordToEdit?.let { record ->
            EditCallRecordDialog(
                initialRecord = record,
                contacts = allContacts,
                onDismiss = { recordToEdit = null },
                onSave = { recordId, phoneNumber, contactId, callType, timestamp, durationSeconds ->
                    onSaveCallRecord(recordId, phoneNumber, contactId, callType, timestamp, durationSeconds)
                    recordToEdit = null
                }
            )
        }
    }
}

@Composable
fun AdvancedRecordItem(
    item: CallWithContact,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateStr = rememberFormattedDate(item.callRecord.timestamp)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("adv_record_${item.callRecord.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ContactAvatar(
                name = item.contact?.name ?: item.displayName,
                colorHex = item.contact?.avatarColorHex ?: "#2196F3",
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CallDirectionIcon(callType = item.callRecord.callType, size = 14.dp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dateStr,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit record",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
