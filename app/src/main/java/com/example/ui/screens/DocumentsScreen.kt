package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
fun DocumentsScreen(
    folders: List<FolderEntity>,
    documents: List<DocumentEntity>,
    onOpenFolder: (String) -> Unit,
    onCreateFolder: (String) -> Unit,
    onOpenUpload: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }

    val filteredFolders = folders.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Crear nueva carpeta", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    label = { Text("Nombre de la carpeta") },
                    placeholder = { Text("ej. Auditorías 2026") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DocuNavy,
                        cursorColor = DocuNavy
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFolderName.isNotBlank()) {
                            onCreateFolder(newFolderName.trim())
                            newFolderName = ""
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold)
                ) {
                    Text("Crear", color = DocuNavy, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancelar", color = DocuTextSecondary)
                }
            }
        )
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
            Text(
                text = "Mis carpetas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DocuNavy
            )
            Text(
                text = "Organiza y encuentra tus documentos",
                fontSize = 13.sp,
                color = DocuTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Campo de búsqueda en carpetas
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar carpetas por nombre...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = DocuTextSecondary
                    )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Grid de Carpetas
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredFolders) { folder ->
                    val docsInFolder = documents.count { it.folderId == folder.id && it.status == "activo" }
                    FolderGridCard(
                        folder = folder,
                        docCount = docsInFolder,
                        onClick = { onOpenFolder(folder.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones inferiores: Nueva carpeta & Subir documento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = DocuNavy),
                    modifier = Modifier
                        .testTag("create_folder_button")
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null, tint = DocuNavy)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Nueva carpeta", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onOpenUpload,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                    modifier = Modifier
                        .testTag("folders_upload_button")
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = DocuNavy)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subir doc", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DocuNavy)
                }
            }
        }
    }
}

@Composable
fun FolderGridCard(
    folder: FolderEntity,
    docCount: Int,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .testTag("folder_card_${folder.name}")
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (folder.name) {
                                "Facturas" -> DocuGold.copy(alpha = 0.25f)
                                "Contratos" -> Color(0xFF3B82F6).copy(alpha = 0.2f)
                                "Bancos" -> Color(0xFF10B981).copy(alpha = 0.2f)
                                "DIAN" -> Color(0xFFEF4444).copy(alpha = 0.2f)
                                "Recursos Humanos" -> Color(0xFF8B5CF6).copy(alpha = 0.2f)
                                else -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = when (folder.name) {
                            "Facturas" -> DocuNavy
                            "Contratos" -> Color(0xFF1D4ED8)
                            "Bancos" -> Color(0xFF047857)
                            "DIAN" -> Color(0xFFB91C1C)
                            "Recursos Humanos" -> Color(0xFF6D28D9)
                            else -> Color(0xFFB45309)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Opciones",
                    tint = DocuTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = folder.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DocuNavy,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$docCount archivos",
                fontSize = 12.sp,
                color = DocuTextSecondary
            )
        }
    }
}
