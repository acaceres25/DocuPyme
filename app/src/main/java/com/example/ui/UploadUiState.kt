package com.example.ui

import com.example.data.ai.ClassificationResult

data class UploadUiState(
    val fileName: String = "",
    val extractedText: String = "",
    val mimeType: String = "application/pdf",
    val sizeBytes: Long = 1048576L,
    val isClassifying: Boolean = false,
    val classification: ClassificationResult? = null,
    val selectedCategory: String = "Facturas",
    val selectedFolderId: String = "f_facturas",
    val customTags: String = "",
    val customDescription: String = "",
    val currentStep: Int = 0, // 0: Select, 1: Reading/Extracting, 2: AI Analyzing, 3: Review & Confirm
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)
