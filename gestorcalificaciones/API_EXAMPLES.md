# Ejemplos de Uso de la API

## 1. Autenticación

### Registro de Docente
**POST** `/api/auth/register`
```json
{
  "name": "Profesor García",
  "email": "profesor@universidad.com",
  "password": "password123",
  "role": "TEACHER",
  "code": "PROF001"
}
```

### Registro de Estudiante
**POST** `/api/auth/register`
```json
{
  "name": "Ana Estudiante",
  "email": "ana@estudiante.com",
  "password": "password123",
  "role": "STUDENT",
  "code": "EST001"
}
```

### Login
**POST** `/api/auth/login`
```json
{
  "email": "profesor@universidad.com",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "name": "Profesor García",
    "email": "profesor@universidad.com",
    "role": "TEACHER"
  }
}
```

## 2. Operaciones de Docente

### Cargar Archivo CSV
**POST** `/api/teacher/upload-csv`
**Content-Type:** `multipart/form-data`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

**Form Data:**
- `file`: archivo CSV (ej: datos2.csv)
- `courseCode`: "PROG101"
- `courseName`: "Programación I"
- `description`: "Curso introductorio de programación"

**Respuesta:**
```json
{
  "message": "Archivo CSV procesado exitosamente",
  "courseId": "456e7890-e89b-12d3-a456-426614174001",
  "courseName": "Programación I",
  "totalStudents": 58,
  "totalExercises": 55,
  "errors": [],
  "success": true
}
```

### Obtener Cursos del Docente
**GET** `/api/teacher/courses`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

**Respuesta:**
```json
[
  {
    "id": "456e7890-e89b-12d3-a456-426614174001",
    "name": "Programación I",
    "description": "Curso introductorio de programación",
    "courseCode": "PROG101",
    "teacherName": "Profesor García",
    "createdAt": "2024-01-15T10:30:00",
    "isActive": true,
    "totalStudents": 58,
    "totalExercises": 55
  }
]
```

### Obtener Estadísticas de un Curso
**GET** `/api/teacher/courses/{courseId}/statistics`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

**Respuesta:**
```json
{
  "courseId": "456e7890-e89b-12d3-a456-426614174001",
  "courseName": "Programación I",
  "totalStudents": 58,
  "totalExercises": 55,
  "correctSubmissions": 1200,
  "incorrectSubmissions": 300,
  "pendingSubmissions": 50,
  "notSubmittedCount": 200,
  "averageScore": 85.5,
  "exerciseStatistics": [
    {
      "exerciseName": "Ejercicio 1- La misión meteorológica",
      "totalSubmissions": 58,
      "correctSubmissions": 45,
      "incorrectSubmissions": 10,
      "pendingSubmissions": 2,
      "notSubmittedCount": 1,
      "averageScore": 88.2
    }
  ],
  "studentPerformance": [
    {
      "studentName": "Andrea Valentina Gutierrez Correa",
      "studentEmail": "andrea.valentina.gutierrez.correa@estudiante.com",
      "totalExercises": 55,
      "correctCount": 50,
      "incorrectCount": 3,
      "pendingCount": 1,
      "notSubmittedCount": 1,
      "averageScore": 95.2,
      "completionPercentage": 98.2
    }
  ]
}
```

## 3. Operaciones de Estudiante

### Obtener Cursos del Estudiante
**GET** `/api/student/courses`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

**Respuesta:**
```json
{
  "studentId": "789e0123-e89b-12d3-a456-426614174002",
  "studentName": "Ana Estudiante",
  "studentEmail": "ana@estudiante.com",
  "courses": [
    {
      "courseId": "456e7890-e89b-12d3-a456-426614174001",
      "courseName": "Programación I",
      "courseCode": "PROG101",
      "teacherName": "Profesor García",
      "enrolledAt": "2024-01-15T10:30:00",
      "totalExercises": 55,
      "completedExercises": 50,
      "averageScore": 85.5,
      "completionPercentage": 90.9
    }
  ]
}
```

### Obtener Calificaciones por Curso
**GET** `/api/student/courses/{courseId}/grades`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

**Respuesta:**
```json
{
  "studentId": "789e0123-e89b-12d3-a456-426614174002",
  "studentName": "Ana Estudiante",
  "studentEmail": "ana@estudiante.com",
  "courseId": "456e7890-e89b-12d3-a456-426614174001",
  "courseName": "Programación I",
  "totalExercises": 55,
  "correctCount": 50,
  "incorrectCount": 3,
  "pendingCount": 1,
  "notSubmittedCount": 1,
  "averageScore": 85.5,
  "completionPercentage": 98.2,
  "exerciseGrades": [
    {
      "exerciseId": "111e2222-e89b-12d3-a456-426614174003",
      "exerciseName": "Ejercicio 1- La misión meteorológica",
      "score": 100,
      "status": "CORRECT",
      "statusDescription": "Correcto",
      "submittedAt": "2024-01-15T14:30:00",
      "maxScore": 100
    },
    {
      "exerciseId": "222e3333-e89b-12d3-a456-426614174004",
      "exerciseName": "Ejercicio 2- El Jardín Circular",
      "score": 80,
      "status": "CORRECT",
      "statusDescription": "Correcto",
      "submittedAt": "2024-01-16T09:15:00",
      "maxScore": 100
    },
    {
      "exerciseId": "333e4444-e89b-12d3-a456-426614174005",
      "exerciseName": "Ejercicio 3- Viaje",
      "score": null,
      "status": "NOT_SUBMITTED",
      "statusDescription": "No enviado",
      "submittedAt": null,
      "maxScore": 100
    }
  ]
}
```

### Obtener Todas las Calificaciones
**GET** `/api/student/grades`
**Headers:** `Authorization: Bearer YOUR_JWT_TOKEN`

## 4. Manejo de Errores

### Error de Autenticación
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Token JWT inválido o expirado"
}
```

### Error de Validación CSV
```json
{
  "message": "Error procesando archivo CSV",
  "errors": [
    "El archivo debe ser un CSV válido",
    "El archivo CSV está vacío"
  ],
  "success": false
}
```

### Error de Recurso No Encontrado
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Curso no encontrado"
}
```

## 5. Códigos de Estado HTTP

- **200 OK**: Operación exitosa
- **201 Created**: Recurso creado exitosamente
- **400 Bad Request**: Error en la petición
- **401 Unauthorized**: No autenticado
- **403 Forbidden**: Sin permisos
- **404 Not Found**: Recurso no encontrado
- **500 Internal Server Error**: Error interno del servidor

## 6. Headers Requeridos

### Para todas las peticiones autenticadas:
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

### Para carga de archivos:
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: multipart/form-data
```

## 7. Ejemplo de Archivo CSV

```csv
Student Name,Ejercicio 1,Ejercicio 2,Ejercicio 3
Ana Estudiante,100,80,Not Submitted
Juan Pérez,90,Not Submitted,100
María García,85,95,90
```

## 8. Testing con Postman

### Colección de Postman
1. Importar la colección desde el archivo `postman_collection.json`
2. Configurar la variable `{{baseUrl}}` como `http://localhost:8080`
3. Configurar la variable `{{token}}` después del login
4. Ejecutar las peticiones en orden

### Variables de Entorno
- `baseUrl`: `http://localhost:8080`
- `token`: Token JWT obtenido del login
- `courseId`: ID del curso (se obtiene al crear)
- `studentId`: ID del estudiante

## 9. Ejemplos de Uso con Herramientas

### Con Postman/Insomnia
1. **Configurar Headers:**
   - `Authorization: Bearer YOUR_JWT_TOKEN`
   - `Content-Type: application/json`

2. **Para carga de CSV:**
   - `Authorization: Bearer YOUR_JWT_TOKEN`
   - `Content-Type: multipart/form-data`
   - Body → form-data → agregar campos

### Con JavaScript/Fetch
```javascript
// Login
const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    email: 'profesor@universidad.com',
    password: 'password123'
  })
});

const { token } = await loginResponse.json();

// Obtener cursos
const coursesResponse = await fetch('http://localhost:8080/api/teacher/courses', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```

### Con Python/Requests
```python
import requests

# Login
login_data = {
    "email": "profesor@universidad.com",
    "password": "password123"
}
response = requests.post('http://localhost:8080/api/auth/login', json=login_data)
token = response.json()['token']

# Obtener cursos
headers = {
    'Authorization': f'Bearer {token}',
    'Content-Type': 'application/json'
}
courses = requests.get('http://localhost:8080/api/teacher/courses', headers=headers)
```
