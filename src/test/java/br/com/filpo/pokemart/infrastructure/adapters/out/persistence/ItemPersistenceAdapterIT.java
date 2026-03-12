package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import br.com.filpo.pokemart.integration.AbstractIntegrationTest;

class ItemPersistenceAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private ItemPersistenceAdapter adapter;

    @Autowired
    private SpringDataItemRepository itemRepository;

    @Autowired
    private SpringDataCategoryRepository categoryRepository;

    private Category domainCategory;

    @BeforeEach
    void setUp() {
        itemRepository.deleteAll();
        categoryRepository.deleteAll();

        CategoryNode catNode = new CategoryNode();
        catNode.setId(UUID.randomUUID());
        catNode.setName("Poké Balls");
        catNode = categoryRepository.save(catNode);

        domainCategory = new Category(catNode.getId(), catNode.getName());
    }

    @Test
    @DisplayName("IT: Deve salvar um novo Item no Neo4j")
    void shouldSaveNewItemToNeo4j() {
        UUID itemId = UUID.randomUUID();
        Item newItem = Item.builder()
                .id(itemId) 
                .name("Master Ball")
                .price(9999.0)
                .stock(1)
                .category(domainCategory)
                .deleted(false)
                .build();

        Item savedItem = adapter.save(newItem);

        assertNotNull(savedItem.getId());
        assertEquals("Master Ball", savedItem.getName());
        assertTrue(itemRepository.findById(savedItem.getId()).isPresent());
    }

    @Test
    @DisplayName("IT: Deve atualizar um Item existente no Neo4j")
    void shouldUpdateExistingItemInNeo4j() {
        UUID itemId = UUID.randomUUID();
        Item item = adapter.save(Item.builder().id(itemId).name("Potion").price(100.0).stock(10).deleted(false).build());

        Item itemToUpdate = Item.builder()
                .id(item.getId())
                .name("Super Potion")
                .price(200.0)
                .stock(5)
                .deleted(false)
                .build();

        Item updatedItem = adapter.save(itemToUpdate);

        assertEquals(item.getId(), updatedItem.getId());
        assertEquals("Super Potion", updatedItem.getName());
        assertEquals(1, itemRepository.count());
    }

    @Test
    @DisplayName("IT: Deve buscar apenas itens ativos com paginação e filtro")
    void shouldFindActiveItemsWithFilters() {
        adapter.save(Item.builder().id(UUID.randomUUID()).name("Ultra Ball").price(800.0).category(domainCategory).deleted(false).build());
        adapter.save(Item.builder().id(UUID.randomUUID()).name("Great Ball").price(600.0).category(domainCategory).deleted(false).build());
        adapter.save(Item.builder().id(UUID.randomUUID()).name("Poke Ball").price(200.0).category(domainCategory).deleted(true).build());

        PageResult<Item> result = adapter.findActiveItems(0, 10, "Poké Balls", null, "price-asc");

        assertEquals(2, result.getTotalElements());
        assertEquals("Great Ball", result.getData().get(0).getName());
    }

    @Test
    @DisplayName("IT: Deve alterar o status lógico do item (Soft Delete)")
    void shouldUpdateItemStatus() {
        Item item = adapter.save(Item.builder().id(UUID.randomUUID()).name("Potion").deleted(false).build());

        adapter.updateStatus(item.getId(), true);

        var node = itemRepository.findById(item.getId()).get();
        assertTrue(node.getDeleted());
    }

    @Test
    @DisplayName("IT: Deve calcular as estatísticas de categorias ativas corretamente")
    void shouldCountActiveItemsByCategory() {
        CategoryNode healNode = categoryRepository.save(CategoryNode.builder().id(UUID.randomUUID()).name("Healing").build());
        Category healCat = new Category(healNode.getId(), healNode.getName());

        adapter.save(Item.builder().id(UUID.randomUUID()).name("Poke Ball").category(domainCategory).deleted(false).build());
        adapter.save(Item.builder().id(UUID.randomUUID()).name("Great Ball").category(domainCategory).deleted(true).build());
        adapter.save(Item.builder().id(UUID.randomUUID()).name("Potion").category(healCat).deleted(false).build());
        adapter.save(Item.builder().id(UUID.randomUUID()).name("Antidote").category(healCat).deleted(false).build());

        List<CategoryStatsDTO> stats = adapter.countActiveItemsByCategory(null);

        assertEquals(2, stats.size());
    }
}
