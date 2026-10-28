# Apuntes UNI — Plataforma Web de Gestión Colaborativa de Apuntes

Sistema web desarrollado con **Spring Boot** y **Thymeleaf** para la gestión y descarga colaborativa de apuntes académicos universitarios.

---

## 🏛️ Características Principales

- **Gestión de Usuarios y Roles**:
  - Registro e inicio de sesión seguro con Spring Security y contraseñas cifradas en BCrypt.
  - Roles jerárquicos: `STUDENT`, `PROFESSOR`, `MODERATOR`, `ADMINISTRATOR`.
  - Panel de perfil con historial de subidas, favoritos y edición de datos personales.
- **Repositorio de Apuntes**:
  - Subida de archivos (PDF, DOCX, PPTX, TXT, PNG, JPG) con validación MIME y nombres UUID seguros.
  - Buscador avanzado con filtros combinados por carrera, materia, formato y paginación.
  - Visualización y previsualización de documentos con contador de descargas y vistas.
- **Interacción Estudiantil**:
  - Sistema de valoraciones (1 a 5 estrellas) y comentarios por documento.
  - Guardado de favoritos y reporte de contenido para moderación.
- **Panel Administrativo y Moderación**:
  - Aprobación, ocultación y eliminación de documentos.
  - Gestión de carreras, materias y asignación de roles de usuario.

---

## 🛠️ Tecnologías Utilizadas

- **Backend**: Java 17 / 21, Spring Boot 4.x
- **Persistencia**: Spring Data JPA, Hibernate, H2 Database (desarrollo), PostgreSQL (producción)
- **Seguridad**: Spring Security 6+ con protección CSRF
- **Frontend**: Thymeleaf, Bootstrap 5, HTML5 Semántico, CSS3 Responsivo
- **Almacenamiento**: Servicio desacoplado `FileSystemStorageService`
- **Contenedores**: Docker, Docker Compose

---

## 🚀 Puesta en Marcha en Local

### Prerrequisitos
- JDK 17 o superior
- Maven 3.8+ (o el wrapper `./mvnw` incluido)

### Ejecución
```bash
cd ApuntesUni
./mvnw spring-boot:run
```

La aplicación iniciará en `http://localhost:8080`.

### Cuentas de Acceso de Prueba (Cargadas en desarrollo)
| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin@uni.edu.pe` | `Password123!` |
| **Moderador** | `moderador@uni.edu.pe` | `Password123!` |
| **Estudiante** | `ana@uni.edu.pe` | `Password123!` |
| **Docente** | `carlos@uni.edu.pe` | `Password123!` |

---

## 🐳 Despliegue con Docker Compose

Para desplegar la aplicación completa con base de datos PostgreSQL:

1. Configurar variables en `.env` (o usar valores por defecto de `.env.example`):
```bash
cp .env.example .env
```

2. Levantar los contenedores:
```bash
docker compose up --build -d
```

3. Verificar logs:
```bash
docker compose logs -f app
```

4. Detener el entorno:
```bash
docker compose down
```

---

## 🧪 Pruebas Automatizadas

Para ejecutar el conjunto de pruebas unitarias y de integración:
```bash
cd ApuntesUni
./mvnw test
```

---

## 📂 Estructura del Proyecto

```
ApuntesUni/
├── src/
│   ├── main/
│   │   ├── java/com/university/website/
│   │   │   ├── config/          # Configuración de Seguridad y Almacenamiento
│   │   │   ├── controller/      # Controladores MVC y REST
│   │   │   ├── dto/             # Objetos de Transferencia de Datos
│   │   │   ├── exception/       # Manejo global de excepciones
│   │   │   ├── model/           # Entidades JPA y Enums de dominio
│   │   │   ├── repository/      # Repositorios Spring Data JPA
│   │   │   └── service/         # Interfaces e implementaciones de negocio
│   │   └── resources/
│   │       ├── static/          # Estilos CSS, scripts y assets
│   │       ├── templates/       # Vistas Thymeleaf y fragmentos
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── messages.properties
│   └── test/                    # Pruebas unitarias y de seguridad
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```
