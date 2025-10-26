package gestor.calificaciones.gestorcalificaciones.DTO.Course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResponse {
    private String id;
    private String name;
    private String description;
    private String courseCode;
    private String teacherName;
    private LocalDateTime createdAt;
    private boolean isActive;
    private int totalStudents;
    private int totalExercises;
}
