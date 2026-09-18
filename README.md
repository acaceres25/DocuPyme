# 📄 DocuPyme — Gestión Documental Inteligente para PYMEs

<p align="center">
  <strong>Sistema integral de archivo digital, clasificación asistida por IA, control de versiones, multi-empresa y trazabilidad para micro y pequeñas empresas.</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Plataforma-Android%207.0%2B%20(API%2024%2B)-brightgreen.svg" alt="Android Version" />
  <img src="https://img.shields.io/badge/Lenguaje-Kotlin%202.0-blue.svg" alt="Kotlin" />
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Database-Room%20(SQLite)-orange.svg" alt="Room Database" />
  <img src="https://img.shields.io/badge/Seguridad-AES--256%20%7C%20SHA--256-red.svg" alt="Security" />
  <img src="https://img.shields.io/badge/Licencia-MIT-lightgrey.svg" alt="License" />
</p>

---

## 🎯 Propósito del Proyecto

**DocuPyme** nace para resolver el desorden de facturas, contratos, certificados mercantiles y planillas laborales en pequeñas y medianas empresas. Reemplaza el caos de papeles físicos y carpetas desorganizadas por un repositorio móvil seguro, local y con capacidades avanzadas de inteligencia artificial.

---

## ✨ Características Principales

### 🗂️ 1. Repositorio Centralizado y Jerárquico
- Organización visual en carpetas temáticas: **Facturas**, **Contratos**, **Bancos**, **DIAN / Tributario**, **Recursos Humanos**, **Proveedores** y **Papelería General**.
- Creación ágil de nuevas carpetas y navegación por niveles.
- Visualización de metadatos (tamaño, tipo MIME, fecha, usuario responsable, versión y hash SHA-256).

### 🤖 2. Clasificación Inteligente y OCR (IA)
- **Análisis Semántico**: Detección automática de tipo documental a partir del nombre y texto extraído (detecta NIT, CUFE, IVA 19%, cláusulas contractuales y formularios DIAN).
- **Sugerencia de Carpeta y Categoría**: Asigna el archivo al destino óptimo con porcentaje de certeza (*Confidence Score*).
- **Revisión Humana Obligatoria**: Cuando la certeza es menor al 80% o el archivo es ambiguo, el sistema alerta al usuario para su validación manual.
- **Detección de Duplicados Criptográficos**: Compara firmas SHA-256 antes de almacenar para prevenir el almacenamiento duplicado de facturas.
- **Escaneo / OCR**: Captura fotográfica de recibos de caja y facturas físicas con digitalización de texto.

### 👓 3. Visor Nativo de Documentos
- Renderizado de archivos **PDF con paginación**, **Excel con cuadrícula y pestañas** e **Imágenes**.
- Gestos multitáctiles de **zoom de pellizco y desplazamiento (pan)**.
- **Sello de Aprobación Digital**: Firma y estampa de aprobación con registro automático en la auditoría.
- Conexión directa para consultar a **DocuBot IA** sobre el contenido del documento en visualización.

### 💬 4. Asistente Virtual Empresarial (DocuBot IA)
- Asistente conversacional con conocimiento en tiempo real de la empresa activa.
- Responde preguntas contextuales clave:
  - *¿Qué documentos están pendientes?*
  - *¿Cuáles documentos toca revisar?*
  - *¿Qué procesos faltan?*
- **Chips de acción rápida**: Navegación directa a depuración de duplicados, prueba de restauración, inventario de documentos y reporte ejecutivo.

### 🛡️ 5. Control de Acceso Basado en Roles (RBAC) y Multi-Empresa
- Aislamiento completo por empresa activa (ej. *DocuPyme*, *Inversiones SAS*, *Comercializadora Andina*).
- Roles preconfigurados con privilegios diferenciados:
  - **Administrador**: Control total de permisos, invitaciones, exportaciones y restauración.
  - **Contador / Editor**: Subida de documentos, versiones y consulta tributaria.
  - **Visualizador**: Lectura restringida sin permisos de borrado ni reconfiguración.

### 📊 6. Reportes y Exportación
- **Informe Ejecutivo en PDF**: Resumen de documentos, distribución por categorías y estado de respaldo para juntas directivas.
- **Matriz de Inventario en Excel / CSV**: Exportación de todos los registros documentales para auditorías contables.

### 🔔 7. Alertas y Trazabilidad (Auditoría RF09)
- Notificaciones push del sistema Android para vencimientos de Cámara de Comercio, pólizas y contratos.
- Registro inmutable de auditoría: cada subida, descarga, visualización, cambio de versión y borrado queda registrado con usuario, hora y estado.

---

## 📱 Capturas y Módulos

| Panel Principal | Visor con Zoom | Chatbot Inteligente |
|:---:|:---:|:---:|
| Indicadores de salud documental, carpetas y accesos rápidos | Renderizado de páginas, zoom táctil y estampa de firma | Respuestas en contexto y botones de navegación a acciones |

| Subida y Clasificación IA | Gestión de Duplicados SHA-256 | Respaldo y Seguridad |
|:---:|:---:|:---:|
| Stepper de extracción, hash y sugerencia de carpeta | Detección de copias redundantes con depuración rápida | Auditoría completa, cifrado AES-256 y simulación de restauración |

---

## 🚀 Cómo Descargar y Probar la Aplicación

### Opción 1: Descargar el APK Directo (Recomendada para usuarios)
1. Ve a la sección de **Releases** de este repositorio en GitHub:
   👉 `https://github.com/<tu-usuario>/<tu-repositorio>/releases`
2. Descarga el archivo `app-debug.apk` o el instalador más reciente.
3. En tu teléfono Android o emulador, abre el archivo descargado y autoriza la instalación desde fuentes desconocidas.
4. ¡Listo! Abre **DocuPyme** y comienza a gestionar tus documentos.

### Opción 2: Clonar y Compilar con Android Studio (Para desarrolladores)

#### Requisitos Previos:
- **Android Studio Ladybug** (o versión más reciente con soporte para Compose BOM 2024+).
- **JDK 17** o superior.
- Dispositivo físico o Emulador Android con **Android 7.0 (API 24)** o superior.

#### Pasos de Instalación:
```bash
# 1. Clonar el repositorio
git clone https://github.com/<tu-usuario>/DocuPyme.git

# 2. Entrar al directorio
cd DocuPyme

# 3. Configurar variables de entorno (opcional para Gemini API)
cp .env.example .env
# Si tienes una API Key de Google Gemini, agrégala en .env:
# GEMINI_API_KEY=tu_clave_aqui

# 4. Compilar e instalar en tu dispositivo conectado
./gradlew installDebug
```

También puedes abrir el proyecto directamente en **Android Studio** y hacer clic en **Run 'app'** (`Shift + F10`).

---

## 🏗️ Arquitectura y Tecnologías

El proyecto sigue los principios de arquitectura limpia (**Clean Architecture**) recomendados por Google:

```
app/src/main/java/com/example/
├── data/
│   ├── ai/               # Servicios de IA (Clasificador, Chatbot, Prompts Gemini)
│   ├── local/            # Entidades Room, DAOs y Pre-poblador de base de datos
│   └── repository/       # Repositorio central con operaciones asíncronas Flow / Coroutines
├── ui/
│   ├── components/       # Barras de navegación superior e inferior
│   ├── screens/          # Pantallas Jetpack Compose (Dashboard, Visor, Chatbot, Subida, etc.)
│   ├── theme/            # Paleta de colores M3 (DocuNavy, DocuGold, DocuBackground)
│   ├── DocuPymeApp.kt    # Scaffold principal y enrutador de pantallas
│   └── DocuPymeViewModel.kt # Máquina de estados y eventos de la aplicación
```

- **Jetpack Compose & Material 3**: UI moderna y adaptativa.
- **Room Database (SQLite)**: Persistencia local offline-first sin dependencia obligatoria de internet.
- **Kotlin Coroutines & StateFlow**: Flujo reactivo unidireccional (UDF).
- **Google Generative AI SDK**: Integración con modelos Gemini para clasificación y respuestas inteligentes.

---

## 🧪 Pruebas Automatizadas

El proyecto cuenta con pruebas unitarias y de simulación de componentes con **Robolectric**:

```bash
# Ejecutar pruebas unitarias
./gradlew testDebugUnitTest
```

---

## 📄 Licencia

Este proyecto está bajo la Licencia [MIT](LICENSE) — siéntete libre de utilizarlo y adaptarlo a las necesidades de tu empresa.

---

<p align="center">
  Desarrollado con ❤️ para impulsar la transformación digital de las micro y pequeñas empresas.
</p>
