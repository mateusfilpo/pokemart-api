package br.com.filpo.pokemart.application.services;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    @Cacheable(value = "categories")
    public List<Category> listAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository
            .findById(id)
            .orElseThrow(() ->
                new RuntimeException("Categoria não encontrada: " + id)
            );
    }

    @Override
    @CacheEvict(value = "categories", allEntries = true)
    public Category createCategory(Category category) {
        category.setId(UUID.randomUUID());
        return categoryRepository.save(category);
    }
}
