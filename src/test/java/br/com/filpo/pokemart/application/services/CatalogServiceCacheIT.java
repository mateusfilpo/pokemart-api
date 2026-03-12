package br.com.filpo.pokemart.application.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import br.com.filpo.pokemart.integration.AbstractIntegrationTest;

class CatalogServiceCacheIT extends AbstractIntegrationTest {

    @Autowired
    private CatalogUseCase catalogService;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private ItemRepositoryPort itemRepositorySpy;

    @BeforeEach
    void setUp() {
        cacheManager
            .getCacheNames()
            .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
    }

    @Test
    @DisplayName(
        "IT (Redis): Deve salvar a vitrine no cache na primeira busca e retornar do cache na segunda"
    )
    void shouldCacheActiveItems() {
        // Arrange
        int page = 0;
        int size = 10;
        String category = "Poké Balls";
        String search = null;
        String sort = "price-asc";

        PageResult<Item> firstCall = catalogService.getActiveItems(
            page,
            size,
            category,
            search,
            sort
        );

        PageResult<Item> secondCall = catalogService.getActiveItems(
            page,
            size,
            category,
            search,
            sort
        );

        // Assert
        assertNotNull(firstCall);
        assertNotNull(secondCall);

        verify(itemRepositorySpy, times(1)).findActiveItems(
            page,
            size,
            category,
            "",
            sort
        ); 
    }

    @Test
    @DisplayName(
        "IT (Redis): Deve limpar o cache da vitrine (CacheEvict) quando um novo item for criado"
    )
    void shouldEvictCacheWhenItemIsCreated() throws InterruptedException {
        // Arrange
        int page = 0;
        int size = 10;
        String category = "Healing";
        String search = null;
        String sort = "price-asc";

        ItemRequestDTO newItemRequest = new ItemRequestDTO();
        newItemRequest.setName("Super Potion");
        newItemRequest.setPrice(200.0);
        newItemRequest.setStock(10);
        newItemRequest.setCategory("Healing");

        catalogService.getActiveItems(page, size, category, search, sort);

        catalogService.createItem(newItemRequest);

        Thread.sleep(500);

        catalogService.getActiveItems(page, size, category, search, sort);

        // Assert
        verify(itemRepositorySpy, times(2)).findActiveItems(
            page,
            size,
            category,
            "",
            sort
        );
    }

    @Test
    @DisplayName(
        "IT (Redis): As estatísticas de categoria devem ser cacheadas corretamente"
    )
    void shouldCacheCategoryStats() {
        catalogService.getCategoryStats(null);

        catalogService.getCategoryStats(null);

        verify(itemRepositorySpy, times(1)).countActiveItemsByCategory("");
    }
}
