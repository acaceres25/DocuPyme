package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onBack: () -> Unit,
    onMarkAllRead: () -> Unit,
    onMarkRead: (String) -> Unit,
    onTriggerSystemAlert: (title: String, message: String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DocuNavy)
                    }
                    Text("Notificaciones", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                }

                TextButton(onClick = onMarkAllRead) {
                    Text("Marcar leídas", color = DocuNavy, fontSize = 12.sp)
                }
            }
        }

        // Action banner to test Android system alert notifications
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DocuGold),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null, tint = DocuGold, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Simulador de Alerta del Sistema", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DocuNavy)
                        Text("Dispara una notificación push nativa de Android para probar avisos de vencimiento", fontSize = 11.sp, color = DocuTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onTriggerSystemAlert(
                                "Alerta DocuPyme: Vencimiento Próximo",
                                "La Matrícula Mercantil de Cámara de Comercio vence en 15 días. Presiona para gestionar renovación."
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DocuNavy),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("Emitir", fontSize = 11.sp, color = DocuGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (notifications.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No hay notificaciones pendientes", color = DocuTextSecondary)
                }
            }
        } else {
            items(notifications) { notif ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (notif.isRead) Color.White else Color(0xFFFFFBEB)),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onMarkRead(notif.id) }
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(DocuGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (notif.type) {
                                    "COMPARTIDO" -> Icons.Default.Share
                                    "VENCIMIENTO" -> Icons.Default.Warning
                                    else -> Icons.Default.CloudDone
                                },
                                contentDescription = null,
                                tint = DocuNavy,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                            Text(notif.message, fontSize = 11.sp, color = DocuTextSecondary)
                        }
                        if (!notif.isRead) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DocuGold))
                        }
                    }
                }
            }
        }
    }
}
