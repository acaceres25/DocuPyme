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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.*
import com.example.ui.Screen
import com.example.ui.theme.*

// -------------------------------------------------------------
// 3. REPORTES
// -------------------------------------------------------------
@Composable
fun ReportsScreen(
    documents: List<DocumentEntity>,
    folders: List<FolderEntity>,
    onBack: () -> Unit,
    onExportPdf: () -> Unit = {},
    onExportCsv: () -> Unit = {}
) {
    val totalDocs = documents.size
    val totalFolders = folders.size

    val pdfCount = documents.count { it.mimeType.contains("pdf") || it.name.endsWith(".pdf") }
    val xlsCount = documents.count { it.mimeType.contains("excel") || it.name.endsWith(".xlsx") }
    val imgCount = documents.count { it.mimeType.contains("image") }
    val otherCount = (totalDocs - pdfCount - xlsCount - imgCount).coerceAtLeast(0)

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
                Text("Reportes", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = DocuGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$totalDocs", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                        Text(text = "Documentos", fontSize = 12.sp, color = DocuTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "+24% vs período anterior", fontSize = 10.sp, color = DocuSuccess, fontWeight = FontWeight.Bold)
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "$totalFolders", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                        Text(text = "Carpetas", fontSize = 12.sp, color = DocuTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "+12% vs período anterior", fontSize = 10.sp, color = DocuSuccess, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Documentos por categoría
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Documentos por categoría", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DocuNavy)
                    Spacer(modifier = Modifier.height(14.dp))

                    val categories = listOf("Facturas" to 0.85f, "Contratos" to 0.6f, "Bancos" to 0.45f, "DIAN" to 0.7f, "Proveedores" to 0.5f)
                    categories.forEach { (cat, frac) ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(cat, fontSize = 12.sp, color = DocuTextPrimary)
                                Text("${(frac * 50).toInt()} docs", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { frac },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = DocuGold,
                                trackColor = Color(0xFFF3F4F6)
                            )
                        }
                    }
                }
            }
        }

        // Tipos de archivo
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Tipos de archivo", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DocuNavy)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        FileTypeBadge(label = "PDF", percent = "65%", color = DocuPdfRed)
                        FileTypeBadge(label = "Excel", percent = "20%", color = DocuExcelGreen)
                        FileTypeBadge(label = "Imágenes", percent = "10%", color = DocuImagePurple)
                        FileTypeBadge(label = "Otros", percent = "5%", color = DocuNavy)
                    }
                }
            }
        }

        // Acciones de Exportación (PDF / Excel)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = DocuGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Exportación y Reportes Oficiales", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DocuNavy)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Genera documentos consolidados para juntas directivas, auditorías tributarias o respaldo contable:",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onExportPdf,
                            colors = ButtonDefaults.buttonColors(containerColor = DocuNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("export_pdf_button")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = DocuGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar PDF", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onExportCsv,
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DocuNavy),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("export_excel_button")
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = DocuExcelGreen, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Exportar Excel", color = DocuNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileTypeBadge(label: String, percent: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.height(4.dp))
        Text(percent, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
        Text(label, fontSize = 11.sp, color = DocuTextSecondary)
    }
}

// -------------------------------------------------------------
// 4. USUARIOS Y PERMISOS
// -------------------------------------------------------------
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

// -------------------------------------------------------------
// 5. RESPALDOS Y SEGURIDAD
// -------------------------------------------------------------
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

// -------------------------------------------------------------
// 6. EDITAR PERFIL
// -------------------------------------------------------------
@Composable
fun ProfileScreen(
    user: UserEntity?,
    company: CompanyEntity?,
    onBack: () -> Unit,
    onSaveProfile: (name: String, cargo: String) -> Unit
) {
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var cargo by remember(user) { mutableStateOf(user?.cargo ?: "") }

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
                Text("Editar perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape).background(DocuNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(2).uppercase(), color = DocuGold, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DocuGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Cambiar", tint = DocuNavy, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = company?.name ?: "",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Empresa") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cargo,
                        onValueChange = { cargo = it },
                        label = { Text("Cargo en la empresa") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Button(
                onClick = { onSaveProfile(name, cargo) },
                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar cambios", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PAPELERA
// -------------------------------------------------------------
@Composable
fun TrashScreen(
    trashedDocuments: List<DocumentEntity>,
    onBack: () -> Unit,
    onRestore: (String) -> Unit,
    onDeletePermanently: (String) -> Unit
) {
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("Eliminación definitiva", fontWeight = FontWeight.Bold, color = DocuError) },
            text = { Text("¿Deseas purgar estos documentos permanentemente? Esta acción no se puede deshacer y registrará evento de auditoría.") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedIds.forEach { onDeletePermanently(it) }
                        selectedIds = emptySet()
                        showConfirmDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuError)
                ) {
                    Text("Eliminar definitivamente", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }

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
                Text("Papelera", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        // Info Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = DocuError, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Los documentos eliminados se pueden recuperar durante 30 días.",
                        fontSize = 12.sp,
                        color = Color(0xFF991B1B)
                    )
                }
            }
        }

        if (trashedDocuments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("La papelera está vacía", color = DocuTextSecondary)
                }
            }
        } else {
            items(trashedDocuments) { doc ->
                val isSelected = selectedIds.contains(doc.id)
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
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = {
                                selectedIds = if (it) selectedIds + doc.id else selectedIds - doc.id
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                            Text("Quedan 25 días para purga", fontSize = 11.sp, color = DocuError)
                        }
                        IconButton(onClick = { onRestore(doc.id) }) {
                            Icon(Icons.Default.Restore, contentDescription = "Restaurar", tint = DocuNavy)
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedIds.forEach { onRestore(it) }
                            selectedIds = emptySet()
                        },
                        enabled = selectedIds.isNotEmpty(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(46.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, tint = DocuNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Restaurar", color = DocuNavy)
                    }

                    Button(
                        onClick = { showConfirmDeleteDialog = true },
                        enabled = selectedIds.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f).height(46.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = DocuNavy)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar def.", color = DocuNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// COMPARTIR DOCUMENTO
// -------------------------------------------------------------
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

// -------------------------------------------------------------
// ARCHIVOS DUPLICADOS (RF12)
// -------------------------------------------------------------
@Composable
fun DuplicateFilesScreen(
    duplicateDocuments: List<DocumentEntity>,
    onBack: () -> Unit,
    onOpenDocument: (String) -> Unit,
    onDeleteCopy: (String) -> Unit
) {
    var docToDelete by remember { mutableStateOf<DocumentEntity?>(null) }

    if (docToDelete != null) {
        AlertDialog(
            onDismissRequest = { docToDelete = null },
            title = { Text("Eliminar copia duplicada", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = { Text("¿Deseas eliminar '${docToDelete?.name}'? El documento original permanecerá intacto en su carpeta.") },
            confirmButton = {
                Button(
                    onClick = {
                        docToDelete?.let { onDeleteCopy(it.id) }
                        docToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuError)
                ) {
                    Text("Eliminar copia", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { docToDelete = null }) { Text("Cancelar") }
            }
        )
    }

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
                Text("Archivos duplicados", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "${duplicateDocuments.size} posibles duplicados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DocuNavy
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hemos encontrado archivos con contenido idéntico por suma de verificación criptográfica SHA-256.",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )
                }
            }
        }

        if (duplicateDocuments.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    Text("No se detectaron archivos duplicados en la empresa.", color = DocuTextSecondary)
                }
            }
        } else {
            items(duplicateDocuments) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(doc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DocuNavy, modifier = Modifier.weight(1f))
                            Surface(shape = RoundedCornerShape(4.dp), color = DocuGold.copy(alpha = 0.25f)) {
                                Text("100% coincidencia", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DocuNavy, modifier = Modifier.padding(4.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Hash SHA-256: ${doc.fileHash.take(16)}...", fontSize = 11.sp, color = DocuTextMuted)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onOpenDocument(doc.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Comparar / Ver", fontSize = 12.sp, color = DocuNavy)
                            }

                            Button(
                                onClick = { docToDelete = doc },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFEE2E2)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).height(38.dp)
                            ) {
                                Text("Eliminar copia", fontSize = 12.sp, color = DocuError, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// NOTIFICACIONES
// -------------------------------------------------------------
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

// -------------------------------------------------------------
// MODAL DE AUDITORÍA Y TRAZABILIDAD (RF09)
// -------------------------------------------------------------
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
