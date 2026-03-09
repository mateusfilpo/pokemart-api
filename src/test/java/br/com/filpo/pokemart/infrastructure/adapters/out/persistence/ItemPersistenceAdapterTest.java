package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemPersistenceAdapterTest {

    @InjectMocks
    private ItemPersistenceAdapter itemPersistenceAdapter;

    @Mock
    private SpringDataItemRepository itemRepository;

    @Mock
    private SpringDataCategoryRepository categoryRepository;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestCaptor;

    private UUID mockItemId;
    private UUID mockCategoryId;
    private Item mockItem;
    private ItemNode mockItemNode;
    private Category mockCategory;
    private CategoryNode mockCategoryNode;

    @BeforeEach
    void setUp() {
        mockItemId = UUID.randomUUID();
        mockCategoryId = UUID.randomUUID();

        mockCategory = new Category(mockCategoryId, "Potions");
        mockCategoryNode = CategoryNode.builder().id(mockCategoryId).name("Potions").build();

        mockItem = Item.builder()
            .id(mockItemId)
            .name("Max Potion")
            .price(2500.0)
            .stock(10)
            .category(mockCategory)
            .build();

        mockItemNode = ItemNode.builder()
            .id(mockItemId)
            .name("Max Potion")
            .price(2500.0)
            .stock(10)
            .category(mockCategoryNode)
            .build();
    }

    @Test
    @DisplayName("findById: Deve retornar Optional com Item quando encontrado")
    void shouldFindByIdAndMapToDomain() {
        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockItemNode));

        Optional<Item> result = itemPersistenceAdapter.findById(mockItemId);

        assertTrue(result.isPresent());
        assertEquals("Max Potion", result.get().getName());
        verify(itemRepository, times(1)).findById(mockItemId);
    }

    @Test
    @DisplayName("save: Deve atualizar Item existente preservando o Node, caso o ID já exista")
    void shouldUpdateExistingItemWhenIdIsPresent() {
        // Arrange
        Item updateRequest = Item.builder()
            .id(mockItemId)
            .name("Hyper Potion")
            .price(1200.0)
            .category(mockCategory)
            .build();

        when(itemRepository.findById(mockItemId)).thenReturn(Optional.of(mockItemNode));
        when(categoryRepository.findById(mockCategoryId)).thenReturn(Optional.of(mockCategoryNode));
        when(itemRepository.save(any(ItemNode.class))).thenReturn(mockItemNode);

        // Act
        Item result = itemPersistenceAdapter.save(updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Hyper Potion", mockItemNode.getName());
        
        verify(itemRepository, times(1)).findById(mockItemId);
        verify(categoryRepository, times(1)).findById(mockCategoryId);
        verify(itemRepository, times(1)).save(mockItemNode);
    }

    @Test
    @DisplayName("save: Deve criar novo ItemNode quando o ID é nulo")
    void shouldCreateNewItemWhenIdIsNull() {
        // Arrange
        Item newItem = Item.builder()
            .name("Poke Ball")
            .price(200.0)
            .category(mockCategory)
            .build();

        when(categoryRepository.findById(mockCategoryId)).thenReturn(Optional.of(mockCategoryNode));
        when(itemRepository.save(any(ItemNode.class))).thenReturn(mockItemNode);

        // Act
        Item result = itemPersistenceAdapter.save(newItem);

        // Assert
        assertNotNull(result);
        verify(itemRepository, never()).findById(any());
        verify(categoryRepository, times(1)).findById(mockCategoryId);
        verify(itemRepository, times(1)).save(any(ItemNode.class));
    }

    @Test
    @DisplayName("findActiveItems: Deve construir o PageRequest correto com base nos parâmetros e mapear o resultado")
    void shouldFindActiveItemsAndMapToPageResult() {
        // Arrange
        Page<ItemNode> springPage = new PageImpl<>(List.of(mockItemNode), PageRequest.of(0, 10), 1);
        when(itemRepository.findActiveItemsWithFilters(eq("Potions"), eq("ball"), any(PageRequest.class)))
            .thenReturn(springPage);

        // Act
        PageResult<Item> result = itemPersistenceAdapter.findActiveItems(0, 10, "Potions", "ball", "price-desc");

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getData().size());
        assertEquals("Max Potion", result.getData().get(0).getName());

        verify(itemRepository).findActiveItemsWithFilters(eq("Potions"), eq("ball"), pageRequestCaptor.capture());
        PageRequest capturedRequest = pageRequestCaptor.getValue();
        assertTrue(capturedRequest.getSort().toString().contains("i.price: DESC"));
    }

    @Test
    @DisplayName("deleteById: Deve chamar o deleteById do repositório")
    void shouldDeleteById() {
        itemPersistenceAdapter.deleteById(mockItemId);
        verify(itemRepository, times(1)).deleteById(mockItemId);
    }

    @Test
    @DisplayName("updateStatus: Deve chamar o updateStatus do repositório")
    void shouldUpdateStatus() {
        itemPersistenceAdapter.updateStatus(mockItemId, true);
        verify(itemRepository, times(1)).updateStatus(mockItemId, true);
    }

    @Test
    @DisplayName("countActiveItemsByCategory: Deve mapear o retorno do Map<String, Object> para CategoryStatsDTO")
    void shouldCountActiveItemsByCategoryAndMapToDto() {
        // Arrange
        Map<String, Object> row1 = Map.of("category", "Potions", "count", 42L);
        Map<String, Object> row2 = Map.of("category", "Pokeballs", "count", 10L);
        
        when(itemRepository.countActiveItemsByCategory("potion")).thenReturn(List.of(row1, row2));

        // Act
        List<CategoryStatsDTO> result = itemPersistenceAdapter.countActiveItemsByCategory("potion");

        // Assert
        assertEquals(2, result.size());
        assertEquals("Potions", result.get(0).getCategory());
        assertEquals(42L, result.get(0).getCount());
        assertEquals("Pokeballs", result.get(1).getCategory());
        assertEquals(10L, result.get(1).getCount());
    }
}