package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.data.model.CallType

@Composable
fun BulkCallDialog(
    onDismiss: () -> Unit,
    onSaveBulk: (rawNumbersText: String, callType: CallType) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var callType by remember { mutableStateOf(CallType.OUTGOING) }

    // Derive parsed phone numbers count
    val parsedNumbers by remember(rawText) {
        derivedStateOf {
            if (rawText.isBlank()) emptyList()
            else {
                rawText.split(Regex("[,;\\n\\s]+"))
                    .map { it.replace(Regex("[^0-9+]"), "") }
                    .filter { it.length >= 3 }
            }
        }
    }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Bulk Call Logger",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Paste Bulk Numbers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Separate numbers using commas (,), semicolons (;), spaces, or newlines. Each number will receive a realistic staggered timestamp (3-4 minutes apart).",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                }

                // Multiline Input Field
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    label = { Text("Numbers (e.g. +12345, +67890; +11223)") },
                    placeholder = { Text("+1234567890, +0987654321; +1122334455") },
                    minLines = 4,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bulk_numbers_input")
                )

                if (parsedNumbers.isNotEmpty()) {
                    Text(
                        text = "✓ Detected ${parsedNumbers.size} number(s) to log",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Call Type
                Column {
                    Text(
                        text = "Call Direction",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = callType == CallType.OUTGOING,
                            onClick = { callType = CallType.OUTGOING },
                            label = { Text("Outgoing") },
                            modifier = Modifier.testTag("bulk_chip_outgoing")
                        )
                        FilterChip(
                            selected = callType == CallType.INCOMING,
                            onClick = { callType = CallType.INCOMING },
                            label = { Text("Incoming") },
                            modifier = Modifier.testTag("bulk_chip_incoming")
                        )
                        FilterChip(
                            selected = callType == CallType.MISSED,
                            onClick = { callType = CallType.MISSED },
                            label = { Text("Missed") },
                            modifier = Modifier.testTag("bulk_chip_missed")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedNumbers.isNotEmpty()) {
                        onSaveBulk(rawText, callType)
                        onDismiss()
                    }
                },
                enabled = parsedNumbers.isNotEmpty(),
                modifier = Modifier.testTag("submit_bulk_calls_button")
            ) {
                Text("Log ${parsedNumbers.size} Calls")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
