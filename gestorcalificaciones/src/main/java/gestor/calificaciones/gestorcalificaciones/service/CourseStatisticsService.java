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
        
        List<Exercise> exercises = exerciseRepository.findActiveExercisesByCourseId(courseId);
        List<StudentCourse> studentCourses = studentCourseRepository.findActiveByCourseId(courseId);
        
        // Calcular estadísticas generales
        int totalStudents = studentCourses.size();
        int totalExercises = exercises.size();
        
        // OPTIMIZADO: Contar ejercicios únicos con al menos una entrega correcta/incorrecta
        // En lugar de contar todas las entregas, contamos cuántos ejercicios tienen al menos una entrega con ese status
        int correctSubmissions = studentGradeRepository
                .countDistinctExercisesByCourseIdAndStatus(courseId, GradeStatus.CORRECT)
                .intValue();
        int incorrectSubmissions = studentGradeRepository
                .countDistinctExercisesByCourseIdAndStatus(courseId, GradeStatus.INCORRECT)
                .intValue();
        
        // Para pending y not_submitted mantenemos el conteo total ya que son estados diferentes
        int pendingSubmissions = studentGradeRepository
                .countByCourseIdAndStatus(courseId, GradeStatus.PENDING)
                .intValue();
        int notSubmittedCount = studentGradeRepository
                .countByCourseIdAndStatus(courseId, GradeStatus.NOT_SUBMITTED)
                .intValue();
        
        // OPTIMIZADO: Calcular promedio usando consulta directa en BD
        Double avgScore = studentGradeRepository.getAverageScoreByCourseIdOptimized(courseId);
        double averageScore = avgScore != null ? avgScore : 0.0;
        
        // OPTIMIZADO: Cargar calificaciones con fetch join para evitar problema N+1
        List<StudentGrade> allGrades = studentGradeRepository.findByCourseIdWithFetch(courseId);
        
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
        // OPTIMIZADO: Filtrar una sola vez y calcular todas las estadísticas en una sola pasada
        UUID exerciseId = exercise.getId();
        int[] counts = new int[5]; // [total, correct, incorrect, pending, notSubmitted]
        int[] scoreSum = new int[1];
        int[] scoreCount = new int[1];
        
        allGrades.stream()
                .filter(grade -> grade.getExercise().getId().equals(exerciseId))
                .forEach(grade -> {
                    counts[0]++; // total
                    switch (grade.getStatus()) {
                        case CORRECT -> counts[1]++;
                        case INCORRECT -> counts[2]++;
                        case PENDING -> counts[3]++;
                        case NOT_SUBMITTED -> counts[4]++;
                    }
                    if (grade.getScore() != null) {
                        scoreSum[0] += grade.getScore();
                        scoreCount[0]++;
                    }
                });
        
        double averageScore = scoreCount[0] > 0 ? (double) scoreSum[0] / scoreCount[0] : 0.0;
        
        return new ExerciseStatistics(
                exercise.getName(),
                counts[0], // totalSubmissions
                counts[1], // correctSubmissions
                counts[2], // incorrectSubmissions
                counts[3], // pendingSubmissions
                counts[4], // notSubmittedCount
                averageScore
        );
    }

    private StudentPerformance calculateStudentPerformance(StudentCourse studentCourse, List<StudentGrade> allGrades, int totalExercises) {
        // OPTIMIZADO: Calcular todas las estadísticas en una sola pasada
        UUID studentId = studentCourse.getStudent().getId();
        int[] counts = new int[4]; // [correct, incorrect, pending, notSubmitted]
        int[] scoreSum = new int[1];
        int[] scoreCount = new int[1];
        
        allGrades.stream()
                .filter(grade -> grade.getStudent().getId().equals(studentId))
                .forEach(grade -> {
                    switch (grade.getStatus()) {
                        case CORRECT -> counts[0]++;
                        case INCORRECT -> counts[1]++;
                        case PENDING -> counts[2]++;
                        case NOT_SUBMITTED -> counts[3]++;
                    }
                    if (grade.getScore() != null) {
                        scoreSum[0] += grade.getScore();
                        scoreCount[0]++;
                    }
                });
        
        double averageScore = scoreCount[0] > 0 ? (double) scoreSum[0] / scoreCount[0] : 0.0;
        double completionPercentage = totalExercises > 0 ? 
                ((double) (counts[0] + counts[1] + counts[2]) / totalExercises) * 100 : 0.0;
        
        return new StudentPerformance(
                studentCourse.getStudent().getName(),
                studentCourse.getStudent().getEmail(),
                totalExercises,
                counts[0], // correctCount
                counts[1], // incorrectCount
                counts[2], // pendingCount
                counts[3], // notSubmittedCount
                averageScore,
                completionPercentage
        );
    }
}
