package gestor.calificaciones.gestorcalificaciones.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import gestor.calificaciones.gestorcalificaciones.DTO.User.LoginRequest;
import gestor.calificaciones.gestorcalificaciones.DTO.User.LoginResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.User.RegisterRequest;
import gestor.calificaciones.gestorcalificaciones.entities.User;
import gestor.calificaciones.gestorcalificaciones.entities.Teacher;
import gestor.calificaciones.gestorcalificaciones.entities.Student;
import gestor.calificaciones.gestorcalificaciones.enums.Role;
import gestor.calificaciones.gestorcalificaciones.exception.UserAlreadyExistsException;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import gestor.calificaciones.gestorcalificaciones.repository.TeacherRepository;
import gestor.calificaciones.gestorcalificaciones.repository.StudentRepository;
import gestor.calificaciones.gestorcalificaciones.security.JWT.JwtService;
import gestor.calificaciones.gestorcalificaciones.security.User.CustomUserDetailsService;

@Service
public class AuthSerice {
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private CustomUserDetailsService userDetailsService;
    @Autowired
    private JwtService jwtService;

    // Methods to make the CRUD operations

    // Method to register a user
    public void register(RegisterRequest request) {
        // Logic to register a user
        // Search if the user already exists
        boolean userExists = false;
        if (request.getRole().equals("TEACHER")) {
            userExists = teacherRepository.findByEmail(request.getEmail()).isPresent();
        } else if (request.getRole().equals("STUDENT")) {
            userExists = studentRepository.findByEmail(request.getEmail()).isPresent();
        }
        
        if (userExists) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // If the user does not exist, create a new user based on role
        User newUser;
        if (request.getRole().equals("TEACHER")) {
            newUser = new Teacher();
            // Set common properties
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Use encoded password
            newUser.setRole(Role.valueOf(request.getRole()));
            newUser.setCode(request.getCode());
            teacherRepository.save((Teacher) newUser);
        } else if (request.getRole().equals("STUDENT")) {
            newUser = new Student();
            // Set common properties
            newUser.setName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Use encoded password
            newUser.setRole(Role.valueOf(request.getRole()));
            newUser.setCode(request.getCode());
            studentRepository.save((Student) newUser);
        } else {
            throw new IllegalArgumentException("Invalid role: " + request.getRole());
        }
    }

    // This method is to allow the login of a user
    public LoginResponse login(LoginRequest request) {
        // Check if the user exists in either repository
        User user = null;
        if (teacherRepository.findByEmail(request.getEmail()).isPresent()) {
            user = teacherRepository.findByEmail(request.getEmail()).get();
        } else if (studentRepository.findByEmail(request.getEmail()).isPresent()) {
            user = studentRepository.findByEmail(request.getEmail()).get();
        }
        
        if (user == null) {
            throw new UsernameNotFoundException("El correo no está registrado");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // Generate a JWT token for the authenticated user
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtService.generateTokenFromUserDetails(userDetails);
        return new LoginResponse(token);
    }
}
