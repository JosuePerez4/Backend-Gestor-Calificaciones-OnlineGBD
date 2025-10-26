package gestor.calificaciones.gestorcalificaciones.DTO.Student;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseSummary {
    private String courseId;
    private String courseName;
    private String courseCode;
    private String teacherName;
    private LocalDateTime enrolledAt;
    private int totalExercises;
    private int completedExercises;
    private double averageScore;
    private double completionPercentage;
}
