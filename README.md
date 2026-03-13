# AD05 - Gestión de Ficheros con Spring Boot

API REST de gestión de empleados, departamentos y proyectos desarrollada con **Spring Boot 3.4.2**, que incorpora un sistema de **almacenamiento de ficheros en el servidor** (patrón StorageService). Proyecto educativo para la unidad **UT5 - Gestión de Ficheros** del módulo *Acceso a Datos* (2º DAM).

## Descripción

Este proyecto extiende una API REST de gestión empresarial añadiendo capacidades de subida, descarga y eliminación de ficheros. En concreto:

- **Fotos de empleados**: cada empleado puede tener una imagen de perfil asociada. Se sube, se sirve inline y se puede eliminar de forma independiente.
- **Ficheros de proyectos**: cada proyecto puede tener múltiples ficheros adjuntos (PDFs u otros documentos). Se gestionan como entidad propia (`FicheroProyecto`) con relación `@ManyToOne` hacia `Proyecto`.

El almacenamiento físico de ficheros se gestiona mediante la interfaz `StorageService` y su implementación `StorageServiceImpl`, que utiliza **Java NIO2** (`java.nio.file`) y **UUIDs** para evitar colisiones de nombres.

## Tecnologías

- **Java 23**
- **Spring Boot 3.4.2** (Spring Web, Spring Data JPA, Spring Validation)
- **MySQL** (con `createDatabaseIfNotExist=true`)
- **Lombok** 1.18.38
- **Maven** (wrapper incluido)

## Estructura del proyecto

```
src/main/java/com/example/apiempleados/
├── ApiEmpleadosApplication.java
├── controllers/
│   ├── DepartamentosController.java
│   ├── EmpleadosController.java
│   └── ProyectosController.java
├── dto/
│   └── ProyectoConNumeroEmpleadosDTO.java
├── entities/
│   ├── Departamento.java
│   ├── Empleado.java
│   ├── FicheroProyecto.java
│   └── Proyecto.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── StorageException.java
│   └── StorageNotFoundException.java
├── repositories/
│   ├── DepartamentoRepository.java
│   ├── EmpleadoRepository.java
│   ├── FicheroProyectoRepository.java
│   └── ProyectoRepository.java
└── services/
    ├── DepartamentoService.java
    ├── EmpleadoService.java
    ├── ProyectoService.java
    ├── StorageService.java
    └── StorageServiceImpl.java
```

## Modelo de datos

### Entidades y relaciones

| Entidad | Relaciones |
|---|---|
| **Empleado** | `@ManyToOne` → Departamento · `@ManyToMany` ← Proyecto · Campo `rutaFoto` para imagen de perfil |
| **Departamento** | `@OneToMany` → Empleado |
| **Proyecto** | `@ManyToMany` → Empleado · `@OneToMany` → FicheroProyecto (cascade ALL, orphanRemoval) |
| **FicheroProyecto** | `@ManyToOne` → Proyecto · Almacena `nombreFichero` (UUID) y `nombreOriginal` |

## Patrón StorageService

La interfaz `StorageService` define las operaciones de almacenamiento:

| Método | Descripción |
|---|---|
| `inicializar()` | Crea la carpeta de almacenamiento al arrancar (`@PostConstruct`) |
| `guardar(MultipartFile)` | Guarda el fichero con nombre UUID y devuelve el nombre generado |
| `cargar(String)` | Devuelve el fichero como `Resource` para servirlo por HTTP |
| `borrar(String)` | Elimina el fichero del disco |
| `listarFicheros()` | Lista todos los ficheros almacenados |

La carpeta de destino se configura en `application.properties` mediante la propiedad `app.storage.location` (por defecto: `uploads`).

## Endpoints de la API

### Empleados (`/empleados`)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/empleados` | Listar empleados (paginado, ordenado por apellidos) |
| `GET` | `/empleados/{id}` | Obtener empleado por ID |
| `POST` | `/empleados` | Crear empleado (JSON, con validación) |
| `PUT` | `/empleados/{id}` | Actualizar empleado |
| `DELETE` | `/empleados/{id}` | Eliminar empleado (borra también su foto del disco) |
| `POST` | `/empleados/{id}/foto` | Subir foto de perfil (`multipart/form-data`, key=`file`) |
| `GET` | `/empleados/{id}/foto` | Obtener foto de perfil (inline) |
| `DELETE` | `/empleados/{id}/foto` | Eliminar foto de perfil |

### Departamentos (`/departamentos`)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/departamentos` | Listar departamentos (paginado) |
| `GET` | `/departamentos/{id}` | Obtener departamento por ID |
| `POST` | `/departamentos` | Crear departamento |
| `PUT` | `/departamentos/{id}` | Actualizar departamento |
| `DELETE` | `/departamentos/{id}` | Eliminar departamento |
| `POST` | `/departamentos/{deptId}/empleados/{empId}` | Asignar empleado a departamento |
| `DELETE` | `/departamentos/{deptId}/empleados/{empId}` | Desasignar empleado de departamento |

### Proyectos (`/proyectos`)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/proyectos` | Listar proyectos (paginado) |
| `GET` | `/proyectos/{id}` | Obtener proyecto por ID |
| `POST` | `/proyectos` | Crear proyecto (JSON, con validación) |
| `PUT` | `/proyectos/{id}` | Actualizar proyecto |
| `DELETE` | `/proyectos/{id}` | Eliminar proyecto (borra ficheros adjuntos del disco y de BD) |
| `POST` | `/proyectos/{pId}/empleados/{eId}` | Asignar empleado a proyecto |
| `DELETE` | `/proyectos/{pId}/empleados/{eId}` | Desasignar empleado de proyecto |
| `POST` | `/proyectos/{id}/ficheros` | Subir ficheros al proyecto (`multipart/form-data`, key=`files`, múltiples) |
| `GET` | `/proyectos/{id}/ficheros` | Listar ficheros del proyecto |
| `GET` | `/proyectos/{id}/ficheros/{fId}` | Descargar/visualizar fichero (inline, PDF) |
| `DELETE` | `/proyectos/{id}/ficheros/{fId}` | Eliminar fichero del proyecto |
| `GET` | `/proyectos/resumen` | Resumen de proyectos con número de empleados (DTO + JPQL) |

## Manejo de errores (GlobalExceptionHandler)

La clase `GlobalExceptionHandler` (`@RestControllerAdvice`) centraliza el tratamiento de excepciones:

| Excepción | Código HTTP | Descripción |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | Errores de validación (`@Valid`) |
| `MaxUploadSizeExceededException` | 400 | Fichero supera el tamaño máximo |
| `StorageNotFoundException` | 404 | Fichero no encontrado en disco |
| `StorageException` | 500 | Error general de almacenamiento |
| `Exception` | 500 | Errores inesperados |

## Consultas personalizadas (ProyectoRepository)

El repositorio de proyectos incluye ejemplos de distintos tipos de consultas JPA:

- **Derived queries** (por nombre del método): `findByFechaInicio`, `findByPresupuestoGreaterThan`, `findByDescripcionContainingIgnoreCase`, etc.
- **JPQL con `@Query`**: presupuesto medio por año, proyectos con número de empleados (proyección a DTO).
- **`@Modifying`**: actualización masiva del estado de todos los proyectos.

## Configuración

### Requisitos previos

- **Java 23** (o compatible)
- **MySQL** en ejecución en `localhost:3306`
- **Maven** (o usar el wrapper incluido `mvnw` / `mvnw.cmd`)

### application.properties

```properties
# Base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/empleados?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=1234

# JPA
spring.jpa.hibernate.ddl-auto=update

# Límites de subida de ficheros
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=20MB

# Carpeta de almacenamiento
app.storage.location=uploads
```

### Ejecución

```bash
# Clonar el repositorio
git clone https://github.com/samuprofe/AD05_ficheros.git
cd AD05_ficheros

# Ejecutar con Maven wrapper
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`.

## Ejemplos de uso con Postman

### Subir foto de empleado

```
POST /empleados/1/foto
Body → form-data → key: "file" (tipo File) → seleccionar imagen
```

### Subir varios ficheros a un proyecto

```
POST /proyectos/1/ficheros
Body → form-data → key: "files" (tipo File) → seleccionar varios ficheros
```

### Ver foto de empleado

```
GET /empleados/1/foto
→ Devuelve la imagen inline con el Content-Type detectado automáticamente
```

### Ver fichero de proyecto

```
GET /proyectos/1/ficheros/3
→ Devuelve el PDF inline para visualización en el navegador
```

## Conceptos clave trabajados

- **Java NIO2** (`java.nio.file.Path`, `Files.copy`, `Files.createDirectories`, `Files.deleteIfExists`)
- **Patrón StorageService** con interfaz e implementación desacopladas
- **Subida de ficheros** con `MultipartFile` y nombres UUID
- **Servir ficheros** como `Resource` con detección automática de `MediaType`
- **`@PostConstruct`** para inicialización de la carpeta de almacenamiento
- **`@Value`** para inyección de propiedades de configuración
- **Cascade y orphanRemoval** en relaciones JPA para limpieza automática
- **Arquitectura Controller → Service → Repository** con Lombok y DTOs
- **GlobalExceptionHandler** para manejo centralizado de errores
