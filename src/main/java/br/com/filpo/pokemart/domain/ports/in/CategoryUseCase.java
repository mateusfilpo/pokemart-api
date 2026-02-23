package br.com.filpo.pokemart.domain.ports.in;

import java.util.List;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Category;

public interface CategoryUseCase {
    List<Category> listAllCategories();
    Category getCategoryById(UUID id);
    Category createCategory(Category category);
}