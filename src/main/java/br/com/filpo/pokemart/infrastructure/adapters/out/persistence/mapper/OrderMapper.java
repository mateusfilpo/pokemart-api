package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import java.util.stream.Collectors;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderItemRelationship;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderNode;

@lombok.experimental.UtilityClass
public class OrderMapper {

    public static Order toDomain(OrderNode node) {
        if (node == null) return null;
        
        var items = node.getItems() == null ? java.util.Collections.<br.com.filpo.pokemart.domain.models.OrderItem>emptyList() :
                node.getItems().stream()
                    .map(rel -> {
                        var itemNode = rel.getItem();
                        return br.com.filpo.pokemart.domain.models.OrderItem.builder()
                            .productId(itemNode.getId())
                            .name(itemNode.getName())
                            .price(rel.getPrice() != null ? rel.getPrice() : itemNode.getPrice())
                            .quantity(rel.getQuantity())
                            .imageUrl(itemNode.getImageUrl())
                            .build();
                    })
                    .collect(Collectors.toList());

        return Order.builder()
            .id(node.getId())
            .totalAmount(node.getTotalAmount())
            .createdAt(node.getCreatedAt())
            .status(node.getStatus())
            .user(UserMapper.toDomain(node.getUser()))
            .items(items)
            .build();
    }

    public static OrderNode toNode(Order domain) {
        if (domain == null) return null;

        var items = domain.getItems() == null ? java.util.Collections.<OrderItemRelationship>emptyList() :
                domain.getItems().stream()
                    .map(domainItem -> OrderItemRelationship.builder()
                        .quantity(domainItem.getQuantity())
                        .price(domainItem.getPrice())
                        .item(ItemNode.builder().id(domainItem.getProductId()).build())
                        .build())
                    .collect(Collectors.toList());

        return OrderNode.builder()
            .id(domain.getId())
            .totalAmount(domain.getTotalAmount())
            .createdAt(domain.getCreatedAt())
            .status(domain.getStatus())
            .user(UserMapper.toNode(domain.getUser()))
            .items(items)
            .build();
    }
}