package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.ui.components.AttendanceTimeBadge
import com.example.ui.components.HeaderBrandingCard
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.verification.RealtimeCameraFaceView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val nama by viewModel.namaLengkap.collectAsState()
    val nip by viewModel.nip.collectAsState()
    val jabatan by viewModel.jabatan.collectAsState()
    val jenisAbsensi by viewModel.jenisAbsensi.collectAsState()
    val capturedBitmap by viewModel.capturedBitmap.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val allUsers by viewModel.allUsersList.collectAsState()
    val scheduleMode by viewModel.scheduleMode.collectAsState()

    var showStaffSelectorDialog by remember { mutableStateOf(false) }

    var isNamaFocused by remember { mutableStateOf(false) }
    var isNipFocused by remember { mutableStateOf(false) }
    var isJabatanFocused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        HeaderBrandingCard()

        // Operational Time Window Badge
        AttendanceTimeBadge(scheduleMode = scheduleMode)

        // Quick Staff Switcher Header
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showStaffSelectorDialog = true }
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B)
                        )
                    }
                    Column {
                        Text(
                            text = if (nama.isNotBlank()) nama else "Pilih atau Masukkan Data Pegawai",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (nip.isNotBlank()) "NIP: $nip • $jabatan" else "Klik di sini untuk memilih profil cepat",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.SwapHoriz,
                    contentDescription = "Pilih Pegawai",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Form Input Fields Section
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "INFORMASI DATA PEGAWAI WFH",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                OutlinedTextField(
                    value = nama,
                    onValueChange = { viewModel.namaLengkap.value = it },
                    label = { Text("NAMA LENGKAP") },
                    placeholder = if (!isNamaFocused) { { Text("Masukkan Nama Lengkap") } } else null,
                    leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isNamaFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = nip,
                    onValueChange = { viewModel.nip.value = it },
                    label = { Text("NIP (Nomor Induk Pegawai)") },
                    placeholder = if (!isNipFocused) { { Text("Masukkan NIP") } } else null,
                    leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isNipFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = jabatan,
                    onValueChange = { viewModel.jabatan.value = it },
                    label = { Text("JABATAN / UNIT KERJA") },
                    placeholder = if (!isJabatanFocused) { { Text("Masukkan Jabatan") } } else null,
                    leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isJabatanFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Text(
                    text = "JENIS ABSENSI (PILIH SALAH SATU):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val isMasukSelected = jenisAbsensi == "ABSENSI MASUK"
                    val isPulangSelected = jenisAbsensi == "ABSENSI PULANG"

                    FilterChip(
                        selected = isMasukSelected,
                        onClick = { viewModel.jenisAbsensi.value = "ABSENSI MASUK" },
                        label = { Text("🟢 ABSENSI MASUK\n(07.00 - 09.00)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = isPulangSelected,
                        onClick = { viewModel.jenisAbsensi.value = "ABSENSI PULANG" },
                        label = { Text("🔴 ABSENSI PULANG\n(12.00 - 14.00)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        // Camera & Photo Box
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.PhotoCamera,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "FOTO SELFIE / UPLOAD FOTO ABSENSI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                RealtimeCameraFaceView(
                    capturedBitmap = capturedBitmap,
                    onCaptured = { bmp, base64 ->
                        viewModel.setCapturedPhoto(bmp, base64)
                    },
                    onReset = {
                        viewModel.capturedBitmap.value = null
                        viewModel.photoBase64.value = ""
                    }
                )
            }
        }

        // GPS Location Status Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = Color(0xFF0284C7)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "LOKASI GPS TERVERIFIKASI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                    Text(
                        text = locationState?.address ?: "Mendeteksi Lokasi GPS Kota Bitung...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = { viewModel.refreshGpsLocation() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh GPS", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // Security Encryption Note
        Surface(
            color = Color(0xFF0F172A),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Keamanan Data Ditingkatkan dengan Enkripsi End-to-End AES-256 & Otomatis Sinkronisasi Google Sheets.",
                    fontSize = 10.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 14.sp
                )
            }
        }

        // Submit Button
        Button(
            onClick = {
                viewModel.submitAttendance(
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "✅ Absensi WFH $jenisAbsensi Berhasil Terkirim & Tersimpan di Google Sheets!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, "❌ $err", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            enabled = !isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("Menyimpan & Sync Google Sheets...", color = Color.White)
            } else {
                Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFFF59E0B))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "KIRIM ABSENSI KEHADIRAN WFH",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }

    // Staff Selection Dialog
    if (showStaffSelectorDialog) {
        AlertDialog(
            onDismissRequest = { showStaffSelectorDialog = false },
            title = { Text("Pilih Profil Pegawai DPRD Bitung", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (allUsers.isEmpty()) {
                        Text(
                            text = "Belum ada data pegawai terdaftar di sistem. Anda dapat mengetik Nama & NIP secara manual pada form di bawah, atau menambah pegawai baru melalui menu Admin.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        allUsers.forEach { user ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectUserForForm(user)
                                        showStaffSelectorDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (user.tipePegawai == "PNS") Icons.Default.Badge else Icons.Default.WorkHistory,
                                        contentDescription = null,
                                        tint = if (user.tipePegawai == "PNS") Color(0xFF0284C7) else Color(0xFFD97706)
                                    )
                                    Column {
                                        Text(user.namaLengkap, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("NIP: ${user.nip} • ${user.jabatan}", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStaffSelectorDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }
}
