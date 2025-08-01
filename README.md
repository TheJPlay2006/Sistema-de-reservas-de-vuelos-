# 🛫 Sistema de Gestión de Reservas de Vuelos

Proyecto académico desarrollado en **Java con NetBeans**, que simula un sistema completo de reservas de vuelos, con autenticación, búsqueda, API externa y generación de PDFs.

---

## 📸 Capturas de Pantalla

### 1. **Login**
<img width="648" height="373" alt="Login del Sistema" src="https://github.com/user-attachments/assets/d07f4453-f97d-4903-bb09-c66179468a14" />

*Pantalla de inicio de sesión con campos para email y contraseña.*

### 2. **Buscar Vuelos**
<img width="975" height="666" alt="Interfaz de Búsqueda de Vuelos" src="https://github.com/user-attachments/assets/fd2228ec-2d57-4669-9849-4ffff30b8eda" />

*Interfaz principal para buscar vuelos por origen, destino y fecha.*

### 3. **Itinerario del Usuario**
<img width="975" height="666" alt="Itinerario de Reservas" src="https://github.com/user-attachments/assets/2dc7794a-5b23-4c9e-9219-f4f17ae01171" />

*Lista de reservas del usuario con opciones para cancelar o exportar a PDF.*

### 4. **PDF Generado**
<img width="975" height="661" alt="PDF de Itinerario Exportado" src="https://github.com/user-attachments/assets/b537ceaf-0f73-497c-81f3-af35857d5fa3" />

*Ejemplo de PDF generado con el itinerario de vuelos del usuario.*

---

## 🎯 Funcionalidades Principales

### ✅ **Autenticación de usuarios**
- Inicio de sesión y registro con contraseña
- Gestión de perfil por usuario

### ✅ **Búsqueda de vuelos**
- Filtros por origen, destino y fecha
- Resultados en tiempo real desde la base de datos

### ✅ **Reservas con validación**
- Verificación de asientos disponibles
- Evita duplicados
- Actualiza automáticamente la disponibilidad

### ✅ **Itinerario de usuario**
- Lista todas las reservas confirmadas
- Permite cancelar reservas

### ✅ **Vuelos en tiempo real (API externa)**
- Integración con **OpenSky Network API**
- Muestra vuelos reales en tiempo real
- Opción para agregarlos al sistema

### ✅ **Exportación de itinerario a PDF**
- Genera un PDF profesional con el itinerario del usuario
- Usa **OpenPDF** (fork moderno y gratuito de iText)

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Uso | Versión |
|----------|-----|---------|
| **Java** | Lenguaje principal | 17+ |
| **NetBeans IDE** | Entorno de desarrollo | - |
| **SQL Server** | Base de datos relacional | Express |
| **JDBC** | Conexión a base de datos | - |
| **Swing** | Interfaz gráfica (GUI) | - |
| **OpenPDF** | Generación de PDFs | 1.3.30 |
| **OpenSky API** | Datos de vuelos en tiempo real | - |

🔗 **OpenPDF**: [Maven Repository](https://mvnrepository.com/artifact/com.github.librepdf/openpdf/1.3.30)

---

## 🧩 Estructura del Proyecto

```
GestionProyectos/
├── src/
│   ├── modelo/                 # Clases POJO: Vuelo, Usuario, Reserva, etc.
│   ├── dao/                    # Data Access Objects
│   ├── vista/                  # Interfaz gráfica (Swing)
│   ├── util/                   # Conexión, API, herramientas
│   └── Main.java               # Clase principal
├── lib/
│   ├── openpdf-1.3.30.jar      # Librería para generar PDFs
│   ├── slf4j-api-1.7.32.jar    # Requerido por OpenPDF
│   └── slf4j-nop-1.7.32.jar    # Opcional, elimina advertencias
├── bd/
│   └── script_bd.sql           # Script para crear la base de datos
├── capturas/
│   ├── login.png               # Captura de pantalla de login
│   ├── vuelos_gui.png          # Interfaz de búsqueda de vuelos
│   ├── itinerario_gui.png      # Vista del itinerario
│   └── pdf_ejemplo.png         # Ejemplo de PDF generado
└── build.xml                   # Archivo de construcción (Ant)
```

---

## 🚀 Cómo Ejecutar el Proyecto

### 1. **Prerrequisitos**
- ☑️ Java JDK 17 o superior
- ☑️ NetBeans IDE (recomendado)
- ☑️ SQL Server Express (instancia: `JPLAYLAPTOP\SQLEXPRESS`)
- ☑️ Base de datos `SistemaReservasVuelo` creada

### 2. **Pasos de Instalación**

1. **📁 Clona o abre el proyecto en NetBeans**

2. **📚 Añade las librerías a `lib/`:**
   - `openpdf-1.3.30.jar`
   - `slf4j-api-1.7.32.jar`
   - `slf4j-nop-1.7.32.jar` *(opcional, para eliminar advertencias)*

3. **🗄️ Ejecuta el script SQL** para crear la base de datos y tablas

4. **▶️ Compila y ejecuta** `Main.java`

> ⚠️ **Importante**: Si usas otro nombre de instancia de SQL Server, actualiza la URL en `ConexionBD.java`.

---

## 💾 Exportación a PDF

Al hacer clic en **"Exportar a PDF"**, el sistema genera un archivo profesional que contiene:

📋 **Contenido del PDF:**
- **Encabezado**: Título y nombre del usuario
- **Tabla detallada**: Vuelos reservados con información completa
- **Información incluida**: Precios, rutas, fechas y horarios
- **Pie de página**: Fecha de emisión del documento

---

## 🧑‍🏫 Información del Proyecto

### **Autor**
**Jairo Steven Herrera Romero**  
Estudiante de Ingeniería en Tecnologías de la Información  
Universidad Técnica Nacional (UTN)

### **📂 Repositorio**
Este proyecto fue desarrollado como parte de la asignatura **Gestión de Proyectos de Software**.

🔗 **Repositorio GitHub**: [https://github.com/TheJPlay2006/Sistema-de-reservas-de-vuelos-](https://github.com/TheJPlay2006/Sistema-de-reservas-de-vuelos-)

---

## 🏆 Características Técnicas

Este sistema demuestra la integración completa de:

### **🏗️ Arquitectura en Capas**
- **Presentación**: Interfaz gráfica con Swing
- **Negocio**: Lógica de aplicación con DAOs
- **Datos**: Persistencia en SQL Server
- **Servicios**: Integración con API externa
- **Documentos**: Generación automática de PDFs

### **🔧 Patrones Implementados**
- **DAO (Data Access Object)**: Abstracción de acceso a datos
- **MVC (Model-View-Controller)**: Separación de responsabilidades
- **Factory**: Creación de conexiones a base de datos

---

## 🎯 Objetivos Académicos Cumplidos

✅ **Desarrollo de aplicaciones desktop en Java**  
✅ **Integración con bases de datos relacionales**  
✅ **Consumo de APIs REST externas**  
✅ **Generación de documentos PDF**  
✅ **Implementación de patrones de diseño**  
✅ **Validación y manejo de errores**  
✅ **Interfaz de usuario intuitiva**

---

## 📝 Notas de Desarrollo

- **Contraseña por defecto**: `12345` para nuevos usuarios
- **Validación automática**: El sistema verifica disponibilidad de asientos
- **API externa**: OpenSky Network no requiere autenticación
- **PDFs**: Se generan con información completa y formato profesional

---

## 🤝 Contribuciones

Este es un proyecto académico desarrollado con fines educativos. Si encuentras algún error o tienes sugerencias de mejora, no dudes en:
- Crear un **issue** en el repositorio
- Enviar un **pull request** con mejoras
- Contactar al desarrollador

---

## 📦 Clonar el Repositorio

Para obtener una copia del proyecto en tu máquina local, usa el siguiente comando:

```bash
git clone https://github.com/TheJPlay2006/Sistema-de-reservas-de-vuelos-.git
```

O si prefieres usar SSH:

```bash
git clone git@github.com:TheJPlay2006/Sistema-de-reservas-de-vuelos-.git
```

### **Pasos después de clonar:**

1. **Navega al directorio del proyecto:**
   ```bash
   cd Sistema-de-reservas-de-vuelos-
   ```

2. **Abre el proyecto en NetBeans:**
   - `File` → `Open Project`
   - Selecciona la carpeta del proyecto

3. **Sigue los pasos de instalación** mencionados en la sección [🚀 Cómo Ejecutar el Proyecto](#-cómo-ejecutar-el-proyecto)

---

<div align="center">

**🎓 Desarrollado con ❤️ para fines educativos**

*Proyecto de Gestión de Software - Universidad Técnica Nacional*

⭐ **¡No olvides darle una estrella al repositorio si te fue útil!** ⭐

</div>
