package com.example.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.CompanyEntity
import com.example.data.local.DocumentEntity
import com.example.ui.components.DocuPymeBottomNav
import com.example.ui.components.DocuPymeTopBar
import com.example.ui.screens.*
import com.example.ui.theme.DocuGold
import com.example.ui.theme.DocuNavy
import com.example.ui.theme.DocuTextSecondary

@Composable
fun DocuPymeApp(viewModel: DocuPymeViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val currentCompany by viewModel.currentCompany.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allCompanies by viewModel.allCompanies.collectAsStateWithLifecycle()
    val companyUsers by viewModel.companyUsers.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val activeDocuments by viewModel.activeDocuments.collectAsStateWithLifecycle()
    val trashedDocuments by viewModel.trashedDocuments.collectAsStateWithLifecycle()
    val duplicateDocuments by viewModel.duplicateDocuments.collectAsStateWithLifecycle()
    val auditEvents by viewModel.auditEvents.collectAsStateWithLifecycle()
    val backups by viewModel.backups.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    val currentFolderDocuments by viewModel.currentFolderDocuments.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentDocument by viewModel.currentDocument.collectAsStateWithLifecycle(initialValue = null)
    val currentDocumentVersions by viewModel.currentDocumentVersions.collectAsStateWithLifecycle(initialValue = emptyList())
    val currentDocumentPermissions by viewModel.currentDocumentPermissions.collectAsStateWithLifecycle(initialValue = emptyList())
    val uploadState by viewModel.uploadState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isChatbotResponding by viewModel.isChatbotResponding.collectAsStateWithLifecycle()

    val userFeedback by viewModel.userFeedback.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCompanyDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showAuditDialog by remember { mutableStateOf(false) }
    var documentToShare by remember { mutableStateOf<DocumentEntity?>(null) }

    val unreadNotificationsCount = notifications.count { !it.isRead }

    // User feedback effect
    LaunchedEffect(userFeedback) {
        userFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    // Modal para alternar empresas (Multi-tenant)
    if (showCompanyDialog) {
        AlertDialog(
            onDismissRequest = { showCompanyDialog = false },
            title = { Text("Seleccionar Empresa", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Cada empresa cuenta con base de datos, carpetas y documentos aislados:",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    allCompanies.forEach { comp ->
                        val isSel = comp.id == currentCompany?.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) DocuGold else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchCompany(comp.id)
                                    showCompanyDialog = false
                                }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(comp.name, fontWeight = FontWeight.Bold, color = DocuNavy, fontSize = 13.sp)
                                Text("NIT: ${comp.nit} • ${comp.status}", fontSize = 11.sp, color = DocuTextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCompanyDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal para alternar usuarios (Demostración de Roles y Permisos)
    if (showUserDialog) {
        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text("Cambiar Usuario Activo", fontWeight = FontWeight.Bold, color = DocuNavy) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Comprueba el comportamiento según el rol (Admin, Editor o Consulta):",
                        fontSize = 12.sp,
                        color = DocuTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    companyUsers.forEach { u ->
                        val isSel = u.id == currentUser?.id
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) DocuNavy else Color(0xFFF3F4F6),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.switchUser(u.id)
                                    showUserDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = u.name,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) DocuGold else DocuNavy,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = u.cargo,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.White.copy(alpha = 0.8f) else DocuTextSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSel) DocuGold else Color(0xFFE5E7EB)
                                ) {
                                    Text(
                                        text = u.role,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DocuNavy,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }

    // Modal de Auditoría (RF09)
    if (showAuditDialog) {
        AuditLogDialog(
            events = auditEvents,
            onDismiss = { showAuditDialog = false }
        )
    }

    Scaffold(
        topBar = {
            DocuPymeTopBar(
                currentScreen = currentScreen,
                company = currentCompany,
                user = currentUser,
                unreadNotificationsCount = unreadNotificationsCount,
                onBackClick = {
                    when (currentScreen) {
                        Screen.FOLDER_CONTENT, Screen.UPLOAD_DOCUMENT -> viewModel.navigateTo(Screen.DOCUMENTS)
                        Screen.DOCUMENT_DETAIL -> viewModel.navigateTo(Screen.FOLDER_CONTENT)
                        Screen.SHARE_DOCUMENT -> viewModel.navigateTo(Screen.DOCUMENT_DETAIL)
                        Screen.BACKUP_SECURITY, Screen.EDIT_PROFILE, Screen.REPORTS -> viewModel.navigateTo(Screen.USERS_PERMISSIONS)
                        else -> viewModel.navigateTo(Screen.DASHBOARD)
                    }
                },
                onNotificationsClick = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
                onChatbotClick = { viewModel.navigateTo(Screen.CHATBOT) },
                onSwitchCompanyClick = { showCompanyDialog = true },
                onSwitchUserClick = { showUserDialog = true },
                onAuditClick = { showAuditDialog = true }
            )
        },
        bottomBar = {
            DocuPymeBottomNav(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> DashboardScreen(
                    company = currentCompany,
                    user = currentUser,
                    activeDocuments = activeDocuments,
                    folders = folders,
                    duplicateDocumentsCount = duplicateDocuments.size,
                    trashedDocumentsCount = trashedDocuments.size,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onOpenDocument = { docId -> viewModel.openDocument(docId) },
                    onOpenUpload = {
                        viewModel.startUploadWithPreset(
                            fileName = "Factura_Electrónica_FE-8492.pdf",
                            extractedText = "Factura Electrónica de Venta No. FE-8492. Emisor: Cemento y Aceros S.A.S. NIT: 900.555.123-4. Cliente: DocuPyme. Subtotal: $8.500.000, IVA 19%: $1.615.000, Total a pagar: $10.115.000. CUFE: 9c8b7a6d5e4f.",
                            mimeType = "application/pdf",
                            sizeBytes = 1420000L
                        )
                    }
                )

                Screen.DOCUMENTS -> DocumentsScreen(
                    folders = folders,
                    documents = activeDocuments,
                    onOpenFolder = { folderId -> viewModel.openFolder(folderId) },
                    onCreateFolder = { name -> viewModel.createFolder(name) },
                    onOpenUpload = {
                        viewModel.startUploadWithPreset(
                            fileName = "Factura_Electrónica_FE-8492.pdf",
                            extractedText = "Factura Electrónica de Venta No. FE-8492. Emisor: Cemento y Aceros S.A.S. NIT: 900.555.123-4. Cliente: DocuPyme. Subtotal: $8.500.000, IVA 19%: $1.615.000, Total a pagar: $10.115.000. CUFE: 9c8b7a6d5e4f.",
                            mimeType = "application/pdf",
                            sizeBytes = 1420000L
                        )
                    }
                )

                Screen.FOLDER_CONTENT -> {
                    val folder = folders.find { it.id == viewModel.selectedFolderId.value }
                    FolderDetailScreen(
                        folder = folder,
                        documents = currentFolderDocuments,
                        onBack = { viewModel.navigateTo(Screen.DOCUMENTS) },
                        onOpenDocument = { docId -> viewModel.openDocument(docId) },
                        onOpenUpload = {
                            viewModel.startUploadWithPreset(
                                fileName = "Factura_Electrónica_FE-8492.pdf",
                                extractedText = "Factura Electrónica de Venta No. FE-8492. Emisor: Cemento y Aceros S.A.S. NIT: 900.555.123-4. Cliente: DocuPyme. Subtotal: $8.500.000, IVA 19%: $1.615.000, Total a pagar: $10.115.000. CUFE: 9c8b7a6d5e4f.",
                                mimeType = "application/pdf",
                                sizeBytes = 1420000L
                            )
                        },
                        onShare = { doc ->
                            documentToShare = doc
                            viewModel.navigateTo(Screen.SHARE_DOCUMENT)
                        },
                        onDownload = { doc -> viewModel.downloadDocument(doc) },
                        onTrash = { docId -> viewModel.moveToTrash(docId) }
                    )
                }

                Screen.DOCUMENT_DETAIL -> DocumentDetailScreen(
                    document = currentDocument,
                    versions = currentDocumentVersions,
                    permissions = currentDocumentPermissions,
                    onBack = { viewModel.navigateTo(Screen.FOLDER_CONTENT) },
                    onShare = {
                        documentToShare = currentDocument
                        viewModel.navigateTo(Screen.SHARE_DOCUMENT)
                    },
                    onDownload = { currentDocument?.let { viewModel.downloadDocument(it) } },
                    onUploadNewVersion = { notes ->
                        currentDocument?.let { viewModel.uploadNewVersion(it.id, notes) }
                    },
                    onMoveToTrash = {
                        currentDocument?.let { viewModel.moveToTrash(it.id) }
                    },
                    onToggleStar = {
                        // Favorite toggle
                    },
                    onOpenViewer = { docId -> viewModel.openDocumentViewer(docId) }
                )

                Screen.DOCUMENT_VIEWER -> DocumentViewerScreen(
                    document = currentDocument,
                    onBack = { viewModel.navigateTo(Screen.DOCUMENT_DETAIL) },
                    onShare = {
                        documentToShare = currentDocument
                        viewModel.navigateTo(Screen.SHARE_DOCUMENT)
                    },
                    onDownload = { currentDocument?.let { viewModel.downloadDocument(it) } },
                    onAskBotAboutDoc = { q ->
                        viewModel.sendChatMessage(q)
                        viewModel.navigateTo(Screen.CHATBOT)
                    },
                    onApplyApprovalStamp = { notes -> viewModel.applyApprovalStamp(notes) }
                )

                Screen.UPLOAD_DOCUMENT -> UploadDocumentScreen(
                    uploadState = uploadState,
                    folders = folders,
                    onBack = { viewModel.navigateTo(Screen.DOCUMENTS) },
                    onStartUploadWithPreset = { name, text, mime, size ->
                        viewModel.startUploadWithPreset(name, text, mime, size)
                    },
                    onProcessPickedFile = { name, text, mime, size ->
                        viewModel.processPickedFile(name, text, mime, size)
                    },
                    onUpdateCategory = { cat -> viewModel.updateUploadCategory(cat) },
                    onUpdateFolder = { fId -> viewModel.updateUploadFolder(fId) },
                    onUpdateFileName = { name -> viewModel.updateUploadFileName(name) },
                    onUpdateExtractedText = { text -> viewModel.updateUploadExtractedText(text) },
                    onUpdateTags = { tags -> viewModel.updateUploadTags(tags) },
                    onUpdateDescription = { desc -> viewModel.updateUploadDescription(desc) },
                    onReanalyze = { viewModel.reanalyzeUpload() },
                    onConfirmSave = { viewModel.confirmSaveDocument() }
                )

                Screen.CHATBOT -> ChatbotScreen(
                    company = currentCompany,
                    user = currentUser,
                    messages = chatMessages,
                    isResponding = isChatbotResponding,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onSendMessage = { q -> viewModel.sendChatMessage(q) },
                    onClearChat = { viewModel.clearChat() },
                    onNavigateAction = { action ->
                        when (action) {
                            "documents" -> viewModel.navigateTo(Screen.DOCUMENTS)
                            "duplicates" -> viewModel.navigateTo(Screen.DUPLICATE_FILES)
                            "backup" -> viewModel.navigateTo(Screen.BACKUP_SECURITY)
                            "reports" -> viewModel.navigateTo(Screen.REPORTS)
                            "notifications" -> viewModel.navigateTo(Screen.NOTIFICATIONS)
                            else -> viewModel.navigateTo(Screen.DASHBOARD)
                        }
                    }
                )

                Screen.SEARCH -> SearchScreen(
                    documents = activeDocuments,
                    onOpenDocument = { docId -> viewModel.openDocument(docId) },
                    onDownloadDocument = { doc -> viewModel.downloadDocument(doc) },
                    onShareDocument = { doc ->
                        documentToShare = doc
                        viewModel.navigateTo(Screen.SHARE_DOCUMENT)
                    }
                )

                Screen.SHARE_DOCUMENT -> ShareDocumentScreen(
                    document = documentToShare ?: currentDocument,
                    users = companyUsers.filter { it.id != currentUser?.id },
                    onBack = { viewModel.navigateTo(Screen.DOCUMENT_DETAIL) },
                    onShare = { targetUser, perm, exp, msg ->
                        val docId = documentToShare?.id ?: currentDocument?.id
                        if (docId != null) {
                            viewModel.shareDocument(docId, targetUser, perm, exp, msg)
                            viewModel.navigateTo(Screen.DOCUMENT_DETAIL)
                        }
                    }
                )

                Screen.DUPLICATE_FILES -> DuplicateFilesScreen(
                    duplicateDocuments = duplicateDocuments,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onOpenDocument = { docId -> viewModel.openDocument(docId) },
                    onDeleteCopy = { docId -> viewModel.deletePermanently(docId) }
                )

                Screen.TRASH -> TrashScreen(
                    trashedDocuments = trashedDocuments,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onRestore = { docId -> viewModel.restoreDocument(docId) },
                    onDeletePermanently = { docId -> viewModel.deletePermanently(docId) }
                )

                Screen.REPORTS -> ReportsScreen(
                    documents = activeDocuments,
                    folders = folders,
                    onBack = { viewModel.navigateTo(Screen.USERS_PERMISSIONS) },
                    onExportPdf = { viewModel.exportReportAsPdf() },
                    onExportCsv = { viewModel.exportReportAsCsv() }
                )

                Screen.USERS_PERMISSIONS -> UsersAndPermissionsScreen(
                    users = companyUsers,
                    currentUser = currentUser,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onInviteUser = { name, email, role, cargo ->
                        viewModel.inviteUser(name, email, role, cargo)
                    },
                    onSwitchUser = { uId -> viewModel.switchUser(uId) },
                    onNavigateToSettingsItem = { screen -> viewModel.navigateTo(screen) }
                )

                Screen.BACKUP_SECURITY -> BackupAndSecurityScreen(
                    latestBackup = backups.firstOrNull(),
                    onBack = { viewModel.navigateTo(Screen.USERS_PERMISSIONS) },
                    onRunBackupNow = { viewModel.triggerBackup() },
                    onRunTestRestore = { viewModel.triggerTestRestore() }
                )

                Screen.EDIT_PROFILE -> ProfileScreen(
                    user = currentUser,
                    company = currentCompany,
                    onBack = { viewModel.navigateTo(Screen.USERS_PERMISSIONS) },
                    onSaveProfile = { name, cargo -> viewModel.updateProfile(name, cargo) }
                )

                Screen.NOTIFICATIONS -> NotificationsScreen(
                    notifications = notifications,
                    onBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                    onMarkAllRead = { viewModel.markAllNotificationsRead() },
                    onMarkRead = { id -> viewModel.markNotificationRead(id) },
                    onTriggerSystemAlert = { title, msg -> viewModel.triggerSystemAlertNotification(title, msg) }
                )

                else -> DashboardScreen(
                    company = currentCompany,
                    user = currentUser,
                    activeDocuments = activeDocuments,
                    folders = folders,
                    duplicateDocumentsCount = duplicateDocuments.size,
                    trashedDocumentsCount = trashedDocuments.size,
                    onNavigate = { screen -> viewModel.navigateTo(screen) },
                    onOpenDocument = { docId -> viewModel.openDocument(docId) },
                    onOpenUpload = {
                        viewModel.startUploadWithPreset(
                            fileName = "Factura_Electrónica_FE-8492.pdf",
                            extractedText = "Factura Electrónica de Venta No. FE-8492. Emisor: Cemento y Aceros S.A.S. NIT: 900.555.123-4. Cliente: DocuPyme. Subtotal: $8.500.000, IVA 19%: $1.615.000, Total a pagar: $10.115.000. CUFE: 9c8b7a6d5e4f.",
                            mimeType = "application/pdf",
                            sizeBytes = 1420000L
                        )
                    }
                )
            }
        }
    }
}
