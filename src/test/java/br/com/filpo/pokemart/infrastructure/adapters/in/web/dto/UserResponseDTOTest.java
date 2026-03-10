package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;

class UserResponseDTOTest {

    @Test
    @DisplayName("Deve converter User de domínio para DTO corretamente")
    void shouldMapFromDomain() {
        // Arrange
        UUID mockId = UUID.randomUUID();
        User domainUser = User.builder()
                .id(mockId)
                .name("Cynthia")
                .email("cynthia@sinnoh.com")
                .role(UserRole.ADMIN)
                .build();

        // Act
        UserResponseDTO dto = UserResponseDTO.fromDomain(domainUser);

        // Assert
        assertEquals(mockId, dto.getId());
        assertEquals("Cynthia", dto.getName());
        assertEquals("cynthia@sinnoh.com", dto.getEmail());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    @DisplayName("Deve retornar null se o User de domínio for nulo (cobrindo as branches)")
    void shouldReturnNullWhenDomainIsNull() {
        // Act
        UserResponseDTO dto = UserResponseDTO.fromDomain(null);

        // Assert
        assertNull(dto);
    }
}