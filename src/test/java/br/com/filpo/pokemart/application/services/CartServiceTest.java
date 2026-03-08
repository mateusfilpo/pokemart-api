package br.com.filpo.pokemart.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @InjectMocks
    private CartService cartService;

    private UUID mockUserId;
    private UUID mockItemId;
    private User mockUser;
    private Item mockItem;
    private CartItem mockCartItem;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockItemId = UUID.randomUUID();

        mockItem = Item.builder()
            .id(mockItemId)
            .name("Potion")
            .stock(20)
            .build();

        mockCartItem = new CartItem(mockItem, 5);
        
        List<CartItem> cart = new ArrayList<>();
        cart.add(mockCartItem);

        mockUser = User.builder()
            .id(mockUserId)
            .name("Dawn")
            .cart(cart)
            .build();
    }

    @Test
    @DisplayName("Deve retornar o carrinho do usuário quando ele existir")
    void shouldGetCartWhenUserExists() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act
        List<CartItem> result = cartService.getCart(mockUserId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Potion", result.get(0).getItem().getName());
        assertEquals(5, result.get(0).getQuantity());
        verify(userRepository, times(1)).findById(mockUserId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar carrinho de usuário inexistente")
    void shouldThrowExceptionWhenGettingCartForUnknownUser() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> cartService.getCart(mockUserId)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    @DisplayName("Deve atualizar (upsert) item no carrinho quando quantidade > 0 e estoque é suficiente")
    void shouldUpdateCartItemWhenQuantityGreaterThanZeroAndStockSufficient() {
        // Arrange
        int requestedQuantity = 10; 
        
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockItem));
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act
        List<CartItem> result = cartService.updateCartItem(mockUserId, mockItemId, requestedQuantity);

        // Assert
        assertNotNull(result);
        verify(itemRepository, times(1)).findById(mockItemId);
        verify(userRepository, times(1)).upsertCartItem(mockUserId, mockItemId, requestedQuantity);
        verify(userRepository, never()).removeCartItem(any(), any());
    }

    @Test
    @DisplayName("Deve remover item do carrinho quando quantidade for igual a 0")
    void shouldRemoveCartItemWhenQuantityIsZero() {
        // Arrange
        int requestedQuantity = 0;
        
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act
        List<CartItem> result = cartService.updateCartItem(mockUserId, mockItemId, requestedQuantity);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).removeCartItem(mockUserId, mockItemId);
        verify(userRepository, never()).upsertCartItem(any(), any(), anyInt()); 
        verify(itemRepository, never()).findById(any()); 
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException quando a quantidade pedida exceder o estoque")
    void shouldThrowExceptionWhenRequestedQuantityExceedsStock() {
        // Arrange
        int requestedQuantity = 50; 
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockItem));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> cartService.updateCartItem(mockUserId, mockItemId, requestedQuantity)
        );

        assertEquals("Requested quantity exceeds available stock", exception.getMessage());
        
        verify(userRepository, never()).upsertCartItem(any(), any(), anyInt());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao tentar atualizar um item que não existe")
    void shouldThrowExceptionWhenUpdatingUnknownItem() {
        // Arrange
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> cartService.updateCartItem(mockUserId, mockItemId, 5)
        );

        assertEquals("Item not found", exception.getMessage());
    }

    @Test
    @DisplayName("Deve limpar o carrinho com sucesso")
    void shouldClearCartSuccessfully() {
        // Act
        cartService.clearCart(mockUserId);

        // Assert
        verify(userRepository, times(1)).clearCart(mockUserId);
    }
}