package br.com.filpo.pokemart.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
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

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category mockCategory;
    private UUID mockId;

    @BeforeEach
    void setUp() {
        mockId = UUID.randomUUID();
        mockCategory = new Category(mockId, "Poké Balls");
    }

    @Test
    @DisplayName("Deve retornar uma lista com todas as categorias")
    void shouldListAllCategories() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of(mockCategory));

        // Act
        List<Category> result = categoryService.listAllCategories();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Poké Balls", result.get(0).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Deve retornar uma categoria quando o ID existir")
    void shouldGetCategoryByIdWhenExists() {
        // Arrange
        when(categoryRepository.findById(mockId)).thenReturn(Optional.of(mockCategory));

        // Act
        Category result = categoryService.getCategoryById(mockId);

        // Assert
        assertNotNull(result);
        assertEquals(mockId, result.getId());
        assertEquals("Poké Balls", result.getName());
        verify(categoryRepository, times(1)).findById(mockId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ID não existir")
    void shouldThrowExceptionWhenCategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(mockId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> categoryService.getCategoryById(mockId)
        );

        assertEquals("Category not found with ID: " + mockId, exception.getMessage());
        verify(categoryRepository, times(1)).findById(mockId);
    }

    @Test
    @DisplayName("Deve atribuir um novo UUID e salvar a categoria com sucesso")
    void shouldCreateCategory() {
        // Arrange
        Category newCategory = new Category(null, "Potions");
        
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Category result = categoryService.createCategory(newCategory);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId()); 
        assertEquals("Potions", result.getName());
        
        verify(categoryRepository, times(1)).save(any(Category.class));
    }
}