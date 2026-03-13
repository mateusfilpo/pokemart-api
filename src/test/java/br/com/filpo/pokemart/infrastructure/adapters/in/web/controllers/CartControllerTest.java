package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.domain.ports.in.CartUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CartItemRequestDTO;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.boot.test.context.TestConfiguration;
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
    controllers = CartController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.filpo.pokemart.infrastructure.(adapters.out.security|adapters.in.web.RateLimit|config).*")
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CartControllerTest.MockSecurityConfig.class})
class CartControllerTest {

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
    private CartUseCase cartUseCase;

    @MockitoBean
    private CacheManager cacheManager;

    private UserNode mockUser;
    private UUID mockUserId;
    private Item mockItem;
    private UUID mockItemId;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockUser = UserNode.builder()
            .id(mockUserId)
            .role(UserRole.USER)
            .build();

        mockItemId = UUID.randomUUID();
        mockItem = Item.builder()
            .id(mockItemId)
            .name("Max Potion")
            .price(2500.0)
            .stock(10)
            .deleted(false)
            .imageUrl("max_potion.png")
            .build();
    }

    @Test
    @DisplayName("GET /api/cart: Deve retornar os itens do carrinho do usuário logado com status 200 OK")
    void shouldGetCartSuccessfully() throws Exception {
        // Arrange
        CartItem cartItem = new CartItem(mockItem, 2);
        when(cartUseCase.getCart(mockUserId)).thenReturn(List.of(cartItem));

        // Act & Assert
        mockMvc.perform(get("/api/cart")
                .requestAttr("mockUserNode", mockUser)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].itemId").value(mockItemId.toString()))
            .andExpect(jsonPath("$[0].name").value("Max Potion"))
            .andExpect(jsonPath("$[0].quantity").value(2))
            .andExpect(jsonPath("$[0].price").value(2500.0));

        verify(cartUseCase, times(1)).getCart(mockUserId);
    }

    @Test
    @DisplayName("POST /api/cart: Deve atualizar o carrinho e retornar a lista atualizada com status 200 OK")
    void shouldUpdateCartSuccessfully() throws Exception {
        // Arrange
        CartItemRequestDTO requestDTO = new CartItemRequestDTO(mockItemId, 5);
        CartItem updatedCartItem = new CartItem(mockItem, 5);
        
        when(cartUseCase.updateCartItem(mockUserId, mockItemId, 5)).thenReturn(List.of(updatedCartItem));

        // Act & Assert
        mockMvc.perform(post("/api/cart")
                .requestAttr("mockUserNode", mockUser)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].quantity").value(5))
            .andExpect(jsonPath("$[0].stock").value(10));

        verify(cartUseCase, times(1)).updateCartItem(mockUserId, mockItemId, 5);
    }

    @Test
    @DisplayName("DELETE /api/cart: Deve limpar o carrinho e retornar status 204 No Content")
    void shouldClearCartSuccessfully() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/cart")
                .requestAttr("mockUserNode", mockUser)) 
            .andExpect(status().isNoContent());

        verify(cartUseCase, times(1)).clearCart(mockUserId);
    }
}