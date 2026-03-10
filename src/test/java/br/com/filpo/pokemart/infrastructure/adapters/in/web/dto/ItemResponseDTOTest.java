package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;

class ItemResponseDTOTest {

    @Test
    @DisplayName("Deve converter Item de domínio para DTO com todos os campos preenchidos")
    void shouldMapFromDomainComplete() {
        // Arrange
        UUID mockId = UUID.randomUUID();
        Category category = Category.builder().name("Potions").build();
        Item item = Item.builder()
                .id(mockId)
                .name("Hyper Potion")
                .description("Restores 120 HP")
                .price(1500.0)
                .imageUrl("url_imagem")
                .stock(10)
                .category(category)
                .deleted(true)
                .build();

        // Act
        ItemResponseDTO dto = ItemResponseDTO.fromDomain(item);

        // Assert
        assertNotNull(dto);
        assertEquals(mockId, dto.getId());
        assertEquals("Potions", dto.getCategory());
        assertTrue(dto.getDeleted());
    }

    @Test
    @DisplayName("Deve tratar campos nulos (Category e Deleted) corretamente")
    void shouldHandleNullFieldsInDomain() {
        // Arrange
        Item item = Item.builder()
                .name("Poké Ball")
                .category(null) 
                .deleted(null)  
                .build();

        // Act
        ItemResponseDTO dto = ItemResponseDTO.fromDomain(item);

        // Assert
        assertNull(dto.getCategory());
        assertFalse(dto.getDeleted(), "Se deleted for null no domínio, o DTO deve ser false");
    }

    @Test
    @DisplayName("Deve retornar null se o Item de domínio for nulo")
    void shouldReturnNullWhenDomainIsNull() {
        // Act & Assert
        assertNull(ItemResponseDTO.fromDomain(null));
    }
}