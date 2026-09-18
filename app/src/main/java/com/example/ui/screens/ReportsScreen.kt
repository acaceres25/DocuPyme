package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.data.local.*
import com.example.ui.theme.*

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
