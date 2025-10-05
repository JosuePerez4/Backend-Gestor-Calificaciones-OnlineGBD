package gestor.calificaciones.gestorcalificaciones.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import gestor.calificaciones.gestorcalificaciones.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>{
    
}
