package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;

@ExtendWith(MockitoExtension.class)
class UserPersistenceAdapterTest {

    @InjectMocks
    private UserPersistenceAdapter userPersistenceAdapter;

    @Mock
    private SpringDataUserRepository userRepository;

    private UUID mockUserId;
    private UserNode mockUserNode;
    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();

        mockUserNode = UserNode.builder()
                .id(mockUserId)
                .name("Red")
                .email("red@mtkilimanjaro.com")
                .password("Pikachu123!")
                .role(UserRole.USER)
                .build();

        mockUser = User.builder()
                .id(mockUserId)
                .name("Red")
                .email("red@mtkilimanjaro.com")
                .password("Pikachu123!")
                .role(UserRole.USER)
                .build();
    }

    @Test
    @DisplayName("findById: Deve retornar Optional com User quando encontrado")
    void shouldFindByIdAndMapToDomain() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUserNode));

        // Act
        Optional<User> result = userPersistenceAdapter.findById(mockUserId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Red", result.get().getName());
        assertEquals("red@mtkilimanjaro.com", result.get().getEmail());
        
        verify(userRepository, times(1)).findById(mockUserId);
    }

    @Test
    @DisplayName("findById: Deve retornar Optional vazio quando não encontrado")
    void shouldReturnEmptyWhenIdNotFound() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userPersistenceAdapter.findById(mockUserId);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(mockUserId);
    }

    @Test
    @DisplayName("findByEmail: Deve retornar Optional com User quando encontrado")
    void shouldFindByEmailAndMapToDomain() {
        // Arrange
        String email = "red@mtkilimanjaro.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUserNode));

        // Act
        Optional<User> result = userPersistenceAdapter.findByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockUserId, result.get().getId());
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("save: Deve mapear para Node, salvar no repositório e retornar o Domínio mapeado")
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(userRepository.save(any(UserNode.class))).thenReturn(mockUserNode);

        // Act
        User savedUser = userPersistenceAdapter.save(mockUser);

        // Assert
        assertNotNull(savedUser);
        assertEquals(mockUserId, savedUser.getId());
        assertEquals("Red", savedUser.getName());
        
        verify(userRepository, times(1)).save(any(UserNode.class));
    }

    @Test
    @DisplayName("upsertCartItem: Deve repassar a chamada para o repositório")
    void shouldUpsertCartItem() {
        // Arrange
        UUID itemId = UUID.randomUUID();
        Integer quantity = 3;

        // Act
        userPersistenceAdapter.upsertCartItem(mockUserId, itemId, quantity);

        // Assert
        verify(userRepository, times(1)).upsertCartItem(mockUserId, itemId, quantity);
    }

    @Test
    @DisplayName("removeCartItem: Deve repassar a chamada para o repositório")
    void shouldRemoveCartItem() {
        // Arrange
        UUID itemId = UUID.randomUUID();

        // Act
        userPersistenceAdapter.removeCartItem(mockUserId, itemId);

        // Assert
        verify(userRepository, times(1)).removeCartItem(mockUserId, itemId);
    }

    @Test
    @DisplayName("clearCart: Deve repassar a chamada para o repositório")
    void shouldClearCart() {
        // Act
        userPersistenceAdapter.clearCart(mockUserId);

        // Assert
        verify(userRepository, times(1)).clearCart(mockUserId);
    }
}