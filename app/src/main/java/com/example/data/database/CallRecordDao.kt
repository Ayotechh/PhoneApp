package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.CallRecord
import com.example.data.model.CallWithContact
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordDao {
    @Transaction
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCallsWithContacts(): Flow<List<CallWithContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecord(callRecord: CallRecord): Long

    @Update
    suspend fun updateCallRecord(callRecord: CallRecord)

    @Delete
    suspend fun deleteCallRecord(callRecord: CallRecord)

    @Query("DELETE FROM call_records")
    suspend fun clearAllCallRecords()
}
