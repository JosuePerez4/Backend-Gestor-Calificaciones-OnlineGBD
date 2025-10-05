package gestor.calificaciones.gestorcalificaciones.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gestor.calificaciones.gestorcalificaciones.DTO.User.LoginRequest;
import gestor.calificaciones.gestorcalificaciones.DTO.User.LoginResponse;
import gestor.calificaciones.gestorcalificaciones.DTO.User.RegisterRequest;
import gestor.calificaciones.gestorcalificaciones.service.AuthSerice;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
     @Autowired
    private AuthSerice authService;

    // Endpoints for user operations
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        // Call the service to register the user
        authService.register(request);
        // Response indicating the status of the registration
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        // Call the service to authenticate the user and generate a token
        return authService.login(req);
  }
}
