package br.com.filpo.pokemart.application.services;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.integration.AbstractIntegrationTest;

class CategoryServiceCacheIT extends AbstractIntegrationTest {

    @Autowired
    private CategoryUseCase categoryUseCase;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private CategoryRepositoryPort categoryRepositorySpy;

    @BeforeEach
    void setUp() {
        cacheManager
            .getCacheNames()
            .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Test
    @DisplayName(
        "IT (Redis): Deve salvar a lista de categorias no cache na primeira busca"
    )
    void shouldCacheCategoryList() {
        List<Category> firstCall = categoryUseCase.listAllCategories();

        List<Category> secondCall = categoryUseCase.listAllCategories();

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        verify(categoryRepositorySpy, times(1)).findAll();
    }

    @Test
    @DisplayName(
        "IT (Redis): Deve limpar o cache de categorias ao criar uma nova (CacheEvict)"
    )
    void shouldEvictCacheWhenCategoryIsCreated() {
        categoryUseCase.listAllCategories();
        verify(categoryRepositorySpy, times(1)).findAll();

        Category newCategory = Category.builder()
            .name("Evolution Stones")
            .build();
        categoryUseCase.createCategory(newCategory);
        verify(categoryRepositorySpy, times(1)).save(any(Category.class));

        categoryUseCase.listAllCategories();
        verify(categoryRepositorySpy, times(2)).findAll();
    }
}
