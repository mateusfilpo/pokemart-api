package br.com.filpo.pokemart.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AuthorizationServiceTest {

    @Mock
    private SpringDataUserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    @Test
    @DisplayName("Deve retornar UserDetails quando o e-mail (username) existir no banco")
    void shouldReturnUserDetailsWhenUsernameExists() {
        // Arrange
        String email = "ash@pallet.com";
        UserNode mockUserNode = mock(UserNode.class); 
        
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUserNode));

        // Act
        UserDetails result = authorizationService.loadUserByUsername(email);

        // Assert
        assertNotNull(result);
        assertEquals(mockUserNode, result);
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o e-mail não existir")
    void shouldThrowExceptionWhenUsernameNotFound() {
        // Arrange
        String email = "equiperocket@esconderijo.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        UsernameNotFoundException exception = assertThrows(
            UsernameNotFoundException.class,
            () -> authorizationService.loadUserByUsername(email)
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(email);
    }
}