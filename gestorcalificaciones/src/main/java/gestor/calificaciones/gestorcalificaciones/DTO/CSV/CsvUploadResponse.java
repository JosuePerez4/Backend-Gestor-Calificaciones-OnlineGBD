package gestor.calificaciones.gestorcalificaciones.DTO.CSV;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvUploadResponse {
    private String message;
    private String courseId;
    private String courseName;
    private int totalStudents;
    private int totalExercises;
    private List<String> errors;
    private boolean success;
}
