package gestor.calificaciones.gestorcalificaciones.repository;

import gestor.calificaciones.gestorcalificaciones.entities.Course;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {
    
    List<Course> findByTeacherAndIsActiveTrue(Teacher teacher);
    
    Optional<Course> findByCourseCode(String courseCode);
    
    @Query("SELECT c FROM Course c WHERE c.teacher.id = :teacherId AND c.isActive = true")
    List<Course> findActiveCoursesByTeacherId(@Param("teacherId") UUID teacherId);
    
    boolean existsByCourseCode(String courseCode);
}
