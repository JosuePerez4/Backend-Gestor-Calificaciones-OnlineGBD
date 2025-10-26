package gestor.calificaciones.gestorcalificaciones.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestor.calificaciones.gestorcalificaciones.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, UUID>{
    
    @Query("SELECT s FROM Student s WHERE s.email = :email")
    Optional<Student> findByEmail(@Param("email") String email);
}
