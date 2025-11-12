package com.stockchef.stockchefback.controller;

import com.stockchef.stockchefback.dto.auth.LoginRequest;
import com.stockchef.stockchefback.dto.auth.LoginResponse;
import com.stockchef.stockchefback.model.User;
import com.stockchef.stockchefback.repository.UserRepository;
import com.stockchef.stockchefback.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controlador para autenticación de usuarios
 * Ahora usando UserRepository con MySQL
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * Endpoint para autenticación de usuarios
     * 
     * @param loginRequest Request con email y password
     * @return LoginResponse con token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Intento de login para email: {}", loginRequest.email());
        
        try {
            // Buscar usuario por email usando UserRepository
            Optional<User> userOptional = userRepository.findByEmail(loginRequest.email());
            if (userOptional.isEmpty()) {
                log.warn("Usuario no encontrado: {}", loginRequest.email());
                throw new UsernameNotFoundException("Credenciales inválidas");
            }
            
            User user = userOptional.get();
            
            // Verificar que el usuario esté activo
            if (!user.getIsActive()) {
                log.warn("Usuario inactivo intenta hacer login: {}", loginRequest.email());
                throw new BadCredentialsException("Usuario inactivo");
            }
            
            // Verificar password
            if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                log.warn("Password incorrecto para usuario: {}", loginRequest.email());
                throw new BadCredentialsException("Credenciales inválidas");
            }
            
            // Generar JWT token
            String token = jwtService.generateToken(user);
            
            // Crear response
            LoginResponse response = new LoginResponse(
                    token,
                    user.getEmail(),
                    user.getFirstName() + " " + user.getLastName(),
                    user.getRole(),
                    86400000L // 24 horas en milisegundos
            );
            
            log.info("Login exitoso para usuario: {} con rol: {}", user.getEmail(), user.getRole());
            return ResponseEntity.ok(response);
            
        } catch (UsernameNotFoundException | BadCredentialsException e) {
            log.error("Error de autenticación: {}", e.getMessage());
            return ResponseEntity.status(401).build();
        } catch (Exception e) {
            log.error("Error interno durante login: ", e);
            return ResponseEntity.status(500).build();
        }
    }
}