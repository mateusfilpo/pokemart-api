package br.com.filpo.pokemart.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
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
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private OrderRepositoryPort orderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User mockUser;
    private UUID mockUserId;

    @BeforeEach
    void setUp() {
        mockUserId = UUID.randomUUID();
        mockUser = User.builder()
            .id(mockUserId)
            .name("Ash Ketchum")
            .email("ash@pallet.com")
            .password("Pikachu123@")
            .role(UserRole.USER)
            .build();
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso, criptografar a senha e atribuir a role USER")
    void shouldCreateUserSuccessfully() {
        // Arrange
        User newUserRequest = User.builder()
            .name("Misty")
            .email("misty@cerulean.com")
            .password("Starmie123@")
            .build();

        when(userRepository.findByEmail(newUserRequest.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Starmie123@")).thenReturn("encrypted_starmie123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        User createdUser = userService.createUser(newUserRequest);

        // Assert
        assertNotNull(createdUser.getId()); 
        assertEquals("encrypted_starmie123", createdUser.getPassword());
        assertEquals(UserRole.USER, createdUser.getRole()); 
        assertEquals("misty@cerulean.com", createdUser.getEmail());

        verify(userRepository, times(1)).findByEmail("misty@cerulean.com");
        verify(passwordEncoder, times(1)).encode("Starmie123@");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar BusinessRuleException ao tentar criar usuário com e-mail já existente")
    void shouldThrowExceptionWhenEmailAlreadyInUse() {
        // Arrange
        User duplicateEmailUser = User.builder()
            .email("ash@pallet.com")
            .password("Newpassword123@")
            .build();

        when(userRepository.findByEmail(duplicateEmailUser.getEmail())).thenReturn(Optional.of(mockUser));

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> userService.createUser(duplicateEmailUser)
        );

        assertEquals("Email is already in use.", exception.getMessage());
        
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Deve retornar o usuário quando o ID for encontrado")
    void shouldGetUserByIdWhenExists() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));

        // Act
        User result = userService.getUserById(mockUserId);

        // Assert
        assertNotNull(result);
        assertEquals(mockUserId, result.getId());
        assertEquals("Ash Ketchum", result.getName());
        verify(userRepository, times(1)).findById(mockUserId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o usuário não for encontrado pelo ID")
    void shouldThrowExceptionWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(mockUserId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> userService.getUserById(mockUserId)
        );

        assertEquals("User not found with ID: " + mockUserId, exception.getMessage());
        verify(userRepository, times(1)).findById(mockUserId);
    }

    @Test
    @DisplayName("Deve retornar o histórico de pedidos de um usuário")
    void shouldReturnUserOrderHistory() {
        // Arrange
        Order mockOrder1 = Order.builder().id(UUID.randomUUID()).build();
        Order mockOrder2 = Order.builder().id(UUID.randomUUID()).build();
        
        when(orderRepository.findByUserId(mockUserId)).thenReturn(List.of(mockOrder1, mockOrder2));

        // Act
        List<Order> result = userService.getUserOrderHistory(mockUserId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(orderRepository, times(1)).findByUserId(mockUserId);
    }
}