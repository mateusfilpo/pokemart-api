package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderItemRelationship;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.OrderMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataOrderRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository orderRepository;
    private final SpringDataItemRepository itemRepository;

    @Override
    public Order save(Order order) {
        var node = OrderMapper.toNode(order);

        if (node.getItems() != null) {
            List<OrderItemRelationship> fullItems = node
                .getItems()
                .stream()
                .map(rel -> {
                    var fullItemNode = itemRepository
                        .findById(rel.getItem().getId())
                        .orElse(rel.getItem());
                    rel.setItem(fullItemNode);
                    return rel;
                })
                .collect(Collectors.toList());
            node.setItems(fullItems);
        }

        var savedNode = orderRepository.save(node);
        return OrderMapper.toDomain(savedNode);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return orderRepository.findById(id).map(OrderMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(UUID userId) {
        return orderRepository
            .findAll()
            .stream()
            .map(OrderMapper::toDomain)
            .filter(
                o -> o.getUser() != null && o.getUser().getId().equals(userId)
            )
            .collect(Collectors.toList());
    }
}
