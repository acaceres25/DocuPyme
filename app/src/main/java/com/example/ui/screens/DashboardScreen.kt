package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CompanyEntity
import com.example.data.local.DocumentEntity
import com.example.data.local.FolderEntity
import com.example.data.local.UserEntity
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    company: CompanyEntity?,
    user: UserEntity?,
    activeDocuments: List<DocumentEntity>,
    folders: List<FolderEntity>,
    duplicateDocumentsCount: Int,
    trashedDocumentsCount: Int,
    onNavigate: (Screen) -> Unit,
    onOpenDocument: (String) -> Unit,
    onOpenUpload: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saludo
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(DocuNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (user?.name?.take(2) ?: "DG").uppercase(),
                            color = DocuGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, ${user?.name ?: "Usuario"} 👋",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = DocuTextPrimary
                        )
                        Text(
                            text = "${user?.cargo ?: "Gestor"} • ${company?.name ?: "DocuPyme"}",
                            fontSize = 13.sp,
                            color = DocuTextSecondary
                        )
                    }
                }
            }
        }

        // Resumen de Documentos (3 Cards)
        item {
            Text(
                text = "Resumen de Documentos",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = DocuNavy
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Total de\ndocumentos",
                    value = activeDocuments.size.toString(),
                    icon = Icons.Default.Description,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(Screen.DOCUMENTS)
                }
                MetricCard(
                    title = "En nube\nrespaldados",
                    value = activeDocuments.count { it.backupStatus.contains("nube") }.toString(),
                    icon = Icons.Default.CloudDone,
                    iconTint = DocuSuccess,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(Screen.BACKUP_SECURITY)
                }
                MetricCard(
                    title = "Carpetas\nactivas",
                    value = folders.size.toString(),
                    icon = Icons.Default.Folder,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(Screen.DOCUMENTS)
                }
            }
        }

        // Alerta de vencimiento (matches mockup!)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DocuGold.copy(alpha = 0.9f)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.SEARCH) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Alerta",
                        tint = DocuNavy,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tu Cámara de comercio vence en 15 días",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = DocuNavy
                        )
                        Text(
                            text = "Toca para revisar el certificado y preparar la renovación.",
                            fontSize = 12.sp,
                            color = DocuNavy.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Asistente DocuBot IA
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DocuNavy),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Screen.CHATBOT) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DocuGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "DocuBot",
                                    tint = DocuNavy,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "DocuBot IA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = DocuGold
                                    ) {
                                        Text(
                                            text = "Gemini 3.5",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DocuNavy,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "Asistente inteligente para tu empresa",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Ir al chat",
                            tint = DocuGold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Consulta al instante qué documentos están pendientes, cuáles toca revisar y qué procesos faltan:",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.CHATBOT) }
                        ) {
                            Text(
                                text = "📋 Pendientes",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.CHATBOT) }
                        ) {
                            Text(
                                text = "🔍 Por revisar",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigate(Screen.CHATBOT) }
                        ) {
                            Text(
                                text = "⚙️ Procesos",
                                fontSize = 11.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Calendario de actividad / Semana
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
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
                        Text(
                            text = "Actividad Semanal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DocuNavy
                        )
                        Text(
                            text = "Septiembre 2026",
                            fontSize = 12.sp,
                            color = DocuTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val days = listOf("LU" to "14", "MA" to "15", "MI" to "16", "JU" to "17", "VI" to "18", "SA" to "19", "DO" to "20")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        days.forEachIndexed { index, (dayLabel, dateNum) ->
                            val isToday = index == 4 // Viernes
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isToday) DocuGold else Color.Transparent)
                                    .padding(horizontal = 6.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = dayLabel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isToday) DocuNavy else DocuTextSecondary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = dateNum,
                                    fontSize = 13.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isToday) DocuNavy else DocuTextPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Gráfico de barras: Documentos Revisados
                    Text(
                        text = "Documentos Revisados",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = DocuNavy
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val barHeights = listOf(0.4f, 0.65f, 0.5f, 0.9f, 0.75f, 0.3f, 0.2f)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        barHeights.forEachIndexed { idx, heightFrac ->
                            val isCurrent = idx == 4
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp)
                                    .fillMaxHeight(heightFrac)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(if (isCurrent) DocuGold else DocuGold.copy(alpha = 0.45f))
                            )
                        }
                    }
                }
            }
        }

        // Botón principal "Subir documento"
        item {
            Button(
                onClick = onOpenUpload,
                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .testTag("upload_document_button")
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = DocuNavy
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Subir documento (Clasificación IA)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DocuNavy
                )
            }
        }

        // Accesos directos a Duplicados, Papelera y Reportes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    title = "Duplicados",
                    subtitle = "$duplicateDocumentsCount copias",
                    icon = Icons.Default.FileCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(Screen.DUPLICATE_FILES)
                }

                QuickActionCard(
                    title = "Papelera",
                    subtitle = "$trashedDocumentsCount archivos",
                    icon = Icons.Default.Delete,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(Screen.TRASH)
                }

                QuickActionCard(
                    title = "Reportes",
                    subtitle = "Métricas",
                    icon = Icons.Default.BarChart,
                    modifier = Modifier.weight(1f)
                ) {
                    onNavigate(Screen.REPORTS)
                }
            }
        }

        // Documentos recientes
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Documentos Recientes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DocuNavy
                )
                Text(
                    text = "Ver todos",
                    fontSize = 13.sp,
                    color = DocuGoldDark,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(Screen.DOCUMENTS) }
                )
            }
        }

        items(activeDocuments.take(4).size) { idx ->
            val doc = activeDocuments[idx]
            DocumentCardItem(doc = doc, onClick = { onOpenDocument(doc.id) })
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color = DocuGold,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = DocuNavy
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = DocuTextSecondary,
                lineHeight = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DocuNavy,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DocuNavy
            )
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = DocuTextSecondary
            )
        }
    }
}

@Composable
fun DocumentCardItem(
    doc: DocumentEntity,
    onClick: () -> Unit
) {
    val isPdf = doc.mimeType.contains("pdf", ignoreCase = true)
    val isExcel = doc.mimeType.contains("excel", ignoreCase = true) || doc.name.endsWith(".xlsx") || doc.name.endsWith(".xls")

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
                    .size(42.dp)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${(doc.sizeBytes / 1024)} KB",
                        fontSize = 11.sp,
                        color = DocuTextMuted
                    )
                    Text(text = " • ", fontSize = 11.sp, color = DocuTextMuted)
                    Text(
                        text = doc.category,
                        fontSize = 11.sp,
                        color = DocuGoldDark,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (doc.isStarred) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Favorito",
                    tint = DocuGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
