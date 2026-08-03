package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Dialpad
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Contact
import com.example.ui.components.EditContactDialog
import com.example.ui.screens.AdvancedSettingsScreen
import com.example.ui.screens.CallDetailsScreen
import com.example.ui.screens.ContactsScreen
import com.example.ui.screens.DialpadScreen
import com.example.ui.screens.InCallScreen
import com.example.ui.screens.RecentCallsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.PhoneTheme
import com.example.ui.viewmodel.PhoneTab
import com.example.ui.viewmodel.PhoneViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: PhoneViewModel = viewModel()
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

            PhoneTheme(darkTheme = isDarkMode) {
                PhoneApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PhoneApp(viewModel: PhoneViewModel = viewModel()) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val activeCallState by viewModel.activeCallState.collectAsStateWithLifecycle()
    val selectedCallId by viewModel.selectedCallId.collectAsStateWithLifecycle()
    val selectedCallDetail by viewModel.selectedCallDetail.collectAsStateWithLifecycle()

    val dialedNumber by viewModel.dialedNumber.collectAsStateWithLifecycle()
    val matchingDialContact by viewModel.matchingDialContact.collectAsStateWithLifecycle()

    val recentCalls by viewModel.recentCalls.collectAsStateWithLifecycle()
    val recentsSearchQuery by viewModel.recentsSearchQuery.collectAsStateWithLifecycle()
    val recentsFilter by viewModel.recentsFilter.collectAsStateWithLifecycle()

    val filteredContacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val allContacts by viewModel.allContacts.collectAsStateWithLifecycle()
    val contactSearchQuery by viewModel.contactSearchQuery.collectAsStateWithLifecycle()

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val updateStatus by viewModel.updateStatus.collectAsStateWithLifecycle()
    val targetServerVersionCode by viewModel.targetServerVersionCode.collectAsStateWithLifecycle()


    // Navigation overlay states
    var isAdvancedSettingsOpen by remember { mutableStateOf(false) }
    var newContactFromDialerNumber by remember { mutableStateOf<String?>(null) }

    // Active in-call screen overlay
    activeCallState?.let { activeState ->
        InCallScreen(
            state = activeState,
            onToggleMute = { viewModel.toggleMute() },
            onToggleSpeaker = { viewModel.toggleSpeaker() },
            onEndCall = { viewModel.endActiveCall() },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // Call Details overlay screen
    if (selectedCallId != null) {
        CallDetailsScreen(
            callWithContact = selectedCallDetail,
            onBackClick = { viewModel.closeCallDetails() },
            onCallClick = { number -> viewModel.startCall(number) },
            onDeleteCallRecord = { record -> viewModel.deleteCallRecord(record) },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    // Advanced Settings overlay screen (strictly inside Settings -> Advanced Settings)
    if (isAdvancedSettingsOpen) {
        AdvancedSettingsScreen(
            recentCalls = recentCalls,
            allContacts = allContacts,
            onBackClick = { isAdvancedSettingsOpen = false },
            onSaveCallRecord = { recordId, phoneNumber, contactId, callType, timestamp, durationSeconds ->
                viewModel.saveCallRecord(recordId, phoneNumber, contactId, callType, timestamp, durationSeconds)
            },
            onSaveBulkCalls = { raw, type -> viewModel.bulkAddCalls(raw, type) },
            onDeleteCallRecord = { record -> viewModel.deleteCallRecord(record) },
            simulatedVersionCode = targetServerVersionCode,
            onSetSimulatedVersionCode = { code -> viewModel.setTargetServerVersionCode(code) },
            modifier = Modifier.fillMaxSize()
        )
        return
    }


    // Main App Shell with Bottom Navigation
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == PhoneTab.KEYPAD,
                    onClick = { viewModel.selectTab(PhoneTab.KEYPAD) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == PhoneTab.KEYPAD) Icons.Filled.Dialpad else Icons.Outlined.Dialpad,
                            contentDescription = "Keypad"
                        )
                    },
                    label = { Text("Keypad") },
                    modifier = Modifier.testTag("nav_tab_keypad")
                )

                NavigationBarItem(
                    selected = currentTab == PhoneTab.RECENTS,
                    onClick = { viewModel.selectTab(PhoneTab.RECENTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == PhoneTab.RECENTS) Icons.Filled.History else Icons.Outlined.History,
                            contentDescription = "Recents"
                        )
                    },
                    label = { Text("Recents") },
                    modifier = Modifier.testTag("nav_tab_recents")
                )

                NavigationBarItem(
                    selected = currentTab == PhoneTab.CONTACTS,
                    onClick = { viewModel.selectTab(PhoneTab.CONTACTS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == PhoneTab.CONTACTS) Icons.Filled.Contacts else Icons.Outlined.Contacts,
                            contentDescription = "Contacts"
                        )
                    },
                    label = { Text("Contacts") },
                    modifier = Modifier.testTag("nav_tab_contacts")
                )

                NavigationBarItem(
                    selected = currentTab == PhoneTab.SETTINGS,
                    onClick = { viewModel.selectTab(PhoneTab.SETTINGS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == PhoneTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("Settings") },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    PhoneTab.KEYPAD -> {
                        DialpadScreen(
                            dialedNumber = dialedNumber,
                            matchingContact = matchingDialContact,
                            onDigitClick = { digit -> viewModel.onDialDigit(digit) },
                            onDeleteDigit = { viewModel.onDeleteDigit() },
                            onClearDigits = { viewModel.onClearDigits() },
                            onCallClick = { number -> viewModel.startCall(number) },
                            onAddContact = { number -> newContactFromDialerNumber = number }
                        )
                    }
                    PhoneTab.RECENTS -> {
                        RecentCallsScreen(
                            recentCalls = recentCalls,
                            searchQuery = recentsSearchQuery,
                            currentFilter = recentsFilter,
                            onSearchQueryChange = { q -> viewModel.setRecentsSearchQuery(q) },
                            onFilterChange = { f -> viewModel.setRecentsFilter(f) },
                            onCallClick = { number -> viewModel.startCall(number) },
                            onCallDetailsClick = { id -> viewModel.openCallDetails(id) }
                        )
                    }
                    PhoneTab.CONTACTS -> {
                        ContactsScreen(
                            contacts = filteredContacts,
                            searchQuery = contactSearchQuery,
                            onSearchQueryChange = { q -> viewModel.setContactSearchQuery(q) },
                            onSaveContact = { c -> viewModel.saveContact(c) },
                            onDeleteContact = { c -> viewModel.deleteContact(c) },
                            onCallContact = { number -> viewModel.startCall(number) }
                        )
                    }
                    PhoneTab.SETTINGS -> {
                        SettingsScreen(
                            isDarkMode = isDarkMode,
                            onToggleDarkMode = { viewModel.toggleDarkMode() },
                            onClearCallHistory = { viewModel.clearCallHistory() },
                            onOpenAdvancedSettings = { isAdvancedSettingsOpen = true },
                            updateStatus = updateStatus,
                            onCheckForUpdates = { viewModel.checkForUpdates() },
                            onDismissUpdateStatus = { viewModel.dismissUpdateStatus() }
                        )
                    }

                }
            }
        }

        // Add Contact from Dialer dialog
        newContactFromDialerNumber?.let { number ->
            EditContactDialog(
                initialContact = Contact(name = "", phoneNumber = number),
                onDismiss = { newContactFromDialerNumber = null },
                onSave = { contact ->
                    viewModel.saveContact(contact)
                    newContactFromDialerNumber = null
                }
            )
        }
    }
}
