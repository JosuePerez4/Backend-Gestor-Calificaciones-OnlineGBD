package gestor.calificaciones.gestorcalificaciones.service;

import gestor.calificaciones.gestorcalificaciones.DTO.Student.StudentCourseResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Student.StudentGradeResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.Student.CourseSummary;
import gestor.calificaciones.gestorcalificaciones.DTO.Student.ExerciseGrade;
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
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentGradeRepository studentGradeRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final ExerciseRepository exerciseRepository;

    public StudentCourseResponse getStudentCourses(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        List<StudentCourse> studentCourses = studentCourseRepository.findActiveByStudentId(studentId);
        
        List<CourseSummary> courseSummaries = studentCourses.stream()
                .map(this::mapToCourseSummary)
                .collect(Collectors.toList());
        
        return StudentCourseResponse.builder()
                .studentId(student.getId().toString())
                .studentName(student.getName())
                .studentEmail(student.getEmail())
                .courses(courseSummaries)
                .build();
    }

    public StudentGradeResponse getStudentGrades(UUID studentId, UUID courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        Course course = studentCourseRepository.findByStudentIdAndCourseId(studentId, courseId)
                .map(StudentCourse::getCourse)
                .orElseThrow(() -> new RuntimeException("El estudiante no está inscrito en este curso"));
        
        List<StudentGrade> studentGrades = studentGradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        List<Exercise> exercises = exerciseRepository.findActiveExercisesByCourseId(courseId);
        
        // Calcular estadísticas
        int totalExercises = exercises.size();
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
        
        double averageScore = studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.CORRECT && grade.getScore() != null)
                .mapToInt(StudentGrade::getScore)
                .average()
                .orElse(0.0);
        
        double completionPercentage = totalExercises > 0 ? 
                ((double) (correctCount + incorrectCount + pendingCount) / totalExercises) * 100 : 0.0;
        
        // Mapear calificaciones de ejercicios
        List<ExerciseGrade> exerciseGrades = exercises.stream()
                .map(exercise -> mapToExerciseGrade(exercise, studentGrades))
                .collect(Collectors.toList());
        
        return StudentGradeResponse.builder()
                .studentId(student.getId().toString())
                .studentName(student.getName())
                .studentEmail(student.getEmail())
                .courseId(course.getId().toString())
                .courseName(course.getName())
                .totalExercises(totalExercises)
                .correctCount(correctCount)
                .incorrectCount(incorrectCount)
                .pendingCount(pendingCount)
                .notSubmittedCount(notSubmittedCount)
                .averageScore(averageScore)
                .completionPercentage(completionPercentage)
                .exerciseGrades(exerciseGrades)
                .build();
    }

    public List<StudentGradeResponse> getAllStudentGrades(UUID studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        
        List<StudentCourse> studentCourses = studentCourseRepository.findActiveByStudentId(studentId);
        
        return studentCourses.stream()
                .map(studentCourse -> getStudentGrades(studentId, studentCourse.getCourse().getId()))
                .collect(Collectors.toList());
    }

    private CourseSummary mapToCourseSummary(StudentCourse studentCourse) {
        Course course = studentCourse.getCourse();
        List<StudentGrade> studentGrades = studentGradeRepository
                .findByStudentIdAndCourseId(studentCourse.getStudent().getId(), course.getId());
        
        int totalExercises = exerciseRepository.findActiveExercisesByCourseId(course.getId()).size();
        int completedExercises = (int) studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.CORRECT || grade.getStatus() == GradeStatus.INCORRECT)
                .count();
        
        double averageScore = studentGrades.stream()
                .filter(grade -> grade.getStatus() == GradeStatus.CORRECT && grade.getScore() != null)
                .mapToInt(StudentGrade::getScore)
                .average()
                .orElse(0.0);
        
        double completionPercentage = totalExercises > 0 ? 
                ((double) completedExercises / totalExercises) * 100 : 0.0;
        
        return new CourseSummary(
                course.getId().toString(),
                course.getName(),
                course.getCourseCode(),
                course.getTeacher().getName(),
                studentCourse.getEnrolledAt(),
                totalExercises,
                completedExercises,
                averageScore,
                completionPercentage
        );
    }

    private ExerciseGrade mapToExerciseGrade(Exercise exercise, List<StudentGrade> studentGrades) {
        StudentGrade studentGrade = studentGrades.stream()
                .filter(grade -> grade.getExercise().getId().equals(exercise.getId()))
                .findFirst()
                .orElse(null);
        
        if (studentGrade == null) {
            return new ExerciseGrade(
                    exercise.getId().toString(),
                    exercise.getName(),
                    null,
                    GradeStatus.NOT_SUBMITTED.name(),
                    GradeStatus.NOT_SUBMITTED.getDescription(),
                    null,
                    exercise.getMaxScore()
            );
        }
        
        return new ExerciseGrade(
                exercise.getId().toString(),
                exercise.getName(),
                studentGrade.getScore(),
                studentGrade.getStatus().name(),
                studentGrade.getStatus().getDescription(),
                studentGrade.getSubmittedAt(),
                exercise.getMaxScore()
        );
    }
}
