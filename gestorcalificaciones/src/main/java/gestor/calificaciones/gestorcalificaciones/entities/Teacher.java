package gestor.calificaciones.gestorcalificaciones.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@DiscriminatorValue("TEACHER")
public class Teacher extends User {
    
    @OneToMany(mappedBy = "teacher", cascade = CascadeType.ALL, fetch = jakarta.persistence.FetchType.LAZY)
    private List<gestor.calificaciones.gestorcalificaciones.entities.Course> courses;
}
