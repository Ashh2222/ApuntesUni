package com.university.website.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.endsWith;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class SecurityTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("Rutas públicas deben ser accesibles sin autenticación")
    void testPublicEndpointsAccessible() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/registro"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/buscar"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Rutas protegidas deben redirigir a /login para usuarios anónimos")
    void testProtectedEndpointsRedirectToLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));

        mockMvc.perform(get("/subir"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));

        mockMvc.perform(get("/perfil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", endsWith("/login")));
    }

    @Test
    @WithMockUser(username = "estudiante@uni.edu.pe", roles = {"STUDENT"})
    @DisplayName("Estudiante autenticado no debe tener acceso a panel de administrador (403)")
    void testStudentCannotAccessAdmin() throws Exception {
        mockMvc.perform(get("/admin/materias"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@uni.edu.pe", roles = {"ADMINISTRATOR"})
    @DisplayName("Administrador debe tener acceso a gestión de materias (200)")
    void testAdminCanAccessAdminSubjects() throws Exception {
        mockMvc.perform(get("/admin/materias"))
                .andExpect(status().isOk());
    }
}
