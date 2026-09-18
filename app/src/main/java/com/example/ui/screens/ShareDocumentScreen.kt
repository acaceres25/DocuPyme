package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*

@Composable
fun ShareDocumentScreen(
    document: DocumentEntity?,
    users: List<UserEntity>,
    onBack: () -> Unit,
    onShare: (targetUser: UserEntity, permissionLevel: String, expiresAt: Long?, message: String) -> Unit
) {
    var selectedUser by remember { mutableStateOf<UserEntity?>(users.firstOrNull()) }
    var permissionLevel by remember { mutableStateOf("Puede ver") }
    var message by remember { mutableStateOf("") }
    var expirationDays by remember { mutableStateOf(30) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DocuNavy)
                }
                Text("Compartir documento", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        // Header del documento a compartir
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = DocuGold, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(document?.name ?: "Archivo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DocuNavy)
                        Text("${(document?.sizeBytes ?: 0) / 1024} KB • ${document?.category}", fontSize = 11.sp, color = DocuTextSecondary)
                    }
                }
            }
        }

        item {
            Text("Seleccionar usuario:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
        }

        items(users) { u ->
            val isSelected = selectedUser?.id == u.id
            Card(
                colors = CardDefaults.cardColors(containerColor = if (isSelected) DocuGold.copy(alpha = 0.15f) else Color.White),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, DocuGold) else null,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().clickable { selectedUser = u }
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = isSelected, onClick = { selectedUser = u })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(u.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                        Text("${u.role} • ${u.email}", fontSize = 11.sp, color = DocuTextSecondary)
                    }
                }
            }
        }

        item {
            Text("Nivel de permiso:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Puede ver", "Puede editar", "Descarga autorizada").forEach { lvl ->
                    val isLvl = permissionLevel == lvl
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLvl) DocuNavy else Color.White,
                        border = if (isLvl) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.clickable { permissionLevel = lvl }
                    ) {
                        Text(lvl, color = if (isLvl) Color.White else DocuTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp))
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Mensaje (opcional)") },
                placeholder = { Text("Te comparto este documento para revisión...") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    selectedUser?.let { u ->
                        val expires = System.currentTimeMillis() + (expirationDays * 86400000L)
                        onShare(u, permissionLevel, expires, message)
                    }
                },
                enabled = selectedUser != null,
                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = DocuNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirmar y compartir", color = DocuNavy, fontWeight = FontWeight.Bold)
            }
        }
    }
}
