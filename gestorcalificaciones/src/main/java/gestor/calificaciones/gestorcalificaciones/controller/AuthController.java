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

/**
 * Controlador REST para operaciones de autenticación y registro de usuarios.
 * 
 * <p>Este controlador maneja las operaciones relacionadas con el registro
 * de nuevos usuarios (estudiantes y profesores) y el inicio de sesión
 * mediante autenticación basada en JWT.</p>
 * 
 * @author Sistema de Gestión de Calificaciones
 * @version 1.0
 */
@RestController
@RequestMapping("/api/auth") 
public class AuthController {
    
    @Autowired
    private AuthSerice authService;

    /**
     * Registra un nuevo usuario en el sistema.
     * 
     * <p>Permite registrar tanto estudiantes como profesores. El sistema
     * crea automáticamente la cuenta correspondiente según el rol especificado
     * y valida que el email y código sean únicos.</p>
     * 
     * <p><strong>Roles soportados:</strong></p>
     * <ul>
     *   <li>STUDENT: Crea una cuenta de estudiante</li>
     *   <li>TEACHER: Crea una cuenta de profesor</li>
     * </ul>
     * 
     * @param request Datos del usuario a registrar (nombre, email, password, role, code)
     * @return Respuesta HTTP 201 si el registro fue exitoso
     * 
     * @throws RuntimeException Si el email o código ya existen en el sistema
     * 
     * @apiNote No requiere autenticación
     * @apiNote Content-Type: application/json
     * 
     * @response 201 Created - Usuario registrado exitosamente
     * @response 400 Bad Request - Datos inválidos o faltantes
     * @response 409 Conflict - El email o código ya existe
     * @response 500 Internal Server Error - Error al registrar el usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Autentica un usuario y genera un token JWT.
     * 
     * <p>Valida las credenciales del usuario (email y contraseña) y genera
     * un token JWT que debe ser incluido en las peticiones subsiguientes
     * para acceder a los endpoints protegidos.</p>
     * 
     * <p><strong>Uso del token:</strong></p>
     * <ul>
     *   <li>Incluir en el header: <code>Authorization: Bearer {token}</code></li>
     *   <li>El token contiene información del usuario y su rol</li>
     *   <li>El token tiene una duración limitada (configurada en el sistema)</li>
     * </ul>
     * 
     * @param req Credenciales de inicio de sesión (email y password)
     * @return Respuesta con el token JWT y datos del usuario autenticado
     * 
     * @throws RuntimeException Si las credenciales son inválidas
     * 
     * @apiNote No requiere autenticación
     * @apiNote Content-Type: application/json
     * 
     * @response 200 OK - Autenticación exitosa, retorna token y datos del usuario
     * @response 401 Unauthorized - Credenciales inválidas
     * @response 500 Internal Server Error - Error al procesar la autenticación
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
