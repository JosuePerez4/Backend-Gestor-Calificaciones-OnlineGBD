package gestor.calificaciones.gestorcalificaciones.mapper;

import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordMapperHelper {
    
    public static PasswordEncoder encoder;

    @Autowired
    public void setEncoder(PasswordEncoder encoder) {
        PasswordMapperHelper.encoder = encoder;
    }

    @Named("encodePassword")
    public static String encodePassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}