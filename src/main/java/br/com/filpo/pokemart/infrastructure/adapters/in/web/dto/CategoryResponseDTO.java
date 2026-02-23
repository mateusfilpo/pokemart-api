package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import br.com.filpo.pokemart.domain.models.Category;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryResponseDTO {
    private UUID id;
    private String name;

    public static CategoryResponseDTO fromDomain(Category category) {
        if (category == null) return null;
        return CategoryResponseDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}