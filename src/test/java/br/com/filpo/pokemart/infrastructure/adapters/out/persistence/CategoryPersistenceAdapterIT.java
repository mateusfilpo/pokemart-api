package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import br.com.filpo.pokemart.integration.AbstractIntegrationTest;

class CategoryPersistenceAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private CategoryPersistenceAdapter categoryAdapter;

    @Autowired
    private SpringDataCategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName(
        "IT: Deve salvar uma nova categoria e conseguir buscá-la por ID"
    )
    void shouldSaveAndFindCategoryById() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        Category newCategory = Category.builder()
            .id(categoryId)
            .name("Berries")
            .build();

        // Act - Save
        Category savedCategory = categoryAdapter.save(newCategory);

        // Assert - Save
        assertNotNull(savedCategory);
        assertEquals(categoryId, savedCategory.getId());
        assertEquals("Berries", savedCategory.getName());

        // Act - Find by ID
        Optional<Category> foundCategory = categoryAdapter.findById(categoryId);

        // Assert - Find by ID
        assertTrue(foundCategory.isPresent());
        assertEquals("Berries", foundCategory.get().getName());
    }

    @Test
    @DisplayName("IT: Deve buscar uma categoria pelo nome exato")
    void shouldFindCategoryByName() {
        // Arrange
        UUID categoryId = UUID.randomUUID();
        Category newCategory = Category.builder()
            .id(categoryId)
            .name("TMs & HMs")
            .build();
        categoryAdapter.save(newCategory);

        // Act
        Optional<Category> foundCategory = categoryAdapter.findByName(
            "TMs & HMs"
        );
        Optional<Category> notFoundCategory = categoryAdapter.findByName(
            "Key Items"
        );

        // Assert
        assertTrue(foundCategory.isPresent());
        assertEquals(categoryId, foundCategory.get().getId());
        assertFalse(
            notFoundCategory.isPresent(),
            "Não deve encontrar uma categoria que não existe"
        );
    }

    @Test
    @DisplayName("IT: Deve listar todas as categorias cadastradas")
    void shouldFindAllCategories() {
        // Arrange
        Category cat1 = Category.builder()
            .id(UUID.randomUUID())
            .name("Poké Balls")
            .build();
        Category cat2 = Category.builder()
            .id(UUID.randomUUID())
            .name("Healing")
            .build();
        Category cat3 = Category.builder()
            .id(UUID.randomUUID())
            .name("Battle Items")
            .build();

        categoryAdapter.save(cat1);
        categoryAdapter.save(cat2);
        categoryAdapter.save(cat3);

        // Act
        List<Category> allCategories = categoryAdapter.findAll();

        // Assert
        assertEquals(
            3,
            allCategories.size(),
            "Deve retornar exatamente 3 categorias"
        );

        List<String> categoryNames = allCategories
            .stream()
            .map(Category::getName)
            .toList();
        assertTrue(categoryNames.contains("Poké Balls"));
        assertTrue(categoryNames.contains("Healing"));
        assertTrue(categoryNames.contains("Battle Items"));
    }
}
