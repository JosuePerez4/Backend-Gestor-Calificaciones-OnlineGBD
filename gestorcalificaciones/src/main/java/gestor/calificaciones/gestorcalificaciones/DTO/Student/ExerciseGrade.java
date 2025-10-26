package gestor.calificaciones.gestorcalificaciones.DTO.Student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseGrade {
    private String exerciseId;
    private String exerciseName;
    private Integer score;
    private String status;
    private String statusDescription;
    private LocalDateTime submittedAt;
    private int maxScore;
}
