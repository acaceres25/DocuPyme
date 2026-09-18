package com.example.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

object DocuPymeDbPrepopulator {

    suspend fun prepopulateIfNeeded(db: DocuPymeDatabase) {
        withContext(Dispatchers.IO) {
            val companyDao = db.companyDao()
            val existing = companyDao.getCompanyById("empresa_ferreteria")
            if (existing != null) {
                if (existing.name == "Ferretería Central") {
                    companyDao.insertCompany(existing.copy(name = "DocuPyme"))
                }
                return@withContext
            }

            val now = System.currentTimeMillis()
            val oneDay = 86400000L

            // 1. Companies (DocuPyme and another company for multi-tenant isolation verification)
            val comp1 = CompanyEntity(
                id = "empresa_ferreteria",
                name = "DocuPyme",
                nit = "900.845.123-1",
                createdAt = now - (90 * oneDay)
            )
            val comp2 = CompanyEntity(
                id = "empresa_andina",
                name = "Distribuidora Andina S.A.S.",
                nit = "901.234.567-8",
                createdAt = now - (60 * oneDay)
            )
            companyDao.insertCompany(comp1)
            companyDao.insertCompany(comp2)

            // 2. Users for DocuPyme
            val userDao = db.userDao()
            val userLaura = UserEntity(
                id = "user_laura",
                companyId = "empresa_ferreteria",
                name = "Laura Gómez",
                email = "laura@docupyme.com",
                passwordHash = "demo123",
                role = "Administrador",
                cargo = "Administradora General"
            )
            val userCarlos = UserEntity(
                id = "user_carlos",
                companyId = "empresa_ferreteria",
                name = "Carlos Ruiz",
                email = "carlos@docupyme.com",
                passwordHash = "demo123",
                role = "Editor",
                cargo = "Contador Auxiliar"
            )
            val userMaria = UserEntity(
                id = "user_maria",
                companyId = "empresa_ferreteria",
                name = "María Méndez",
                email = "maria@docupyme.com",
                passwordHash = "demo123",
                role = "Editor",
                cargo = "Coordinadora de Compras"
            )
            val userJorge = UserEntity(
                id = "user_jorge",
                companyId = "empresa_ferreteria",
                name = "Jorge Pérez",
                email = "jorge@docupyme.com",
                passwordHash = "demo123",
                role = "Consulta",
                cargo = "Auditor Externo"
            )

            // User for Empresa Andina (isolated)
            val userSantiago = UserEntity(
                id = "user_santiago",
                companyId = "empresa_andina",
                name = "Santiago Morales",
                email = "santiago@distribuidoraandina.com",
                passwordHash = "demo123",
                role = "Administrador",
                cargo = "Gerente de Operaciones"
            )

            userDao.insertUsers(listOf(userLaura, userCarlos, userMaria, userJorge, userSantiago))

            // 3. Folders
            val folderDao = db.folderDao()
            val folderFacturas = FolderEntity("f_facturas", "empresa_ferreteria", "Facturas", null, "user_laura", "#FFC400", "receipt")
            val folderContratos = FolderEntity("f_contratos", "empresa_ferreteria", "Contratos", null, "user_laura", "#3B82F6", "description")
            val folderBancos = FolderEntity("f_bancos", "empresa_ferreteria", "Bancos", null, "user_laura", "#10B981", "account_balance")
            val folderDian = FolderEntity("f_dian", "empresa_ferreteria", "DIAN", null, "user_laura", "#EF4444", "gavel")
            val folderRrhh = FolderEntity("f_rrhh", "empresa_ferreteria", "Recursos Humanos", null, "user_laura", "#8B5CF6", "groups")
            val folderProveedores = FolderEntity("f_proveedores", "empresa_ferreteria", "Proveedores", null, "user_laura", "#F59E0B", "local_shipping")

            // Folders for Empresa Andina
            val folderAndina1 = FolderEntity("f_andina_1", "empresa_andina", "Ventas Nacionales", null, "user_santiago", "#FFC400", "folder")

            folderDao.insertFolders(listOf(folderFacturas, folderContratos, folderBancos, folderDian, folderRrhh, folderProveedores, folderAndina1))

            // 4. Documents
            val docDao = db.documentDao()

            val hashFactura1 = computeHash("FACTURA_ELECTRONICA_2024_001_FERRETERIA_CENTRAL_CONTENIDO_BYTES")
            val hashContrato = computeHash("CONTRATO_SUMINISTRO_ACEROS_COLOMBIA_VIGENCIA_2024")
            val hashDian = computeHash("DIAN_DECLARACION_RENTA_PJ_2023_PRESENTADA_2024")
            val hashExcel = computeHash("ESTADOS_FINANCIEROS_BALANCE_RESULTADOS_Q1_2024")
            val hashCamara = computeHash("CERTIFICADO_CAMARA_DE_COMERCIO_BOGOTA_2024")
            val hashLista = computeHash("LISTA_PRECIOS_PROVEEDORES_MATERIALES_PESADOS_2024")

            val docFactura1 = DocumentEntity(
                id = "doc_factura_1",
                companyId = "empresa_ferreteria",
                folderId = "f_facturas",
                name = "Factura_2024_001.pdf",
                category = "Facturas",
                mimeType = "application/pdf",
                sizeBytes = 1258291, // 1.2 MB
                fileHash = hashFactura1,
                status = "activo",
                ownerId = "user_laura",
                createdAt = now - (6 * oneDay),
                updatedAt = now - (1 * oneDay),
                textContent = "Factura Electrónica de Venta No. FE-2024-001. Emisor: Aceros de Colombia S.A.S. NIT: 890.123.456-7. Cliente: DocuPyme. Subtotal: $14.500.000, IVA 19%: $2.755.000, Total a Pagar: $17.255.000. CUFE: a1b2c3d4e5f6. Forma de pago: Transferencia bancaria 30 días.",
                aiSuggestion = "Facturas",
                aiConfidence = 98,
                aiReasoning = "Detectado formato de Factura Electrónica con NIT emisor, desglose de IVA (19%) y total fiscal.",
                isStarred = true
            )

            // Duplicate document with same fileHash for duplicate testing (RF12)
            val docFacturaDuplicate = DocumentEntity(
                id = "doc_factura_dup",
                companyId = "empresa_ferreteria",
                folderId = "f_facturas",
                name = "Factura_2024_001 (copia).pdf",
                category = "Facturas",
                mimeType = "application/pdf",
                sizeBytes = 1258291,
                fileHash = hashFactura1,
                status = "activo",
                ownerId = "user_carlos",
                createdAt = now - (2 * oneDay),
                updatedAt = now - (2 * oneDay),
                textContent = docFactura1.textContent,
                aiSuggestion = "Facturas",
                aiConfidence = 98,
                aiReasoning = "Contenido idéntico a Factura_2024_001.pdf",
                isStarred = false
            )

            val docContrato = DocumentEntity(
                id = "doc_contrato_1",
                companyId = "empresa_ferreteria",
                folderId = "f_contratos",
                name = "Contrato_proveedor_acero.pdf",
                category = "Contratos",
                mimeType = "application/pdf",
                sizeBytes = 1468006, // 1.4 MB
                fileHash = hashContrato,
                status = "activo",
                ownerId = "user_laura",
                createdAt = now - (15 * oneDay),
                updatedAt = now - (10 * oneDay),
                textContent = "Contrato de suministro comercial celebrado entre Aceros de Colombia S.A.S. y DocuPyme. Cláusula 1: Objeto y especificaciones técnicas de perfiles estructurales. Cláusula 2: Precios y forma de pago. Cláusula 3: Vigencia anual renovable. Cláusula 4: Confidencialidad y arbitraje Cámara de Comercio.",
                aiSuggestion = "Contratos",
                aiConfidence = 95,
                aiReasoning = "Detectada estructura contractual con cláusulas legales, partes intervinientes y firmas notariales."
            )

            val docDian = DocumentEntity(
                id = "doc_dian_1",
                companyId = "empresa_ferreteria",
                folderId = "f_dian",
                name = "Declaración Renta 2024.pdf",
                category = "DIAN",
                mimeType = "application/pdf",
                sizeBytes = 912000,
                fileHash = hashDian,
                status = "activo",
                ownerId = "user_carlos",
                createdAt = now - (20 * oneDay),
                updatedAt = now - (20 * oneDay),
                textContent = "Dirección de Impuestos y Aduanas Nacionales - DIAN. Formulario 110. Declaración de Renta y Complementarios Personas Jurídicas. Fracción Año 2023. Total patrimonio líquido y renta gravable.",
                aiSuggestion = "DIAN",
                aiConfidence = 99,
                aiReasoning = "Encabezado oficial DIAN Formulario 110 con renglones de liquidación tributaria."
            )

            val docFinanciero = DocumentEntity(
                id = "doc_financiero_1",
                companyId = "empresa_ferreteria",
                folderId = "f_bancos",
                name = "Estados Financieros 2024.xlsx",
                category = "Bancos",
                mimeType = "application/vnd.ms-excel",
                sizeBytes = 532480,
                fileHash = hashExcel,
                status = "activo",
                ownerId = "user_carlos",
                createdAt = now - (8 * oneDay),
                updatedAt = now - (5 * oneDay),
                textContent = "Hoja 1: Balance General comparativo 2023-2024. Hoja 2: Estado de Pérdidas y Ganancias (PyG). Hoja 3: Flujo de caja proyectado para entidad bancaria Banco de Bogotá.",
                aiSuggestion = "Bancos",
                aiConfidence = 91,
                aiReasoning = "Planillas de balance financiero, conciliación contable y proyecciones bancarias."
            )

            val docCamara = DocumentEntity(
                id = "doc_camara_1",
                companyId = "empresa_ferreteria",
                folderId = "f_dian",
                name = "Cámara de Comercio 2024.pdf",
                category = "DIAN",
                mimeType = "application/pdf",
                sizeBytes = 655360,
                fileHash = hashCamara,
                status = "activo",
                ownerId = "user_laura",
                createdAt = now - (25 * oneDay),
                updatedAt = now - (25 * oneDay),
                textContent = "Cámara de Comercio de Bogotá. Certificado de Existencia y Representación Legal. Razón Social: DocuPyme. Matrícula Mercantil 0294821 renovada para el periodo fiscal vigente.",
                aiSuggestion = "DIAN",
                aiConfidence = 94,
                aiReasoning = "Certificado de Cámara de Comercio con matrícula mercantil y facultades del representante legal."
            )

            val docLista = DocumentEntity(
                id = "doc_lista_1",
                companyId = "empresa_ferreteria",
                folderId = "f_proveedores",
                name = "Lista_productos.xlsx",
                category = "Proveedores",
                mimeType = "application/vnd.ms-excel",
                sizeBytes = 327680,
                fileHash = hashLista,
                status = "activo",
                ownerId = "user_maria",
                createdAt = now - (18 * oneDay),
                updatedAt = now - (18 * oneDay),
                textContent = "Catálogo general y lista de precios de proveedores: clavos, tornillos, tubería PVC, pintura epóxica y herramientas manuales con descuentos por volumen.",
                aiSuggestion = "Proveedores",
                aiConfidence = 89,
                aiReasoning = "Planilla de inventario, referencias SKU y cotizaciones de proveedores industriales."
            )

            val docListaDuplicate = DocumentEntity(
                id = "doc_lista_dup",
                companyId = "empresa_ferreteria",
                folderId = "f_proveedores",
                name = "Lista_productos(1).xlsx",
                category = "Proveedores",
                mimeType = "application/vnd.ms-excel",
                sizeBytes = 327680,
                fileHash = hashLista,
                status = "activo",
                ownerId = "user_carlos",
                createdAt = now - (3 * oneDay),
                updatedAt = now - (3 * oneDay),
                textContent = docLista.textContent,
                aiSuggestion = "Proveedores",
                aiConfidence = 89,
                aiReasoning = "Copia exacta de lista de productos por hash",
                isStarred = false
            )

            // Trashed documents (Papelera)
            val docTrash1 = DocumentEntity(
                id = "doc_trash_1",
                companyId = "empresa_ferreteria",
                folderId = "f_facturas",
                name = "Finiquito_2023.pdf",
                category = "Facturas",
                mimeType = "application/pdf",
                sizeBytes = 1258291,
                fileHash = computeHash("FINIQUITO_ANTERIOR_2023"),
                status = "papelera",
                trashedAt = now - (5 * oneDay),
                ownerId = "user_laura",
                createdAt = now - (40 * oneDay),
                updatedAt = now - (5 * oneDay),
                textContent = "Documento liquidado del ejercicio fiscal anterior enviado a papelera."
            )

            val docTrash2 = DocumentEntity(
                id = "doc_trash_2",
                companyId = "empresa_ferreteria",
                folderId = "f_bancos",
                name = "Lista_clientes.xlsx",
                category = "Bancos",
                mimeType = "application/vnd.ms-excel",
                sizeBytes = 225280,
                fileHash = computeHash("CLIENTES_HISTORICO_2023"),
                status = "papelera",
                trashedAt = now - (9 * oneDay),
                ownerId = "user_carlos",
                createdAt = now - (50 * oneDay),
                updatedAt = now - (9 * oneDay),
                textContent = "Lista obsoleta de clientes 2023."
            )

            // Document strictly belonging to empresa_andina (to test that DocuPyme CANNOT see it)
            val docAndina = DocumentEntity(
                id = "doc_andina_confidencial",
                companyId = "empresa_andina",
                folderId = "f_andina_1",
                name = "Ventas_Confidenciales_Andina.pdf",
                category = "Facturas",
                mimeType = "application/pdf",
                sizeBytes = 2097152,
                fileHash = computeHash("DOCUMENTO_PRIVADO_ANDINA_NO_DOCUPYME"),
                status = "activo",
                ownerId = "user_santiago",
                createdAt = now - (10 * oneDay),
                updatedAt = now - (10 * oneDay),
                textContent = "DOCUMENTO CONFIDENCIAL EXCLUSIVO DE DISTRIBUIDORA ANDINA S.A.S."
            )

            docDao.insertDocuments(listOf(
                docFactura1, docFacturaDuplicate, docContrato, docDian, docFinanciero, docCamara,
                docLista, docListaDuplicate, docTrash1, docTrash2, docAndina
            ))

            // 5. Document Versions for Factura_2024_001.pdf
            val versionDao = db.documentVersionDao()
            val v3 = DocumentVersionEntity(
                id = "v_factura_3",
                documentId = "doc_factura_1",
                versionNumber = 3,
                sizeBytes = 1258291,
                fileHash = hashFactura1,
                uploadedById = "user_laura",
                uploadedByName = "Laura Gómez",
                uploadedAt = now - (1 * oneDay),
                notes = "Ajuste de retención en la fuente y sello fiscal electrónico"
            )
            val v2 = DocumentVersionEntity(
                id = "v_factura_2",
                documentId = "doc_factura_1",
                versionNumber = 2,
                sizeBytes = 1240000,
                fileHash = computeHash("VERSION_2_FACTURA"),
                uploadedById = "user_carlos",
                uploadedByName = "Carlos Ruiz",
                uploadedAt = now - (4 * oneDay),
                notes = "Corrección en dirección de entrega de materiales"
            )
            val v1 = DocumentVersionEntity(
                id = "v_factura_1",
                documentId = "doc_factura_1",
                versionNumber = 1,
                sizeBytes = 1220000,
                fileHash = computeHash("VERSION_1_FACTURA"),
                uploadedById = "user_laura",
                uploadedByName = "Laura Gómez",
                uploadedAt = now - (6 * oneDay),
                notes = "Carga inicial del documento emitido por proveedor"
            )
            versionDao.insertVersions(listOf(v3, v2, v1))

            // 6. Sharing / Permissions
            val permDao = db.documentPermissionDao()
            val permContrato = DocumentPermissionEntity(
                id = "perm_1",
                companyId = "empresa_ferreteria",
                documentId = "doc_contrato_1",
                userId = "user_jorge",
                userName = "Jorge Pérez",
                userEmail = "jorge@docupyme.com",
                permissionLevel = "Puede ver",
                expiresAt = now + (25 * oneDay),
                message = "Te comparto este contrato para la revisión de auditoría legal.",
                grantedAt = now - (3 * oneDay)
            )
            permDao.insertPermission(permContrato)

            // 7. Audit Events (Auditoría)
            val auditDao = db.auditEventDao()
            val events = listOf(
                AuditEventEntity(
                    id = "evt_1",
                    companyId = "empresa_ferreteria",
                    actorId = "user_laura",
                    actorName = "Laura Gómez",
                    documentId = "doc_factura_1",
                    documentName = "Factura_2024_001.pdf",
                    action = "Carga",
                    result = "Éxito",
                    details = "Carga de documento con clasificación IA aceptada (Categoría: Facturas).",
                    timestamp = now - (6 * oneDay)
                ),
                AuditEventEntity(
                    id = "evt_2",
                    companyId = "empresa_ferreteria",
                    actorId = "user_carlos",
                    actorName = "Carlos Ruiz",
                    documentId = "doc_factura_1",
                    documentName = "Factura_2024_001.pdf",
                    action = "Nueva versión",
                    result = "Éxito",
                    details = "Actualización a versión v2.",
                    timestamp = now - (4 * oneDay)
                ),
                AuditEventEntity(
                    id = "evt_3",
                    companyId = "empresa_ferreteria",
                    actorId = "user_laura",
                    actorName = "Laura Gómez",
                    documentId = "doc_contrato_1",
                    documentName = "Contrato_proveedor_acero.pdf",
                    action = "Compartición",
                    result = "Éxito",
                    details = "Concedido permiso de solo lectura a Jorge Pérez.",
                    timestamp = now - (3 * oneDay)
                ),
                AuditEventEntity(
                    id = "evt_4",
                    companyId = "empresa_ferreteria",
                    actorId = "user_laura",
                    actorName = "Laura Gómez",
                    action = "Respaldo",
                    result = "Éxito",
                    details = "Copia de seguridad en la nube ejecutada satisfactoriamente (128 archivos sincronizados).",
                    timestamp = now - (1 * oneDay)
                )
            )
            auditDao.insertEvents(events)

            // 8. Backups
            val backupDao = db.backupDao()
            val backupCompleted = BackupEntity(
                id = "bck_1",
                companyId = "empresa_ferreteria",
                startedAt = now - (1 * oneDay),
                completedAt = now - (1 * oneDay) + 125000,
                status = "Completado",
                totalFiles = 128,
                totalSizeBytes = 2576980377L, // ~2.4 GB
                location = "Almacenamiento privado seguro AWS S3 / Cloud Storage",
                restorationResult = "Integridad 100% verificada. 128 de 128 archivos íntegros con SHA-256 coincidente."
            )
            backupDao.insertBackup(backupCompleted)

            // 9. Notifications
            val notifDao = db.notificationDao()
            val notifications = listOf(
                NotificationEntity(
                    id = "notif_1",
                    companyId = "empresa_ferreteria",
                    recipientId = "user_laura",
                    title = "Documento compartido",
                    message = "Laura compartió Factura 2024 con el equipo contable.",
                    type = "COMPARTIDO",
                    isRead = false,
                    createdAt = now - (2 * 3600000L)
                ),
                NotificationEntity(
                    id = "notif_2",
                    companyId = "empresa_ferreteria",
                    recipientId = "user_laura",
                    title = "Próximo a vencer",
                    message = "Cámara de Comercio 2024: renovación sugerida antes del fin de mes.",
                    type = "VENCIMIENTO",
                    isRead = false,
                    createdAt = now - (5 * 3600000L)
                ),
                NotificationEntity(
                    id = "notif_3",
                    companyId = "empresa_ferreteria",
                    recipientId = "user_laura",
                    title = "Respaldo completado",
                    message = "El respaldo en la nube se completó exitosamente (2.4 GB asegurados).",
                    type = "RESPALDO",
                    isRead = true,
                    createdAt = now - (1 * oneDay)
                )
            )
            notifDao.insertNotifications(notifications)
        }
    }

    fun computeHash(text: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(text.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
