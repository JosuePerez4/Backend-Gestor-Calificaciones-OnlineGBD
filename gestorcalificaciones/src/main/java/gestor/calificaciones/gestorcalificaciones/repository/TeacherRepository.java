package gestor.calificaciones.gestorcalificaciones.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import gestor.calificaciones.gestorcalificaciones.entities.Teacher;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, UUID>{
    
    @Query("SELECT t FROM Teacher t WHERE t.email = :email")
    Optional<Teacher> findByEmail(@Param("email") String email);
}
