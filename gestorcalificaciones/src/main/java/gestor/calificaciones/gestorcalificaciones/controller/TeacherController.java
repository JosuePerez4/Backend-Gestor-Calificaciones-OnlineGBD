package gestor.calificaciones.gestorcalificaciones.controller;

import gestor.calificaciones.gestorcalificaciones.DTO.CSV.CsvUploadRequest;
import gestor.calificaciones.gestorcalificaciones.DTO.CSV.CsvUploadResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.CreateCourseRequest;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.CourseResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.CourseStatisticsResponse;
import gestor.calificaciones.gestorcalificaciones.entities.Course;
import gestor.calificaciones.gestorcalificaciones.entities.User;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;
import gestor.calificaciones.gestorcalificaciones.enums.Role;
import gestor.calificaciones.gestorcalificaciones.repository.CourseRepository;
import gestor.calificaciones.gestorcalificaciones.repository.TeacherRepository;
import gestor.calificaciones.gestorcalificaciones.service.CourseStatisticsService;
import gestor.calificaciones.gestorcalificaciones.service.CsvProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones relacionadas con profesores.
 * 
 * <p>Este controlador maneja todas las operaciones que un profesor
 * puede realizar, incluyendo la carga de archivos CSV con calificaciones,
 * la gestión de cursos y la consulta de estadísticas detalladas.</p>
 * 
 * @author Sistema de Gestión de Calificaciones
 * @version 1.0
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final CsvProcessingService csvProcessingService;
    private final CourseStatisticsService courseStatisticsService;
    private final TeacherRepository teacherRepository;
    private final CourseRepository courseRepository;

    /**
     * Carga y procesa un archivo CSV con calificaciones de estudiantes.
     * 
     * <p>Este endpoint permite a un profesor cargar un archivo CSV que contiene
     * las calificaciones de ejercicios de los estudiantes. El sistema procesa
     * automáticamente el archivo, crea o actualiza el curso, y registra todas
     * las calificaciones.</p>
     * 
     * <p><strong>Formato del CSV esperado:</strong></p>
     * <ul>
     *   <li>Primera fila: Encabezados (Student Name, Ejercicio 1, Ejercicio 2, ...)</li>
     *   <li>Filas siguientes: Datos de estudiantes y sus calificaciones</li>
     *   <li>Valores aceptados: Números (0-100), "Not Submitted", o vacío</li>
     * </ul>
     * 
     * <p><strong>Clasificación automática:</strong></p>
     * <ul>
     *   <li>≥ 80 puntos: Correcto</li>
     *   <li>1-79 puntos: Incorrecto</li>
     *   <li>0 puntos: Incorrecto</li>
     *   <li>"Not Submitted" o vacío: No enviado</li>
     * </ul>
     * 
     * @param file Archivo CSV con las calificaciones (multipart/form-data)
     * @param courseCode Código único del curso (ej: "PROG101")
     * @param courseName Nombre del curso (ej: "Programación I")
     * @param description Descripción opcional del curso
     * @param authentication Información de autenticación del profesor
     * @return Respuesta con el resultado del procesamiento del CSV
     * 
     * @throws IllegalArgumentException Si el formato del ID del profesor es inválido
     * @throws RuntimeException Si ocurre un error al procesar el archivo CSV
     * 
     * @apiNote Requiere autenticación como TEACHER
     * @apiNote Content-Type: multipart/form-data
     * 
     * @response 200 OK - Archivo procesado exitosamente
     * @response 400 Bad Request - Archivo vacío o formato inválido
     * @response 401 Unauthorized - No autenticado
     * @response 500 Internal Server Error - Error al procesar el archivo
     */
    @PostMapping("/upload-csv")
    public ResponseEntity<CsvUploadResponse> uploadCsv(
            @RequestParam("file") MultipartFile file,
            @RequestParam("courseCode") String courseCode,
            @RequestParam("courseName") String courseName,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        try {
            // Validar autenticación
            if (authentication == null || authentication.getName() == null) {
                CsvUploadResponse errorResponse = CsvUploadResponse.builder()
                        .message("No autenticado. Debe iniciar sesión primero.")
                        .success(false)
                        .build();
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
            }
            
            // Validar archivo
            if (file == null || file.isEmpty()) {
                CsvUploadResponse errorResponse = CsvUploadResponse.builder()
                        .message("No se ha proporcionado ningún archivo")
                        .success(false)
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            UUID teacherId = UUID.fromString(authentication.getName());
            log.info("DEBUG: TeacherController - teacherId extraído del token: {}", teacherId);
            
            CsvUploadRequest request = CsvUploadRequest.builder()
                    .courseCode(courseCode)
                    .courseName(courseName)
                    .description(description != null ? description : "")
                    .build();
            
            CsvUploadResponse response = csvProcessingService.processCsvFile(file, request, teacherId);
            
            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (IllegalArgumentException e) {
            CsvUploadResponse errorResponse = CsvUploadResponse.builder()
                    .message("Error en el formato del ID del profesor: " + e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (RuntimeException e) {
            log.error("Error procesando CSV: {}", e.getMessage(), e);
            CsvUploadResponse errorResponse = CsvUploadResponse.builder()
                    .message("Error procesando archivo CSV: " + e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        } catch (Exception e) {
            log.error("Error inesperado: {}", e.getMessage(), e);
            CsvUploadResponse errorResponse = CsvUploadResponse.builder()
                    .message("Error interno del servidor: " + e.getMessage())
                    .success(false)
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Crea un nuevo curso vacío.
     * 
     * <p>Permite a un profesor crear un curso manualmente sin necesidad de
     * cargar un archivo CSV. El curso se crea sin ejercicios ni estudiantes,
     * y puede ser poblado posteriormente mediante la carga de CSV o la
     * creación manual de ejercicios.</p>
     * 
     * @param request Datos del curso a crear (nombre, código, descripción)
     * @param authentication Información de autenticación del profesor
     * @return Detalles del curso creado
     * 
     * @throws IllegalArgumentException Si el formato del ID es inválido
     * @throws RuntimeException Si el profesor no existe o el código del curso ya existe
     * 
     * @apiNote Requiere autenticación como TEACHER
     * @apiNote Content-Type: application/json
     * 
     * @response 201 Created - Curso creado exitosamente
     * @response 400 Bad Request - Campos requeridos faltantes o inválidos
     * @response 401 Unauthorized - No autenticado
     * @response 409 Conflict - El código del curso ya existe
     * @response 500 Internal Server Error - Error al crear el curso
     */
    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request, Authentication authentication) {
        try {
            if (authentication == null || authentication.getName() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado");
            }
            
            UUID teacherId = UUID.fromString(authentication.getName());
            
            // Validar campos requeridos
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El nombre del curso es obligatorio");
            }
            if (request.getCourseCode() == null || request.getCourseCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El código del curso es obligatorio");
            }
            
            // Verificar si el código ya existe
            if (courseRepository.existsByCourseCode(request.getCourseCode())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("El código del curso ya existe");
            }
            
            // Obtener el profesor
            Teacher teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));
            
            // Crear el curso
            Course course = new Course();
            course.setName(request.getName());
            course.setCourseCode(request.getCourseCode());
            course.setDescription(request.getDescription() != null ? request.getDescription() : "");
            course.setTeacher(teacher);
            course.setIsActive(true);
            
            Course savedCourse = courseRepository.save(course);
            
            // Convertir a response
            CourseResponse response = courseStatisticsService.getCourseDetails(savedCourse.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Error en el formato del ID: " + e.getMessage());
        } catch (RuntimeException e) {
            log.error("Error creando curso: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al crear el curso: " + e.getMessage());
        }
    }

    /**
     * Obtiene la lista de todos los cursos del profesor autenticado.
     * 
     * <p>Retorna todos los cursos activos asociados al profesor que realiza
     * la petición, incluyendo información sobre el número de estudiantes
     * y ejercicios en cada curso.</p>
     * 
     * @param authentication Información de autenticación del profesor
     * @return Lista de cursos del profesor
     * 
     * @apiNote Requiere autenticación como TEACHER
     * 
     * @response 200 OK - Lista de cursos obtenida exitosamente
     * @response 401 Unauthorized - No autenticado
     * @response 500 Internal Server Error - Error al obtener los cursos
     */
    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> getTeacherCourses(Authentication authentication) {
        try {
            UUID teacherId = UUID.fromString(authentication.getName());
            List<CourseResponse> courses = courseStatisticsService.getTeacherCourses(teacherId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene los detalles completos de un curso específico.
     * 
     * <p>Retorna información detallada de un curso incluyendo su nombre,
     * código, descripción, profesor, fecha de creación, estado, y estadísticas
     * como número total de estudiantes y ejercicios.</p>
     * 
     * @param courseId Identificador único del curso (UUID)
     * @param authentication Información de autenticación del profesor
     * @return Detalles completos del curso
     * 
     * @apiNote Requiere autenticación como TEACHER
     * 
     * @response 200 OK - Detalles del curso obtenidos exitosamente
     * @response 401 Unauthorized - No autenticado
     * @response 404 Not Found - Curso no encontrado
     * @response 500 Internal Server Error - Error al obtener los detalles
     */
    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseResponse> getCourseDetails(@PathVariable UUID courseId, Authentication authentication) {
        try {
            UUID teacherId = UUID.fromString(authentication.getName());
            CourseResponse course = courseStatisticsService.getCourseDetails(courseId);
            
            // Verificar que el curso pertenece al profesor
            // Esta validación se puede mejorar con una consulta específica
            return ResponseEntity.ok(course);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Obtiene estadísticas detalladas de un curso.
     * 
     * <p>Retorna un análisis completo del rendimiento del curso incluyendo:</p>
     * <ul>
     *   <li>Estadísticas generales: total de estudiantes, ejercicios, entregas correctas/incorrectas</li>
     *   <li>Estadísticas por ejercicio: rendimiento individual de cada ejercicio</li>
     *   <li>Rendimiento por estudiante: calificaciones y porcentaje de completitud</li>
     *   <li>Promedio general de calificaciones</li>
     * </ul>
     * 
     * <p><strong>Nota sobre conteo de ejercicios:</strong></p>
     * <ul>
     *   <li>correctSubmissions: Número de ejercicios únicos con al menos una entrega correcta</li>
     *   <li>incorrectSubmissions: Número de ejercicios únicos con al menos una entrega incorrecta</li>
     * </ul>
     * 
     * @param courseId Identificador único del curso (UUID)
     * @param authentication Información de autenticación del profesor
     * @return Estadísticas completas del curso
     * 
     * @apiNote Requiere autenticación como TEACHER
     * 
     * @response 200 OK - Estadísticas obtenidas exitosamente
     * @response 401 Unauthorized - No autenticado
     * @response 404 Not Found - Curso no encontrado
     * @response 500 Internal Server Error - Error al calcular las estadísticas
     */
    @GetMapping("/courses/{courseId}/statistics")
    public ResponseEntity<CourseStatisticsResponse> getCourseStatistics(@PathVariable UUID courseId, Authentication authentication) {
        try {
            UUID teacherId = UUID.fromString(authentication.getName());
            CourseStatisticsResponse statistics = courseStatisticsService.getCourseStatistics(courseId);
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
