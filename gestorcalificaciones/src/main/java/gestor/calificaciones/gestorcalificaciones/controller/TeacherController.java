package gestor.calificaciones.gestorcalificaciones.controller;

import gestor.calificaciones.gestorcalificaciones.DTO.CSV.CsvUploadRequest;
import gestor.calificaciones.gestorcalificaciones.DTO.CSV.CsvUploadResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.CourseResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.CourseStatisticsResponse;
import gestor.calificaciones.gestorcalificaciones.entities.User;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;
import gestor.calificaciones.gestorcalificaciones.enums.Role;
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

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Slf4j
public class TeacherController {

    private final CsvProcessingService csvProcessingService;
    private final CourseStatisticsService courseStatisticsService;
    private final TeacherRepository teacherRepository;

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("No autenticado");
        }
        return ResponseEntity.ok("Autenticado como: " + authentication.getName() + " - Roles: " + authentication.getAuthorities());
    }

    @GetMapping("/debug-teacher")
    public ResponseEntity<String> debugTeacher(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("No autenticado");
        }
        
        try {
            UUID teacherId = UUID.fromString(authentication.getName());
            log.info("DEBUG: TeacherController - Verificando profesor con ID: {}", teacherId);
            
            var teacherOptional = teacherRepository.findById(teacherId);
            if (teacherOptional.isPresent()) {
                var teacher = teacherOptional.get();
                return ResponseEntity.ok("Profesor encontrado: " + teacher.getName() + " - Email: " + teacher.getEmail());
            } else {
                return ResponseEntity.ok("Profesor NO encontrado con ID: " + teacherId);
            }
        } catch (Exception e) {
            return ResponseEntity.ok("Error: " + e.getMessage());
        }
    }

    @PostMapping("/migrate-to-teacher")
    public ResponseEntity<String> migrateToTeacher(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.ok("No autenticado");
        }
        
        try {
            UUID userId = UUID.fromString(authentication.getName());
            log.info("DEBUG: TeacherController - Migrando usuario a Teacher con ID: {}", userId);
            
            // Verificar si ya es Teacher
            var teacherOptional = teacherRepository.findById(userId);
            if (teacherOptional.isPresent()) {
                return ResponseEntity.ok("El usuario ya es Teacher: " + teacherOptional.get().getName());
            }
            
            return ResponseEntity.ok("Migración no necesaria. El usuario ya es Teacher o no existe.");
            
        } catch (Exception e) {
            return ResponseEntity.ok("Error en migración: " + e.getMessage());
        }
    }

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
