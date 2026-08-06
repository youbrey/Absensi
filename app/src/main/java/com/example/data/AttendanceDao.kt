package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllAttendanceFlow(): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE nip = :nip ORDER BY timestamp DESC")
    fun getAttendanceForUserFlow(nip: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE dateFormatted = :dateFormatted ORDER BY timestamp DESC")
    fun getAttendanceByDateFlow(dateFormatted: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance_records WHERE nip = :nip AND dateFormatted = :dateFormatted AND jenisAbsensi = :jenis LIMIT 1")
    suspend fun getTodayRecord(nip: String, dateFormatted: String, jenis: String): AttendanceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(record: AttendanceEntity): Long

    @Update
    suspend fun updateAttendance(record: AttendanceEntity)

    @Query("UPDATE attendance_records SET isSyncedToSheets = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Delete
    suspend fun deleteAttendance(record: AttendanceEntity)

    @Query("DELETE FROM attendance_records")
    suspend fun clearAll()
}

@Dao
interface UserDao {

    @Query("SELECT * FROM users ORDER BY namaLengkap ASC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE nip = :nip LIMIT 1")
    suspend fun getUserByNip(nip: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
}
