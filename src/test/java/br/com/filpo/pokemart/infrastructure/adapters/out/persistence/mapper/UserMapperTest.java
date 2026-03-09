package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CartItemRelationship;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserMapperTest {

    @Test
    @DisplayName("toDomain: Deve mapear UserNode para User com o carrinho preenchido")
    void shouldMapUserNodeToDomainSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        ItemNode itemNode = ItemNode.builder()
            .id(itemId)
            .name("Hyper Potion")
            .price(1200.0)
            .build();

        CartItemRelationship cartItemRel = CartItemRelationship.builder()
            .item(itemNode)
            .quantity(3)
            .build();

        UserNode userNode = UserNode.builder()
            .id(userId)
            .name("Red")
            .email("red@champion.com")
            .password("pikachu")
            .role(UserRole.USER)
            .cartItems(List.of(cartItemRel))
            .build();

        // Act
        User result = UserMapper.toDomain(userNode);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Red", result.getName());
        assertEquals("red@champion.com", result.getEmail());
        assertEquals("pikachu", result.getPassword());
        assertEquals(UserRole.USER, result.getRole());

        assertNotNull(result.getCart());
        assertEquals(1, result.getCart().size());
        
        CartItem mappedCartItem = result.getCart().get(0);
        assertEquals(3, mappedCartItem.getQuantity());
        assertEquals(itemId, mappedCartItem.getItem().getId());
        assertEquals("Hyper Potion", mappedCartItem.getItem().getName());
    }

    @Test
    @DisplayName("toDomain: Deve retornar null se UserNode for null")
    void shouldReturnNullWhenUserNodeIsNull() {
        assertNull(UserMapper.toDomain(null));
    }

    @Test
    @DisplayName("toDomain: Deve inicializar o carrinho como lista vazia se a lista de relacionamentos for null")
    void shouldInitializeEmptyCartListWhenCartItemsNodeIsNull() {
        // Arrange
        UserNode userNode = UserNode.builder()
            .id(UUID.randomUUID())
            .name("Blue")
            .cartItems(null) 
            .build();

        // Act
        User result = UserMapper.toDomain(userNode);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCart(), "A lista do carrinho não pode ser nula no Domínio");
        assertTrue(result.getCart().isEmpty(), "A lista do carrinho deve estar vazia");
    }

    @Test
    @DisplayName("toNode: Deve mapear User de Domínio para UserNode com carrinho preenchido")
    void shouldMapDomainToUserNodeSuccessfully() {
        // Arrange
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Item item = Item.builder()
            .id(itemId)
            .name("Full Restore")
            .price(3000.0)
            .build();

        CartItem cartItem = CartItem.builder()
            .item(item)
            .quantity(5)
            .build();

        User userDomain = User.builder()
            .id(userId)
            .name("Professor Oak")
            .email("oak@lab.com")
            .password("password123")
            .role(UserRole.ADMIN)
            .cart(List.of(cartItem))
            .build();

        // Act
        UserNode result = UserMapper.toNode(userDomain);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("Professor Oak", result.getName());
        assertEquals("oak@lab.com", result.getEmail());
        assertEquals("password123", result.getPassword());
        assertEquals(UserRole.ADMIN, result.getRole());

        assertNotNull(result.getCartItems());
        assertEquals(1, result.getCartItems().size());

        CartItemRelationship mappedRel = result.getCartItems().get(0);
        assertEquals(5, mappedRel.getQuantity());
        assertEquals(itemId, mappedRel.getItem().getId());
        assertEquals("Full Restore", mappedRel.getItem().getName());
    }

    @Test
    @DisplayName("toNode: Deve retornar null se User de Domínio for null")
    void shouldReturnNullWhenUserDomainIsNull() {
        assertNull(UserMapper.toNode(null));
    }

    @Test
    @DisplayName("toNode: Deve inicializar lista de CartItems como vazia se carrinho do Domínio for null")
    void shouldInitializeEmptyCartItemsNodeWhenDomainCartIsNull() {
        // Arrange
        User userDomain = User.builder()
            .id(UUID.randomUUID())
            .name("Leaf")
            .cart(null) 
            .build();

        // Act
        UserNode result = UserMapper.toNode(userDomain);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCartItems(), "A lista de CartItems não pode ser nula no Node");
        assertTrue(result.getCartItems().isEmpty(), "A lista de CartItems deve estar vazia");
    }
}