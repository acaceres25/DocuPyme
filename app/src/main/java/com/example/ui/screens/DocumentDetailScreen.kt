package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DocumentEntity
import com.example.data.local.DocumentPermissionEntity
import com.example.data.local.DocumentVersionEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DocumentDetailScreen(
    document: DocumentEntity?,
    versions: List<DocumentVersionEntity>,
    permissions: List<DocumentPermissionEntity>,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onUploadNewVersion: (notes: String) -> Unit,
    onMoveToTrash: () -> Unit,
    onToggleStar: () -> Unit,
    onOpenViewer: (String) -> Unit = {}
) {
    var showPreviewDialog by remember { mutableStateOf(false) }
    var showNewVersionDialog by remember { mutableStateOf(false) }
    var versionNotes by remember { mutableStateOf("") }

    if (document == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Documento no encontrado", color = DocuTextSecondary)
        }
        return
    }

    val isPdf = document.mimeType.contains("pdf", ignoreCase = true) || document.name.endsWith(".pdf", ignoreCase = true)
    val isExcel = document.mimeType.contains("excel", ignoreCase = true) || document.name.endsWith(".xlsx", ignoreCase = true)

    // Modal de vista previa (Ver)
    if (showPreviewDialog) {
        DocumentPreviewModal(
            document = document,
            onDismiss = { showPreviewDialog = false },
            onShare = onShare,
            onDownload = onDownload
        )
    }

    // Modal de nueva versión
    if (showNewVersionDialog) {
        AlertDialog(
            onDismissRequest = { showNewVersionDialog = false },
            title = { Text("Subir nueva versión", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Se registrará la versión v${versions.size + 1} manteniendo las anteriores en el historial.",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )
                    OutlinedTextField(
                        value = versionNotes,
                        onValueChange = { versionNotes = it },
                        label = { Text("Nota de cambio / motivo") },
                        placeholder = { Text("ej. Firma autorizada y sello DIAN") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUploadNewVersion(versionNotes)
                        versionNotes = ""
                        showNewVersionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold)
                ) {
                    Text("Subir v${versions.size + 1}", color = DocuNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewVersionDialog = false }) {
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
        // Top Header
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
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Documento",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DocuNavy
                    )
                }

                IconButton(onClick = onToggleStar) {
                    Icon(
                        imageVector = if (document.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorito",
                        tint = if (document.isStarred) DocuGold else DocuTextMuted
                    )
                }
            }
        }

        // File Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isPdf) DocuPdfRed.copy(alpha = 0.12f) else if (isExcel) DocuExcelGreen.copy(alpha = 0.12f) else DocuGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPdf) "PDF" else if (isExcel) "XLS" else "DOC",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPdf) DocuPdfRed else if (isExcel) DocuExcelGreen else DocuNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = document.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DocuTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${(document.sizeBytes / 1024)} KB • ${document.category}",
                            fontSize = 12.sp,
                            color = DocuTextSecondary
                        )
                    }
                }
            }
        }

        // Metadata Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetadataRow(
                        icon = Icons.Default.Folder,
                        label = "Carpeta",
                        value = document.category
                    )
                    Divider(color = Color(0xFFF3F4F6))
                    MetadataRow(
                        icon = Icons.Default.CalendarToday,
                        label = "Fecha de subida",
                        value = formatTimestamp(document.createdAt)
                    )
                    Divider(color = Color(0xFFF3F4F6))
                    MetadataRow(
                        icon = Icons.Default.Person,
                        label = "Responsable",
                        value = "Laura Gómez (Admin)"
                    )
                    Divider(color = Color(0xFFF3F4F6))
                    MetadataRow(
                        icon = Icons.Default.CloudDone,
                        label = "Estado de respaldo",
                        value = document.backupStatus,
                        valueColor = DocuSuccess
                    )
                }
            }
        }

        // Action Buttons: Ver, Compartir, Descargar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { onOpenViewer(document.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .testTag("preview_button")
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null, tint = DocuNavy)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abrir Visor Nativo (Zoom, Páginas y Firma)", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onShare,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("share_button")
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compartir", color = DocuNavy, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onDownload,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .testTag("download_button")
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Descargar", color = DocuNavy, fontSize = 13.sp)
                    }
                }
            }
        }

        // Historial de Versiones (RF07)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = DocuNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Historial de versiones",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DocuNavy
                            )
                        }

                        TextButton(onClick = { showNewVersionDialog = true }) {
                            Text("+ Nueva versión", color = DocuGoldDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (versions.isEmpty()) {
                        Text("v1 • Versión actual cargada", fontSize = 12.sp, color = DocuTextSecondary)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            versions.forEach { ver ->
                                VersionItemRow(version = ver)
                            }
                        }
                    }
                }
            }
        }

        // Accesos compartidos activos (si los hay)
        if (permissions.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Accesos Compartidos",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DocuNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        permissions.forEach { perm ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = perm.userName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DocuTextPrimary)
                                    Text(text = "${perm.permissionLevel} • ${perm.userEmail}", fontSize = 11.sp, color = DocuTextSecondary)
                                }
                                Surface(shape = RoundedCornerShape(4.dp), color = DocuGold.copy(alpha = 0.2f)) {
                                    Text("Activo", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DocuNavy, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Zona de eliminación (Papelera)
        item {
            OutlinedButton(
                onClick = onMoveToTrash,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DocuError),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DocuError.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = DocuError, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mover a papelera (30 días de retención)", color = DocuError, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun MetadataRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color = DocuTextPrimary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = DocuTextMuted, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = label, fontSize = 13.sp, color = DocuTextSecondary)
        }
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

@Composable
fun VersionItemRow(version: DocumentVersionEntity) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF9FAFB))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(DocuGold.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "v${version.versionNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = DocuNavy
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = formatTimestamp(version.uploadedAt),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = DocuTextPrimary
                )
                if (version.notes.isNotBlank()) {
                    Text(
                        text = version.notes,
                        fontSize = 11.sp,
                        color = DocuTextSecondary
                    )
                }
            }
        }

        Text(
            text = version.uploadedByName,
            fontSize = 11.sp,
            color = DocuTextMuted
        )
    }
}

@Composable
fun DocumentPreviewModal(
    document: DocumentEntity,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Modal Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DocuNavy)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vista previa del documento",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                    }
                }

                // Document Content / Sheet simulation
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = document.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = DocuNavy
                                )
                                Text(
                                    text = document.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DocuGoldDark
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "TEXTO EXTRAÍDO Y VERIFICADO:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DocuTextMuted
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = document.textContent.ifEmpty { "Contenido digital del documento procesado con firma criptográfica SHA-256: ${document.fileHash}." },
                                fontSize = 13.sp,
                                color = DocuTextPrimary,
                                lineHeight = 20.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFF3F4F6),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "Hash SHA-256 (Integridad verificada):",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DocuTextSecondary
                                    )
                                    Text(
                                        text = document.fileHash,
                                        fontSize = 10.sp,
                                        color = DocuTextMuted,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Modal Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            onShare()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Compartir", color = DocuNavy)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onDownload()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Descargar", color = DocuNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd 'de' MMMM 'de' yyyy, HH:mm", Locale("es", "CO"))
    return sdf.format(Date(timestamp))
}
