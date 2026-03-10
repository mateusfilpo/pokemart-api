package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

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
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderItemRelationship;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderNode;

class OrderMapperTest {

    @Test
    @DisplayName("toDomain: Deve mapear OrderNode para Order priorizando o preço congelado no relacionamento")
    void shouldMapOrderNodeToDomainUsingRelationshipPrice() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        ItemNode itemNode = ItemNode.builder()
            .id(itemId)
            .name("Poké Ball")
            .price(200.0) 
            .build();

        OrderItemRelationship relationship = OrderItemRelationship.builder()
            .item(itemNode)
            .quantity(3)
            .price(150.0) 
            .build();

        OrderNode orderNode = OrderNode.builder()
            .id(orderId)
            .totalAmount(450.0)
            .createdAt(now)
            .status("APPROVED")
            .user(null) 
            .items(List.of(relationship))
            .build();

        // Act
        Order result = OrderMapper.toDomain(orderNode);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(450.0, result.getTotalAmount());
        assertEquals(now, result.getCreatedAt());
        assertEquals("APPROVED", result.getStatus());
        
        assertEquals(1, result.getItems().size());
        OrderItem mappedItem = result.getItems().get(0);
        assertEquals(itemId, mappedItem.getProductId());
        assertEquals("Poké Ball", mappedItem.getName());
        assertEquals(3, mappedItem.getQuantity());
        
        assertEquals(150.0, mappedItem.getPrice());
    }

    @Test
    @DisplayName("toDomain: Deve fazer fallback para o preço do ItemNode se o relacionamento não tiver preço salvo")
    void shouldMapOrderNodeToDomainWithPriceFallback() {
        // Arrange
        ItemNode itemNode = ItemNode.builder()
            .id(UUID.randomUUID())
            .name("Potion")
            .price(300.0) 
            .build();

        OrderItemRelationship relationship = OrderItemRelationship.builder()
            .item(itemNode)
            .quantity(2)
            .price(null) 
            .build();

        OrderNode orderNode = OrderNode.builder()
            .items(List.of(relationship))
            .build();

        // Act
        Order result = OrderMapper.toDomain(orderNode);

        // Assert
        OrderItem mappedItem = result.getItems().get(0);
        assertEquals(300.0, mappedItem.getPrice(), "Deveria ter feito fallback para o preço do ItemNode");
    }

    @Test
    @DisplayName("toDomain: Deve retornar null se o OrderNode for null")
    void shouldReturnNullWhenOrderNodeIsNull() {
        assertNull(OrderMapper.toDomain(null));
    }

    @Test
    @DisplayName("toNode: Deve mapear Order de Domínio para OrderNode criando relacionamentos com ID do produto")
    void shouldMapDomainToOrderNodeSuccessfully() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrderItem orderItem = OrderItem.builder()
            .productId(productId)
            .name("Super Potion")
            .price(700.0)
            .quantity(5)
            .build();

        Order orderDomain = Order.builder()
            .id(orderId)
            .totalAmount(3500.0)
            .createdAt(now)
            .status("PENDING")
            .user(null)
            .items(List.of(orderItem))
            .build();

        // Act
        OrderNode result = OrderMapper.toNode(orderDomain);

        // Assert
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(3500.0, result.getTotalAmount());
        assertEquals(now, result.getCreatedAt());
        assertEquals("PENDING", result.getStatus());
        
        assertEquals(1, result.getItems().size());
        OrderItemRelationship relationship = result.getItems().get(0);
        assertEquals(700.0, relationship.getPrice());
        assertEquals(5, relationship.getQuantity());
        
        assertNotNull(relationship.getItem());
        assertEquals(productId, relationship.getItem().getId());
    }

    @Test
    @DisplayName("toNode: Deve retornar null se a Order de Domínio for null")
    void shouldReturnNullWhenOrderDomainIsNull() {
        assertNull(OrderMapper.toNode(null));
    }

    @Test
    @DisplayName("toDomain: Deve retornar uma lista vazia se o OrderNode não tiver itens (Cobre branch null)")
    void shouldReturnEmptyListWhenNodeItemsIsNull() {
        // Arrange
        OrderNode node = OrderNode.builder()
                .id(UUID.randomUUID())
                .items(null)
                .build();

        // Act
        Order result = OrderMapper.toDomain(node);

        // Assert
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty(), "Deveria ter retornado uma lista vazia e não null");
    }

    @Test
    @DisplayName("toNode: Deve retornar uma lista vazia se a Order de domínio não tiver itens (Cobre branch null)")
    void shouldReturnEmptyListWhenDomainItemsIsNull() {
        // Arrange
        Order domain = Order.builder()
                .id(UUID.randomUUID())
                .items(null)
                .build();

        // Act
        OrderNode result = OrderMapper.toNode(domain);

        // Assert
        assertNotNull(result.getItems());
        assertTrue(result.getItems().isEmpty(), "Deveria ter mapeado para uma lista vazia de relacionamentos");
    }
}