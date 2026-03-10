package br.com.filpo.pokemart.infrastructure.adapters.out.security;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import org.springframework.test.util.ReflectionTestUtils;

import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;

import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;

class TokenServiceTest {

    private TokenService tokenService;
    private UserNode mockUser;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService();
        
        ReflectionTestUtils.setField(tokenService, "secret", "my-super-secret-test-key-pokemon-123");

        mockUser = UserNode.builder()
            .id(UUID.randomUUID())
            .name("Lance")
            .email("lance@elitefour.com")
            .role(UserRole.ADMIN)
            .build();
    }

    @Test
    @DisplayName("generateToken: Deve gerar um token JWT válido com 3 partes (Header, Payload, Signature)")
    void shouldGenerateTokenSuccessfully() {
        // Act
        String token = tokenService.generateToken(mockUser);

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length, "Um token JWT padrão deve ter 3 partes separadas por ponto");
    }

    @Test
    @DisplayName("validateToken: Deve ler o token corretamente e devolver o subject (e-mail)")
    void shouldValidateTokenAndReturnSubject() {
        // Arrange
        String validToken = tokenService.generateToken(mockUser);

        // Act
        String subject = tokenService.validateToken(validToken);

        // Assert
        assertEquals("lance@elitefour.com", subject);
    }

    @Test
    @DisplayName("validateToken: Deve retornar string vazia ao tentar validar um token adulterado ou inválido")
    void shouldReturnEmptyStringWhenTokenIsInvalid() {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.payload.assinatura_falsa";

        // Act
        String subject = tokenService.validateToken(invalidToken);

        // Assert
        assertEquals("", subject, "O catch(JWTVerificationException) deve capturar o erro e retornar string vazia");
    }

    @Test
    @DisplayName("generateToken: Deve capturar JWTCreationException e lançar RuntimeException (Cobre o catch)")
    void shouldThrowExceptionWhenCreationFails() {
        // Arrange
        try (MockedStatic<Algorithm> mockedAlgorithm = mockStatic(Algorithm.class)) {
            mockedAlgorithm.when(() -> Algorithm.HMAC256(anyString()))
                           .thenThrow(new JWTCreationException("Erro forçado do Mockito", new Throwable()));

            // Act & Assert
            RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tokenService.generateToken(mockUser)
            );

            assertEquals("Erro ao gerar token JWT", exception.getMessage());
        }
    }
}