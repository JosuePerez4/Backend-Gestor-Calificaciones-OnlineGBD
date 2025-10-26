package gestor.calificaciones.gestorcalificaciones.repository;

import gestor.calificaciones.gestorcalificaciones.entities.Course;
import gestor.calificaciones.gestorcalificaciones.entities.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {
    
    List<Exercise> findByCourseAndIsActiveTrue(Course course);
    
    @Query("SELECT e FROM Exercise e WHERE e.course.id = :courseId AND e.isActive = true")
    List<Exercise> findActiveExercisesByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT e FROM Exercise e WHERE e.course.id = :courseId AND e.name = :exerciseName")
    Optional<Exercise> findByCourseIdAndName(@Param("courseId") UUID courseId, @Param("exerciseName") String exerciseName);
}
