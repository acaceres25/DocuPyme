package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "companies",
    indices = [Index(value = ["id"], unique = true)]
)
data class CompanyEntity(
    @PrimaryKey val id: String,
    val name: String,
    val nit: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVO"
)

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["companyId"])
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String, // "Administrador", "Editor", "Consulta"
    val cargo: String,
    val avatarUrl: String? = null
)

@Entity(
    tableName = "folders",
    indices = [Index(value = ["companyId"])]
)
data class FolderEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val name: String,
    val parentFolderId: String? = null,
    val creatorId: String,
    val colorHex: String = "#FFC400",
    val iconName: String = "folder"
)

@Entity(
    tableName = "documents",
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["folderId"]),
        Index(value = ["fileHash"]),
        Index(value = ["status"])
    ]
)
data class DocumentEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val folderId: String,
    val name: String,
    val category: String, // "Factura", "Contrato", "Banco", "DIAN", "Recursos Humanos", "Proveedores", "Otra"
    val mimeType: String, // "application/pdf", "application/vnd.ms-excel", "image/jpeg", etc.
    val sizeBytes: Long,
    val fileHash: String, // SHA-256 for exact duplicate detection
    val status: String = "activo", // "activo", "papelera"
    val trashedAt: Long? = null,
    val ownerId: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val textContent: String = "",
    val aiSuggestion: String? = null,
    val aiConfidence: Int? = null,
    val aiReasoning: String? = null,
    val isStarred: Boolean = false,
    val backupStatus: String = "Respaldado en la nube"
)

@Entity(
    tableName = "document_versions",
    indices = [Index(value = ["documentId"])]
)
data class DocumentVersionEntity(
    @PrimaryKey val id: String,
    val documentId: String,
    val versionNumber: Int,
    val sizeBytes: Long,
    val fileHash: String,
    val uploadedById: String,
    val uploadedByName: String,
    val uploadedAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "document_permissions",
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["documentId"])
    ]
)
data class DocumentPermissionEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val documentId: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val permissionLevel: String, // "Puede ver", "Puede editar", "Descarga autorizada"
    val expiresAt: Long? = null,
    val message: String = "",
    val grantedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "audit_events",
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["timestamp"])
    ]
)
data class AuditEventEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val actorId: String,
    val actorName: String,
    val documentId: String? = null,
    val documentName: String? = null,
    val action: String, // "Carga", "Consulta", "Descarga", "Edición", "Compartición", "Eliminación", "Restauración", "Nueva versión", "Respaldo"
    val result: String = "Éxito", // "Éxito", "Denegado"
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "backups",
    indices = [Index(value = ["companyId"])]
)
data class BackupEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val status: String = "Completado", // "Completado", "En proceso", "Fallido"
    val totalFiles: Int = 0,
    val totalSizeBytes: Long = 0L,
    val location: String = "Almacenamiento privado seguro AWS S3 / Cloud Storage",
    val restorationResult: String? = null
)

@Entity(
    tableName = "notifications",
    indices = [
        Index(value = ["companyId"]),
        Index(value = ["recipientId"])
    ]
)
data class NotificationEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val recipientId: String,
    val title: String,
    val message: String,
    val type: String, // "COMPARTIDO", "VENCIMIENTO", "RESPALDO", "VERSION"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
