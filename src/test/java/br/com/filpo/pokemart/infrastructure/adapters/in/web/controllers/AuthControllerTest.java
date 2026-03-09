package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.AuthenticationRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.GlobalExceptionHandler;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.security.TokenService;
import tools.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(
    controllers = AuthController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.filpo.pokemart.infrastructure.(adapters.out.security|config).*")
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, AuthControllerTest.MockSecurityConfig.class})
class AuthControllerTest {

    @TestConfiguration
    static class MockSecurityConfig implements WebMvcConfigurer {
        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(AuthenticationPrincipal.class);
                }

                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                    return webRequest.getAttribute("mockUserNode", RequestAttributes.SCOPE_REQUEST);
                }
            });
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CacheManager cacheManager;

    private UserNode mockUser;
    private UUID mockUserId;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockUser = UserNode.builder()
            .id(mockUserId)
            .name("Misty")
            .email("misty@cerulean.com")
            .role(UserRole.ADMIN)
            .build();
    }

    @Test
    @DisplayName("POST /api/auth/login: Deve autenticar o usuário e retornar 200 OK com o Cookie HttpOnly")
    void shouldLoginSuccessfullyAndReturnCookie() throws Exception {
        // Arrange
        AuthenticationRequestDTO requestDTO = new AuthenticationRequestDTO("misty@cerulean.com", "starmie123");
        String fakeJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.fake_payload.fake_signature";

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(mockUser, null, mockUser.getAuthorities());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuthentication);
        
        when(tokenService.generateToken(mockUser)).thenReturn(fakeJwtToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("ADMIN"))
            .andExpect(jsonPath("$.id").value(mockUserId.toString()))
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(cookie().value("pokemart_token", fakeJwtToken))
            .andExpect(cookie().httpOnly("pokemart_token", true))
            .andExpect(cookie().path("pokemart_token", "/"));
    }

    @Test
    @DisplayName("POST /api/auth/login: Deve retornar 401 Unauthorized para credenciais inválidas")
    void shouldReturn401ForInvalidCredentials() throws Exception {
        // Arrange
        AuthenticationRequestDTO invalidRequest = new AuthenticationRequestDTO("rocket@team.com", "wrongpass");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.error").value("Invalid credentials"));
    }

    @Test
    @DisplayName("POST /api/auth/logout: Deve retornar 204 No Content e apagar o Cookie da sessão")
    void shouldLogoutAndClearCookie() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
            .andExpect(status().isNoContent())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(cookie().value("pokemart_token", ""))
            .andExpect(cookie().maxAge("pokemart_token", 0));
    }

    @Test
    @DisplayName("GET /api/auth/me: Deve retornar os dados do próprio usuário se o Token for válido")
    void shouldGetMeSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/me")
                .requestAttr("mockUserNode", mockUser)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(mockUserId.toString()))
            .andExpect(jsonPath("$.name").value("Misty"))
            .andExpect(jsonPath("$.email").value("misty@cerulean.com"))
            .andExpect(jsonPath("$.role").value("ADMIN"));
    }
}