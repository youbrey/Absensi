package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GoogleSheetsManager
import com.example.ui.viewmodel.AttendanceViewModel
import com.example.util.NotificationHelper

@Composable
fun AdminSettingsScreen(
    viewModel: AttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var webhookInput by remember { mutableStateOf(viewModel.webhookUrlState.value) }
    var pushEnabled by remember { mutableStateOf(viewModel.pushNotificationsEnabled.value) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(32.dp)
                    )

                    Column {
                        Text(
                            text = "PENGATURAN SISTEM & GOOGLE SHEETS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Konfigurasi Webhook API, Push Notification & Keamanan",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = {
                        viewModel.logoutAdmin()
                        Toast.makeText(context, "Berhasil Keluar Mode Admin", Toast.LENGTH_SHORT).show()
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = "Keluar Admin",
                        tint = Color(0xFFF43F5E)
                    )
                }
            }
        }

        // Google Sheets Integration Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.TableRows, contentDescription = null, tint = Color(0xFF059669))
                    Text(
                        text = "SINKRONISASI GOOGLE SHEETS API",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669)
                    )
                }

                Text(
                    text = "URL Webhook Google Apps Script tempat data absensi WFH dikirim secara otomatis:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = webhookInput,
                    onValueChange = {
                        webhookInput = it
                        GoogleSheetsManager.webhookUrl = it
                        viewModel.webhookUrlState.value = it
                    },
                    label = { Text("URL Webhook Apps Script") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        GoogleSheetsManager.webhookUrl = webhookInput
                        Toast.makeText(context, "URL Google Sheets Webhook disimpan!", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Simpan Konfigurasi Webhook")
                }
            }
        }

        // Push Notification System Settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "NOTIFIKASI PENGINGAT HARIAN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Switch(
                        checked = pushEnabled,
                        onCheckedChange = {
                            pushEnabled = it
                            viewModel.pushNotificationsEnabled.value = it
                        }
                    )
                }

                Text(
                    text = "Jadwal Pengingat Absensi Harian WFH:\n• Pengingat Absensi Masuk: Pukul 07.15 WITA\n• Pengingat Absensi Pulang: Pukul 12.15 WITA",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            NotificationHelper.showAbsensiReminder(context, isMasuk = true)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tes Notif Masuk", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            NotificationHelper.showAbsensiReminder(context, isMasuk = false)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Tes Notif Pulang", fontSize = 11.sp)
                    }
                }
            }
        }

        // Encryption Security Info Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.EnhancedEncryption, contentDescription = null, tint = Color(0xFF10B981))
                    Text(
                        text = "ENKRIPSI END-TO-END AES-256",
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Seluruh payload NIP, data foto, dan koordinat GPS dienkripsi dengan standar AES-256 & hash SHA-256 sebelum disimpan ke dalam database lokal maupun ditransmisikan.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
