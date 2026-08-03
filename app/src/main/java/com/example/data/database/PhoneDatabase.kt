package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.Contact
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromCallType(value: CallType): String = value.name

    @TypeConverter
    fun toCallType(value: String): CallType = try {
        CallType.valueOf(value)
    } catch (e: Exception) {
        CallType.INCOMING
    }
}

@Database(
    entities = [Contact::class, CallRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PhoneDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
    abstract fun callRecordDao(): CallRecordDao

    companion object {
        @Volatile
        private var INSTANCE: PhoneDatabase? = null

        fun getDatabase(context: Context): PhoneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhoneDatabase::class.java,
                    "phone_app_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database.contactDao(), database.callRecordDao())
                    }
                }
            }
        }

        private suspend fun populateInitialData(contactDao: ContactDao, callDao: CallRecordDao) {
            val aliceId = contactDao.insertContact(
                Contact(name = "Alice Smith", phoneNumber = "+1 555-0192", avatarColorHex = "#4285F4", isFavorite = true)
            )
            val bobId = contactDao.insertContact(
                Contact(name = "Bob Jones", phoneNumber = "+1 555-0143", avatarColorHex = "#EA4335", isFavorite = true)
            )
            val charlieId = contactDao.insertContact(
                Contact(name = "Charlie Brown", phoneNumber = "+1 555-0188", avatarColorHex = "#34A853", isFavorite = false)
            )
            contactDao.insertContact(
                Contact(name = "Diana Prince", phoneNumber = "+1 555-0177", avatarColorHex = "#FBBC05", isFavorite = false)
            )

            val now = System.currentTimeMillis()
            val hour = 3600000L
            val day = 86400000L

            callDao.insertCallRecord(
                CallRecord(phoneNumber = "+1 555-0192", contactId = aliceId, callType = CallType.INCOMING, timestamp = now - hour, durationSeconds = 142)
            )
            callDao.insertCallRecord(
                CallRecord(phoneNumber = "+1 555-0143", contactId = bobId, callType = CallType.MISSED, timestamp = now - (hour * 3), durationSeconds = 0)
            )
            callDao.insertCallRecord(
                CallRecord(phoneNumber = "+1 555-0188", contactId = charlieId, callType = CallType.OUTGOING, timestamp = now - (day * 1), durationSeconds = 48)
            )
            callDao.insertCallRecord(
                CallRecord(phoneNumber = "+1 555-0999", contactId = null, callType = CallType.MISSED, timestamp = now - (day * 2), durationSeconds = 0)
            )
        }
    }
}
