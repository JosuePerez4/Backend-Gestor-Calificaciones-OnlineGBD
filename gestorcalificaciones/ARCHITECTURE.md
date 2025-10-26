# Arquitectura del Sistema

## Diagrama de Entidades

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│      User       │    │     Course      │    │    Exercise     │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ id (UUID)       │    │ id (UUID)       │    │ id (UUID)       │
│ name            │    │ name            │    │ name            │
│ email           │    │ description     │    │ description     │
│ password        │    │ courseCode      │    │ maxScore        │
│ role (STUDENT/  │    │ teacher_id (FK) │    │ course_id (FK)  │
│      TEACHER)   │    │ createdAt       │    │ createdAt       │
│ code            │    │ isActive        │    │ isActive        │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         │                        │                        │
         ▼                        ▼                        ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│    Student      │    │ StudentCourse   │    │  StudentGrade   │
├─────────────────┤    ├─────────────────┤    ├─────────────────┤
│ (inherits from  │    │ id (UUID)       │    │ id (UUID)       │
│  User)          │    │ student_id (FK) │    │ student_id (FK) │
│                 │    │ course_id (FK)  │    │ exercise_id (FK)│
└─────────────────┘    │ enrolledAt      │    │ score           │
         │              │ isActive        │    │ status          │
         │              └─────────────────┘    │ submittedAt     │
         │                        │            │ createdAt       │
         │                        │            │ updatedAt       │
         └────────────────────────┼────────────┘                │
                                  │                             │
                                  ▼                             ▼
                         ┌─────────────────┐            ┌─────────────────┐
                         │    Teacher      │            │  GradeStatus    │
                         ├─────────────────┤            ├─────────────────┤
                         │ (inherits from  │            │ CORRECT         │
                         │  User)          │            │ INCORRECT       │
                         │                 │            │ PENDING         │
                         └─────────────────┘            │ NOT_SUBMITTED   │
                                                       └─────────────────┘
```

## Flujo de Datos

### 1. Carga de CSV por Docente

```
Docente → TeacherController → CsvProcessingService → Base de Datos
    ↓
1. Validar archivo CSV
2. Extraer nombres de ejercicios (header)
3. Extraer datos de estudiantes
4. Crear/actualizar curso
5. Crear/actualizar ejercicios
6. Crear/actualizar estudiantes
7. Procesar calificaciones
8. Vincular estudiantes al curso
```

### 2. Consulta de Estadísticas por Docente

```
Docente → TeacherController → CourseStatisticsService → Base de Datos
    ↓
1. Obtener cursos del docente
2. Calcular estadísticas generales
3. Calcular estadísticas por ejercicio
4. Calcular rendimiento por estudiante
5. Retornar datos estructurados
```

### 3. Consulta de Calificaciones por Estudiante

```
Estudiante → StudentController → StudentService → Base de Datos
    ↓
1. Obtener cursos del estudiante
2. Obtener calificaciones por curso
3. Calcular estadísticas personales
4. Clasificar ejercicios por estado
5. Retornar datos organizados
```

## Capas de la Aplicación

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │ AuthController  │  │TeacherController│  │StudentCtrl│ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────┐
│                     Business Layer                      │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │   AuthService   │  │CsvProcessingSvc │  │StudentSvc│ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐               │
│  │CourseStatsSvc   │  │   UserService   │               │
│  └─────────────────┘  └─────────────────┘               │
└─────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────┐
│                    Persistence Layer                    │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │  UserRepository │  │ CourseRepository│  │ExerciseRepo│ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐ │
│  │StudentGradeRepo │  │StudentCourseRepo│  │StudentRepo│ │
│  └─────────────────┘  └─────────────────┘  └──────────┘ │
└─────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────────────────────────────────┐
│                    Database Layer                       │
│                    PostgreSQL                           │
└─────────────────────────────────────────────────────────┘
```

## Patrones de Diseño Utilizados

### 1. Repository Pattern
- Abstracción del acceso a datos
- Separación entre lógica de negocio y persistencia
- Facilita testing y mantenimiento

### 2. DTO Pattern
- Transferencia de datos entre capas
- Validación de entrada
- Versionado de APIs

### 3. Service Layer Pattern
- Lógica de negocio centralizada
- Reutilización de código
- Transacciones y validaciones

### 4. Builder Pattern
- Construcción de objetos complejos
- Código más legible
- Inmutabilidad

## Consideraciones de Seguridad

### Autenticación
- JWT tokens para sesiones
- Roles diferenciados (STUDENT/TEACHER)
- Validación de permisos por endpoint

### Validación de Datos
- Validación de archivos CSV
- Sanitización de entrada
- Manejo de errores robusto

### Base de Datos
- Relaciones bien definidas
- Índices para consultas frecuentes
- Transacciones ACID

## Escalabilidad

### Horizontal
- Stateless design
- Separación de responsabilidades
- Microservicios ready

### Vertical
- Optimización de consultas
- Caching de datos frecuentes
- Paginación de resultados

## Monitoreo y Logging

### Logs Estructurados
- Información de procesamiento CSV
- Errores y excepciones
- Métricas de rendimiento

### Health Checks
- Estado de la aplicación
- Conectividad a base de datos
- Recursos del sistema
