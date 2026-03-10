package br.com.filpo.pokemart.application.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private ItemRepositoryPort itemRepository;

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private UserRepositoryPort userRepository;

    @InjectMocks
    private CheckoutService checkoutService;

    private UUID mockUserId;
    private User mockUser;
    private Item mockItem;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        
        mockItem = Item.builder()
            .id(UUID.randomUUID())
            .name("Great Ball")
            .price(500.0)
            .stock(10)
            .build();

        CartItem cartItem = new CartItem(mockItem, 2);
        List<CartItem> cart = new ArrayList<>();
        cart.add(cartItem);

        mockUser = User.builder()
            .id(mockUserId)
            .name("Brock")
            .cart(cart)
            .build();
    }

    @Test
    @DisplayName("Deve processar o checkout com sucesso, reduzir estoque e limpar carrinho")
    void shouldPlaceOrderSuccessfully() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Order result = checkoutService.placeOrder(mockUserId);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("APPROVED", result.getStatus());
        assertEquals(1000.0, result.getTotalAmount()); 
        assertEquals(1, result.getItems().size()); 
        assertEquals("Great Ball", result.getItems().get(0).getName());
        assertEquals(2, result.getItems().get(0).getQuantity());

        assertEquals(8, mockItem.getStock());
        assertTrue(mockUser.getCart().isEmpty());

        verify(itemRepository, times(1)).save(mockItem);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(userRepository, times(1)).clearCart(mockUserId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException se o usuário não existir")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> checkoutService.placeOrder(mockUserId)
        );

        assertEquals("User not found", exception.getMessage());
        
        verify(itemRepository, never()).save(any(Item.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException se o carrinho estiver vazio")
    void shouldThrowExceptionWhenCartIsEmpty() {
        // Arrange
        mockUser.setCart(new ArrayList<>());
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> checkoutService.placeOrder(mockUserId)
        );

        assertEquals("Cannot complete checkout with an empty cart.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException se o estoque for insuficiente")
    void shouldThrowExceptionWhenInsufficientStock() {
        // Arrange
        mockUser.getCart().get(0).setQuantity(50);
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> checkoutService.placeOrder(mockUserId)
        );

        assertEquals("Insufficient stock for: Great Ball", exception.getMessage());
        
        assertEquals(10, mockItem.getStock()); 
        verify(itemRepository, never()).save(any(Item.class));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException se o carrinho for nulo")
    void shouldThrowExceptionWhenCartIsNull() {
        // Arrange
        mockUser.setCart(null); 
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> checkoutService.placeOrder(mockUserId)
        );

        assertEquals("Cannot complete checkout with an empty cart.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }
}