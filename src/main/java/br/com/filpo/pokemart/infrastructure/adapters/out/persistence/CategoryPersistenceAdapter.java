package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CategoryPersistenceAdapter implements CategoryRepositoryPort {

    private final SpringDataCategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll().stream()
                .map(node -> Category.builder()
                        .id(node.getId())
                        .name(node.getName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return categoryRepository.findById(id)
                .map(node -> Category.builder()
                        .id(node.getId())
                        .name(node.getName())
                        .build());
    }

    @Override
    public Category save(Category category) {
        CategoryNode node = CategoryNode.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
        CategoryNode saved = categoryRepository.save(node);
        return Category.builder()
                .id(saved.getId())
                .name(saved.getName())
                .build();
    }
}