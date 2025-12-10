package com.stockchef.stockchefback.controller.reports;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.PostConstruct;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class SimpleReportController {

    @PostConstruct
    public void init() {
        System.out.println("========== SIMPLE REPORT CONTROLLER INITIALIZED ==========");
    }

    /**
     * Test endpoint básico
     */
    @GetMapping("/test")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<Map<String, String>> getTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Reports endpoint funcionando!");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    /**
     * Dashboard simple sin dependencias
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'CHEF', 'ADMIN', 'DEVELOPER')")
    public ResponseEntity<Map<String, Object>> getDashboardSimple() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalProductos", 10);
        dashboard.put("alertas", 2);
        dashboard.put("mensaje", "Dashboard básico funcionando");
        dashboard.put("timestamp", java.time.LocalDateTime.now());
        return ResponseEntity.ok(dashboard);
    }
}