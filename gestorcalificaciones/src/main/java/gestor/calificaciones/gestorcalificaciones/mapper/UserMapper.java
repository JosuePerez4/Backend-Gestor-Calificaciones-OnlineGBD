package gestor.calificaciones.gestorcalificaciones.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gestor.calificaciones.gestorcalificaciones.DTO.User.RegisterRequest;
import gestor.calificaciones.gestorcalificaciones.entities.Student;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "studentCourses", ignore = true)
    @Mapping(target = "studentGrades", ignore = true)
    Student toStudent(RegisterRequest request);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "courses", ignore = true)
    Teacher toTeacher(RegisterRequest request);
}
