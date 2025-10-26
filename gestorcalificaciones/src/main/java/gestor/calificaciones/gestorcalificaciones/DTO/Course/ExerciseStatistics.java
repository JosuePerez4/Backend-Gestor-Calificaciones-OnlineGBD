package gestor.calificaciones.gestorcalificaciones.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseStatistics {
    private String exerciseName;
    private int totalSubmissions;
    private int correctSubmissions;
    private int incorrectSubmissions;
    private int pendingSubmissions;
    private int notSubmittedCount;
    private double averageScore;
}
