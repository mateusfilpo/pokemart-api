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
public class Item {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Integer stock;
    private Boolean deleted;
    private Category category;
    private Long version;
    private String normalizedSearch;

    public boolean hasStock(int quantity) {
        return this.stock != null && this.stock >= quantity;
    }

    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalArgumentException("Estoque insuficiente para o item: " + this.name);
        }
        this.stock -= quantity;
    }
}