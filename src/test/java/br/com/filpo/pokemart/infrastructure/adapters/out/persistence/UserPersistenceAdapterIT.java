package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.*;

import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;
import br.com.filpo.pokemart.integration.AbstractIntegrationTest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserPersistenceAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private UserPersistenceAdapter userAdapter;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataItemRepository itemRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        itemRepository.deleteAll();
    }

    @Test
    @DisplayName("IT: Deve salvar e buscar um usuário por ID e por Email")
    void shouldSaveAndFindUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        String email = "ash.ketchum@pallet.com";
        User newUser = User.builder()
            .id(userId)
            .name("Ash Ketchum")
            .email(email)
            .password("pikachu123")
            .build();

        // Act - Save
        User savedUser = userAdapter.save(newUser);

        // Assert - Save
        assertNotNull(savedUser);
        assertEquals(userId, savedUser.getId());
        assertEquals("Ash Ketchum", savedUser.getName());

        // Act & Assert - Find by ID
        Optional<User> foundById = userAdapter.findById(userId);
        assertTrue(foundById.isPresent());
        assertEquals(email, foundById.get().getEmail());

        // Act & Assert - Find by Email
        Optional<User> foundByEmail = userAdapter.findByEmail(email);
        assertTrue(foundByEmail.isPresent());
        assertEquals(userId, foundByEmail.get().getId());
    }

    @Test
    @DisplayName(
        "IT: Deve gerenciar os relacionamentos do carrinho de compras (upsert, remove e clear)"
    )
    void shouldManageCartItems() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
            .id(userId)
            .name("Brock")
            .email("brock@pewter.com")
            .password("onix123")
            .build();
        userAdapter.save(user);

        ItemNode potionNode = ItemNode.builder()
            .id(UUID.randomUUID())
            .name("Potion")
            .price(300.0)
            .stock(50)
            .build();
        itemRepository.save(potionNode);

        ItemNode pokeballNode = ItemNode.builder()
            .id(UUID.randomUUID())
            .name("Poké Ball")
            .price(200.0)
            .stock(50)
            .build();
        itemRepository.save(pokeballNode);

        userAdapter.upsertCartItem(userId, potionNode.getId(), 2);
        userAdapter.upsertCartItem(userId, pokeballNode.getId(), 5);

        User userWithCart = userAdapter.findById(userId).orElseThrow();
        assertEquals(
            2,
            userWithCart.getCart().size(),
            "O carrinho deve ter 2 tipos de itens diferentes"
        );

        boolean hasPotion = userWithCart
            .getCart()
            .stream()
            .anyMatch(
                cartItem ->
                    cartItem.getItem().getId().equals(potionNode.getId()) &&
                    cartItem.getQuantity() == 2
            );
        assertTrue(
            hasPotion,
            "Deve encontrar a Potion com quantidade 2 no carrinho"
        );

        userAdapter.upsertCartItem(userId, potionNode.getId(), 10);
        User userWithUpdatedCart = userAdapter.findById(userId).orElseThrow();
        boolean hasUpdatedPotion = userWithUpdatedCart
            .getCart()
            .stream()
            .anyMatch(
                cartItem ->
                    cartItem.getItem().getId().equals(potionNode.getId()) &&
                    cartItem.getQuantity() == 10
            );
        assertTrue(
            hasUpdatedPotion,
            "A quantidade da Potion deve ter sido atualizada para 10"
        );

        // --- ACT & ASSERT: REMOVE ---
        userAdapter.removeCartItem(userId, potionNode.getId());
        User userAfterRemove = userAdapter.findById(userId).orElseThrow();
        assertEquals(
            1,
            userAfterRemove.getCart().size(),
            "O carrinho deve ter 1 tipo de item após a remoção"
        );
        assertNotEquals(
            potionNode.getId(),
            userAfterRemove.getCart().get(0).getItem().getId(),
            "A Potion não deve estar mais no carrinho"
        );

        // --- ACT & ASSERT: CLEAR ---
        userAdapter.clearCart(userId);
        User userAfterClear = userAdapter.findById(userId).orElseThrow();
        assertTrue(
            userAfterClear.getCart().isEmpty(),
            "O carrinho deve estar vazio após o clear"
        );
    }
}
