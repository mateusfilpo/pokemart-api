package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryPersistenceAdapterTest {

    @InjectMocks
    private CategoryPersistenceAdapter categoryPersistenceAdapter;

    @Mock
    private SpringDataCategoryRepository categoryRepository;

    private UUID mockCategoryId;
    private String mockCategoryName;
    private CategoryNode mockCategoryNode;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        mockCategoryId = UUID.randomUUID();
        mockCategoryName = "Poké Balls";

        mockCategoryNode = CategoryNode.builder()
                .id(mockCategoryId)
                .name(mockCategoryName)
                .build();

        mockCategory = Category.builder()
                .id(mockCategoryId)
                .name(mockCategoryName)
                .build();
    }

    @Test
    @DisplayName("findAll: Deve retornar lista de Categories convertendo os Nodes do banco")
    void shouldFindAllAndMapToDomain() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategoryNode));

        // Act
        List<Category> result = categoryPersistenceAdapter.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(mockCategoryId, result.get(0).getId());
        assertEquals(mockCategoryName, result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findById: Deve retornar Optional com Category quando encontrado")
    void shouldFindByIdAndMapToDomain() {
        // Arrange
        when(categoryRepository.findById(mockCategoryId)).thenReturn(Optional.of(mockCategoryNode));

        // Act
        Optional<Category> result = categoryPersistenceAdapter.findById(mockCategoryId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockCategoryId, result.get().getId());
        assertEquals(mockCategoryName, result.get().getName());
        verify(categoryRepository, times(1)).findById(mockCategoryId);
    }

    @Test
    @DisplayName("findById: Deve retornar Optional vazio quando não encontrar a categoria")
    void shouldReturnEmptyWhenIdNotFound() {
        // Arrange
        when(categoryRepository.findById(mockCategoryId)).thenReturn(Optional.empty());

        // Act
        Optional<Category> result = categoryPersistenceAdapter.findById(mockCategoryId);

        // Assert
        assertFalse(result.isPresent());
        verify(categoryRepository, times(1)).findById(mockCategoryId);
    }

    @Test
    @DisplayName("findByName: Deve retornar Optional com Category quando encontrado")
    void shouldFindByNameAndMapToDomain() {
        // Arrange
        when(categoryRepository.findByName(mockCategoryName)).thenReturn(Optional.of(mockCategoryNode));

        // Act
        Optional<Category> result = categoryPersistenceAdapter.findByName(mockCategoryName);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(mockCategoryId, result.get().getId());
        assertEquals(mockCategoryName, result.get().getName());
        verify(categoryRepository, times(1)).findByName(mockCategoryName);
    }

    @Test
    @DisplayName("save: Deve converter para Node, salvar no repositório e retornar a Category de Domínio mapeada")
    void shouldSaveCategoryAndMapToDomain() {
        // Arrange
        when(categoryRepository.save(any(CategoryNode.class))).thenReturn(mockCategoryNode);

        // Act
        Category savedCategory = categoryPersistenceAdapter.save(mockCategory);

        // Assert
        assertNotNull(savedCategory);
        assertEquals(mockCategoryId, savedCategory.getId());
        assertEquals(mockCategoryName, savedCategory.getName());
        verify(categoryRepository, times(1)).save(any(CategoryNode.class));
    }
}