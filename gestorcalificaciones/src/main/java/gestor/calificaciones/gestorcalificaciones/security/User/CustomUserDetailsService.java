package gestor.calificaciones.gestorcalificaciones.security.User;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gestor.calificaciones.gestorcalificaciones.entities.User;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;
import gestor.calificaciones.gestorcalificaciones.entities.Student;
import gestor.calificaciones.gestorcalificaciones.repository.TeacherRepository;
import gestor.calificaciones.gestorcalificaciones.repository.StudentRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public CustomUserDetailsService(TeacherRepository teacherRepository, StudentRepository studentRepository) {
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = null;
            
            if (username.contains("@")) {
                // It's an email - search in both repositories
                user = teacherRepository.findByEmail(username).orElse(null);
                if (user == null) {
                    user = studentRepository.findByEmail(username).orElse(null);
                }
            } else {
                // It's a UUID - search in both repositories
                user = teacherRepository.findById(java.util.UUID.fromString(username)).orElse(null);
                if (user == null) {
                    user = studentRepository.findById(java.util.UUID.fromString(username)).orElse(null);
                }
            }
            
            if (user == null) {
                throw new UsernameNotFoundException("User not found: " + username);
            }
            
            return new CustomUserDetails(user);
        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}
