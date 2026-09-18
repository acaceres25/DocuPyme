package com.example.ui.screens

import androidx.compose.foundation.background
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
