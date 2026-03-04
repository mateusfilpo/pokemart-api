package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

import br.com.filpo.pokemart.domain.models.OrderItem;

public record OrderItemResponseDTO(
        UUID itemId,
        String name,
        Integer quantity,
        Double price
) {
    public static OrderItemResponseDTO fromDomain(OrderItem orderItem) {
        if (orderItem == null) return null;

        return new OrderItemResponseDTO(
                orderItem.getProductId(), 
                orderItem.getName(),
                orderItem.getQuantity(),
                orderItem.getPrice() 
        );
    }
}