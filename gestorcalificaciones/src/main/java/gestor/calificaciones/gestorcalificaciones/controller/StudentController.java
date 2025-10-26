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

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

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
