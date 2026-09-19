package com.ejemplo.laboratorio05.security;

import com.ejemplo.laboratorio05.dto.LoginRequest;
import com.ejemplo.laboratorio05.model.Producto;
import com.ejemplo.laboratorio05.service.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Suite de Integración de Seguridad y CORS - Spring Security 6 (Lab 05)")
class SecurityAuthCorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductoService productoService;

    @Nested
    @DisplayName("Autenticación y Generación de Tokens")
    class AuthenticationTests {

        @Test
        @DisplayName("POST /api/auth/login - Debe autenticar a 'admin' y devolver JWT")
        void loginAdmin_debeRetornarTokenJWT() throws Exception {
            LoginRequest login = new LoginRequest("admin", "Admin123*");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").isString())
                    .andExpect(jsonPath("$.tipo").value("Bearer"))
                    .andExpect(jsonPath("$.expiraEnSegundos").value(3600));
        }

        @Test
        @DisplayName("POST /api/auth/login - Credenciales erróneas debe devolver 401 Unauthorized")
        void loginInvalido_debeRetornar401() throws Exception {
            LoginRequest login = new LoginRequest("admin", "ClaveIncorrecta");

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /api/productos - Petición sin token debe ser rechazada con 401 Unauthorized")
        void getSinToken_debeRetornar401() throws Exception {
            mockMvc.perform(get("/api/productos"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Control de Acceso basado en Roles (USER vs SUPERVISOR vs ADMIN)")
    class RoleBasedAccessControlTests {

        @Test
        @WithMockUser(authorities = "SCOPE_USER")
        @DisplayName("USER: Puede consultar productos (200 OK)")
        void user_puedeConsultar() throws Exception {
            when(productoService.listarTodos()).thenReturn(List.of());

            mockMvc.perform(get("/api/productos"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(authorities = "SCOPE_USER")
        @DisplayName("USER: Denegado al intentar POST (403 Forbidden)")
        void user_noPuedeCrear() throws Exception {
            Producto p = new Producto(null, "Mouse", "Periféricos", 50.0, 10);

            mockMvc.perform(post("/api/productos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(p)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(authorities = "SCOPE_SUPERVISOR")
        @DisplayName("SUPERVISOR: Puede consultar (200 OK) y actualizar con PUT (200 OK) [Reto Adicional]")
        void supervisor_puedeConsultarYActualizar() throws Exception {
            Producto update = new Producto(1L, "Teclado", "Periféricos", 80.0, 5);
            when(productoService.actualizar(eq(1L), any(Producto.class))).thenReturn(update);

            mockMvc.perform(put("/api/productos/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(authorities = "SCOPE_SUPERVISOR")
        @DisplayName("SUPERVISOR: Denegado al intentar DELETE (403 Forbidden) [Reto Adicional]")
        void supervisor_noPuedeEliminar() throws Exception {
            mockMvc.perform(delete("/api/productos/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(authorities = "SCOPE_ADMIN")
        @DisplayName("ADMIN: Acceso total - Creación (201 Created) y Eliminación (204 No Content)")
        void admin_accesoTotal() throws Exception {
            Producto entrada = new Producto(null, "Webcam 4K", "Video", 250.0, 15);
            Producto creado = new Producto(5L, "Webcam 4K", "Video", 250.0, 15);

            when(productoService.crear(any(Producto.class))).thenReturn(creado);
            doNothing().when(productoService).eliminar(5L);

            mockMvc.perform(post("/api/productos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(entrada)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(5L));

            mockMvc.perform(delete("/api/productos/5"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("Políticas de Origen Cruzado (CORS)")
    class CorsTests {

        @Test
        @DisplayName("Pre-flight OPTIONS desde localhost:4200 debe ser permitido con cabeceras CORS")
        void preflightCors_angularLocalhost_permitido() throws Exception {
            mockMvc.perform(options("/api/productos")
                    .header(HttpHeaders.ORIGIN, "http://localhost:4200")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
                    .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        }
    }
}
