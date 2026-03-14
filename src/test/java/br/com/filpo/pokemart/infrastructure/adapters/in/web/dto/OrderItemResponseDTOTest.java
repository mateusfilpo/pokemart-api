package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.filpo.pokemart.domain.models.OrderItem;

class OrderItemResponseDTOTest {

    @Test
    @DisplayName("Deve converter OrderItem de domínio para DTO corretamente")
    void shouldMapFromDomain() {
        // Arrange
        UUID mockProductId = UUID.randomUUID();
        OrderItem domainItem = OrderItem.builder()
                .productId(mockProductId)
                .name("Ultra Ball")
                .price(1200.0)
                .quantity(5)
                .imageUrl("https://pokeapi.co/media/sprites/items/ultra-ball.png")
                .build();

        // Act
        OrderItemResponseDTO dto = OrderItemResponseDTO.fromDomain(domainItem);

        // Assert
        assertEquals(mockProductId, dto.itemId()); 
        assertEquals("Ultra Ball", dto.name());
        assertEquals(5, dto.quantity());
        assertEquals(1200.0, dto.price());
        assertEquals("https://pokeapi.co/media/sprites/items/ultra-ball.png", dto.imageUrl());
    }

    @Test
    @DisplayName("Deve retornar null se o OrderItem de domínio for nulo (cobrindo as branches)")
    void shouldReturnNullWhenDomainIsNull() {
        // Act
        OrderItemResponseDTO dto = OrderItemResponseDTO.fromDomain(null);

        // Assert
        assertNull(dto);
    }
}