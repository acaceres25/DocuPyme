package com.example.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FolderEntity
import com.example.ui.UploadUiState
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadDocumentScreen(
    uploadState: UploadUiState,
    folders: List<FolderEntity>,
    onBack: () -> Unit,
    onStartUploadWithPreset: (fileName: String, extractedText: String, mimeType: String, sizeBytes: Long) -> Unit,
    onProcessPickedFile: (fileName: String, extractedText: String, mimeType: String, sizeBytes: Long) -> Unit,
    onUpdateCategory: (String) -> Unit,
    onUpdateFolder: (String) -> Unit,
    onUpdateFileName: (String) -> Unit,
    onUpdateExtractedText: (String) -> Unit,
    onUpdateTags: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onReanalyze: () -> Unit,
    onConfirmSave: () -> Unit
) {
    val context = LocalContext.current
    var showCustomInput by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    var isTextExpanded by remember { mutableStateOf(false) }

    // System File Picker for PDF, DOCX, XLSX, TXT, etc.
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            var fileName = "Documento_Subido.pdf"
            var fileSize = 1048576L
            val mimeType = context.contentResolver.getType(uri) ?: "application/pdf"

            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                // Fallback default
            }

            // Extract sample text preview depending on file name or mime
            val extractedPreview = when {
                fileName.contains("factura", ignoreCase = true) ->
                    "Factura Electrónica de Venta. Emisor: Proveedor Local S.A.S. NIT: 901.442.112-9. Total: $450.000, IVA 19%: $85.500. CUFE: a1b2c3d4e5f6g7h8."
                fileName.contains("contrato", ignoreCase = true) ->
                    "Contrato comercial celebrado en 2026. Cláusulas de confidencialidad y plazos de entrega. Vigencia: 12 meses."
                fileName.contains("dian", ignoreCase = true) || fileName.contains("rut", ignoreCase = true) ->
                    "Formulario Oficial DIAN. Registro Único Tributario (RUT). Régimen Común del Impuesto sobre las Ventas."
                else ->
                    "Documento digital recibido para custodia segura en DocuPyme. Archivo: $fileName. Tamaño: ${fileSize / 1024} KB."
            }

            onProcessPickedFile(fileName, extractedPreview, mimeType, fileSize)
        }
    }

    // Photo/Camera scan picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = "Escaneo_Foto_${System.currentTimeMillis() % 10000}.jpg"
            val textPreview = "Escaneo óptico de documento físico: Recibo de caja menor, concepto compra de suministros de oficina. Fecha: 18 de Septiembre 2026. Total: $120.000."
            onProcessPickedFile(fileName, textPreview, "image/jpeg", 2150000L)
        }
    }

    val categories = listOf("Facturas", "Contratos", "Bancos", "DIAN", "Recursos Humanos", "Proveedores", "Otra")

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DocuNavy)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Subir y Clasificar Documento",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DocuNavy
                    )
                    Text(
                        text = "Extracción de texto, verificación hash y clasificación por IA",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )
                }
            }
        }

        // Stepper Progress Indicator (RF07, RF11)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StepItem(number = "1", title = "Selección", isDone = uploadState.fileName.isNotBlank(), isActive = uploadState.currentStep == 0)
                    StepDivider(isDone = uploadState.fileName.isNotBlank())
                    StepItem(number = "2", title = "OCR / Hash", isDone = uploadState.currentStep >= 2, isActive = uploadState.currentStep == 1)
                    StepDivider(isDone = uploadState.currentStep >= 2)
                    StepItem(number = "3", title = "IA Gemini", isDone = uploadState.classification != null, isActive = uploadState.currentStep == 2)
                    StepDivider(isDone = uploadState.classification != null)
                    StepItem(number = "4", title = "Guardar", isDone = uploadState.isSaved, isActive = uploadState.currentStep == 3)
                }
            }
        }

        // Primary Upload Box / Dropzone
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DocuGold),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(DocuGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = "Subir archivo",
                            tint = DocuNavy,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (uploadState.fileName.isBlank()) "Sube o escanea tu documento" else uploadState.fileName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DocuNavy,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Soporta PDF, Word, Excel, JPG y PNG con cifrado AES-256",
                        fontSize = 12.sp,
                        color = DocuTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { filePickerLauncher.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = DocuNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_file_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = DocuGold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Elegir archivo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DocuNavy),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("scan_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = DocuNavy
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Foto / Escaneo", fontSize = 12.sp, color = DocuNavy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Quick Preset Buttons for In-app testing
        item {
            Text(
                text = "Plantillas de prueba para demostración instantánea",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DocuNavy
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Prueba cómo la IA clasifica automáticamente según el contenido extraído:",
                fontSize = 12.sp,
                color = DocuTextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PresetButton(
                    title = "Factura de Venta FE-8492.pdf",
                    subtitle = "Detecta NIT, IVA 19%, CUFE y montos tributarios",
                    icon = Icons.AutoMirrored.Filled.ReceiptLong,
                    badge = "Caso Facturas"
                ) {
                    onStartUploadWithPreset(
                        "Factura_Electrónica_FE-8492.pdf",
                        "Factura Electrónica de Venta No. FE-8492. Emisor: Cemento y Aceros S.A.S. NIT: 900.555.123-4. Cliente: DocuPyme. Subtotal: $8.500.000, IVA 19%: $1.615.000, Total a pagar: $10.115.000. CUFE: 9c8b7a6d5e4f.",
                        "application/pdf",
                        1420000L
                    )
                }

                PresetButton(
                    title = "Contrato_Servicios_Logistica.pdf",
                    subtitle = "Detecta Cláusulas, Partes intervinientes y Plazos",
                    icon = Icons.Default.Description,
                    badge = "Caso Contratos"
                ) {
                    onStartUploadWithPreset(
                        "Contrato_Servicios_Logistica.pdf",
                        "Contrato de prestación de servicios de transporte y logística comercial celebrado entre Transportes del Norte S.A. y DocuPyme. Cláusula 1: Objeto. Cláusula 2: Tarifa por flete. Cláusula 3: Responsabilidad civil. Vigencia: 1 año.",
                        "application/pdf",
                        1680000L
                    )
                }

                PresetButton(
                    title = "RUT_DIAN_Actualizado_2026.pdf",
                    subtitle = "Detecta Formulario oficial DIAN y responsabilidades",
                    icon = Icons.Default.Gavel,
                    badge = "Caso DIAN"
                ) {
                    onStartUploadWithPreset(
                        "RUT_DIAN_Actualizado_2026.pdf",
                        "Formulario Oficial DIAN No. 001 - Registro Único Tributario (RUT). NIT: 900.842.119-3. Actividad económica principal: 4752 Comercio al por menor de artículos de ferretería. Régimen común.",
                        "application/pdf",
                        980000L
                    )
                }

                PresetButton(
                    title = "Escaneo_Foto_Recibo_Caja.jpg (OCR)",
                    subtitle = "Simula captura de cámara y extracción OCR de factura física",
                    icon = Icons.Default.DocumentScanner,
                    badge = "Escaneo OCR"
                ) {
                    onStartUploadWithPreset(
                        "Escaneo_Foto_Recibo_Caja.jpg",
                        "[OCR Reconocido]: RECIBO DE CAJA MENOR No. 0481. Fecha: 18/09/2026. Pagado a: Papelería Industrial S.A.S. NIT: 830.122.909-1. Por concepto de resmas de papel y tóner de impresión. Valor total: $245.000 COP.",
                        "image/jpeg",
                        2300000L
                    )
                }

                PresetButton(
                    title = "Borrador_Notas_Reunion.txt (Ambiguo)",
                    subtitle = "Activa el modo de 'Revisión Humana' por confianza < 80%",
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    badge = "Caso Revisión"
                ) {
                    onStartUploadWithPreset(
                        "Borrador_Notas_Reunion.txt",
                        "Notas informales de la charla con Don Carlos sobre posibles proveedores y cotizaciones sin firma ni sellos comerciales.",
                        "text/plain",
                        45000L
                    )
                }
            }
        }

        // Editable Custom Text Drawer / Accordion
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCustomInput = !showCustomInput },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EditNote, contentDescription = null, tint = DocuNavy)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escribir o pegar texto personalizado", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DocuNavy)
                        }
                        Icon(
                            imageVector = if (showCustomInput) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = DocuNavy
                        )
                    }

                    AnimatedVisibility(visible = showCustomInput) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = uploadState.fileName,
                                onValueChange = onUpdateFileName,
                                label = { Text("Nombre del archivo") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            OutlinedTextField(
                                value = uploadState.extractedText,
                                onValueChange = onUpdateExtractedText,
                                label = { Text("Contenido de texto / OCR") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                minLines = 3,
                                maxLines = 6
                            )

                            Button(
                                onClick = onReanalyze,
                                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DocuNavy)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analizar y clasificar con IA", color = DocuNavy, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Loading AI state
        if (uploadState.isClassifying) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = DocuNavy,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Analizando documento con IA...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DocuNavy
                            )
                            Text(
                                text = "Extrayendo señales, entidades fiscales y estructura",
                                fontSize = 12.sp,
                                color = DocuTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // AI Classification Result & Final Customization Card
        uploadState.classification?.let { result ->
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.needsHumanReview) Color(0xFFFFFBEB) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (result.needsHumanReview) DocuWarning else DocuGold
                    ),
                    elevation = CardDefaults.cardElevation(3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        // Header badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DocuNavy
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = DocuGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = result.modelUsed,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (result.confidence >= 80) DocuSuccess.copy(alpha = 0.15f) else DocuWarning.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${result.confidence}% certeza",
                                    color = if (result.confidence >= 80) DocuSuccess else Color(0xFFB45309),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = uploadState.fileName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DocuNavy
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Category prediction pill
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Clasificación sugerida: ", fontSize = 12.sp, color = DocuTextSecondary)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DocuGold.copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = result.category,
                                    color = DocuNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Reasoning explanation
                        Text(
                            text = "Motivo: ${result.reasoning}",
                            fontSize = 12.sp,
                            color = DocuTextSecondary,
                            lineHeight = 16.sp
                        )

                        if (result.needsHumanReview) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = DocuWarning.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = DocuWarning,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Requiere confirmación humana (Certeza menor a 80%)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DocuNavy
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFFE5E7EB))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Destination Folder Selection
                        Text(
                            text = "Carpeta de destino asignada:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = DocuNavy
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                val isSelected = uploadState.selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onUpdateCategory(cat) },
                                    label = { Text(cat, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DocuGold,
                                        selectedLabelColor = DocuNavy
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Custom Tags & Description
                        OutlinedTextField(
                            value = uploadState.customTags,
                            onValueChange = onUpdateTags,
                            label = { Text("Etiquetas personalizadas (ej. Contabilidad, Urgente)", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Confirmation CTA Button
                        Button(
                            onClick = onConfirmSave,
                            colors = ButtonDefaults.buttonColors(containerColor = DocuNavy),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_save_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DocuGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Confirmar y guardar en ${uploadState.selectedCategory}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(
    number: String,
    title: String,
    isDone: Boolean,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> DocuGold
                        isActive -> DocuNavy
                        else -> Color(0xFFE5E7EB)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = DocuNavy,
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = number,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else DocuTextSecondary
                )
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = if (isActive || isDone) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive || isDone) DocuNavy else DocuTextSecondary
        )
    }
}

@Composable
private fun StepDivider(isDone: Boolean) {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(2.dp)
            .background(if (isDone) DocuGold else Color(0xFFE5E7EB))
    )
}

@Composable
private fun PresetButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DocuNavy.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = DocuNavy, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DocuNavy)
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = DocuGold.copy(alpha = 0.2f)) {
                        Text(badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DocuNavy, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                    }
                }
                Text(subtitle, fontSize = 11.sp, color = DocuTextSecondary)
            }
            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = DocuTextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
