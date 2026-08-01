package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallMissed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.CallType
import com.example.ui.theme.CallIncomingGreen
import com.example.ui.theme.CallMissedRed
import com.example.ui.theme.CallOutgoingBlue

@Composable
fun CallDirectionIcon(
    callType: CallType,
    size: Dp = 18.dp,
    modifier: Modifier = Modifier
) {
    when (callType) {
        CallType.INCOMING -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CallReceived,
                contentDescription = "Incoming call",
                tint = CallIncomingGreen,
                modifier = modifier.size(size)
            )
        }
        CallType.OUTGOING -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CallMade,
                contentDescription = "Outgoing call",
                tint = CallOutgoingBlue,
                modifier = modifier.size(size)
            )
        }
        CallType.MISSED -> {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.CallMissed,
                contentDescription = "Missed call",
                tint = CallMissedRed,
                modifier = modifier.size(size)
            )
        }
    }
}
