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
