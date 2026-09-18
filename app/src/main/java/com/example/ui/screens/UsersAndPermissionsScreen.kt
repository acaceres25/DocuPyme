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
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun UsersAndPermissionsScreen(
    users: List<UserEntity>,
    currentUser: UserEntity?,
    onBack: () -> Unit,
    onInviteUser: (name: String, email: String, role: String, cargo: String) -> Unit,
    onSwitchUser: (String) -> Unit,
    onNavigateToSettingsItem: (Screen) -> Unit
) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var inviteName by remember { mutableStateOf("") }
    var inviteEmail by remember { mutableStateOf("") }
    var inviteRole by remember { mutableStateOf("Editor") }
    var inviteCargo by remember { mutableStateOf("Auxiliar") }

    // Toggle states for permissions demo
    var canCreate by remember { mutableStateOf(true) }
    var canEdit by remember { mutableStateOf(true) }
    var canShare by remember { mutableStateOf(true) }
    var canDelete by remember { mutableStateOf(false) }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invitar nuevo usuario", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = inviteName,
                        onValueChange = { inviteName = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inviteEmail,
                        onValueChange = { inviteEmail = it },
                        label = { Text("Correo electrónico") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = inviteCargo,
                        onValueChange = { inviteCargo = it },
                        label = { Text("Cargo en la empresa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Rol de permisos:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Editor", "Consulta", "Administrador").forEach { r ->
                            val isSel = inviteRole == r
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) DocuGold else Color(0xFFF3F4F6),
                                modifier = Modifier.clickable { inviteRole = r }
                            ) {
                                Text(r, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DocuNavy, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inviteName.isNotBlank() && inviteEmail.isNotBlank()) {
                            onInviteUser(inviteName, inviteEmail, inviteRole, inviteCargo)
                            showInviteDialog = false
                            inviteName = ""
                            inviteEmail = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold)
                ) {
                    Text("Enviar invitación", color = DocuNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    Text("Usuarios y permisos", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                }

                IconButton(onClick = { showInviteDialog = true }) {
                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Invitar", tint = DocuNavy)
                }
            }
        }

        // Navegación a otros módulos de Ajustes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Módulos del Sistema", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onNavigateToSettingsItem(Screen.BACKUP_SECURITY) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Respaldos", color = DocuNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onNavigateToSettingsItem(Screen.EDIT_PROFILE) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Mi Perfil", color = DocuNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onNavigateToSettingsItem(Screen.REPORTS) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F4F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reportes", color = DocuNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Selector rápido de usuario activo para pruebas de permisos (CUJ de permisos)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DocuGold.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DocuGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Demostración de Permisos y Roles", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                    Text("Toca un usuario para probar la experiencia según su rol:", fontSize = 11.sp, color = DocuTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        users.forEach { u ->
                            val isCurrent = currentUser?.id == u.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCurrent) DocuNavy else Color.White,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSwitchUser(u.id) }
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = u.name.split(" ").first(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) DocuGold else DocuNavy
                                    )
                                    Text(
                                        text = u.role,
                                        fontSize = 9.sp,
                                        color = if (isCurrent) Color.White.copy(alpha = 0.8f) else DocuTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Lista de usuarios
        item {
            Text("Equipo de la Empresa (${users.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DocuNavy)
        }

        items(users) { userItem ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(DocuNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userItem.name.take(2).uppercase(), color = DocuGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(userItem.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = DocuNavy)
                        Text("${userItem.cargo} • ${userItem.email}", fontSize = 11.sp, color = DocuTextSecondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (userItem.role) {
                            "Administrador" -> DocuGold
                            "Editor" -> Color(0xFF60A5FA).copy(alpha = 0.3f)
                            else -> Color(0xFFE5E7EB)
                        }
                    ) {
                        Text(
                            text = userItem.role,
                            color = DocuNavy,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Permisos Generales
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Políticas de Acceso", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DocuNavy)
                    PermissionToggleRow("Crear y subir documentos", canCreate) { canCreate = it }
                    PermissionToggleRow("Editar y versionar documentos", canEdit) { canEdit = it }
                    PermissionToggleRow("Compartir documentos externamente", canShare) { canShare = it }
                    PermissionToggleRow("Eliminar documentos definitivamente", canDelete) { canDelete = it }
                }
            }
        }

        item {
            Button(
                onClick = { showInviteDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, tint = DocuNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar usuario", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun PermissionToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = DocuTextPrimary)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = DocuNavy, checkedTrackColor = DocuGold)
        )
    }
}
