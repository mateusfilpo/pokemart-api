package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import br.com.filpo.pokemart.domain.models.Category;
import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(
        @NotBlank(message = "Category name is required.")
        String name

) {
    public Category toDomain() {
        Category category = new Category();
        category.setName(this.name());

        return category;
    }
}