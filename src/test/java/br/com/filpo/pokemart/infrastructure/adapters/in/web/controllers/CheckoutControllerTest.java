package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.domain.ports.in.CheckoutUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.GlobalExceptionHandler;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;

@WebMvcTest(
    controllers = CheckoutController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.filpo.pokemart.infrastructure.(adapters.out.security|adapters.in.web.RateLimit|config).*")
)
@AutoConfigureMockMvc(addFilters = false)
@Import({GlobalExceptionHandler.class, CheckoutControllerTest.MockSecurityConfig.class})
class CheckoutControllerTest {

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

    @MockitoBean
    private CheckoutUseCase checkoutUseCase;

    @MockitoBean
    private CacheManager cacheManager;

    private UUID mockUserId;
    private UserNode mockUserNode;
    private Order mockOrder;
    private UUID mockOrderId;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockUserNode = UserNode.builder()
            .id(mockUserId)
            .role(UserRole.USER)
            .build();

        mockOrderId = UUID.randomUUID();
        User orderUser = User.builder().id(mockUserId).build(); 

        mockOrder = Order.builder()
            .id(mockOrderId)
            .user(orderUser)
            .totalAmount(4500.0)
            .status("APPROVED")
            .createdAt(LocalDateTime.now())
            .items(List.of()) 
            .build();
    }

    @Test
    @DisplayName("POST /api/checkout: Deve processar o pedido e retornar 201 Created com cabeçalho Location")
    void shouldPlaceOrderSuccessfully() throws Exception {
        // Arrange
        when(checkoutUseCase.placeOrder(mockUserId)).thenReturn(mockOrder);

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                .requestAttr("mockUserNode", mockUserNode) 
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(header().string("Location", Matchers.containsString("/api/users/" + mockUserId + "/orders")))
            .andExpect(jsonPath("$.id").value(mockOrderId.toString()))
            .andExpect(jsonPath("$.userId").value(mockUserId.toString()))
            .andExpect(jsonPath("$.totalAmount").value(4500.0))
            .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(checkoutUseCase, times(1)).placeOrder(mockUserId);
    }

    @Test
    @DisplayName("POST /api/checkout: Deve retornar 422 Unprocessable Content se o carrinho estiver vazio")
    void shouldReturn422WhenCartIsEmpty() throws Exception {
        when(checkoutUseCase.placeOrder(mockUserId))
            .thenThrow(new BusinessRuleException("Carrinho vazio. Adicione itens antes de finalizar a compra."));

        // Act & Assert
        mockMvc.perform(post("/api/checkout")
                .requestAttr("mockUserNode", mockUserNode)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity()) 
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Carrinho vazio. Adicione itens antes de finalizar a compra."));

        verify(checkoutUseCase, times(1)).placeOrder(mockUserId);
    }
}