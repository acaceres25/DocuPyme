package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.local.DocumentEntity
import com.example.data.local.FolderEntity
import com.example.ui.theme.*

@Composable
fun FolderDetailScreen(
    folder: FolderEntity?,
    documents: List<DocumentEntity>,
    onBack: () -> Unit,
    onOpenDocument: (String) -> Unit,
    onOpenUpload: () -> Unit,
    onShare: (DocumentEntity) -> Unit,
    onDownload: (DocumentEntity) -> Unit,
    onTrash: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") } // "Todos", "PDF", "Excel", "Imágenes"

    val filteredDocuments = documents.filter { doc ->
        val matchesSearch = doc.name.contains(searchQuery, ignoreCase = true)
        val matchesType = when (selectedFilter) {
            "PDF" -> doc.mimeType.contains("pdf", ignoreCase = true) || doc.name.endsWith(".pdf", ignoreCase = true)
            "Excel" -> doc.mimeType.contains("excel", ignoreCase = true) || doc.name.endsWith(".xlsx", ignoreCase = true) || doc.name.endsWith(".xls", ignoreCase = true)
            "Imágenes" -> doc.mimeType.contains("image", ignoreCase = true) || doc.name.endsWith(".png", ignoreCase = true) || doc.name.endsWith(".jpg", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesType
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header with back button & title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = DocuNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = folder?.name ?: "Carpeta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = DocuNavy
                    )
                }

                IconButton(onClick = { /* Folder options */ }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Opciones", tint = DocuNavy)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador dentro de carpeta
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar en esta carpeta...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = DocuTextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DocuNavy,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips (Todos, PDF, Excel, Imágenes)
            val filters = listOf("Todos", "PDF", "Excel", "Imágenes")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filterName ->
                    val isSelected = selectedFilter == filterName
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) DocuGold else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.clickable { selectedFilter = filterName }
                    ) {
                        Text(
                            text = filterName,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DocuNavy else DocuTextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Document List
            if (filteredDocuments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            tint = DocuTextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay documentos en esta vista",
                            color = DocuTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredDocuments) { doc ->
                        FolderDocumentRow(
                            doc = doc,
                            onClick = { onOpenDocument(doc.id) },
                            onShare = { onShare(doc) },
                            onDownload = { onDownload(doc) },
                            onTrash = { onTrash(doc.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón inferior "Subir documento"
            Button(
                onClick = onOpenUpload,
                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .testTag("folder_upload_button")
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = DocuNavy)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Subir documento", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FolderDocumentRow(
    doc: DocumentEntity,
    onClick: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onTrash: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    val isPdf = doc.mimeType.contains("pdf", ignoreCase = true) || doc.name.endsWith(".pdf", ignoreCase = true)
    val isExcel = doc.mimeType.contains("excel", ignoreCase = true) || doc.name.endsWith(".xlsx", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPdf) DocuPdfRed.copy(alpha = 0.12f) else if (isExcel) DocuExcelGreen.copy(alpha = 0.12f) else DocuGold.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPdf) "PDF" else if (isExcel) "XLS" else "DOC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPdf) DocuPdfRed else if (isExcel) DocuExcelGreen else DocuNavy
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = doc.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = DocuTextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${(doc.sizeBytes / 1024)} KB • ${doc.backupStatus}",
                    fontSize = 11.sp,
                    color = DocuTextMuted
                )
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = DocuTextSecondary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Ver detalle") },
                        leadingIcon = { Icon(Icons.Default.Visibility, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Compartir") },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Descargar") },
                        leadingIcon = { Icon(Icons.Default.Download, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onDownload()
                        }
                    )
                    Divider()
                    DropdownMenuItem(
                        text = { Text("Mover a papelera", color = DocuError) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = DocuError) },
                        onClick = {
                            showMenu = false
                            onTrash()
                        }
                    )
                }
            }
        }
    }
}
