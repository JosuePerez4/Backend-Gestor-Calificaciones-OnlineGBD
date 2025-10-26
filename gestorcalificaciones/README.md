# Sistema de Gestión de Calificaciones Online

## Descripción

Sistema backend desarrollado en Spring Boot para la gestión de calificaciones de estudiantes. Permite a los docentes cargar archivos CSV con calificaciones de ejercicios y a los estudiantes visualizar su rendimiento académico.

## Características Principales

### Para Docentes
- **Carga de archivos CSV**: Procesamiento automático de archivos CSV con calificaciones de ejercicios
- **Gestión de cursos**: Creación y administración de cursos
- **Estadísticas detalladas**: Análisis del rendimiento de estudiantes y ejercicios
- **Visualización de datos**: Informes organizados y fáciles de interpretar

### Para Estudiantes
- **Visualización de calificaciones**: Acceso a sus calificaciones por curso
- **Clasificación de ejercicios**: Correctos, incorrectos, pendientes y no enviados
- **Progreso académico**: Seguimiento del rendimiento y porcentaje de completitud
- **Historial de cursos**: Acceso a todos los cursos en los que está inscrito

## Estructura del Proyecto

```
src/main/java/gestor/calificaciones/gestorcalificaciones/
├── controller/          # Controladores REST
│   ├── AuthController.java
│   ├── TeacherController.java
│   └── StudentController.java
├── entities/           # Entidades JPA
│   ├── User.java
│   ├── Student.java
│   ├── Teacher.java
│   ├── Course.java
│   ├── Exercise.java
│   ├── StudentGrade.java
│   └── StudentCourse.java
├── repository/         # Repositorios JPA
├── service/           # Lógica de negocio
│   ├── CsvProcessingService.java
│   ├── CourseStatisticsService.java
│   └── StudentService.java
├── DTO/              # Objetos de transferencia de datos
│   ├── CSV/
│   ├── Course/
│   └── Student/
└── enums/            # Enumeraciones
    ├── Role.java
    └── GradeStatus.java
```

## API Endpoints

### Autenticación
- `POST /api/auth/register` - Registro de usuarios
- `POST /api/auth/login` - Inicio de sesión

### Docentes
- `POST /api/teacher/upload-csv` - Carga de archivo CSV
- `GET /api/teacher/courses` - Obtener cursos del docente
- `GET /api/teacher/courses/{courseId}` - Detalles de un curso
- `GET /api/teacher/courses/{courseId}/statistics` - Estadísticas del curso

### Estudiantes
- `GET /api/student/courses` - Cursos del estudiante
- `GET /api/student/grades` - Todas las calificaciones
- `GET /api/student/courses/{courseId}/grades` - Calificaciones por curso

## Formato del Archivo CSV

El sistema procesa archivos CSV con el siguiente formato:

```csv
Student Name,Ejercicio 1,Ejercicio 2,Ejercicio 3,...
Andrea Gutierrez,100,80,100,...
Juan Perez,Not Submitted,90,100,...
```

### Valores de Calificación Soportados
- **Números (0-100)**: Calificaciones numéricas
- **"Not Submitted"**: Ejercicio no enviado
- **Valores vacíos**: Tratados como no enviado

### Clasificación Automática
- **≥ 80 puntos**: Correcto
- **1-79 puntos**: Incorrecto
- **0 puntos**: Incorrecto
- **"Not Submitted"**: No enviado
- **Otros valores**: Pendiente

## Configuración de Base de Datos

El sistema utiliza PostgreSQL. Configura la conexión en `application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/gestorCalificaciones
spring.datasource.username=postgres
spring.datasource.password=tu_password
```

## Instalación y Ejecución

1. **Clonar el repositorio**
2. **Configurar PostgreSQL** y crear la base de datos
3. **Actualizar configuración** en `application.properties`
4. **Ejecutar la aplicación**:
   ```bash
   mvn spring-boot:run
   ```

## Dependencias Principales

- Spring Boot 3.5.6
- Spring Security
- Spring Data JPA
- PostgreSQL Driver
- JWT (JSON Web Tokens)
- OpenCSV
- Lombok
- MapStruct

## Seguridad

- Autenticación basada en JWT
- Roles diferenciados (STUDENT, TEACHER)
- Validación de permisos por endpoint
- Protección CSRF habilitada

## Flujo de Trabajo

1. **Docente se registra/inicia sesión**
2. **Docente carga archivo CSV** con calificaciones
3. **Sistema procesa CSV** y crea/actualiza:
   - Curso
   - Ejercicios
   - Estudiantes (si no existen)
   - Calificaciones
4. **Estudiantes pueden ver** sus calificaciones organizadas
5. **Docente puede consultar** estadísticas detalladas

## Características Técnicas

- **Arquitectura RESTful**
- **Base de datos relacional** con JPA/Hibernate
- **Procesamiento asíncrono** de archivos CSV
- **Validación robusta** de datos
- **Manejo de errores** centralizado
- **Logging detallado** para debugging

## Próximas Mejoras

- [ ] Interfaz web frontend
- [ ] Notificaciones por email
- [ ] Exportación de reportes en PDF
- [ ] Gráficos y visualizaciones avanzadas
- [ ] API para móviles
- [ ] Integración con sistemas externos
