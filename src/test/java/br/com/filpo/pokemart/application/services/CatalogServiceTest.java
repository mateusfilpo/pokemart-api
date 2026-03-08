package br.com.filpo.pokemart.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ItemRepositoryPort itemRepository;

    @Mock
    private CategoryRepositoryPort categoryRepository;

    @InjectMocks
    private CatalogService catalogService;

    private UUID mockItemId;
    private Item mockItem;
    private Category mockCategory;
    private ItemRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        mockItemId = UUID.randomUUID();
        mockCategory = new Category(UUID.randomUUID(), "Poké Balls");

        mockItem = Item.builder()
            .id(mockItemId)
            .name("Ultra Ball")
            .description("A high-performance Ball.")
            .price(1200.0)
            .stock(50)
            .category(mockCategory)
            .deleted(false)
            .build();

        requestDTO = new ItemRequestDTO();
        requestDTO.setName("Ultra Ball");
        requestDTO.setDescription("A high-performance Ball.");
        requestDTO.setCategory("Poké Balls");
        requestDTO.setPrice(1200.0);
        requestDTO.setStock(50);
        requestDTO.setImage("url");
    }

    @Test
    @DisplayName("Deve retornar detalhes do item quando o ID existir")
    void shouldGetItemDetailsWhenExists() {
        // Arrange
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockItem));

        // Act
        Item result = catalogService.getItemDetails(mockItemId);

        // Assert
        assertNotNull(result);
        assertEquals(mockItemId, result.getId());
        assertEquals("Ultra Ball", result.getName());
        verify(itemRepository, times(1)).findById(mockItemId);
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o item não for encontrado")
    void shouldThrowExceptionWhenItemNotFound() {
        // Arrange
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> catalogService.getItemDetails(mockItemId)
        );

        assertEquals("Item not found with ID: " + mockItemId, exception.getMessage());
        verify(itemRepository, times(1)).findById(mockItemId);
    }

    @Test
    @DisplayName("Deve criar um item vinculando a uma categoria já existente")
    void shouldCreateItemWithExistingCategory() {
        // Arrange
        when(categoryRepository.findByName("Poké Balls")).thenReturn(Optional.of(mockCategory));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Item result = catalogService.createItem(requestDTO);

        // Assert
        assertNotNull(result.getId());
        assertEquals("Ultra Ball", result.getName());
        assertEquals(mockCategory, result.getCategory());
        
        verify(categoryRepository, times(1)).findByName("Poké Balls");
        verify(categoryRepository, never()).save(any(Category.class)); 
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve criar uma nova categoria automaticamente ao criar um item, se ela não existir")
    void shouldCreateItemAndNewCategoryIfNotExists() {
        // Arrange
        requestDTO.setCategory("Nova Categoria");
        when(categoryRepository.findByName("Nova Categoria")).thenReturn(Optional.empty()); 
        
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Item result = catalogService.createItem(requestDTO);

        // Assert
        assertEquals("Nova Categoria", result.getCategory().getName());
        verify(categoryRepository, times(1)).save(any(Category.class)); 
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve atualizar o item com sucesso")
    void shouldUpdateItemSuccessfully() {
        // Arrange
        requestDTO.setName("Master Ball");
        requestDTO.setPrice(9999.0);
        
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockItem));
        when(categoryRepository.findByName("Poké Balls")).thenReturn(Optional.of(mockCategory));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Item result = catalogService.updateItem(mockItemId, requestDTO);

        // Assert
        assertEquals(mockItemId, result.getId()); 
        assertEquals("Master Ball", result.getName());
        assertEquals(9999.0, result.getPrice());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    @DisplayName("Deve realizar um Soft Delete corretamente chamando o toggleItemStatus")
    void shouldSoftDeleteItem() {
        // Act
        catalogService.deleteItem(mockItemId);

        // Assert
        verify(itemRepository, times(1)).updateStatus(mockItemId, true);
    }

    @Test
    @DisplayName("Deve buscar itens ativos e normalizar o texto da busca (remover acentos e minúsculas)")
    void shouldGetActiveItemsAndNormalizeSearchText() {
        // Arrange
        String termoBusca = "Pókê Bâll"; 
        PageResult<Item> mockPage = new PageResult<>(List.of(mockItem), 1L, 1, 0, false);
        
        when(itemRepository.findActiveItems(0, 10, null, "poke ball", "price-asc"))
            .thenReturn(mockPage);

        // Act
        PageResult<Item> result = catalogService.getActiveItems(0, 10, null, termoBusca, "price-asc");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        
        verify(itemRepository, times(1)).findActiveItems(
            eq(0), eq(10), isNull(), eq("poke ball"), eq("price-asc")
        );
    }
}