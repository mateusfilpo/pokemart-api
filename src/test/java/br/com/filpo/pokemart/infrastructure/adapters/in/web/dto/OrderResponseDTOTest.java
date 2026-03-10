package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.OrderItem;
import br.com.filpo.pokemart.domain.models.User;

class OrderResponseDTOTest {

    @Test
    @DisplayName("Deve converter Order de domínio para DTO com todos os campos (Caminho Feliz)")
    void shouldMapFromDomainComplete() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).build();
        
        OrderItem item = OrderItem.builder()
                .productId(UUID.randomUUID())
                .name("Rare Candy")
                .price(4800.0)
                .quantity(1)
                .build();

        Order order = Order.builder()
                .id(orderId)
                .user(user)
                .totalAmount(4800.0)
                .createdAt(LocalDateTime.now())
                .status("COMPLETED")
                .items(List.of(item))
                .build();

        // Act
        OrderResponseDTO dto = OrderResponseDTO.fromDomain(order);

        // Assert
        assertNotNull(dto);
        assertEquals(orderId, dto.id());
        assertEquals(userId, dto.userId());
        assertEquals(1, dto.items().size());
        assertEquals("Rare Candy", dto.items().get(0).name());
    }

    @Test
    @DisplayName("Deve tratar Order sem itens e sem usuário (Cobre as branches de null)")
    void shouldHandleNullItemsAndUser() {
        // Arrange
        Order order = Order.builder()
                .id(UUID.randomUUID())
                .user(null) 
                .items(null)
                .totalAmount(0.0)
                .build();

        // Act
        OrderResponseDTO dto = OrderResponseDTO.fromDomain(order);

        // Assert
        assertNull(dto.userId());
        assertTrue(dto.items().isEmpty(), "A lista de itens deve ser uma lista vazia, não null");
    }

    @Test
    @DisplayName("Deve retornar null se a Order de domínio for nula")
    void shouldReturnNullWhenDomainIsNull() {
        // Act & Assert
        assertNull(OrderResponseDTO.fromDomain(null));
    }
}