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
public class StudentCourseResponse {
    private String studentId;
    private String studentName;
    private String studentEmail;
    private List<CourseSummary> courses;
}

