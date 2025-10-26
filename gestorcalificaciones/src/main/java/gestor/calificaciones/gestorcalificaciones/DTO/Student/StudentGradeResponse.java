package gestor.calificaciones.gestorcalificaciones.DTO.Student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentGradeResponse {
    private String studentId;
    private String studentName;
    private String studentEmail;
    private String courseId;
    private String courseName;
    private int totalExercises;
    private int correctCount;
    private int incorrectCount;
    private int pendingCount;
    private int notSubmittedCount;
    private double averageScore;
    private double completionPercentage;
    private List<ExerciseGrade> exerciseGrades;
}

