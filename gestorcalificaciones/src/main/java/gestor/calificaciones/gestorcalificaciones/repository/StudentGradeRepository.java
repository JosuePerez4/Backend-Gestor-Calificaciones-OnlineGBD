package gestor.calificaciones.gestorcalificaciones.repository;

import gestor.calificaciones.gestorcalificaciones.entities.Student;
import gestor.calificaciones.gestorcalificaciones.entities.StudentGrade;
import gestor.calificaciones.gestorcalificaciones.enums.GradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentGradeRepository extends JpaRepository<StudentGrade, UUID> {
    
    List<StudentGrade> findByStudent(Student student);
    
    @Query("SELECT sg FROM StudentGrade sg WHERE sg.student.id = :studentId")
    List<StudentGrade> findByStudentId(@Param("studentId") UUID studentId);
    
    @Query("SELECT sg FROM StudentGrade sg WHERE sg.student.id = :studentId AND sg.exercise.course.id = :courseId")
    List<StudentGrade> findByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);
    
    @Query("SELECT sg FROM StudentGrade sg WHERE sg.exercise.course.id = :courseId")
    List<StudentGrade> findByCourseId(@Param("courseId") UUID courseId);
    
    @Query("SELECT sg FROM StudentGrade sg WHERE sg.student.id = :studentId AND sg.status = :status")
    List<StudentGrade> findByStudentIdAndStatus(@Param("studentId") UUID studentId, @Param("status") GradeStatus status);
    
    Optional<StudentGrade> findByStudentIdAndExerciseId(UUID studentId, UUID exerciseId);
    
    @Query("SELECT COUNT(sg) FROM StudentGrade sg WHERE sg.exercise.course.id = :courseId AND sg.status = :status")
    Long countByCourseIdAndStatus(@Param("courseId") UUID courseId, @Param("status") GradeStatus status);
    
    @Query("SELECT AVG(sg.score) FROM StudentGrade sg WHERE sg.exercise.course.id = :courseId AND sg.status = 'CORRECT'")
    Double getAverageScoreByCourseId(@Param("courseId") UUID courseId);
}
