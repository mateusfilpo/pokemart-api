package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.GlobalExceptionHandler;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
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
import org.springframework.http.MediaType;
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
    controllers = UserController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.filpo.pokemart.infrastructure.(adapters.out.security|adapters.in.web.RateLimit|config).*")
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, UserControllerTest.MockSecurityConfig.class})
class UserControllerTest {

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
    private UserUseCase userUseCase;

    @MockitoBean
    private CacheManager cacheManager;

    private UUID targetUserId;
    private UserNode targetUserNode;
    private User targetUserDomain;

    @BeforeEach
    void setUp() {
        targetUserId = UUID.randomUUID();
        
        targetUserNode = UserNode.builder()
            .id(targetUserId)
            .role(UserRole.USER)
            .build();

        targetUserDomain = User.builder()
            .id(targetUserId)
            .name("Ash Ketchum")
            .email("ash@pallet.com")
            .role(UserRole.USER)
            .build();
    }

    @Test
    @DisplayName("GET /api/users/{id}: Deve retornar perfil com sucesso quando for o próprio usuário")
    void shouldGetUserProfileWhenIsSelf() throws Exception {
        when(userUseCase.getUserById(targetUserId)).thenReturn(targetUserDomain);

        mockMvc.perform(get("/api/users/{id}", targetUserId)
                .requestAttr("mockUserNode", targetUserNode)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(targetUserId.toString()))
            .andExpect(jsonPath("$.name").value("Ash Ketchum"));

        verify(userUseCase, times(1)).getUserById(targetUserId);
    }

    @Test
    @DisplayName("GET /api/users/{id}: Deve retornar perfil com sucesso se o usuário for ADMIN")
    void shouldGetUserProfileWhenIsAdmin() throws Exception {
        UserNode adminNode = UserNode.builder().id(UUID.randomUUID()).role(UserRole.ADMIN).build();
        when(userUseCase.getUserById(targetUserId)).thenReturn(targetUserDomain);

        mockMvc.perform(get("/api/users/{id}", targetUserId)
                .requestAttr("mockUserNode", adminNode) 
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(userUseCase, times(1)).getUserById(targetUserId);
    }

    @Test
    @DisplayName("GET /api/users/{id}: Deve retornar 403 Forbidden ao tentar ver o perfil de outro usuário")
    void shouldReturn403WhenFetchingOtherUserProfile() throws Exception {
        UserNode hackerNode = UserNode.builder().id(UUID.randomUUID()).role(UserRole.USER).build();

        mockMvc.perform(get("/api/users/{id}", targetUserId)
                .requestAttr("mockUserNode", hackerNode)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden()) 
            .andExpect(jsonPath("$.status").value(403))
            .andExpect(jsonPath("$.error").value("Acesso negado. Você não tem permissão para visualizar estes dados."));

        verify(userUseCase, never()).getUserById(any());
    }

    @Test
    @DisplayName("GET /api/users/{id}/orders: Deve retornar a lista de pedidos do usuário")
    void shouldGetUserOrdersSuccessfully() throws Exception {
        // Arrange
        Order mockOrder = Order.builder()
            .id(UUID.randomUUID())
            .totalAmount(1200.0)
            .status("APPROVED")
            .build();

        when(userUseCase.getUserOrderHistory(targetUserId)).thenReturn(List.of(mockOrder));

        // Act & Assert
        mockMvc.perform(get("/api/users/{id}/orders", targetUserId)
                .requestAttr("mockUserNode", targetUserNode)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].totalAmount").value(1200.0))
            .andExpect(jsonPath("$[0].status").value("APPROVED"));
    }

    @Test
    @DisplayName("POST /api/users: Deve criar novo treinador e retornar 201 Created")
    void shouldCreateUserSuccessfully() throws Exception {
        UserRequestDTO requestDTO = new UserRequestDTO("Dawn", "dawn@sinnoh.com", "StrongPass@2026!");
        
        User createdUser = User.builder()
            .id(UUID.randomUUID())
            .name("Dawn")
            .email("dawn@sinnoh.com")
            .role(UserRole.USER)
            .build();

        when(userUseCase.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Dawn"))
            .andExpect(jsonPath("$.email").value("dawn@sinnoh.com"));
    }

    @Test
    @DisplayName("POST /api/users: Deve retornar 422 Unprocessable Content se a senha for fraca (Regex falhou)")
    void shouldReturn422WhenPasswordIsWeak() throws Exception {
        UserRequestDTO invalidRequest = new UserRequestDTO("Brock", "brock@pewter.com", "brock12");

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.errors[0].fieldName").value("password"))
            .andExpect(jsonPath("$.errors[0].message").value("Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character."));
            
        verify(userUseCase, never()).createUser(any());
    }
}