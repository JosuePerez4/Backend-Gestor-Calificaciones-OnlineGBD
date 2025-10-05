package gestor.calificaciones.gestorcalificaciones.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import gestor.calificaciones.gestorcalificaciones.DTO.User.RegisterRequest;
import gestor.calificaciones.gestorcalificaciones.entities.User;

@Mapper(componentModel = "spring", uses = { PasswordMapperHelper.class, EnumMapperHelper.class })
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", source = "password", qualifiedByName = "encodePassword")
    @Mapping(target = "role", source = "role", qualifiedByName = "stringToEnum")
    User toUserEntity(RegisterRequest request);
}