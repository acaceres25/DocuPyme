package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.*
import com.example.ui.theme.*

@Composable
fun AuditLogDialog(
    events: List<AuditEventEntity>,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(DocuNavy).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = DocuGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Registro de Auditoría (RF09)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f).padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(events) { evt ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(evt.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DocuNavy)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (evt.result == "Éxito") DocuSuccess.copy(alpha = 0.15f) else DocuError.copy(alpha = 0.15f)
                                    ) {
                                        Text(evt.result, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (evt.result == "Éxito") DocuSuccess else DocuError, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("Actor: ${evt.actorName}", fontSize = 11.sp, color = DocuTextPrimary)
                                Text(evt.details, fontSize = 11.sp, color = DocuTextSecondary)
                                Text(formatTimestamp(evt.timestamp), fontSize = 9.sp, color = DocuTextMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}
