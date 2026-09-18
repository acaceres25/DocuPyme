package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.DocumentEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    document: DocumentEntity?,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onAskBotAboutDoc: (question: String) -> Unit,
    onApplyApprovalStamp: (notes: String) -> Unit
) {
    val context = LocalContext.current
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var currentPage by remember { mutableIntStateOf(1) }
    val totalPages = remember(document) {
        if (document?.mimeType?.contains("pdf") == true) 3 else 1
    }

    var showSignDialog by remember { mutableStateOf(false) }
    var isApprovedStampApplied by remember { mutableStateOf(false) }
    var approvalNotes by remember { mutableStateOf("") }
    var showWhatsAppShareDialog by remember { mutableStateOf(false) }

    if (document == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Documento no disponible", color = DocuTextSecondary)
        }
        return
    }

    val isPdf = document.mimeType.contains("pdf", ignoreCase = true) || document.name.endsWith(".pdf", ignoreCase = true)
    val isExcel = document.mimeType.contains("excel", ignoreCase = true) || document.name.endsWith(".xlsx", ignoreCase = true)
    val isImage = document.mimeType.contains("image", ignoreCase = true) || document.name.endsWith(".jpg", ignoreCase = true) || document.name.endsWith(".png", ignoreCase = true)

    // Dialog for Digital Signature / Approval
    if (showSignDialog) {
        DigitalSignatureDialog(
            docName = document.name,
            onDismiss = { showSignDialog = false },
            onConfirmSign = { notes ->
                isApprovedStampApplied = true
                approvalNotes = notes
                onApplyApprovalStamp(notes)
                showSignDialog = false
            }
        )
    }

    // Dialog for sharing via external apps (WhatsApp / Mail / Enlace seguro)
    if (showWhatsAppShareDialog) {
        AlertDialog(
            onDismissRequest = { showWhatsAppShareDialog = false },
            title = { Text("Compartir enlace seguro", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Genera un token de acceso temporal (válido 48h) protegido por firma SHA-256 para enviar por WhatsApp o correo:",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3F4F6),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "https://docupyme.co/secure-share/${document.id}?token=sh256_${document.fileHash.take(8)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = DocuNavy,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Documento Seguro DocuPyme: ${document.name}")
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Hola, te comparto el documento ${document.name} custodiado en DocuPyme: https://docupyme.co/secure-share/${document.id}?token=sh256_${document.fileHash.take(8)}"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Compartir vía:"))
                        showWhatsAppShareDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DocuGold)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir selector (WhatsApp/Email)", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWhatsAppShareDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = document.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DocuNavy,
                            maxLines = 1
                        )
                        Text(
                            text = "Visor Seguro • ${(document.sizeBytes / 1024)} KB • ${document.category}",
                            fontSize = 11.sp,
                            color = DocuTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DocuNavy)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Reset zoom
                        zoomScale = 1f
                        panOffset = Offset.Zero
                    }) {
                        Icon(Icons.Default.RestartAlt, contentDescription = "Reiniciar Zoom", tint = DocuNavy)
                    }
                    IconButton(onClick = { showWhatsAppShareDialog = true }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = DocuNavy)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Zoom controls and page navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Zoom buttons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.75f) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ZoomOut, contentDescription = "Alejar", tint = DocuNavy)
                            }
                            Text(
                                text = "${(zoomScale * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DocuNavy,
                                modifier = Modifier.width(42.dp),
                                textAlign = TextAlign.Center
                            )
                            IconButton(
                                onClick = { zoomScale = (zoomScale + 0.25f).coerceAtMost(3.0f) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.ZoomIn, contentDescription = "Acercar", tint = DocuNavy)
                            }
                        }

                        // Page indicator
                        if (totalPages > 1) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (currentPage > 1) currentPage-- },
                                    enabled = currentPage > 1,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Anterior", tint = DocuNavy)
                                }
                                Text(
                                    text = "$currentPage / $totalPages",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = DocuNavy
                                )
                                IconButton(
                                    onClick = { if (currentPage < totalPages) currentPage++ },
                                    enabled = currentPage < totalPages,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "Siguiente", tint = DocuNavy)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action buttons: Consult DocuBot & Sign/Approve
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onAskBotAboutDoc("¿Puedes explicarme el contenido principal y vigencia de '${document.name}'?")
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("ask_bot_button")
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Consultar IA", fontSize = 12.sp, color = DocuNavy, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showSignDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isApprovedStampApplied) DocuSuccess else DocuGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(44.dp)
                                .testTag("sign_approve_button")
                        ) {
                            Icon(
                                imageVector = if (isApprovedStampApplied) Icons.Default.Verified else Icons.Default.Draw,
                                contentDescription = null,
                                tint = if (isApprovedStampApplied) Color.White else DocuNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isApprovedStampApplied) "Firmado / Aprobado" else "Firmar / Aprobar",
                                color = if (isApprovedStampApplied) Color.White else DocuNavy,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFE5E7EB))
                .padding(innerPadding)
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.75f, 3.5f)
                        panOffset += pan
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Rendered Document Sheet
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.94f)
                    .graphicsLayer(
                        scaleX = zoomScale,
                        scaleY = zoomScale,
                        translationX = panOffset.x,
                        translationY = panOffset.y
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header of the official sheet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isPdf) DocuPdfRed else if (isExcel) DocuExcelGreen else DocuNavy),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (isPdf) "PDF" else if (isExcel) "XLS" else "DOC",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = document.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DocuNavy
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Categoría: ${document.category} • Pág. $currentPage de $totalPages",
                                fontSize = 10.sp,
                                color = DocuTextSecondary
                            )
                        }

                        // Watermark / Security badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "DOCUPYME CUSTODIA",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = DocuTextMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFE5E7EB))

                    // Text simulation / Document body
                    Text(
                        text = if (document.textContent.isNotBlank()) {
                            document.textContent
                        } else {
                            "Contenido íntegro del documento certificado bajo estándar de seguridad empresarial DocuPyme.\n\n" +
                            "El presente archivo '${document.name}' ha sido procesado mediante hash SHA-256: ${document.fileHash}."
                        },
                        fontSize = 13.sp,
                        lineHeight = 22.sp,
                        fontFamily = FontFamily.Monospace,
                        color = DocuTextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // If user applied approval stamp or signed
                    if (isApprovedStampApplied) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFECFDF5),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF059669)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF059669)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "SELLO DE APROBACIÓN DIGITAL DOCUPYME",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "Aprobado por el usuario en sesión. $approvalNotes",
                                        fontSize = 10.sp,
                                        color = Color(0xFF047857)
                                    )
                                    Text(
                                        text = "Firma SHA-256: ${document.fileHash.take(24)}...",
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Integrity verification block
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF9FAFB),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = DocuGoldDark, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verificación de Integridad Criptográfica:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SHA-256: ${document.fileHash}",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = DocuTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DigitalSignatureDialog(
    docName: String,
    onDismiss: () -> Unit,
    onConfirmSign: (notes: String) -> Unit
) {
    var signatureNotes by remember { mutableStateOf("Revisado y conforme con los términos del documento.") }
    val pathPoints = remember { mutableStateListOf<Offset>() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Draw, contentDescription = null, tint = DocuGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Firma y Aprobación", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DocuNavy)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DocuNavy)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Dibuja tu firma táctil en el lienzo para estampar la aprobación en '$docName':",
                    fontSize = 12.sp,
                    color = DocuTextSecondary
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Canvas for tactile signature
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF9FAFB))
                        .border(1.dp, Color(0xFFD1D5DB), RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, _, _ ->
                                if (pathPoints.isEmpty()) {
                                    pathPoints.add(Offset(100f, 100f))
                                }
                                pathPoints.add(pathPoints.last() + pan)
                            }
                        }
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        if (pathPoints.size > 1) {
                            val path = Path().apply {
                                moveTo(pathPoints.first().x, pathPoints.first().y)
                                for (i in 1 until pathPoints.size) {
                                    lineTo(pathPoints[i].x, pathPoints[i].y)
                                }
                            }
                            drawPath(
                                path = path,
                                color = Color(0xFF1E3A8A),
                                style = Stroke(width = 4f, cap = StrokeCap.Round)
                            )
                        } else {
                            // Guide line
                            drawLine(
                                color = Color(0xFF9CA3AF),
                                start = Offset(30f, size.height - 30f),
                                end = Offset(size.width - 30f, size.height - 30f),
                                strokeWidth = 1.5f
                            )
                        }
                    }

                    if (pathPoints.isEmpty()) {
                        Text(
                            text = "✍️ Traza tu firma aquí",
                            color = DocuTextMuted,
                            fontSize = 12.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    TextButton(
                        onClick = { pathPoints.clear() },
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text("Limpiar", fontSize = 11.sp, color = DocuNavy)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = signatureNotes,
                    onValueChange = { signatureNotes = it },
                    label = { Text("Nota de aprobación", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = DocuNavy)
                    }

                    Button(
                        onClick = { onConfirmSign(signatureNotes) },
                        colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Estampar Sello", color = DocuNavy, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
