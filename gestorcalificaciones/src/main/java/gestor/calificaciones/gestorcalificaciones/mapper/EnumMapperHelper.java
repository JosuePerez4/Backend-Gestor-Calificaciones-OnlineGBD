package gestor.calificaciones.gestorcalificaciones.mapper;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import gestor.calificaciones.gestorcalificaciones.enums.Role;

@Component
public class EnumMapperHelper {

    @Named("enumToString")
    public static String enumToString(Role e) {
        return e != null ? e.name() : null;
    }

    @Named("stringToEnum")
    public static Role stringToEnum(String role) {
        return role != null ? Role.valueOf(role) : null;
    }
}
