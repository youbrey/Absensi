package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar

data class TimeWindowStatus(
    val isOpen: Boolean,
    val windowType: String, // "MASUK", "PULANG", or "CLOSED"
    val title: String,
    val subtitle: String,
    val color: Color
)

fun checkTimeWindow(): TimeWindowStatus {
    val cal = Calendar.getInstance()
    val hour = cal.get(Calendar.HOUR_OF_DAY)

    return when {
        hour in 7..8 || (hour == 9 && cal.get(Calendar.MINUTE) == 0) -> {
            TimeWindowStatus(
                isOpen = true,
                windowType = "MASUK",
                title = "JADWAL ABSENSI MASUK WFH DIBUKA (07.00 - 09.00 WITA)",
                subtitle = "Sistem menerima verifikasi kehadiran masuk.",
                color = Color(0xFF10B981) // Green
            )
        }
        hour in 12..13 || (hour == 14 && cal.get(Calendar.MINUTE) == 0) -> {
            TimeWindowStatus(
                isOpen = true,
                windowType = "PULANG",
                title = "JADWAL ABSENSI PULANG WFH DIBUKA (12.00 - 14.00 WITA)",
                subtitle = "Sistem menerima verifikasi kepulangan.",
                color = Color(0xFF38BDF8) // Blue
            )
        }
        else -> {
            TimeWindowStatus(
                isOpen = false,
                windowType = "CLOSED",
                title = "DILUAR JAM OPERASIONAL ABSENSI WFH",
                subtitle = "Masuk: 07.00-09.00 WITA | Pulang: 12.00-14.00 WITA",
                color = Color(0xFFEF4444) // Red
            )
        }
    }
}

@Composable
fun AttendanceTimeBadge(
    isAdminOverride: Boolean = false,
    modifier: Modifier = Modifier
) {
    val status = remember { checkTimeWindow() }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = status.color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(status.color))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = if (status.isOpen || isAdminOverride) Icons.Default.CheckCircle else Icons.Default.LockClock,
                contentDescription = null,
                tint = if (isAdminOverride) Color(0xFFF59E0B) else status.color,
                modifier = Modifier.size(28.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isAdminOverride) "MODE ADM / DISPENSASI KHUSUS DIBUKA" else status.title,
                    color = if (isAdminOverride) Color(0xFFF59E0B) else status.color,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAdminOverride) "Bebas melakukan verifikasi masuk / pulang kapan saja." else status.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}
