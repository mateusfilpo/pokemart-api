package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.List;
import java.util.UUID;

import lombok.Data;

@Data
public class CheckoutRequestDTO {
    private UUID userId;
    private List<OrderItemRequestDTO> items;
}