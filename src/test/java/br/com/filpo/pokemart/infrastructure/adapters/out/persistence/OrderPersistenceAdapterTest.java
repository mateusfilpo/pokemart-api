package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.OrderItem;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderItemRelationship;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataOrderRepository;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterTest {

    @InjectMocks
    private OrderPersistenceAdapter orderPersistenceAdapter;

    @Mock
    private SpringDataOrderRepository orderRepository;

    @Mock
    private SpringDataItemRepository itemRepository;

    private UUID mockOrderId;
    private UUID mockUserId;
    private UUID mockItemId;
    private Order mockOrder;
    private OrderNode mockOrderNode;
    private ItemNode mockFullItemNode;

    @BeforeEach
    void setUp() {
        mockOrderId = UUID.randomUUID();
        mockUserId = UUID.randomUUID();
        mockItemId = UUID.randomUUID();

        User mockUser = User.builder().id(mockUserId).build();
        UserNode mockUserNode = UserNode.builder().id(mockUserId).build();

        OrderItem mockOrderItem = OrderItem.builder()
            .productId(mockItemId)
            .name("Master Ball")
            .price(1200.0)
            .quantity(2)
            .build();

        mockOrder = Order.builder()
            .id(mockOrderId)
            .user(mockUser)
            .status("APPROVED")
            .totalAmount(2400.0)
            .createdAt(LocalDateTime.now())
            .items(List.of(mockOrderItem))
            .build();

        mockFullItemNode = ItemNode.builder()
            .id(mockItemId)
            .name("Master Ball")
            .price(1200.0)
            .stock(10)
            .build();

        OrderItemRelationship relationship = new OrderItemRelationship();
        relationship.setItem(mockFullItemNode);
        relationship.setQuantity(2);
        relationship.setPrice(1200.0);

        mockOrderNode = OrderNode.builder()
            .id(mockOrderId)
            .user(mockUserNode)
            .status("APPROVED")
            .totalAmount(2400.0)
            .items(List.of(relationship)) 
            .build();
    }

    @Test
    @DisplayName("save: Deve buscar o item no repositório, hidratar os relacionamentos e salvar o pedido")
    void shouldSaveOrderAndHydrateItems() {
        // Arrange
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockFullItemNode));
        when(orderRepository.save(any(OrderNode.class))).thenReturn(mockOrderNode);

        // Act
        Order savedOrder = orderPersistenceAdapter.save(mockOrder);

        // Assert
        assertNotNull(savedOrder);
        assertEquals("APPROVED", savedOrder.getStatus());
        assertEquals(2400.0, savedOrder.getTotalAmount());
        
        verify(itemRepository, times(1)).findById(mockItemId);
        verify(orderRepository, times(1)).save(any(OrderNode.class));
    }

    @Test
    @DisplayName("save: Deve salvar pedido sem itens e não chamar o itemRepository")
    void shouldSaveOrderWithoutItems() {
        // Arrange
        Order emptyOrder = Order.builder()
            .id(mockOrderId)
            .totalAmount(0.0)
            .status("PENDING")
            .items(List.of()) 
            .build();
            
        OrderNode emptyOrderNode = OrderNode.builder()
            .id(mockOrderId)
            .totalAmount(0.0)
            .status("PENDING")
            .items(List.of()) 
            .build();
            
        when(orderRepository.save(any(OrderNode.class))).thenReturn(emptyOrderNode);

        // Act
        orderPersistenceAdapter.save(emptyOrder);

        // Assert
        verify(itemRepository, never()).findById(any()); 
        verify(orderRepository, times(1)).save(any(OrderNode.class));
    }

    @Test
    @DisplayName("findById: Deve retornar Optional com Order quando encontrado")
    void shouldFindByIdAndMapToDomain() {
        // Arrange
        when(orderRepository.findById(mockOrderId)).thenReturn(Optional.of(mockOrderNode));

        // Act
        Optional<Order> result = orderPersistenceAdapter.findById(mockOrderId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockOrderId, result.get().getId());
        verify(orderRepository, times(1)).findById(mockOrderId);
    }

    @Test
    @DisplayName("findByUserId: Deve buscar todos os pedidos, filtrar em memória e retornar apenas do usuário")
    void shouldFindByUserIdFilteringInMemory() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        
        OrderNode myOrder = OrderNode.builder().id(mockOrderId).user(UserNode.builder().id(mockUserId).build()).items(List.of()).build();
        OrderNode otherOrder = OrderNode.builder().id(UUID.randomUUID()).user(UserNode.builder().id(otherUserId).build()).items(List.of()).build();
        OrderNode orphanOrder = OrderNode.builder().id(UUID.randomUUID()).user(null).items(List.of()).build();

        when(orderRepository.findAll()).thenReturn(List.of(myOrder, otherOrder, orphanOrder));

        // Act
        List<Order> result = orderPersistenceAdapter.findByUserId(mockUserId);

        // Assert
        assertEquals(1, result.size(), "Deveria retornar apenas o pedido associado ao mockUserId");
        assertEquals(mockOrderId, result.get(0).getId());
        
        verify(orderRepository, times(1)).findAll();
    }
}