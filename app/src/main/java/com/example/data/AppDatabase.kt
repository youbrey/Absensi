package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AttendanceEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun attendanceDao(): AttendanceDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dprd_bitung_absensi.db"
                )
                .addCallback(DatabaseCallback())
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialUsers(database.userDao())
                        populateInitialAttendance(database.attendanceDao())
                    }
                }
            }
        }

        private suspend fun populateInitialUsers(userDao: UserDao) {
            val initialUsers = listOf(
                UserEntity(
                    namaLengkap = "Jackson F. Ruaw, S.Sos",
                    nip = "19780512 200501 1 008",
                    jabatan = "Sekretaris DPRD",
                    tipePegawai = "PNS",
                    role = "ADMIN",
                    pinCode = "123456",
                    allowTimeOverride = true
                ),
                UserEntity(
                    namaLengkap = "Dra. Femmy W. Sondakh",
                    nip = "19801103 200802 2 011",
                    jabatan = "Kabag Umum & Keuangan",
                    tipePegawai = "PNS",
                    role = "ADMIN",
                    pinCode = "123456",
                    allowTimeOverride = true
                ),
                UserEntity(
                    namaLengkap = "Yulius M. Tambing, S.T.",
                    nip = "19850419 201001 1 015",
                    jabatan = "Pranata Komputer Ahli Muda",
                    tipePegawai = "PNS",
                    role = "USER",
                    pinCode = "123456"
                ),
                UserEntity(
                    namaLengkap = "Meita V. Poluan, S.E.",
                    nip = "19890912 201403 2 004",
                    jabatan = "Analis Kebijakan",
                    tipePegawai = "PNS",
                    role = "USER",
                    pinCode = "123456"
                ),
                UserEntity(
                    namaLengkap = "Efraim S. Lumangkun",
                    nip = "19940210 202321 1 002",
                    jabatan = "Staff Humas & Protokol",
                    tipePegawai = "PPPK",
                    role = "USER",
                    pinCode = "123456"
                ),
                UserEntity(
                    namaLengkap = "Natalia R. Rantung",
                    nip = "19961225 202421 2 009",
                    jabatan = "Pengelola Risalah DPRD",
                    tipePegawai = "PPPK",
                    role = "USER",
                    pinCode = "123456"
                )
            )
            for (user in initialUsers) {
                userDao.insertUser(user)
            }
        }

        private suspend fun populateInitialAttendance(attendanceDao: AttendanceDao) {
            val sampleRecords = listOf(
                AttendanceEntity(
                    namaLengkap = "Jackson F. Ruaw, S.Sos",
                    nip = "19780512 200501 1 008",
                    jabatan = "Sekretaris DPRD",
                    jenisAbsensi = "ABSENSI MASUK",
                    timestamp = System.currentTimeMillis() - 86400000L * 2 + 27000000L, // 07.30
                    dateFormatted = "Selasa, 4 Agustus 2026",
                    timeFormatted = "07:30:15",
                    jamMasuk = "07:30:15",
                    jamPulang = "13:05:22",
                    latitude = 1.4421,
                    longitude = 125.1834,
                    locationAddress = "Kec. Maesa, Kota Bitung, Sulawesi Utara",
                    faceVerified = true,
                    faceConfidence = 0.99f,
                    isSyncedToSheets = true,
                    encryptedHash = "SHA256_e8d91a21b02..."
                ),
                AttendanceEntity(
                    namaLengkap = "Yulius M. Tambing, S.T.",
                    nip = "19850419 201001 1 015",
                    jabatan = "Pranata Komputer Ahli Muda",
                    jenisAbsensi = "ABSENSI MASUK",
                    timestamp = System.currentTimeMillis() - 86400000L + 28200000L, // 07.50
                    dateFormatted = "Rabu, 5 Agustus 2026",
                    timeFormatted = "07:50:40",
                    jamMasuk = "07:50:40",
                    jamPulang = "-",
                    latitude = 1.4428,
                    longitude = 125.1840,
                    locationAddress = "Aertembaga, Kota Bitung, Sulawesi Utara",
                    faceVerified = true,
                    faceConfidence = 0.97f,
                    isSyncedToSheets = true,
                    encryptedHash = "SHA256_a4f10901e12..."
                ),
                AttendanceEntity(
                    namaLengkap = "Efraim S. Lumangkun",
                    nip = "19940210 202321 1 002",
                    jabatan = "Staff Humas & Protokol",
                    jenisAbsensi = "ABSENSI MASUK",
                    timestamp = System.currentTimeMillis() - 86400000L + 29400000L, // 08.10
                    dateFormatted = "Rabu, 5 Agustus 2026",
                    timeFormatted = "08:10:05",
                    jamMasuk = "08:10:05",
                    jamPulang = "-",
                    latitude = 1.4415,
                    longitude = 125.1820,
                    locationAddress = "Girian, Kota Bitung, Sulawesi Utara",
                    faceVerified = true,
                    faceConfidence = 0.98f,
                    isSyncedToSheets = false,
                    encryptedHash = "SHA256_c910283f511..."
                )
            )
            for (record in sampleRecords) {
                attendanceDao.insertAttendance(record)
            }
        }
    }
}
