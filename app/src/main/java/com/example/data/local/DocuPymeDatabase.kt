package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CompanyEntity::class,
        UserEntity::class,
        FolderEntity::class,
        DocumentEntity::class,
        DocumentVersionEntity::class,
        DocumentPermissionEntity::class,
        AuditEventEntity::class,
        BackupEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class DocuPymeDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun userDao(): UserDao
    abstract fun folderDao(): FolderDao
    abstract fun documentDao(): DocumentDao
    abstract fun documentVersionDao(): DocumentVersionDao
    abstract fun documentPermissionDao(): DocumentPermissionDao
    abstract fun auditEventDao(): AuditEventDao
    abstract fun backupDao(): BackupDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: DocuPymeDatabase? = null

        fun getDatabase(context: Context): DocuPymeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DocuPymeDatabase::class.java,
                    "docupyme_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
