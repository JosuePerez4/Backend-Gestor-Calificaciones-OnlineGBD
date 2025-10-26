package gestor.calificaciones.gestorcalificaciones.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseStatisticsResponse {
    private String courseId;
    private String courseName;
    private int totalStudents;
    private int totalExercises;
    private int correctSubmissions;
    private int incorrectSubmissions;
    private int pendingSubmissions;
    private int notSubmittedCount;
    private double averageScore;
    private List<ExerciseStatistics> exerciseStatistics;
    private List<StudentPerformance> studentPerformance;
}

