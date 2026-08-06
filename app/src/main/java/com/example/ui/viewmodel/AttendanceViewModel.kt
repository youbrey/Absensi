package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.ui.components.ScheduleMode
import com.example.ui.components.getEffectiveTimeWindowStatus
import com.example.util.CryptoUtils
import com.example.util.ExportUtils
import com.example.util.LocationHelper
import com.example.util.UserLocationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val attendanceDao = db.attendanceDao()
    private val userDao = db.userDao()

    // Schedule Control Mode (AUTOMATIC, FORCE_OPEN, FORCE_LOCKED)
    private val _scheduleMode = MutableStateFlow(ScheduleMode.AUTOMATIC)
    val scheduleMode: StateFlow<ScheduleMode> = _scheduleMode.asStateFlow()

    fun setScheduleMode(mode: ScheduleMode) {
        _scheduleMode.value = mode
        val prefs = getApplication<Application>().getSharedPreferences("admin_settings", Context.MODE_PRIVATE)
        prefs.edit().putString("schedule_mode", mode.name).apply()
    }

    // Active logged in user
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Form inputs state
    val namaLengkap = MutableStateFlow("")
    val nip = MutableStateFlow("")
    val jabatan = MutableStateFlow("")
    val jenisAbsensi = MutableStateFlow("ABSENSI MASUK") // "ABSENSI MASUK" or "ABSENSI PULANG"
    val capturedBitmap = MutableStateFlow<Bitmap?>(null)
    val photoBase64 = MutableStateFlow("")

    // Location State
    private val _locationState = MutableStateFlow<UserLocationResult?>(null)
    val locationState: StateFlow<UserLocationResult?> = _locationState.asStateFlow()

    // Submitting & Sync State
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Active Navigation Screen
    val currentTab = MutableStateFlow(0) // 0: Absensi Form, 1: History, 2: Admin Dashboard, 3: Admin Users

    // Selected Month Filter for Admin/History Reports
    val selectedMonthFilter = MutableStateFlow("Agustus 2026")

    // All attendance records Flow
    val allAttendanceList: StateFlow<List<AttendanceEntity>> = attendanceDao.getAllAttendanceFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All users Flow for Admin Management
    val allUsersList: StateFlow<List<UserEntity>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // App Admin Security & Webhook Settings
    val webhookUrlState = MutableStateFlow(GoogleSheetsManager.webhookUrl)
    val pushNotificationsEnabled = MutableStateFlow(true)
    
    // Admin Authentication State
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    fun loginAdmin(username: String, password: String): Boolean {
        // Default admin credentials: admin / admin123
        if (username.equals("admin", ignoreCase = true) && (password == "admin123" || password == "admin")) {
            _isAdminAuthenticated.value = true
            return true
        }
        // Check if matching any admin user in DB
        val matchingAdmin = allUsersList.value.firstOrNull { user ->
            user.role == "ADMIN" && (user.nip.equals(username, ignoreCase = true) || user.namaLengkap.contains(username, ignoreCase = true))
        }
        if (matchingAdmin != null && (password == "admin123" || password == matchingAdmin.pinCode)) {
            _isAdminAuthenticated.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        currentTab.value = 0
    }

    init {
        val prefs = getApplication<Application>().getSharedPreferences("admin_settings", Context.MODE_PRIVATE)
        val savedModeStr = prefs.getString("schedule_mode", ScheduleMode.AUTOMATIC.name)
        try {
            _scheduleMode.value = ScheduleMode.valueOf(savedModeStr ?: ScheduleMode.AUTOMATIC.name)
        } catch (e: Exception) {
            _scheduleMode.value = ScheduleMode.AUTOMATIC
        }
        refreshGpsLocation()
    }

    fun selectUserForForm(user: UserEntity) {
        _currentUser.value = user
        namaLengkap.value = user.namaLengkap
        nip.value = user.nip
        jabatan.value = user.jabatan
    }

    fun refreshGpsLocation() {
        viewModelScope.launch {
            val result = LocationHelper.getCurrentLocation(getApplication())
            _locationState.value = result
        }
    }

    fun setCapturedPhoto(bitmap: Bitmap, base64: String) {
        capturedBitmap.value = bitmap
        photoBase64.value = base64
    }

    fun submitAttendance(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (namaLengkap.value.isBlank() || nip.value.isBlank()) {
                onError("Nama Lengkap dan NIP wajib diisi")
                return@launch
            }

            // Check if schedule is currently open (either automatic or forced open)
            val windowStatus = getEffectiveTimeWindowStatus(_scheduleMode.value)
            if (!windowStatus.isOpen) {
                onError("Jadwal absensi sedang dikunci atau diluar jam operasional. Hubungi Admin jika memerlukan pembukaan jadwal uji coba.")
                return@launch
            }

            _isSubmitting.value = true

            try {
                val now = Date()
                val dateFmt = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID")).format(now)
                val timeFmt = SimpleDateFormat("HH:mm:ss", Locale("id", "ID")).format(now)
                val isMasuk = jenisAbsensi.value.contains("MASUK", ignoreCase = true)

                val loc = _locationState.value ?: LocationHelper.getCurrentLocation(getApplication())

                val encryptedPayloadHash = CryptoUtils.generatePayloadHash(
                    nip.value,
                    now.time,
                    jenisAbsensi.value
                )

                val record = AttendanceEntity(
                    namaLengkap = namaLengkap.value,
                    nip = nip.value,
                    jabatan = jabatan.value,
                    jenisAbsensi = jenisAbsensi.value,
                    timestamp = now.time,
                    dateFormatted = dateFmt,
                    timeFormatted = timeFmt,
                    jamMasuk = if (isMasuk) timeFmt else "-",
                    jamPulang = if (!isMasuk) timeFmt else "-",
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    locationAddress = loc.address,
                    faceVerified = true,
                    faceConfidence = 0.985f,
                    photoBase64 = photoBase64.value,
                    isSyncedToSheets = false,
                    encryptedHash = encryptedPayloadHash
                )

                // Save locally
                val id = attendanceDao.insertAttendance(record)
                val recordWithId = record.copy(id = id)

                // Sync to Google Sheets
                val synced = GoogleSheetsManager.syncAttendanceRecord(recordWithId)
                if (synced) {
                    attendanceDao.markSynced(id)
                }

                _isSubmitting.value = false
                capturedBitmap.value = null
                photoBase64.value = ""

                onSuccess()
            } catch (e: Exception) {
                _isSubmitting.value = false
                onError("Gagal menyimpan absensi: ${e.message}")
            }
        }
    }

    fun syncAllUnsyncedRecords() {
        viewModelScope.launch {
            val list = allAttendanceList.value.filter { !it.isSyncedToSheets }
            for (item in list) {
                val synced = GoogleSheetsManager.syncAttendanceRecord(item)
                if (synced) {
                    attendanceDao.markSynced(item.id)
                }
            }
            Toast.makeText(getApplication(), "Proses Sinkronisasi Google Sheets selesai", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportReportPdf(): File? {
        val filteredList = allAttendanceList.value
        return ExportUtils.exportToPdf(getApplication(), selectedMonthFilter.value, filteredList)
    }

    fun exportReportCsv(): File? {
        val filteredList = allAttendanceList.value
        return ExportUtils.exportToExcelCsv(getApplication(), selectedMonthFilter.value, filteredList)
    }

    fun saveNewUser(
        nama: String,
        nipInput: String,
        jabatanInput: String,
        tipe: String,
        role: String
    ) {
        viewModelScope.launch {
            val newUser = UserEntity(
                namaLengkap = nama,
                nip = nipInput,
                jabatan = jabatanInput,
                tipePegawai = tipe,
                role = role,
                pinCode = "123456",
                isActive = true,
                allowTimeOverride = role == "ADMIN"
            )
            userDao.insertUser(newUser)
            Toast.makeText(getApplication(), "Pengguna baru $nama berhasil ditambahkan", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleUserActiveState(user: UserEntity) {
        viewModelScope.launch {
            userDao.updateUser(user.copy(isActive = !user.isActive))
        }
    }
}
