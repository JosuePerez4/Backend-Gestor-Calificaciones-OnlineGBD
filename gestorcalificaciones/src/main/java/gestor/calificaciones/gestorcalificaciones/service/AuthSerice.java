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
import gestor.calificaciones.gestorcalificaciones.exception.UserAlreadyExistsException;
import gestor.calificaciones.gestorcalificaciones.mapper.UserMapper;
import gestor.calificaciones.gestorcalificaciones.repository.UserRepository;
import gestor.calificaciones.gestorcalificaciones.security.JWT.JwtService;
import gestor.calificaciones.gestorcalificaciones.security.User.CustomUserDetailsService;

@Service
public class AuthSerice {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserMapper userMapper;
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
        User existingUser = userRepository.findByEmail(request.getEmail());
        if (existingUser != null) {
            throw new UserAlreadyExistsException("User already exists");
        }

        // If the user does not exist, create a new user
        User newUser = userMapper.toUserEntity(request);

        // Save the new user to the database
        userRepository.save(newUser);
    }

    // This method is to allow the login of a user
    public LoginResponse login(LoginRequest request) {
        // Check if the user exists
        User user = userRepository.findByEmail(request.getEmail());
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
        String token = jwtService.generateToken(userDetails);
        return new LoginResponse(token);
    }
}
