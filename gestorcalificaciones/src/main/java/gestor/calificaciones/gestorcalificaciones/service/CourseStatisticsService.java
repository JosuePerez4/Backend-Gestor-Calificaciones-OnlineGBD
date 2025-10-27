package gestor.calificaciones.gestorcalificaciones.service;

import gestor.calificaciones.gestorcalificaciones.DTO.Course.CourseResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.CourseStatisticsResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.ExerciseStatistics;
import gestor.calificaciones.gestorcalificaciones.DTO.Course.StudentPerformance;
import gestor.calificaciones.gestorcalificaciones.entities.*;
import gestor.calificaciones.gestorcalificaciones.enums.GradeStatus;
import gestor.calificaciones.gestorcalificaciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseStatisticsService {

    private final CourseRepository courseRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final ExerciseRepository exerciseRepository;

    public List<CourseResponse> getTeacherCourses(UUID teacherId) {
        List<Course> courses = courseRepository.findActiveCoursesByTeacherId(teacherId);
        
        return courses.stream()
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    public CourseResponse getCourseDetails(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        return mapToCourseResponse(course);
    }

    public CourseStatisticsResponse getCourseStatistics(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Curso no encontrado"));
        
        List<StudentGrade> allGrades = studentGradeRepository.findByCourseId(courseId);
        List<Exercise> exercises = exerciseRepository.findActiveExercisesByCourseId(courseId);
        List<StudentCourse> studentCourses = studentCourseRepository.findActiveByCourseId(courseId);
        
        // Calcular estadísticas generales
        int totalStudents = studentCourses.size();
        int totalExercises = exercises.size();
        int correctSubmissions = (int) allGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.CORRECT)
                .count();
        int incorrectSubmissions = (int) allGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.INCORRECT)
                .count();
        int pendingSubmissions = (int) allGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.PENDING)
                .count();
        int notSubmittedCount = (int) allGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.NOT_SUBMITTED)
                .count();
        
        // Calcular promedio de TODAS las calificaciones con score (tanto CORRECT como INCORRECT)
        double averageScore = allGrades.stream()
                .filter(grade -> grade.getScore() != null) // Incluir todas las calificaciones con score
                .mapToInt(StudentGrade::getScore)
                .average()
                .orElse(0.0);
        
        // Estadísticas por ejercicio
        List<ExerciseStatistics> exerciseStats = exercises.stream()
                .map(exercise -> calculateExerciseStatistics(exercise, allGrades))
                .collect(Collectors.toList());
        
        // Rendimiento por estudiante
        List<StudentPerformance> studentPerformance = studentCourses.stream()
                .map(studentCourse -> calculateStudentPerformance(studentCourse, allGrades, totalExercises))
                .collect(Collectors.toList());
        
        return CourseStatisticsResponse.builder()
                .courseId(course.getId().toString())
                .courseName(course.getName())
                .totalStudents(totalStudents)
                .totalExercises(totalExercises)
                .correctSubmissions(correctSubmissions)
                .incorrectSubmissions(incorrectSubmissions)
                .pendingSubmissions(pendingSubmissions)
                .notSubmittedCount(notSubmittedCount)
                .averageScore(averageScore)
                .exerciseStatistics(exerciseStats)
                .studentPerformance(studentPerformance)
                .build();
    }

    private CourseResponse mapToCourseResponse(Course course) {
        int totalStudents = studentCourseRepository.findActiveByCourseId(course.getId()).size();
        int totalExercises = exerciseRepository.findActiveExercisesByCourseId(course.getId()).size();
        
        return CourseResponse.builder()
                .id(course.getId().toString())
                .name(course.getName())
                .description(course.getDescription())
                .courseCode(course.getCourseCode())
                .teacherName(course.getTeacher().getName())
                .createdAt(course.getCreatedAt())
                .isActive(course.getIsActive())
                .totalStudents(totalStudents)
                .totalExercises(totalExercises)
                .build();
    }

    private ExerciseStatistics calculateExerciseStatistics(Exercise exercise, List<StudentGrade> allGrades) {
        List<StudentGrade> exerciseGrades = allGrades.stream()
                .filter(grade -> grade.getExercise().getId().equals(exercise.getId()))
                .collect(Collectors.toList());
        
        int totalSubmissions = exerciseGrades.size();
        int correctSubmissions = (int) exerciseGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.CORRECT)
                .count();
        int incorrectSubmissions = (int) exerciseGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.INCORRECT)
                .count();
        int pendingSubmissions = (int) exerciseGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.PENDING)
                .count();
        int notSubmittedCount = (int) exerciseGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.NOT_SUBMITTED)
                .count();
        
        // Calcular promedio de TODAS las calificaciones con score (tanto CORRECT como INCORRECT)
        double averageScore = exerciseGrades.stream()
                .filter(grade -> grade.getScore() != null) // Incluir todas las calificaciones con score
                .mapToInt(StudentGrade::getScore)
                .average()
                .orElse(0.0);
        
        return new ExerciseStatistics(
                exercise.getName(),
                totalSubmissions,
                correctSubmissions,
                incorrectSubmissions,
                pendingSubmissions,
                notSubmittedCount,
                averageScore
        );
    }

    private StudentPerformance calculateStudentPerformance(StudentCourse studentCourse, List<StudentGrade> allGrades, int totalExercises) {
        List<StudentGrade> studentGrades = allGrades.stream()
                .filter(grade -> grade.getStudent().getId().equals(studentCourse.getStudent().getId()))
                .collect(Collectors.toList());
        
        int correctCount = (int) studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.CORRECT)
                .count();
        int incorrectCount = (int) studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.INCORRECT)
                .count();
        int pendingCount = (int) studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.PENDING)
                .count();
        int notSubmittedCount = (int) studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.NOT_SUBMITTED)
                .count();
        
        // Calcular promedio de TODAS las calificaciones con score (tanto CORRECT como INCORRECT)
        double averageScore = studentGrades.stream()
                .filter(grade -> grade.getScore() != null) // Incluir todas las calificaciones con score
                .mapToInt(StudentGrade::getScore)
                .average()
                .orElse(0.0);
        
        double completionPercentage = totalExercises > 0 ? 
                ((double) (correctCount + incorrectCount + pendingCount) / totalExercises) * 100 : 0.0;
        
        return new StudentPerformance(
                studentCourse.getStudent().getName(),
                studentCourse.getStudent().getEmail(),
                totalExercises,
                correctCount,
                incorrectCount,
                pendingCount,
                notSubmittedCount,
                averageScore,
                completionPercentage
        );
    }
}
