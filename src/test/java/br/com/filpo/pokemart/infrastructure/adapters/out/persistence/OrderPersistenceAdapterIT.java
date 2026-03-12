package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.OrderItem;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataOrderRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;
import br.com.filpo.pokemart.integration.AbstractIntegrationTest;

class OrderPersistenceAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private OrderPersistenceAdapter orderAdapter;

    @Autowired
    private SpringDataOrderRepository orderRepository;

    @Autowired
    private SpringDataUserRepository userRepository;

    @Autowired
    private SpringDataItemRepository itemRepository;

    private User domainUser;
    private Item domainItem;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
        itemRepository.deleteAll();

        UserNode userNode = UserNode.builder()
            .id(UUID.randomUUID())
            .name("Treinador Ash")
            .email("ash@pallet.com")
            .password("pikachu123")
            .build();
        userRepository.save(userNode);
        domainUser = User.builder()
            .id(userNode.getId())
            .name(userNode.getName())
            .email(userNode.getEmail())
            .build();

        ItemNode itemNode = ItemNode.builder()
            .id(UUID.randomUUID())
            .name("Poké Ball")
            .price(200.0)
            .stock(10)
            .build();
        itemRepository.save(itemNode);
        domainItem = Item.builder()
            .id(itemNode.getId())
            .name(itemNode.getName())
            .price(itemNode.getPrice())
            .build();
    }

    @Test
    @DisplayName(
        "IT: Deve salvar um pedido complexo com relacionamentos no Neo4j"
    )
    void shouldSaveComplexOrder() {
        OrderItem orderItem = OrderItem.builder()
            .productId(domainItem.getId()) 
            .name(domainItem.getName()) 
            .quantity(3)
            .price(200.0)
            .build();

        Order newOrder = Order.builder()
            .id(UUID.randomUUID())
            .user(domainUser)
            .items(List.of(orderItem))
            .totalAmount(600.0)
            .status("APPROVED")
            .build();

        // Act - Salva no banco
        Order savedOrder = orderAdapter.save(newOrder);

        // Assert - Verifica se o mapeamento bidirecional ocorreu com sucesso
        assertNotNull(savedOrder.getId());
        assertEquals("APPROVED", savedOrder.getStatus());
        assertEquals(600.0, savedOrder.getTotalAmount());
        assertEquals(
            1,
            savedOrder.getItems().size(),
            "Deve conter 1 tipo de item no pedido"
        );
        assertEquals(
            "Poké Ball",
            savedOrder.getItems().get(0).getName() 
        );
        assertEquals(3, savedOrder.getItems().get(0).getQuantity());

        assertTrue(orderRepository.findById(savedOrder.getId()).isPresent());
    }

    @Test
    @DisplayName("IT: Deve buscar a lista de pedidos de um usuário específico")
    void shouldFindOrdersByUserId() {
        // Arrange - Cria dois pedidos diferentes para o Ash
        OrderItem orderItem = OrderItem.builder()
            .productId(domainItem.getId())
            .name(domainItem.getName())
            .quantity(1)
            .price(200.0)
            .build();

        Order order1 = Order.builder()
            .id(UUID.randomUUID())
            .user(domainUser)
            .items(List.of(orderItem))
            .totalAmount(200.0)
            .status("APPROVED")
            .build();

        Order order2 = Order.builder()
            .id(UUID.randomUUID())
            .user(domainUser)
            .items(List.of(orderItem))
            .totalAmount(200.0)
            .status("PENDING")
            .build();

        orderAdapter.save(order1);
        orderAdapter.save(order2);

        // Act - Busca os pedidos
        List<Order> userOrders = orderAdapter.findByUserId(domainUser.getId());

        // Assert
        assertEquals(
            2,
            userOrders.size(),
            "O Treinador Ash deve ter 2 pedidos no histórico"
        );
        assertTrue(
            userOrders.stream().anyMatch(o -> o.getStatus().equals("APPROVED"))
        );
        assertTrue(
            userOrders.stream().anyMatch(o -> o.getStatus().equals("PENDING"))
        );
    }
}
