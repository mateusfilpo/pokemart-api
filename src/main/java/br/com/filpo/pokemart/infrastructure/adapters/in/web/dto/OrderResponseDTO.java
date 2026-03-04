package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import br.com.filpo.pokemart.domain.models.Order;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record OrderResponseDTO(
    UUID id,
    UUID userId,
    Double totalAmount,
    LocalDateTime createdAt,
    String status,
    List<OrderItemResponseDTO> items
) {
    public static OrderResponseDTO fromDomain(Order order) {
        if (order == null) return null;

        List<OrderItemResponseDTO> itemsDto =
            order.getItems() != null
                ? order
                      .getItems()
                      .stream()
                      .map(OrderItemResponseDTO::fromDomain)
                      .collect(Collectors.toList())
                : List.of();

        return new OrderResponseDTO(
            order.getId(),
            order.getUser() != null ? order.getUser().getId() : null,
            order.getTotalAmount(),
            order.getCreatedAt(),
            order.getStatus(),
            itemsDto
        );
    }
}
