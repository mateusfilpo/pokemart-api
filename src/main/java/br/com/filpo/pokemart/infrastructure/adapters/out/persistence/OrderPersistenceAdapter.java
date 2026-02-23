package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.OrderMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataOrderRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository orderRepository;
    private final SpringDataItemRepository itemRepository; // ⚠️ NOVO: Repositório de itens injetado!

    @Override
    public Order save(Order order) {
        var node = OrderMapper.toNode(order);
        
        // ⚠️ A MÁGICA DA CORREÇÃO AQUI:
        // Buscamos o nó completo do banco usando o ID. Assim o Spring não apaga
        // o nome, o preço e o estoque quando for salvar a relação do pedido.
        if (node.getItems() != null) {
            List<ItemNode> fullItems = node.getItems().stream()
                    .map(itemNode -> itemRepository.findById(itemNode.getId()).orElse(itemNode))
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
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDomain)
                .filter(o -> o.getUser() != null && o.getUser().getId().equals(userId))
                .collect(Collectors.toList());
    }
}