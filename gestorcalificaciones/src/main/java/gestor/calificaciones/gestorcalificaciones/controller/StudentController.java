package gestor.calificaciones.gestorcalificaciones.controller;

import gestor.calificaciones.gestorcalificaciones.DTO.Student.StudentCourseResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Student.StudentGradeResponse;
import gestor.calificaciones.gestorcalificaciones.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST para operaciones relacionadas con estudiantes.
 * 
 * <p>Este controlador maneja todas las operaciones que un estudiante
 * puede realizar, incluyendo la visualización de sus cursos y calificaciones.</p>
 * 
 * @author Sistema de Gestión de Calificaciones
 * @version 1.0
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /**
     * Obtiene todos los cursos en los que el estudiante está inscrito.
     * 
     * <p>Retorna una lista completa de todos los cursos activos en los que
     * el estudiante autenticado está matriculado, incluyendo información
     * sobre el progreso, calificaciones promedio y porcentaje de completitud
     * de cada curso.</p>
     * 
     * @param authentication Información de autenticación del estudiante
     * @return Respuesta con la lista de cursos del estudiante y sus estadísticas
     * 
     * @apiNote Requiere autenticación como STUDENT
     * 
     * @response 200 OK - Lista de cursos obtenida exitosamente
     * @response 401 Unauthorized - No autenticado
     * @response 500 Internal Server Error - Error al obtener los cursos
     */
    @GetMapping("/courses")
    public ResponseEntity<StudentCourseResponse> getStudentCourses(Authentication authentication) {
        try {
            UUID studentId = UUID.fromString(authentication.getName());
            StudentCourseResponse courses = studentService.getStudentCourses(studentId);
            return ResponseEntity.ok(courses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene todas las calificaciones del estudiante en todos sus cursos.
     * 
     * <p>Retorna un resumen completo de todas las calificaciones del estudiante
     * autenticado, agrupadas por curso. Incluye información detallada sobre
     * cada ejercicio, su estado (correcto, incorrecto, pendiente, no enviado),
     * puntuación y estadísticas generales.</p>
     * 
     * @param authentication Información de autenticación del estudiante
     * @return Lista de respuestas con calificaciones agrupadas por curso
     * 
     * @apiNote Requiere autenticación como STUDENT
     * 
     * @response 200 OK - Calificaciones obtenidas exitosamente
     * @response 401 Unauthorized - No autenticado
     * @response 500 Internal Server Error - Error al obtener las calificaciones
     */
    @GetMapping("/grades")
    public ResponseEntity<List<StudentGradeResponse>> getAllStudentGrades(Authentication authentication) {
        try {
            UUID studentId = UUID.fromString(authentication.getName());
            List<StudentGradeResponse> grades = studentService.getAllStudentGrades(studentId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtiene las calificaciones del estudiante para un curso específico.
     * 
     * <p>Retorna información detallada sobre las calificaciones del estudiante
     * autenticado en un curso en particular, incluyendo:</p>
     * <ul>
     *   <li>Lista de todos los ejercicios del curso</li>
     *   <li>Estado de cada ejercicio (correcto, incorrecto, pendiente, no enviado)</li>
     *   <li>Puntuación obtenida en cada ejercicio</li>
     *   <li>Estadísticas generales: total de ejercicios, correctos, incorrectos, etc.</li>
     *   <li>Promedio de calificaciones y porcentaje de completitud</li>
     * </ul>
     * 
     * @param courseId Identificador único del curso (UUID)
     * @param authentication Información de autenticación del estudiante
     * @return Respuesta con las calificaciones del estudiante en el curso especificado
     * 
     * @apiNote Requiere autenticación como STUDENT
     * @apiNote El estudiante debe estar inscrito en el curso
     * 
     * @response 200 OK - Calificaciones obtenidas exitosamente
     * @response 401 Unauthorized - No autenticado
     * @response 404 Not Found - Curso no encontrado o estudiante no inscrito
     * @response 500 Internal Server Error - Error al obtener las calificaciones
     */
    @GetMapping("/courses/{courseId}/grades")
    public ResponseEntity<StudentGradeResponse> getStudentGradesByCourse(
            @PathVariable UUID courseId, 
            Authentication authentication) {
        try {
            UUID studentId = UUID.fromString(authentication.getName());
            StudentGradeResponse grades = studentService.getStudentGrades(studentId, courseId);
            return ResponseEntity.ok(grades);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
