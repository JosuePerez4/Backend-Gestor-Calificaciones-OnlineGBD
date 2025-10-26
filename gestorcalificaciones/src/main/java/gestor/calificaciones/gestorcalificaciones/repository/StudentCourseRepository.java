package gestor.calificaciones.gestorcalificaciones.repository;

import gestor.calificaciones.gestorcalificaciones.entities.Course;
import gestor.calificaciones.gestorcalificaciones.entities.Student;
import gestor.calificaciones.gestorcalificaciones.entities.StudentCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, UUID> {
    
    List<StudentCourse> findByStudentAndIsActiveTrue(Student student);
    
    List<StudentCourse> findByCourseAndIsActiveTrue(Course course);
    
    @Query("SELECT sc FROM StudentCourse sc WHERE sc.student.id = :studentId AND sc.isActive = true")
    List<StudentCourse> findActiveByStudentId(@Param("studentId") UUID studentId);
    
    @Query("SELECT sc FROM StudentCourse sc WHERE sc.course.id = :courseId AND sc.isActive = true")
    List<StudentCourse> findActiveByCourseId(@Param("courseId") UUID courseId);
    
    Optional<StudentCourse> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
    
    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
