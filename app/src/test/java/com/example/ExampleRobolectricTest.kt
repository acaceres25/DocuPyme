package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ai.DocumentClassifierService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DocuPyme", appName)
  }

  @Test
  fun `test ai document classifier for invoice`() = runBlocking {
    val result = DocumentClassifierService.classifyDocument(
      fileName = "Factura_Venta_FE-9901.pdf",
      extractedText = "Factura Electrónica de Venta No. FE-9901. NIT: 900.123.456-7. Subtotal: $100.000. IVA 19%: $19.000. Total: $119.000.",
      mimeType = "application/pdf"
    )
    assertEquals("Facturas", result.category)
    assertTrue(result.confidence > 80)
  }

  @Test
  fun `test ai document classifier for ambiguous text`() = runBlocking {
    val result = DocumentClassifierService.classifyDocument(
      fileName = "Borrador_Notas.txt",
      extractedText = "Hola don Pedro, nos vemos el martes a las 3pm.",
      mimeType = "text/plain"
    )
    assertEquals("Otra", result.category)
    assertTrue(result.needsHumanReview)
  }

  @Test
  fun `test chatbot answers pending documents and missing processes`() = runBlocking {
    val context = com.example.data.ai.CompanyContext(
      companyName = "DocuPyme",
      nit = "900.842.119-3",
      userName = "Carlos Gómez",
      userRole = "Administrador",
      totalDocuments = 8,
      activeDocumentsList = listOf("Factura FE-8492 (Facturas)", "Contrato Arriendo (Contratos)"),
      pendingReviewDocs = listOf("Cámara de Comercio 2025 (Requiere actualización anual en 15 días)"),
      urgentAlerts = listOf("Cámara de comercio vence en 15 días"),
      missingProcesses = listOf("Renovar Matrícula Mercantil", "Depurar 4 duplicados"),
      duplicateCount = 4,
      trashedCount = 1
    )

    // Test Pending question
    val pendingAnswer = com.example.data.ai.ChatbotService.answerQuestion(
      question = "¿Qué documentos están pendientes?",
      context = context,
      chatHistory = emptyList()
    )
    assertTrue(pendingAnswer.contains("pendiente", ignoreCase = true) || pendingAnswer.contains("Cámara de Comercio", ignoreCase = true))

    // Test Missing processes question
    val processesAnswer = com.example.data.ai.ChatbotService.answerQuestion(
      question = "¿Qué procesos faltan?",
      context = context,
      chatHistory = emptyList()
    )
    assertTrue(processesAnswer.contains("proceso", ignoreCase = true) || processesAnswer.contains("Matrícula", ignoreCase = true))
  }
}

