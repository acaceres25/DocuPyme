package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ClassificationResult(
    val category: String, // "Facturas", "Contratos", "Bancos", "DIAN", "Recursos Humanos", "Proveedores", "Otra"
    val confidence: Int, // 0 to 100
    val reasoning: String,
    val suggestedFolderId: String,
    val modelUsed: String, // "Gemini 3.5 Flash" or "Clasificador por Reglas (Modo Local)"
    val needsHumanReview: Boolean
)

object DocumentClassifierService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun classifyDocument(
        fileName: String,
        extractedText: String,
        mimeType: String
    ): ClassificationResult = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        // If Gemini API Key is available and not a placeholder, attempt real Gemini API call
        if (!apiKey.isNullOrBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val geminiResult = callGeminiApi(apiKey, fileName, extractedText, mimeType)
                if (geminiResult != null) {
                    return@withContext geminiResult
                }
            } catch (e: Exception) {
                // Fallback to local rule engine if API fails
            }
        }

        // Intelligent rule-based classifier (clearly stated in UI and audit logs)
        return@withContext classifyLocally(fileName, extractedText)
    }

    private fun callGeminiApi(
        apiKey: String,
        fileName: String,
        text: String,
        mimeType: String
    ): ClassificationResult? {
        val prompt = """
            Eres el clasificador de documentos de DocuPyme para empresas.
            Analiza el nombre del archivo y el texto extraído:
            Nombre: "$fileName"
            MIME: "$mimeType"
            Texto: "$text"

            Clasifica en una de estas categorías exactas:
            ["Facturas", "Contratos", "Bancos", "DIAN", "Recursos Humanos", "Proveedores", "Otra"]

            Responde ÚNICAMENTE un objeto JSON válido con este formato:
            {
              "category": "Facturas",
              "confidence": 95,
              "reasoning": "Breve explicación de las señales encontradas",
              "suggestedFolderId": "f_facturas"
            }
            Folders mapping:
            - Facturas -> "f_facturas"
            - Contratos -> "f_contratos"
            - Bancos -> "f_bancos"
            - DIAN -> "f_dian"
            - Recursos Humanos -> "f_rrhh"
            - Proveedores -> "f_proveedores"
            - Otra -> "f_facturas"
        """.trimIndent()

        val jsonBody = JSONObject().apply {
            put("contents", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", org.json.JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.1)
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

        val content = candidates.getJSONObject(0).getJSONObject("content")
        val parts = content.getJSONArray("parts")
        val outputText = parts.getJSONObject(0).getString("text")

        val parsed = JSONObject(outputText)
        val category = parsed.optString("category", "Otra")
        val confidence = parsed.optInt("confidence", 85)
        val reasoning = parsed.optString("reasoning", "Clasificado mediante análisis de Gemini 3.5 Flash")
        val folderId = parsed.optString("suggestedFolderId", getFolderForCategory(category))

        return ClassificationResult(
            category = category,
            confidence = confidence,
            reasoning = reasoning,
            suggestedFolderId = folderId,
            modelUsed = "Gemini 3.5 Flash",
            needsHumanReview = confidence < 80
        )
    }

    private fun classifyLocally(fileName: String, text: String): ClassificationResult {
        val combined = "$fileName $text".lowercase()

        val signals = mutableListOf<String>()

        val isFactura = combined.contains("factura") || combined.contains("fe-") || combined.contains("iva") || combined.contains("cufe") || combined.contains("subtotal") || combined.contains("total a pagar")
        val isContrato = combined.contains("contrato") || combined.contains("cláusula") || combined.contains("clausula") || combined.contains("arrendamiento") || combined.contains("suministro") || combined.contains("partes intervinientes")
        val isDian = combined.contains("dian") || combined.contains("formulario 110") || combined.contains("renta") || combined.contains("cámara de comercio") || combined.contains("camara de comercio") || combined.contains("matrícula mercantil") || combined.contains("rut")
        val isBanco = combined.contains("banco") || combined.contains("balance") || combined.contains("financiero") || combined.contains("extracto") || combined.contains("pyg") || combined.contains("flujo de caja")
        val isRrhh = combined.contains("nómina") || combined.contains("nomina") || combined.contains("empleado") || combined.contains("laboral") || combined.contains("seguridad social") || combined.contains("recursos humanos")
        val isProveedores = combined.contains("proveedor") || combined.contains("catálogo") || combined.contains("catalogo") || combined.contains("cotización") || combined.contains("cotizacion") || combined.contains("lista de precios") || combined.contains("tarifa")

        return when {
            isFactura -> {
                if (combined.contains("iva")) signals.add("Impuesto IVA")
                if (combined.contains("cufe") || combined.contains("fe-")) signals.add("CUFE / Factura electrónica")
                if (combined.contains("subtotal") || combined.contains("total")) signals.add("Montos fiscales")
                ClassificationResult(
                    category = "Facturas",
                    confidence = 96,
                    reasoning = "Señales detectadas: " + signals.joinToString(", ") + ". Patrón de Factura Electrónica comercial.",
                    suggestedFolderId = "f_facturas",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = false
                )
            }
            isContrato -> {
                if (combined.contains("cláusula") || combined.contains("clausula")) signals.add("Cláusulas legales")
                if (combined.contains("suministro") || combined.contains("arrendamiento")) signals.add("Objeto contractual")
                signals.add("Acuerdo entre partes")
                ClassificationResult(
                    category = "Contratos",
                    confidence = 94,
                    reasoning = "Señales detectadas: " + signals.joinToString(", ") + ". Estructura contractual identificada.",
                    suggestedFolderId = "f_contratos",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = false
                )
            }
            isDian -> {
                if (combined.contains("dian") || combined.contains("formulario")) signals.add("Formulario tributario DIAN")
                if (combined.contains("cámara de comercio") || combined.contains("camara de comercio")) signals.add("Registro mercantil")
                ClassificationResult(
                    category = "DIAN",
                    confidence = 97,
                    reasoning = "Señales detectadas: " + signals.joinToString(", ") + ". Documentación tributaria o societaria oficial.",
                    suggestedFolderId = "f_dian",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = false
                )
            }
            isBanco -> {
                signals.add("Estados contables / Conciliación bancaria")
                ClassificationResult(
                    category = "Bancos",
                    confidence = 92,
                    reasoning = "Señales detectadas: Indicadores de balance y finanzas bancarias.",
                    suggestedFolderId = "f_bancos",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = false
                )
            }
            isRrhh -> {
                signals.add("Nómina y seguridad social")
                ClassificationResult(
                    category = "Recursos Humanos",
                    confidence = 90,
                    reasoning = "Señales detectadas: Gestión de personal y novedades de nómina.",
                    suggestedFolderId = "f_rrhh",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = false
                )
            }
            isProveedores -> {
                signals.add("Listas de precios / Cotizaciones de insumos")
                ClassificationResult(
                    category = "Proveedores",
                    confidence = 88,
                    reasoning = "Señales detectadas: Cotizaciones, catálogo de productos y condiciones de compra.",
                    suggestedFolderId = "f_proveedores",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = false
                )
            }
            else -> {
                // Ambiguous document - low confidence triggers "Revisar" mode
                ClassificationResult(
                    category = "Otra",
                    confidence = 45,
                    reasoning = "Documento ambiguo: no se encontraron términos tributarios o contractuales concluyentes. Requiere revisión manual.",
                    suggestedFolderId = "f_facturas",
                    modelUsed = "Clasificador por Reglas (Modo Local)",
                    needsHumanReview = true
                )
            }
        }
    }

    fun getFolderForCategory(category: String): String {
        return when (category) {
            "Facturas" -> "f_facturas"
            "Contratos" -> "f_contratos"
            "Bancos" -> "f_bancos"
            "DIAN" -> "f_dian"
            "Recursos Humanos" -> "f_rrhh"
            "Proveedores" -> "f_proveedores"
            else -> "f_facturas"
        }
    }
}
