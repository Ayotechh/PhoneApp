package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.PhoneDatabase
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.CallWithContact
import com.example.data.model.Contact
import com.example.repository.PhoneRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class PhoneTab {
    KEYPAD,
    RECENTS,
    CONTACTS,
    SETTINGS
}

enum class RecentsFilter {
    ALL,
    MISSED
}

data class ActiveCallState(
    val phoneNumber: String,
    val contactName: String?,
    val avatarColorHex: String,
    val durationSeconds: Int = 0,
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isCallActive: Boolean = true
)

class PhoneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = PhoneDatabase.getDatabase(application)
    private val repository = PhoneRepository(db.contactDao(), db.callRecordDao())

    // Navigation State
    private val _currentTab = MutableStateFlow(PhoneTab.RECENTS)
    val currentTab: StateFlow<PhoneTab> = _currentTab.asStateFlow()

    private val _selectedCallId = MutableStateFlow<Long?>(null)
    val selectedCallId: StateFlow<Long?> = _selectedCallId.asStateFlow()

    // Active Simulated Call
    private val _activeCallState = MutableStateFlow<ActiveCallState?>(null)
    val activeCallState: StateFlow<ActiveCallState?> = _activeCallState.asStateFlow()
    private var callTimerJob: Job? = null

    // Dialpad State
    private val _dialedNumber = MutableStateFlow("")
    val dialedNumber: StateFlow<String> = _dialedNumber.asStateFlow()

    // Matching contact for typed number
    val allContacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val matchingDialContact: StateFlow<Contact?> = combine(_dialedNumber, allContacts) { number, contacts ->
        if (number.length < 3) null
        else {
            val sanitized = number.replace(Regex("[^0-9+]"), "")
            contacts.find { c ->
                c.phoneNumber.replace(Regex("[^0-9+]"), "").contains(sanitized)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Recent Calls State & Search/Filter
    private val _recentsSearchQuery = MutableStateFlow("")
    val recentsSearchQuery: StateFlow<String> = _recentsSearchQuery.asStateFlow()

    private val _recentsFilter = MutableStateFlow(RecentsFilter.ALL)
    val recentsFilter: StateFlow<RecentsFilter> = _recentsFilter.asStateFlow()

    val recentCalls: StateFlow<List<CallWithContact>> = combine(
        repository.allCallsWithContacts,
        _recentsSearchQuery,
        _recentsFilter
    ) { calls, query, filter ->
        calls.filter { item ->
            val matchesFilter = when (filter) {
                RecentsFilter.ALL -> true
                RecentsFilter.MISSED -> item.callRecord.callType == CallType.MISSED
            }
            val matchesQuery = if (query.isBlank()) true else {
                item.displayName.contains(query, ignoreCase = true) ||
                        item.displayPhoneNumber.contains(query)
            }
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Call Detail
    val selectedCallDetail: StateFlow<CallWithContact?> = _selectedCallId.map { id ->
        if (id == null) null
        else repository.allCallsWithContacts.map { list -> list.find { it.callRecord.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null).let { stateFlow ->
        // Direct combination for current selectedCallId
        combine(_selectedCallId, repository.allCallsWithContacts) { id, list ->
            id?.let { callId -> list.find { it.callRecord.id == callId } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    // Contacts State & Search
    private val _contactSearchQuery = MutableStateFlow("")
    val contactSearchQuery: StateFlow<String> = _contactSearchQuery.asStateFlow()

    val filteredContacts: StateFlow<List<Contact>> = combine(
        allContacts,
        _contactSearchQuery
    ) { contacts, query ->
        if (query.isBlank()) contacts
        else contacts.filter {
            it.name.contains(query, ignoreCase = true) || it.phoneNumber.contains(query)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Theme & Settings
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Navigation Methods
    fun selectTab(tab: PhoneTab) {
        _currentTab.value = tab
        _selectedCallId.value = null
    }

    fun openCallDetails(callId: Long) {
        _selectedCallId.value = callId
    }

    fun closeCallDetails() {
        _selectedCallId.value = null
    }

    // Dialpad Methods
    fun onDialDigit(digit: String) {
        _dialedNumber.value += digit
    }

    fun onDeleteDigit() {
        if (_dialedNumber.value.isNotEmpty()) {
            _dialedNumber.value = _dialedNumber.value.dropLast(1)
        }
    }

    fun onClearDigits() {
        _dialedNumber.value = ""
    }

    // Place Call
    fun startCall(phoneNumber: String) {
        if (phoneNumber.isBlank()) return

        val matching = allContacts.value.find {
            it.phoneNumber.replace(Regex("[^0-9+]"), "") == phoneNumber.replace(Regex("[^0-9+]"), "")
        }

        val avatarColor = matching?.avatarColorHex ?: "#4CAF50"

        _activeCallState.value = ActiveCallState(
            phoneNumber = phoneNumber,
            contactName = matching?.name,
            avatarColorHex = avatarColor,
            durationSeconds = 0
        )

        // Start duration timer
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCallState.value?.isCallActive == true) {
                delay(1000)
                _activeCallState.value = _activeCallState.value?.let { state ->
                    state.copy(durationSeconds = state.durationSeconds + 1)
                }
            }
        }
    }

    fun toggleMute() {
        _activeCallState.value = _activeCallState.value?.let {
            it.copy(isMuted = !it.isMuted)
        }
    }

    fun toggleSpeaker() {
        _activeCallState.value = _activeCallState.value?.let {
            it.copy(isSpeakerOn = !it.isSpeakerOn)
        }
    }

    fun endActiveCall() {
        callTimerJob?.cancel()
        val current = _activeCallState.value
        if (current != null) {
            viewModelScope.launch {
                repository.placeCall(
                    phoneNumber = current.phoneNumber,
                    durationSeconds = current.durationSeconds
                )
                _activeCallState.value = null
                _dialedNumber.value = ""
                // Switch tab to recents to see new call at the top
                _currentTab.value = PhoneTab.RECENTS
            }
        }
    }

    // Direct Quick Call creation (instant log place)
    fun placeQuickCall(phoneNumber: String, type: CallType = CallType.OUTGOING, durationSeconds: Int = 120) {
        viewModelScope.launch {
            val matching = allContacts.value.find {
                it.phoneNumber.replace(Regex("[^0-9+]"), "") == phoneNumber.replace(Regex("[^0-9+]"), "")
            }
            val record = CallRecord(
                phoneNumber = phoneNumber,
                contactId = matching?.id,
                callType = type,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds
            )
            repository.insertCallRecord(record)
        }
    }

    // Contact Management
    fun setContactSearchQuery(query: String) {
        _contactSearchQuery.value = query
    }

    fun saveContact(contact: Contact) {
        viewModelScope.launch {
            if (contact.id == 0L) {
                repository.insertContact(contact)
            } else {
                repository.updateContact(contact)
            }
        }
    }

    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    // Recents Search / Filter
    fun setRecentsSearchQuery(query: String) {
        _recentsSearchQuery.value = query
    }

    fun setRecentsFilter(filter: RecentsFilter) {
        _recentsFilter.value = filter
    }

    // Bulk Call Logging
    fun bulkAddCalls(
        rawNumbersText: String,
        callType: CallType = CallType.OUTGOING
    ) {
        if (rawNumbersText.isBlank()) return
        val rawTokens = rawNumbersText.split(Regex("[,;\\n\\s]+"))
        val numbers = rawTokens.map { token ->
            token.replace(Regex("[^0-9+]"), "")
        }.filter { it.isNotBlank() && it.length >= 3 }

        if (numbers.isEmpty()) return

        viewModelScope.launch {
            var currentTimestamp = System.currentTimeMillis()
            val random = java.util.Random()

            numbers.forEachIndexed { index, number ->
                if (index > 0) {
                    // Subtract 3 to 4 minutes (180 to 240 seconds) + a few random seconds
                    val gapSeconds = 180 + random.nextInt(60)
                    currentTimestamp -= gapSeconds * 1000L
                }

                val matching = allContacts.value.find {
                    it.phoneNumber.replace(Regex("[^0-9+]"), "") == number.replace(Regex("[^0-9+]"), "")
                }

                val duration = if (callType == CallType.MISSED) 0 else 30 + random.nextInt(150)

                val record = CallRecord(
                    phoneNumber = number,
                    contactId = matching?.id,
                    callType = callType,
                    timestamp = currentTimestamp,
                    durationSeconds = duration
                )
                repository.insertCallRecord(record)
            }
        }
    }

    // Call Record Editing (Record Management)
    fun saveCallRecord(
        recordId: Long,
        phoneNumber: String,
        contactId: Long?,
        callType: CallType,
        timestamp: Long,
        durationSeconds: Int
    ) {
        viewModelScope.launch {
            val updated = CallRecord(
                id = recordId,
                phoneNumber = phoneNumber.trim(),
                contactId = contactId,
                callType = callType,
                timestamp = timestamp,
                durationSeconds = durationSeconds
            )
            if (recordId == 0L) {
                repository.insertCallRecord(updated)
            } else {
                repository.updateCallRecord(updated)
            }
        }
    }

    fun deleteCallRecord(callRecord: CallRecord) {
        viewModelScope.launch {
            repository.deleteCallRecord(callRecord)
            if (_selectedCallId.value == callRecord.id) {
                _selectedCallId.value = null
            }
        }
    }

    fun clearCallHistory() {
        viewModelScope.launch {
            repository.clearCallHistory()
            _selectedCallId.value = null
        }
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }
}
