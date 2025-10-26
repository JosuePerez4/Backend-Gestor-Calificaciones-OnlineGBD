package gestor.calificaciones.gestorcalificaciones.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPerformance {
    private String studentName;
    private String studentEmail;
    private int totalExercises;
    private int correctCount;
    private int incorrectCount;
    private int pendingCount;
    private int notSubmittedCount;
    private double averageScore;
    private double completionPercentage;
}
