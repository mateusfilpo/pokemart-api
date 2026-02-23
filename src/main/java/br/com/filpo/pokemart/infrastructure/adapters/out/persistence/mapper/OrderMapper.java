package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import java.util.stream.Collectors;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderNode;

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
                        .map(itemNode -> {
                            var item = ItemMapper.toDomain(itemNode);
                            return br.com.filpo.pokemart.domain.models.OrderItem.builder()
                                    .productId(item.getId())
                                    .name(item.getName())
                                    .price(item.getPrice())
                                    .quantity(1)
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
                        .map(item -> ItemMapper.toNode(
                            br.com.filpo.pokemart.domain.models.Item.builder()
                                .id(item.getProductId())
                                .build()
                        ))
                        .collect(Collectors.toList()))
                .build();
    }
}