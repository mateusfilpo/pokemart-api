package br.com.filpo.pokemart.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    @Override
    public List<Category> listAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada: " + id));
    }

    @Override
    public Category createCategory(Category category) {
        // Gera um ID único para a nova categoria antes de salvar
        category.setId(UUID.randomUUID());
        return categoryRepository.save(category);
    }
}