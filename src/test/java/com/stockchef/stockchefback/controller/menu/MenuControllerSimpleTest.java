package com.stockchef.stockchefback.controller.menu;

import com.stockchef.stockchefback.dto.menu.MenuCreationDTO;
import com.stockchef.stockchefback.service.menu.MenuService;
import com.stockchef.stockchefback.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test simple para verificar que el MenuController se puede instanciar correctamente
 */
@WebMvcTest(MenuController.class)
class MenuControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuService menuService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("Test simple de compilación del controller")
    void shouldCreateMenuController() {
        // Este test simplemente verifica que el controller puede ser instanciado
        // y que Spring puede cargar el contexto correctamente
        
        // Si llegamos a este punto, significa que:
        // 1. El controller se compiló correctamente
        // 2. Spring puede cargar el contexto
        // 3. Los imports y dependencias están bien configurados
        
        System.out.println("✓ MenuController se ha cargado correctamente");
        System.out.println("✓ ApplicationContext se ha inicializado");
        System.out.println("✓ Todas las dependencias están resueltas");
    }
}