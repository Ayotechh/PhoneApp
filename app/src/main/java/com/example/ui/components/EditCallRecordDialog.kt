package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.Contact
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun EditCallRecordDialog(
    initialRecord: CallRecord? = null,
    contacts: List<Contact> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        recordId: Long,
        phoneNumber: String,
        contactId: Long?,
        callType: CallType,
        timestamp: Long,
        durationSeconds: Int
    ) -> Unit
) {
    val cal = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialRecord?.timestamp ?: System.currentTimeMillis()
        }
    }

    // Explicit state variables preserved strictly as user inputs
    var phoneNumber by remember { mutableStateOf(initialRecord?.phoneNumber ?: "") }

    // Contact selection
    var selectedContactId by remember { mutableStateOf(initialRecord?.contactId) }
    var contactDropdownExpanded by remember { mutableStateOf(false) }

    val initialContact = contacts.find { it.id == selectedContactId }
    var selectedContactName by remember { mutableStateOf(initialContact?.name ?: "None (Raw Number)") }

    // Call Direction
    var callType by remember { mutableStateOf(initialRecord?.callType ?: CallType.OUTGOING) }

    // Duration in minutes and seconds
    val initialSecs = initialRecord?.durationSeconds ?: 0
    var durationMinutesText by remember { mutableStateOf((initialSecs / 60).toString()) }
    var durationSecondsText by remember { mutableStateOf((initialSecs % 60).toString()) }

    // Time Management (100% manual control - never resets hour or minute automatically!)
    var yearText by remember { mutableStateOf(cal.get(Calendar.YEAR).toString()) }
    var monthText by remember { mutableStateOf((cal.get(Calendar.MONTH) + 1).toString()) }
    var dayText by remember { mutableStateOf(cal.get(Calendar.DAY_OF_MONTH).toString()) }

    val hour12 = cal.get(Calendar.HOUR)
    val initialHourString = if (hour12 == 0) "12" else hour12.toString()
    var hourText by remember { mutableStateOf(initialHourString) }

    val minVal = cal.get(Calendar.MINUTE)
    var minuteText by remember { mutableStateOf(String.format("%02d", minVal)) }

    val isPmInitial = cal.get(Calendar.AM_PM) == Calendar.PM
    var isPm by remember { mutableStateOf(isPmInitial) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialRecord == null) "Add Call Record" else "Edit Call Record",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Phone Number
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_call_phone_input")
                )

                // Contact Association
                Column {
                    Text("Contact", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { contactDropdownExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedContactName)
                        }
                        DropdownMenu(
                            expanded = contactDropdownExpanded,
                            onDismissRequest = { contactDropdownExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Raw Number)") },
                                onClick = {
                                    selectedContactId = null
                                    selectedContactName = "None (Raw Number)"
                                    contactDropdownExpanded = false
                                }
                            )
                            contacts.forEach { contact ->
                                DropdownMenuItem(
                                    text = { Text("${contact.name} (${contact.phoneNumber})") },
                                    onClick = {
                                        selectedContactId = contact.id
                                        selectedContactName = contact.name
                                        if (phoneNumber.isBlank()) {
                                            phoneNumber = contact.phoneNumber
                                        }
                                        contactDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Call Direction
                Column {
                    Text("Call Direction", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = callType == CallType.INCOMING,
                            onClick = { callType = CallType.INCOMING },
                            label = { Text("Incoming") }
                        )
                        FilterChip(
                            selected = callType == CallType.OUTGOING,
                            onClick = { callType = CallType.OUTGOING },
                            label = { Text("Outgoing") }
                        )
                        FilterChip(
                            selected = callType == CallType.MISSED,
                            onClick = { callType = CallType.MISSED },
                            label = { Text("Missed") }
                        )
                    }
                }

                // Call Duration
                Column {
                    Text("Call Duration", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = durationMinutesText,
                            onValueChange = { durationMinutesText = it.filter { c -> c.isDigit() } },
                            label = { Text("Minutes") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = durationSecondsText,
                            onValueChange = { durationSecondsText = it.filter { c -> c.isDigit() } },
                            label = { Text("Seconds") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Time Management Section (Precision Manual Control)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Call Time Management",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Date fields (Year, Month, Day)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = yearText,
                                onValueChange = { yearText = it.filter { c -> c.isDigit() } },
                                label = { Text("Year") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1.2f)
                            )
                            OutlinedTextField(
                                value = monthText,
                                onValueChange = { monthText = it.filter { c -> c.isDigit() } },
                                label = { Text("Month") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = dayText,
                                onValueChange = { dayText = it.filter { c -> c.isDigit() } },
                                label = { Text("Day") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Hour, Minute, AM/PM
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = hourText,
                                onValueChange = { hourText = it.filter { c -> c.isDigit() } },
                                label = { Text("Hour (1-12)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = minuteText,
                                onValueChange = { minuteText = it.filter { c -> c.isDigit() } },
                                label = { Text("Minute (0-59)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            // AM / PM Toggle
                            OutlinedButton(
                                onClick = { isPm = !isPm },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    text = if (isPm) "PM" else "AM",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mins = durationMinutesText.toIntOrNull() ?: 0
                    val secs = durationSecondsText.toIntOrNull() ?: 0
                    val totalDuration = (mins * 60) + secs

                    // Build timestamp preserving exact user date & time inputs
                    val saveCal = Calendar.getInstance()
                    val y = yearText.toIntOrNull() ?: saveCal.get(Calendar.YEAR)
                    val m = (monthText.toIntOrNull() ?: (saveCal.get(Calendar.MONTH) + 1)) - 1
                    val d = dayText.toIntOrNull() ?: saveCal.get(Calendar.DAY_OF_MONTH)

                    var hr12 = hourText.toIntOrNull() ?: 12
                    if (hr12 < 1) hr12 = 12
                    if (hr12 > 12) hr12 = 12

                    val hr24 = when {
                        isPm && hr12 < 12 -> hr12 + 12
                        !isPm && hr12 == 12 -> 0
                        else -> hr12
                    }

                    var min = minuteText.toIntOrNull() ?: 0
                    if (min < 0) min = 0
                    if (min > 59) min = 59

                    saveCal.set(Calendar.YEAR, y)
                    saveCal.set(Calendar.MONTH, m)
                    saveCal.set(Calendar.DAY_OF_MONTH, d)
                    saveCal.set(Calendar.HOUR_OF_DAY, hr24)
                    saveCal.set(Calendar.MINUTE, min)
                    saveCal.set(Calendar.SECOND, 0)
                    saveCal.set(Calendar.MILLISECOND, 0)

                    onSave(
                        initialRecord?.id ?: 0L,
                        phoneNumber.ifBlank { "Unknown" },
                        selectedContactId,
                        callType,
                        saveCal.timeInMillis,
                        totalDuration
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("save_call_record_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
