package com.stockchef.stockchefback.config;

import com.stockchef.stockchefback.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT que intercepta todas las requests para verificar y procesar tokens JWT
 * Se ejecuta una vez por request para establecer la autenticación en SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header Authorization o no empieza con "Bearer ", continuar sin autenticación
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extraer el token JWT (remover "Bearer " del principio)
            final String jwt = authHeader.substring(7);
            log.debug("Token JWT extraído de la request: {}", jwt.substring(0, Math.min(jwt.length(), 20)) + "...");

            // Validar el token y extraer el email
            if (!jwtService.isTokenExpired(jwt)) {
                String userEmail = jwtService.extractEmail(jwt);
                log.debug("Token válido para usuario: {}", userEmail);

                // Si no hay autenticación previa en el contexto
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // Extraer el role del token para establecer las authorities
                    String role = jwtService.extractRole(jwt);
                    List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

                    // Crear token de autenticación
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userEmail, // Principal (username)
                            null,      // Credentials (no needed for JWT)
                            authorities // Authorities from token
                    );

                    // Establecer detalles de la request
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Establecer autenticación en SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    
                    log.debug("Autenticación establecida para usuario: {} con role: {}", userEmail, role);
                }
            } else {
                log.warn("Token JWT inválido o expirado");
            }
        } catch (Exception e) {
            log.error("Error al procesar token JWT: {}", e.getMessage(), e);
            // No lanzamos la excepción, solo logeamos y continuamos sin autenticación
        }

        // Continuar con la cadena de filtros
        filterChain.doFilter(request, response);
    }
}