package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*

@Composable
fun BackupAndSecurityScreen(
    latestBackup: BackupEntity?,
    onBack: () -> Unit,
    onRunBackupNow: () -> Unit,
    onRunTestRestore: () -> Unit
) {
    var autoBackupEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DocuNavy)
                }
                Text("Respaldos y seguridad", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        // Respaldo Activo Banner Card (matches mockup!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(60.dp).clip(CircleShape).background(DocuSuccess.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CloudDone, contentDescription = null, tint = DocuSuccess, modifier = Modifier.size(36.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Respaldo activo", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DocuNavy)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tus documentos se respaldan automáticamente en la nube con cifrado AES-256.",
                        fontSize = 12.sp,
                        color = DocuTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        // Detalles del Respaldo
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetadataRow(icon = Icons.Default.CalendarToday, label = "Último respaldo", value = "18 de septiembre de 2026, 02:30")
                    Divider(color = Color(0xFFF3F4F6))
                    MetadataRow(icon = Icons.Default.Schedule, label = "Próximo respaldo", value = "19 de septiembre de 2026, 02:30")
                    Divider(color = Color(0xFFF3F4F6))
                    MetadataRow(icon = Icons.Default.Sync, label = "Frecuencia", value = "Diaria (02:00 AM)")
                    Divider(color = Color(0xFFF3F4F6))

                    // Almacenamiento usado con barra de progreso
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Almacenamiento usado", fontSize = 12.sp, color = DocuTextSecondary)
                            Text("2.4 GB de 10 GB (24%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { 0.24f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = DocuNavy,
                            trackColor = Color(0xFFF3F4F6)
                        )
                    }

                    Divider(color = Color(0xFFF3F4F6))

                    // Switch respaldo automático
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Respaldo automático", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                            Text("Se realiza diariamente", fontSize = 11.sp, color = DocuTextSecondary)
                        }
                        Switch(
                            checked = autoBackupEnabled,
                            onCheckedChange = { autoBackupEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DocuNavy, checkedTrackColor = DocuGold)
                        )
                    }
                }
            }
        }

        // Resultado de restauración de prueba
        latestBackup?.restorationResult?.let { result ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DocuSuccess.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DocuSuccess.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = DocuSuccess, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verificación de Integridad de Respaldo", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(result, fontSize = 12.sp, color = DocuTextPrimary, lineHeight = 16.sp)
                    }
                }
            }
        }

        // Action buttons
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onRunBackupNow,
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = DocuNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Realizar respaldo ahora", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = onRunTestRestore,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(46.dp)
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = DocuNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ejecutar prueba de restauración (RF10)", color = DocuNavy, fontSize = 13.sp)
                }
            }
        }
    }
}
