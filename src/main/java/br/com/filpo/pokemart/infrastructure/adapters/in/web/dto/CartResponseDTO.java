package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartResponseDTO {
    private UUID itemId;
    private String name;
    private String image;
    private Double price;
    private Integer quantity;
    private Integer stock;
    private Boolean deleted;
}