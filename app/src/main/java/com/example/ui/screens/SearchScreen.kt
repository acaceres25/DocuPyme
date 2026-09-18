package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    documents: List<DocumentEntity>,
    onOpenDocument: (String) -> Unit,
    onDownloadDocument: (DocumentEntity) -> Unit,
    onShareDocument: (DocumentEntity) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    val categories = listOf("Todos", "Facturas", "Contratos", "Bancos", "DIAN", "Proveedores")

    val searchResults = remember(query, selectedCategory, documents) {
        documents.filter { doc ->
            val matchesQuery = query.isBlank() ||
                    doc.name.contains(query, ignoreCase = true) ||
                    doc.category.contains(query, ignoreCase = true) ||
                    doc.textContent.contains(query, ignoreCase = true)

            val matchesCat = selectedCategory == "Todos" || doc.category.equals(selectedCategory, ignoreCase = true)

            matchesQuery && matchesCat && doc.status == "activo"
        }
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
                text = "Buscar documentos",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DocuNavy
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador principal
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar por nombre, categoría o texto...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = DocuTextSecondary)
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
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
                    .testTag("search_text_input")
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros de categoría
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) DocuGold else Color.White,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DocuNavy else DocuTextSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Contador de resultados (matches mockup: "Encontré 3 documentos:")
            Text(
                text = "Encontré ${searchResults.size} documento${if (searchResults.size != 1) "s" else ""}:",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DocuNavy
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Default.SearchOff, contentDescription = null, tint = DocuTextMuted, modifier = Modifier.size(54.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No se encontraron documentos coincidentes", color = DocuTextSecondary, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(searchResults) { doc ->
                        SearchResultCard(
                            doc = doc,
                            onOpen = { onOpenDocument(doc.id) },
                            onDownload = { onDownloadDocument(doc) },
                            onShare = { onShareDocument(doc) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    doc: DocumentEntity,
    onOpen: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit
) {
    val isPdf = doc.mimeType.contains("pdf", ignoreCase = true) || doc.name.endsWith(".pdf", ignoreCase = true)
    val isExcel = doc.mimeType.contains("excel", ignoreCase = true) || doc.name.endsWith(".xlsx", ignoreCase = true)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DocuNavy,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${(doc.sizeBytes / 1024)} KB • ${doc.category}",
                        fontSize = 11.sp,
                        color = DocuTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons (Matches mockup: "Ver" yellow button, "Descargar" outlined button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                ) {
                    Text("Ver", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onDownload,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Descargar", color = DocuNavy, fontSize = 12.sp)
                }
            }
        }
    }
}
