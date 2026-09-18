package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: String): CompanyEntity?

    @Query("SELECT * FROM companies")
    fun getAllCompanies(): Flow<List<CompanyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE companyId = :companyId")
    fun getUsersByCompany(companyId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE companyId = :companyId")
    fun getFoldersByCompany(companyId: String): Flow<List<FolderEntity>>

    @Query("SELECT * FROM folders WHERE id = :id")
    suspend fun getFolderById(id: String): FolderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Update
    suspend fun updateFolder(folder: FolderEntity)

    @Delete
    suspend fun deleteFolder(folder: FolderEntity)
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents WHERE companyId = :companyId AND status = 'activo' ORDER BY updatedAt DESC")
    fun getActiveDocuments(companyId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE companyId = :companyId AND folderId = :folderId AND status = 'activo' ORDER BY updatedAt DESC")
    fun getDocumentsByFolder(companyId: String, folderId: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: String): DocumentEntity?

    @Query("SELECT * FROM documents WHERE id = :id")
    fun getDocumentByIdFlow(id: String): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE companyId = :companyId AND status = 'papelera' ORDER BY trashedAt DESC")
    fun getTrashedDocuments(companyId: String): Flow<List<DocumentEntity>>

    @Query("""
        SELECT * FROM documents 
        WHERE companyId = :companyId 
        AND status = 'activo' 
        AND (name LIKE '%' || :query || '%' 
             OR category LIKE '%' || :query || '%' 
             OR textContent LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun searchDocuments(companyId: String, query: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE companyId = :companyId AND status = 'activo' AND fileHash = :hash")
    suspend fun getDocumentsByHash(companyId: String, hash: String): List<DocumentEntity>

    @Query("""
        SELECT d1.* FROM documents d1
        INNER JOIN documents d2 ON d1.fileHash = d2.fileHash AND d1.id != d2.id
        WHERE d1.companyId = :companyId AND d1.status = 'activo' AND d2.status = 'activo'
        ORDER BY d1.fileHash, d1.createdAt DESC
    """)
    fun getDuplicateDocuments(companyId: String): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: String)
}

@Dao
interface DocumentVersionDao {
    @Query("SELECT * FROM document_versions WHERE documentId = :documentId ORDER BY versionNumber DESC")
    fun getVersionsForDocument(documentId: String): Flow<List<DocumentVersionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: DocumentVersionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersions(versions: List<DocumentVersionEntity>)
}

@Dao
interface DocumentPermissionDao {
    @Query("SELECT * FROM document_permissions WHERE documentId = :documentId")
    fun getPermissionsForDocument(documentId: String): Flow<List<DocumentPermissionEntity>>

    @Query("SELECT * FROM document_permissions WHERE companyId = :companyId AND userId = :userId")
    fun getPermissionsForUser(companyId: String, userId: String): Flow<List<DocumentPermissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermission(permission: DocumentPermissionEntity)

    @Query("DELETE FROM document_permissions WHERE id = :id")
    suspend fun deletePermissionById(id: String)
}

@Dao
interface AuditEventDao {
    @Query("SELECT * FROM audit_events WHERE companyId = :companyId ORDER BY timestamp DESC LIMIT 100")
    fun getEventsByCompany(companyId: String): Flow<List<AuditEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: AuditEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<AuditEventEntity>)
}

@Dao
interface BackupDao {
    @Query("SELECT * FROM backups WHERE companyId = :companyId ORDER BY startedAt DESC")
    fun getBackupsByCompany(companyId: String): Flow<List<BackupEntity>>

    @Query("SELECT * FROM backups WHERE companyId = :companyId ORDER BY startedAt DESC LIMIT 1")
    suspend fun getLatestBackup(companyId: String): BackupEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: BackupEntity)

    @Update
    suspend fun updateBackup(backup: BackupEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE companyId = :companyId AND recipientId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(companyId: String, userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE companyId = :companyId AND recipientId = :userId")
    suspend fun markAllAsRead(companyId: String, userId: String)
}
