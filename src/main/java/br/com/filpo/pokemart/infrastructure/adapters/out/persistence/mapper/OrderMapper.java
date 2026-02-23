package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderItemRelationship;

import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toDomain(OrderNode node) {
        if (node == null) return null;
        return Order.builder()
                .id(node.getId())
                .totalAmount(node.getTotalAmount())
                .createdAt(node.getCreatedAt())
                .status(node.getStatus())
                .user(UserMapper.toDomain(node.getUser()))
                .items(node.getItems().stream()
                        .map(rel -> { // ⚠️ 'rel' é o relacionamento agora
                            var itemNode = rel.getItem();
                            return br.com.filpo.pokemart.domain.models.OrderItem.builder()
                                    .productId(itemNode.getId())
                                    .name(itemNode.getName())
                                    .price(itemNode.getPrice())
                                    .quantity(rel.getQuantity()) // ✅ Pegando a quantidade real do banco!
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    public static OrderNode toNode(Order domain) {
        if (domain == null) return null;
        return OrderNode.builder()
                .id(domain.getId())
                .totalAmount(domain.getTotalAmount())
                .createdAt(domain.getCreatedAt())
                .status(domain.getStatus())
                .user(UserMapper.toNode(domain.getUser()))
                .items(domain.getItems().stream()
                        .map(domainItem -> OrderItemRelationship.builder()
                                .quantity(domainItem.getQuantity()) // ✅ Salvando a quantidade no banco!
                                .item(ItemNode.builder().id(domainItem.getProductId()).build())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}