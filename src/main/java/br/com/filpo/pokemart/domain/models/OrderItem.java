package br.com.filpo.pokemart.domain.models;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private UUID productId;
    private String name;
    private Double price;
    private Integer quantity;
    private String imageUrl;
}