package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Item;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ItemResponseDTO {
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private String image;
    private Integer stock;
    private String category;
    private Boolean deleted;

    public static ItemResponseDTO fromDomain(Item item) {
        if (item == null) return null;
        
        return ItemResponseDTO.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .image(item.getImageUrl())
                .stock(item.getStock())
                .category(item.getCategory() != null ? item.getCategory().getName() : null)
                .deleted(item.getDeleted() != null ? item.getDeleted() : false)
                .build();
    }
}