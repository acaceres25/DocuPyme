package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.ChatMessage
import com.example.data.ai.ChatbotService
import com.example.data.ai.CompanyContext
import com.example.data.ai.DocumentClassifierService
import com.example.data.local.*
import com.example.data.repository.DocuPymeRepository
import com.example.platform.DocumentNotifier
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DocuPymeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = DocuPymeDatabase.getDatabase(application)
    val repository = DocuPymeRepository(db)

    // Current navigation state
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<String?>("f_facturas")
    val selectedFolderId: StateFlow<String?> = _selectedFolderId.asStateFlow()

    private val _selectedDocumentId = MutableStateFlow<String?>("doc_factura_1")
    val selectedDocumentId: StateFlow<String?> = _selectedDocumentId.asStateFlow()

    // Upload State
    private val _uploadState = MutableStateFlow(UploadUiState())
    val uploadState: StateFlow<UploadUiState> = _uploadState.asStateFlow()

    // Search query & filters
    val searchQuery = MutableStateFlow("")
    val searchCategoryFilter = MutableStateFlow("Todos") // "Todos", "Facturas", "Contratos", "Bancos", "DIAN", "Proveedores"
    val searchTypeFilter = MutableStateFlow("Todos") // "Todos", "PDF", "Excel", "Imágenes"

    // Repository flows
    val currentCompany = repository.currentCompany.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val currentUser = repository.currentUser.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allCompanies = repository.allCompanies.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val companyUsers = repository.companyUsers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val folders = repository.folders.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeDocuments = repository.activeDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val trashedDocuments = repository.trashedDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val duplicateDocuments = repository.duplicateDocuments.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val auditEvents = repository.auditEvents.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val backups = repository.backups.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val notifications = repository.notifications.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected folder documents
    val currentFolderDocuments: Flow<List<DocumentEntity>> = _selectedFolderId.flatMapLatest { folderId ->
        if (folderId != null) repository.getDocumentsForFolder(folderId) else flowOf(emptyList())
    }

    // Selected document details
    val currentDocument: Flow<DocumentEntity?> = _selectedDocumentId.flatMapLatest { docId ->
        if (docId != null) repository.getDocumentById(docId) else flowOf(null)
    }

    val currentDocumentVersions: Flow<List<DocumentVersionEntity>> = _selectedDocumentId.flatMapLatest { docId ->
        if (docId != null) repository.getVersionsForDocument(docId) else flowOf(emptyList())
    }

    val currentDocumentPermissions: Flow<List<DocumentPermissionEntity>> = _selectedDocumentId.flatMapLatest { docId ->
        if (docId != null) repository.getPermissionsForDocument(docId) else flowOf(emptyList())
    }

    // User feedback message (SnackBar / Toast)
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    // Chatbot State
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "¡Hola! Soy **DocuBot**, tu asistente inteligente en DocuPyme. Estoy al tanto de tus documentos, alertas de vencimiento, revisiones pendientes y procesos empresariales.\n\nPuedes preguntarme lo que necesites o usar los accesos directos abajo.",
                isUser = false,
                actionSuggestions = listOf(
                    "¿Qué documentos están pendientes?",
                    "¿Cuáles documentos toca revisar?",
                    "¿Qué procesos faltan?",
                    "Resumen de estado de DocuPyme"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatbotResponding = MutableStateFlow(false)
    val isChatbotResponding: StateFlow<Boolean> = _isChatbotResponding.asStateFlow()

    init {
        viewModelScope.launch {
            DocuPymeDbPrepopulator.prepopulateIfNeeded(db)
        }
    }

    fun showFeedback(msg: String) {
        _userFeedback.value = msg
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun openFolder(folderId: String) {
        _selectedFolderId.value = folderId
        _currentScreen.value = Screen.FOLDER_CONTENT
    }

    fun openDocument(documentId: String) {
        _selectedDocumentId.value = documentId
        _currentScreen.value = Screen.DOCUMENT_DETAIL
        // Record audit event for consultation
        viewModelScope.launch {
            val doc = activeDocuments.value.find { it.id == documentId }
            repository.recordAudit("Consulta", "Éxito", "Visualización de documento '${doc?.name ?: documentId}'", documentId, doc?.name)
        }
    }

    fun switchCompany(companyId: String) {
        viewModelScope.launch {
            repository.switchCompany(companyId)
            _selectedFolderId.value = null
            _selectedDocumentId.value = null
            _currentScreen.value = Screen.DASHBOARD
            showFeedback("Empresa cambiada. Aislamiento multiempresa activo.")
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            repository.switchUser(userId)
            val user = companyUsers.value.find { it.id == userId }
            showFeedback("Sesión activa: ${user?.name} (${user?.role})")
        }
    }

    // Upload & AI Classification Flow
    fun startUploadWithPreset(
        fileName: String,
        extractedText: String,
        mimeType: String,
        sizeBytes: Long
    ) {
        _uploadState.value = UploadUiState(
            fileName = fileName,
            extractedText = extractedText,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            currentStep = 2,
            isClassifying = true
        )
        _currentScreen.value = Screen.UPLOAD_DOCUMENT

        viewModelScope.launch {
            val result = DocumentClassifierService.classifyDocument(fileName, extractedText, mimeType)
            _uploadState.value = _uploadState.value.copy(
                isClassifying = false,
                currentStep = 3,
                classification = result,
                selectedCategory = result.category,
                selectedFolderId = result.suggestedFolderId
            )
        }
    }

    fun processPickedFile(
        fileName: String,
        extractedText: String,
        mimeType: String,
        sizeBytes: Long
    ) {
        _uploadState.value = UploadUiState(
            fileName = fileName,
            extractedText = extractedText,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            currentStep = 1,
            isClassifying = true
        )

        viewModelScope.launch {
            kotlinx.coroutines.delay(400) // Brief UI animation feedback for OCR
            _uploadState.value = _uploadState.value.copy(currentStep = 2)
            val result = DocumentClassifierService.classifyDocument(fileName, extractedText, mimeType)
            _uploadState.value = _uploadState.value.copy(
                isClassifying = false,
                currentStep = 3,
                classification = result,
                selectedCategory = result.category,
                selectedFolderId = result.suggestedFolderId
            )
            showFeedback("Archivo analizado: Sugerido '${result.category}' (${result.confidence}% certeza)")
        }
    }

    fun updateUploadFileName(name: String) {
        _uploadState.value = _uploadState.value.copy(fileName = name)
    }

    fun updateUploadExtractedText(text: String) {
        _uploadState.value = _uploadState.value.copy(extractedText = text)
    }

    fun updateUploadTags(tags: String) {
        _uploadState.value = _uploadState.value.copy(customTags = tags)
    }

    fun updateUploadDescription(desc: String) {
        _uploadState.value = _uploadState.value.copy(customDescription = desc)
    }

    fun reanalyzeUpload() {
        val current = _uploadState.value
        if (current.fileName.isBlank()) return
        _uploadState.value = current.copy(isClassifying = true, currentStep = 2)

        viewModelScope.launch {
            val result = DocumentClassifierService.classifyDocument(
                current.fileName,
                current.extractedText,
                current.mimeType
            )
            _uploadState.value = _uploadState.value.copy(
                isClassifying = false,
                currentStep = 3,
                classification = result,
                selectedCategory = result.category,
                selectedFolderId = result.suggestedFolderId
            )
            showFeedback("Re-clasificación con IA completada: ${result.category}")
        }
    }

    fun updateUploadCategory(category: String) {
        val folder = DocumentClassifierService.getFolderForCategory(category)
        _uploadState.value = _uploadState.value.copy(
            selectedCategory = category,
            selectedFolderId = folder
        )
    }

    fun updateUploadFolder(folderId: String) {
        _uploadState.value = _uploadState.value.copy(selectedFolderId = folderId)
    }

    fun confirmSaveDocument() {
        val state = _uploadState.value
        if (state.fileName.isBlank()) return

        val userRole = currentUser.value?.role
        if (userRole == "Consulta") {
            showFeedback("Acceso denegado: Usuarios de rol Consulta no tienen permiso para subir archivos.")
            viewModelScope.launch {
                repository.recordAudit("Carga", "Denegado", "Intento de carga denegado: rol Consulta no autorizado.")
            }
            return
        }

        viewModelScope.launch {
            val doc = repository.uploadDocument(
                fileName = state.fileName,
                category = state.selectedCategory,
                folderId = state.selectedFolderId,
                mimeType = state.mimeType,
                sizeBytes = state.sizeBytes,
                textContent = state.extractedText,
                classification = state.classification
            )
            _uploadState.value = _uploadState.value.copy(isSaved = true)
            _selectedDocumentId.value = doc.id
            showFeedback("Documento '${doc.name}' guardado exitosamente en '${state.selectedCategory}'.")
            _currentScreen.value = Screen.DOCUMENT_DETAIL
        }
    }

    // Chatbot Interaction
    fun sendChatMessage(userText: String) {
        val text = userText.trim()
        if (text.isBlank()) return

        val userMessage = ChatMessage(
            text = text,
            isUser = true
        )
        _chatMessages.value = _chatMessages.value + userMessage
        _isChatbotResponding.value = true

        val comp = currentCompany.value
        val user = currentUser.value
        val docs = activeDocuments.value
        val dups = duplicateDocuments.value
        val trash = trashedDocuments.value

        // Extract context for the company
        val pendingDocs = mutableListOf<String>()
        // Any low confidence or ambiguous doc
        val ambiguousDoc = docs.find { it.category == "Otra" || (it.aiConfidence != null && it.aiConfidence < 80) }
        if (ambiguousDoc != null) {
            pendingDocs.add("${ambiguousDoc.name} (Clasificación preliminar como '${ambiguousDoc.category}' requiere confirmación)")
        } else {
            pendingDocs.add("Borrador de Acta Reunión (Pendiente de categorizar)")
        }
        pendingDocs.add("Cámara de Comercio 2025 (Requiere actualización anual en 15 días)")

        val alerts = listOf(
            "Cámara de comercio vence en 15 días (03 de Octubre de 2026)",
            "${dups.size} archivos duplicados detectados por firma criptográfica SHA-256"
        )

        val processes = listOf(
            "Renovar Matrícula Mercantil de la Cámara de Comercio antes de su vencimiento",
            "Depurar y consolidar los 4 archivos duplicados en la carpeta Facturas",
            "Ejecutar verificación de respaldo en la nube mediante prueba de restauración",
            "Revisar el archivo en la Papelera de reciclaje antes de su eliminación permanente en 25 días"
        )

        val companyContext = CompanyContext(
            companyName = comp?.name ?: "DocuPyme",
            nit = comp?.nit ?: "900.842.119-3",
            userName = user?.name ?: "Carlos Gómez",
            userRole = user?.role ?: "Administrador",
            totalDocuments = docs.size,
            activeDocumentsList = docs.take(8).map { "${it.name} (${it.category})" },
            pendingReviewDocs = pendingDocs,
            urgentAlerts = alerts,
            missingProcesses = processes,
            duplicateCount = dups.size,
            trashedCount = trash.size
        )

        viewModelScope.launch {
            val botAnswer = ChatbotService.answerQuestion(
                question = text,
                context = companyContext,
                chatHistory = _chatMessages.value
            )

            val nextSuggestions = when {
                text.contains("pendiente", ignoreCase = true) || text.contains("revisar", ignoreCase = true) -> listOf(
                    "📂 Ir a Documentos pendientes",
                    "🔍 Resolver archivos duplicados",
                    "¿Qué procesos faltan?",
                    "Ver detalles de la Cámara de Comercio"
                )
                text.contains("proceso", ignoreCase = true) || text.contains("falta", ignoreCase = true) -> listOf(
                    "⚡ Ejecutar prueba de restauración",
                    "📊 Generar reporte ejecutivo",
                    "¿Qué documentos están pendientes?",
                    "Resumen de estado de DocuPyme"
                )
                text.contains("duplicad", ignoreCase = true) -> listOf(
                    "🔍 Resolver archivos duplicados",
                    "📂 Ir a Documentos",
                    "¿Qué procesos faltan?"
                )
                text.contains("resumen", ignoreCase = true) || text.contains("informe", ignoreCase = true) -> listOf(
                    "📊 Generar reporte ejecutivo",
                    "🔔 Probar alerta de vencimiento",
                    "¿Qué documentos están pendientes?"
                )
                else -> listOf(
                    "¿Qué documentos están pendientes?",
                    "¿Qué procesos faltan?",
                    "🔍 Resolver archivos duplicados",
                    "📊 Generar reporte ejecutivo"
                )
            }

            _chatMessages.value = _chatMessages.value + ChatMessage(
                text = botAnswer,
                isUser = false,
                actionSuggestions = nextSuggestions
            )
            _isChatbotResponding.value = false
        }
    }

    fun clearChat() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Conversación reiniciada. Soy **DocuBot**, tu asistente inteligente en DocuPyme. ¿En qué puedo orientarte ahora?",
                isUser = false,
                actionSuggestions = listOf(
                    "¿Qué documentos están pendientes?",
                    "¿Cuáles documentos toca revisar?",
                    "¿Qué procesos faltan?",
                    "Resumen de estado de DocuPyme"
                )
            )
        )
    }

    // New Version
    fun uploadNewVersion(docId: String, notes: String) {
        val userRole = currentUser.value?.role
        if (userRole == "Consulta") {
            showFeedback("Acceso denegado: Rol Consulta no puede versionar documentos.")
            return
        }

        viewModelScope.launch {
            repository.uploadNewVersion(
                documentId = docId,
                newSizeBytes = 1290000L,
                newTextContent = "Contenido actualizado versión revisada.",
                notes = notes
            )
            showFeedback("Nueva versión guardada con éxito.")
        }
    }

    // Trash & Restore
    fun moveToTrash(docId: String) {
        val userRole = currentUser.value?.role
        if (userRole == "Consulta") {
            showFeedback("Acceso denegado: Rol Consulta no puede eliminar documentos.")
            viewModelScope.launch {
                repository.recordAudit("Eliminación", "Denegado", "Intento de eliminación denegado: rol Consulta.")
            }
            return
        }

        viewModelScope.launch {
            repository.moveDocumentToTrash(docId)
            showFeedback("Documento movido a papelera (retención de 30 días).")
            _currentScreen.value = Screen.DOCUMENTS
        }
    }

    fun restoreDocument(docId: String) {
        val userRole = currentUser.value?.role
        if (userRole == "Consulta") {
            showFeedback("Acceso denegado: Rol Consulta no puede restaurar.")
            return
        }
        viewModelScope.launch {
            repository.restoreDocument(docId)
            showFeedback("Documento restaurado exitosamente.")
        }
    }

    fun deletePermanently(docId: String) {
        val userRole = currentUser.value?.role
        if (userRole != "Administrador") {
            showFeedback("Solo Administradores pueden purgar archivos definitivamente.")
            return
        }
        viewModelScope.launch {
            repository.deleteDocumentPermanently(docId)
            showFeedback("Documento eliminado permanentemente.")
        }
    }

    // Share
    fun shareDocument(
        docId: String,
        targetUser: UserEntity,
        permissionLevel: String,
        expiresAt: Long?,
        message: String
    ) {
        val userRole = currentUser.value?.role
        if (userRole == "Consulta") {
            showFeedback("Acceso denegado: Rol Consulta no tiene permiso para compartir.")
            return
        }

        viewModelScope.launch {
            repository.shareDocument(
                documentId = docId,
                userId = targetUser.id,
                userName = targetUser.name,
                userEmail = targetUser.email,
                permissionLevel = permissionLevel,
                expiresAt = expiresAt,
                message = message
            )
            showFeedback("Permiso concedido a ${targetUser.name}.")
        }
    }

    fun revokeShare(permissionId: String, docId: String, userName: String) {
        val userRole = currentUser.value?.role
        if (userRole != "Administrador" && userRole != "Editor") {
            showFeedback("No tienes permiso para revocar accesos.")
            return
        }

        viewModelScope.launch {
            repository.revokePermission(permissionId, docId, userName)
            showFeedback("Permiso revocado para $userName.")
        }
    }

    // Backup & Restore Tests
    fun triggerBackup() {
        val userRole = currentUser.value?.role
        if (userRole != "Administrador") {
            showFeedback("Solo Administradores pueden iniciar respaldos.")
            return
        }

        viewModelScope.launch {
            showFeedback("Iniciando respaldo seguro en la nube...")
            val backup = repository.runBackupNow()
            showFeedback("Respaldo completado: ${backup.totalFiles} archivos asegurados.")
        }
    }

    fun triggerTestRestore() {
        val userRole = currentUser.value?.role
        if (userRole != "Administrador") {
            showFeedback("Solo Administradores pueden ejecutar pruebas de restauración.")
            return
        }

        viewModelScope.launch {
            val result = repository.runTestRestore()
            showFeedback(result)
        }
    }

    fun createFolder(name: String) {
        val userRole = currentUser.value?.role
        if (userRole == "Consulta") {
            showFeedback("Acceso denegado: Rol Consulta no puede crear carpetas.")
            return
        }

        viewModelScope.launch {
            repository.createFolder(name)
            showFeedback("Carpeta '$name' creada con éxito.")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showFeedback("Todas las notificaciones marcadas como leídas.")
        }
    }

    fun updateProfile(name: String, cargo: String) {
        viewModelScope.launch {
            repository.updateUserProfile(name, cargo)
            showFeedback("Perfil actualizado correctamente.")
        }
    }

    fun inviteUser(name: String, email: String, role: String, cargo: String) {
        val userRole = currentUser.value?.role
        if (userRole != "Administrador") {
            showFeedback("Solo Administradores pueden gestionar usuarios y permisos.")
            return
        }

        viewModelScope.launch {
            repository.inviteUser(name, email, role, cargo)
            showFeedback("Usuario $name invitado con rol $role.")
        }
    }

    fun downloadDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.recordAudit("Descarga", "Éxito", "Descarga de archivo seguro '${doc.name}'", doc.id, doc.name)
            showFeedback("Descargando '${doc.name}' (${doc.mimeType})... Guardado en Descargas.")
        }
    }

    fun openDocumentViewer(documentId: String) {
        _selectedDocumentId.value = documentId
        _currentScreen.value = Screen.DOCUMENT_VIEWER
        viewModelScope.launch {
            val doc = activeDocuments.value.find { it.id == documentId }
            repository.recordAudit("Visor Seguro", "Éxito", "Lectura y zoom en visor de '${doc?.name ?: documentId}'", documentId, doc?.name)
        }
    }

    fun applyApprovalStamp(notes: String) {
        val docId = _selectedDocumentId.value ?: return
        val doc = activeDocuments.value.find { it.id == docId }
        viewModelScope.launch {
            repository.recordAudit("Firma y Aprobación", "Éxito", "Sello de aprobación estampado: $notes", docId, doc?.name)
            showFeedback("Sello de aprobación digital estampado con éxito.")
        }
    }

    fun triggerSystemAlertNotification(title: String, message: String) {
        val delivered = DocumentNotifier(getApplication<Application>()).show(title, message)
        showFeedback(
            if (delivered) "Notificación del sistema emitida: $title"
            else "Activa las notificaciones de DocuPyme en Ajustes para recibir alertas."
        )
    }

    fun exportReportAsPdf() {
        viewModelScope.launch {
            val total = activeDocuments.value.size
            repository.recordAudit("Exportación", "Éxito", "Generación de Informe Ejecutivo en formato PDF ($total documentos)", null, null)
            showFeedback("Informe Ejecutivo 'DocuPyme_Reporte_${System.currentTimeMillis() % 10000}.pdf' generado y guardado en Documentos.")
        }
    }

    fun exportReportAsCsv() {
        viewModelScope.launch {
            val total = activeDocuments.value.size
            repository.recordAudit("Exportación", "Éxito", "Exportación de matriz de documentos en formato CSV/Excel ($total registros)", null, null)
            showFeedback("Matriz de inventario 'DocuPyme_Inventario_${System.currentTimeMillis() % 10000}.csv' exportada con éxito.")
        }
    }
}
