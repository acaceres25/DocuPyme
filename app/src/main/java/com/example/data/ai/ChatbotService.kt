package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSuggestions: List<String> = emptyList()
)

data class CompanyContext(
    val companyName: String,
    val nit: String,
    val userName: String,
    val userRole: String,
    val totalDocuments: Int,
    val activeDocumentsList: List<String>,
    val pendingReviewDocs: List<String>,
    val urgentAlerts: List<String>,
    val missingProcesses: List<String>,
    val duplicateCount: Int,
    val trashedCount: Int
)

object ChatbotService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .build()

    suspend fun answerQuestion(
        question: String,
        context: CompanyContext,
        chatHistory: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResponse = callGeminiChat(apiKey, question, context, chatHistory)
                if (!geminiResponse.isNullOrBlank()) {
                    return@withContext geminiResponse
                }
            } catch (e: Exception) {
                // Fallback to local intelligent response
            }
        }

        return@withContext generateLocalResponse(question, context)
    }

    private fun callGeminiChat(
        apiKey: String,
        question: String,
        context: CompanyContext,
        chatHistory: List<ChatMessage>
    ): String? {
        val systemPrompt = """
            Eres "DocuBot", el asistente de inteligencia artificial integrado en la plataforma DocuPyme para la empresa ${context.companyName} (NIT: ${context.nit}).
            Usuario actual: ${context.userName} (${context.userRole}).

            Información en tiempo real del estado de la empresa:
            - Total de documentos activos: ${context.totalDocuments}
            - Documentos principales: ${context.activeDocumentsList.joinToString("; ")}
            - Documentos pendientes o por revisar: ${if (context.pendingReviewDocs.isEmpty()) "Ninguno crítico" else context.pendingReviewDocs.joinToString("; ")}
            - Alertas vigentes: ${context.urgentAlerts.joinToString("; ")}
            - Procesos pendientes o faltantes: ${context.missingProcesses.joinToString("; ")}
            - Archivos duplicados detectados: ${context.duplicateCount}
            - Archivos en papelera: ${context.trashedCount}

            Instrucciones:
            1. Responde de manera clara, profesional, amable y concisa en español.
            2. Si preguntan qué documentos están pendientes o por revisar, detalla exactamente los documentos indicados en el estado anterior.
            3. Si preguntan qué procesos faltan, enumera las acciones requeridas (como renovación de Cámara de Comercio, depuración de duplicados, etc.).
            4. Si hacen preguntas generales de contabilidad, DIAN, impuestos, contratos, almacenamiento o gestión documental para PYMES, respóndelas con precisión técnica.
            5. Usa viñetas y formato claro cuando sea conveniente.
        """.trimIndent()

        val contentsArray = JSONArray()

        // Include recent history (up to last 6 messages)
        val recentHistory = chatHistory.takeLast(6)
        for (msg in recentHistory) {
            val role = if (msg.isUser) "user" else "model"
            contentsArray.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", msg.text))
                })
            })
        }

        // Add current question
        contentsArray.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().put("text", question))
            })
        })

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().put("text", systemPrompt))
                })
            })
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 800)
            })
        }

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) return null

        val responseBody = response.body?.string() ?: return null
        val root = JSONObject(responseBody)
        val candidates = root.optJSONArray("candidates") ?: return null
        if (candidates.length() == 0) return null

        val parts = candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts")
        return parts.getJSONObject(0).getString("text")
    }

    private fun generateLocalResponse(question: String, context: CompanyContext): String {
        val q = question.lowercase()

        return when {
            q.contains("pendiente") || q.contains("por revisar") || q.contains("toca revisar") || q.contains("revisar") -> {
                buildString {
                    appendLine("📋 **Documentos y Asuntos Pendientes de Revisión en ${context.companyName}:**\n")
                    if (context.pendingReviewDocs.isNotEmpty()) {
                        appendLine("1. **Documentos con revisión sugerida:**")
                        context.pendingReviewDocs.forEach { appendLine("   • $it") }
                        appendLine()
                    }
                    if (context.urgentAlerts.isNotEmpty()) {
                        appendLine("2. **Alertas y vencimientos próximos:**")
                        context.urgentAlerts.forEach { appendLine("   ⚠️ $it") }
                        appendLine()
                    }
                    if (context.duplicateCount > 0) {
                        appendLine("3. **Archivos duplicados:** Hay ${context.duplicateCount} copias idénticas detectadas por hash SHA-256 esperando resolución.")
                    }
                    appendLine("\n¿Deseas que te lleve a alguna carpeta o te ayude a revisar alguno en detalle?")
                }
            }

            q.contains("proceso") || q.contains("falta") || q.contains("faltan") || q.contains("tareas") || q.contains("pendiente de hacer") -> {
                buildString {
                    appendLine("⚙️ **Procesos Pendientes para la Empresa (${context.companyName}):**\n")
                    if (context.missingProcesses.isNotEmpty()) {
                        context.missingProcesses.forEachIndexed { index, proc ->
                            appendLine("${index + 1}. **$proc**")
                        }
                    } else {
                        appendLine("• Todos los procesos principales de respaldo y clasificación están al día.")
                    }
                    appendLine("\n💡 **Recomendación prioritaria:** Atender la renovación de la Matrícula Mercantil de la Cámara de Comercio antes de los 15 días de vencimiento.")
                }
            }

            q.contains("resumen") || q.contains("estado") || q.contains("informe") || q.contains("cómo está") -> {
                buildString {
                    appendLine("📊 **Resumen General de DocuPyme para ${context.companyName}:**\n")
                    appendLine("• **Documentos activos:** ${context.totalDocuments} archivos clasificados.")
                    appendLine("• **Seguridad y nube:** 100% de los documentos respaldados con cifrado AES-256.")
                    appendLine("• **Archivos duplicados:** ${context.duplicateCount} detectados.")
                    appendLine("• **Papelera:** ${context.trashedCount} archivos en retención temporal de 30 días.")
                    appendLine("• **Usuario en sesión:** ${context.userName} (${context.userRole}).")
                    appendLine("\nPuedes consultarme sobre cualquier documento, trámite ante la DIAN o proceso administrativo.")
                }
            }

            q.contains("dian") || q.contains("factura") || q.contains("rut") || q.contains("cufe") -> {
                """
                💼 **Gestión Tributaria y Facturación Electrónica DIAN:**
                
                • **Facturas Electrónicas:** DocuPyme extrae automáticamente el CUFE, fecha de expedición, NIT del emisor, subtotales y el 19% de IVA para clasificarlas en la carpeta *Facturas*.
                • **RUT y Documentos Fiscales:** Se archivan en la carpeta *DIAN* con verificación de formulario oficial.
                • **Respaldo legal:** Los documentos comerciales se custodian garantizando integridad con firma hash SHA-256.
                
                ¿Tienes una factura o formulario específico que desees verificar?
                """.trimIndent()
            }

            q.contains("contrato") || q.contains("laboral") || q.contains("proveedor") -> {
                """
                📄 **Gestión de Contratos y Proveedores:**
                
                • Los contratos se organizan en la carpeta *Contratos* identificando cláusulas legales, partes intervinientes y vigencia.
                • Puedes compartir cualquier contrato con usuarios de tu empresa estableciendo permisos (*Solo lectura* o *Edición*) y fecha de expiración del enlace.
                • Las novedades de proveedores o recursos humanos cuentan con carpetas independientes para mantener la separación documental.
                """.trimIndent()
            }

            q.contains("hola") || q.contains("buenos") || q.contains("buenas") || q.contains("quién eres") -> {
                """
                ¡Hola, ${context.userName}! 👋 Soy **DocuBot**, tu asistente inteligente en **DocuPyme**.

                Estoy conectado en tiempo real con los documentos, carpetas y procesos de **${context.companyName}**. Puedo responderte:
                • ¿Qué documentos están pendientes o por revisar?
                • ¿Qué procesos administrativos faltan?
                • Dudas sobre facturación, normativas DIAN y contratos.
                • Resúmenes del estado de tus archivos y respaldos.

                ¿En qué te puedo colaborar hoy?
                """.trimIndent()
            }

            else -> {
                """
                En **DocuPyme** para **${context.companyName}**, tienes **${context.totalDocuments} documentos activos** organizados en tus carpetas.
                
                Respecto a tu consulta: *$question*
                
                Puedo ayudarte a localizar archivos, revisar documentos pendientes, consultar requisitos de la DIAN o guiarte en la gestión de permisos y respaldos. ¿Quieres que revisemos los documentos pendientes o las tareas prioritarias?
                """.trimIndent()
            }
        }
    }
}
