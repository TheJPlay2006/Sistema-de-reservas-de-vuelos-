# 🛫 Sistema de Gestión de Reservas de Vuelo

Proyecto académico desarrollado en **Java con NetBeans**, que simula un sistema completo de reservas de vuelos, con interfaz gráfica, base de datos SQL Server, autenticación de usuarios, y exportación de itinerarios.

---

## 📸 Captura de Pantalla

![Interfaz del sistema](capturas/interfaz.png)  
*Interfaz principal con búsqueda, itinerario y exportación a PDF*

---

## 🎯 Funcionalidades Principales

✅ **Autenticación de usuarios**  
- Inicio de sesión y registro con contraseña  
- Gestión de perfil por usuario

✅ **Búsqueda de vuelos**  
- Filtros por origen, destino y fecha  
- Resultados en tiempo real desde la base de datos

✅ **Reservas con validación**  
- Verificación de asientos disponibles  
- Evita duplicados  
- Actualiza automáticamente la disponibilidad

✅ **Itinerario de usuario**  
- Lista todas las reservas confirmadas  
- Permite cancelar reservas

✅ **Vuelos en tiempo real (API externa)**  
- Integración con **OpenSky Network API**  
- Muestra vuelos reales en tiempo real  
- Opción para agregarlos al sistema

✅ **Exportación de itinerario a PDF**  
- Genera un PDF profesional con el itinerario del usuario  
- Usa **OpenPDF** (fork moderno y gratuito de iText)

---

## 🛠️ Tecnologías Utilizadas

| Tecnología | Uso |
|----------|-----|
| **Java 17+** | Lenguaje principal |
| **NetBeans IDE** | Entorno de desarrollo |
| **SQL Server** | Base de datos relacional |
| **JDBC** | Conexión a base de datos |
| **Swing** | Interfaz gráfica (GUI) |
| **OpenPDF 1.3.30** | Generación de PDFs |  
| **OpenSky API** | Datos de vuelos en tiempo real |

🔗 **OpenPDF**: [https://mvnrepository.com/artifact/com.github.librepdf/openpdf/1.3.30](https://mvnrepository.com/artifact/com.github.librepdf/openpdf/1.3.30)

---

## 🧩 Estructura del Proyecto

```
GestionProyectos/
├── src/
│   ├── modelo/                # Clases POJO: Vuelo, Usuario, Reserva, etc.
│   ├── dao/                   # Data Access Objects
│   ├── vista/                 # Interfaz gráfica (Swing)
│   ├── util/                  # Conexión, API, herramientas
│   └── Main.java
├── lib/
│   ├── openpdf-1.3.30.jar     # Librería para generar PDFs
│   └── slf4j-api-1.7.32.jar   # Requerido por OpenPDF
├── bd/
│   └── script_bd.sql          # Script para crear la base de datos
├── capturas/
│   └── interfaz.png           # Captura de pantalla
└── build.xml                  # Archivo de construcción (Ant)
```

---

## 🚀 Cómo Ejecutar el Proyecto

### 1. Prerrequisitos
- Java JDK 17 o superior
- NetBeans IDE (recomendado)
- SQL Server Express (instancia: `JPLAYLAPTOP\SQLEXPRESS`)
- Base de datos `SistemaReservasVuelo` creada

### 2. Pasos de Instalación
1. **Clona o abre el proyecto en NetBeans**
2. **Añade las librerías a `lib/`**:
   - `openpdf-1.3.30.jar`
   - `slf4j-api-1.7.32.jar`
3. **Ejecuta el script SQL** para crear la base de datos y tablas
4. **Compila y ejecuta** `SistemaReservasGUI.java`

> ⚠️ **Nota**: Si usas otro nombre de instancia de SQL Server, actualiza la URL en `ConexionBD.java`.

---

## 💾 Script de Base de Datos

Ejecuta este script en **SQL Server Management Studio (SSMS)** para crear la base de datos:

```sql
-- script_bd.sql
USE master;
GO

IF DB_ID('SistemaReservasVuelo') IS NOT NULL
    DROP DATABASE SistemaReservasVuelo;
GO

CREATE DATABASE SistemaReservasVuelo;
GO

USE SistemaReservasVuelo;
GO

-- Tabla Aerolinea
CREATE TABLE Aerolinea (
    id_aerolinea INT PRIMARY KEY IDENTITY(1,1),
    nombre NVARCHAR(100) NOT NULL,
    codigo NVARCHAR(10) NOT NULL
);

-- Tabla Usuario
CREATE TABLE Usuario (
    id_usuario INT PRIMARY KEY IDENTITY(1,1),
    nombre NVARCHAR(100) NOT NULL,
    email NVARCHAR(100) NOT NULL UNIQUE,
    telefono NVARCHAR(20),
    fecha_registro DATETIME NOT NULL DEFAULT GETDATE(),
    password NVARCHAR(255) NOT NULL DEFAULT '12345'
);

-- Tabla Vuelo
CREATE TABLE Vuelo (
    id_vuelo INT PRIMARY KEY IDENTITY(1,1),
    id_aerolinea INT FOREIGN KEY REFERENCES Aerolinea(id_aerolinea),
    numero_vuelo NVARCHAR(20) NOT NULL,
    origen NVARCHAR(100) NOT NULL,
    destino NVARCHAR(100) NOT NULL,
    fecha_salida DATETIME NOT NULL,
    fecha_llegada DATETIME NOT NULL,
    asientos_totales INT NOT NULL,
    asientos_disponibles INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    escalas INT NOT NULL,
    estado NVARCHAR(20) NOT NULL
);

-- Tabla Reserva
CREATE TABLE Reserva (
    id_reserva INT PRIMARY KEY IDENTITY(1,1),
    id_usuario INT FOREIGN KEY REFERENCES Usuario(id_usuario),
    id_vuelo INT FOREIGN KEY REFERENCES Vuelo(id_vuelo),
    fecha_reserva DATETIME NOT NULL DEFAULT GETDATE(),
    estado NVARCHAR(20) NOT NULL,
    cantidad_asientos INT NOT NULL
);

-- Procedimiento almacenado para insertar reserva
CREATE PROCEDURE sp_insertar_reserva
    @id_usuario INT,
    @id_vuelo INT,
    @cantidad_asientos INT,
    @id_reserva_generada INT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    INSERT INTO Reserva (id_usuario, id_vuelo, cantidad_asientos, estado)
    VALUES (@id_usuario, @id_vuelo, @cantidad_asientos, 'Confirmada');
    SET @id_reserva_generada = SCOPE_IDENTITY();
END
GO
```

---

## 🔧 Configuración Adicional

### Conexión a Base de Datos
Asegúrate de que la cadena de conexión en `ConexionBD.java` coincida con tu configuración:

```java
String url = "jdbc:sqlserver://JPLAYLAPTOP\\SQLEXPRESS:1433;databaseName=SistemaReservasVuelo;encrypt=false;";
```

### API Externa
El sistema utiliza la **OpenSky Network API** para obtener datos de vuelos en tiempo real. No requiere autenticación para uso básico.

---

## 🎓 Características del Proyecto

Este proyecto académico demuestra:
- Arquitectura en capas (DAO, Modelo, Vista)
- Manejo de base de datos relacionales
- Integración con APIs externas
- Generación de documentos PDF
- Validación de datos y manejo de errores
- Interfaz gráfica con Swing

---

## 📝 Notas del Desarrollador

- La contraseña por defecto para nuevos usuarios es `12345`
- El sistema valida automáticamente la disponibilidad de asientos
- Los PDFs se generan con información completa del itinerario
- La integración con OpenSky API permite visualizar vuelos reales

---

## 🤝 Contribuciones

Este es un proyecto académico. Si encuentras algún error o mejora, no dudes en crear un issue o pull request.

---

**Desarrollado con ❤️ para fines educativos**
