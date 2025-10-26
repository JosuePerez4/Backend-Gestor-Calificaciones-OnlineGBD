package gestor.calificaciones.gestorcalificaciones.DTO.CSV;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CsvUploadRequest {
    private String courseCode;
    private String courseName;
    private String description;
}
