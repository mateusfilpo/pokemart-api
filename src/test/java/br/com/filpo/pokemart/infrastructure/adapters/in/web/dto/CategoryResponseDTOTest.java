package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.filpo.pokemart.domain.models.Category;

class CategoryResponseDTOTest {

    @Test
    @DisplayName("Deve converter Category de domínio para DTO corretamente")
    void shouldMapFromDomain() {
        // Arrange
        UUID mockId = UUID.randomUUID();
        Category domainCategory = Category.builder()
                .id(mockId)
                .name("Key Items")
                .build();

        // Act
        CategoryResponseDTO dto = CategoryResponseDTO.fromDomain(domainCategory);

        // Assert
        assertEquals(mockId, dto.getId());
        assertEquals("Key Items", dto.getName());
    }

    @Test
    @DisplayName("Deve retornar null se a Category de domínio for nula (cobrindo as branches)")
    void shouldReturnNullWhenDomainIsNull() {
        // Act
        CategoryResponseDTO dto = CategoryResponseDTO.fromDomain(null);

        // Assert
        assertNull(dto);
    }
}