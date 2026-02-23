package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class OrderItemRequestDTO {
    private UUID productId;
    private Integer quantity;
}