package com.example.repository

import com.example.data.database.CallRecordDao
import com.example.data.database.ContactDao
import com.example.data.model.CallRecord
import com.example.data.model.CallWithContact
import com.example.data.model.Contact
import kotlinx.coroutines.flow.Flow

class PhoneRepository(
    private val contactDao: ContactDao,
    private val callRecordDao: CallRecordDao
) {
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val allCallsWithContacts: Flow<List<CallWithContact>> = callRecordDao.getAllCallsWithContacts()

    suspend fun getContactById(id: Long): Contact? = contactDao.getContactById(id)
    suspend fun getContactByNumber(number: String): Contact? = contactDao.getContactByNumber(number)

    suspend fun insertContact(contact: Contact): Long = contactDao.insertContact(contact)
    suspend fun updateContact(contact: Contact) = contactDao.updateContact(contact)
    suspend fun deleteContact(contact: Contact) = contactDao.deleteContact(contact)

    suspend fun insertCallRecord(callRecord: CallRecord): Long = callRecordDao.insertCallRecord(callRecord)
    suspend fun updateCallRecord(callRecord: CallRecord) = callRecordDao.updateCallRecord(callRecord)
    suspend fun deleteCallRecord(callRecord: CallRecord) = callRecordDao.deleteCallRecord(callRecord)
    suspend fun clearAllCallRecords() = callRecordDao.clearAllCallRecords()
}
