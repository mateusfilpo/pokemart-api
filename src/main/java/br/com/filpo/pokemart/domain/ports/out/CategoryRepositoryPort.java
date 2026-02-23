package br.com.filpo.pokemart.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Category;

public interface CategoryRepositoryPort {
    List<Category> findAll();
    Optional<Category> findById(UUID id);
    Category save(Category category);
}