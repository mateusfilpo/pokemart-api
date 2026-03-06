package br.com.filpo.pokemart.application.services;

import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import jakarta.transaction.Transactional;
import java.text.Normalizer;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogUseCase {

    private final ItemRepositoryPort itemRepository;
    private final CategoryRepositoryPort categoryRepository;

    @Override
    public Item getItemDetails(UUID id) {
        return itemRepository
            .findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("Item not found with ID: " + id)
            );
    }

    @Override
    @CacheEvict(
        value = { "vitrine", "adminVitrine", "categoryStats" },
        allEntries = true
    )
    public Item createItem(ItemRequestDTO request) {
        Category category = resolveCategory(request.getCategory());

        Item newItem = Item.builder()
            .id(UUID.randomUUID())
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .imageUrl(request.getImage())
            .category(category)
            .deleted(false)
            .build();

        return itemRepository.save(newItem);
    }

    @Override
    @Transactional
    @CacheEvict(
        value = { "vitrine", "adminVitrine", "categoryStats" },
        allEntries = true
    )
    public Item updateItem(UUID id, ItemRequestDTO request) {
        Item existingItem = getItemDetails(id);
        Category category = resolveCategory(request.getCategory());

        Item updatedItem = Item.builder()
            .id(existingItem.getId())
            .name(request.getName())
            .description(request.getDescription())
            .price(request.getPrice())
            .stock(request.getStock())
            .imageUrl(request.getImage())
            .category(category)
            .deleted(
                request.getDeleted() != null
                    ? request.getDeleted()
                    : existingItem.getDeleted()
            )
            .build();

        return itemRepository.save(updatedItem);
    }

    @Override
    @CacheEvict(
        value = { "vitrine", "adminVitrine", "categoryStats" },
        allEntries = true
    )
    public void toggleItemStatus(UUID id, boolean status) {
        itemRepository.updateStatus(id, status);
    }

    @Override
    public void deleteItem(UUID id) {
        toggleItemStatus(id, true);
    }

    private Category resolveCategory(String categoryName) {
        return categoryRepository
            .findByName(categoryName)
            .orElseGet(() -> {
                Category newCat = Category.builder()
                    .id(UUID.randomUUID())
                    .name(categoryName)
                    .build();
                return categoryRepository.save(newCat);
            });
    }

    @Override
    @Cacheable(
        value = "vitrine",
        key = "{#page, #size, #category, #search, #sort}"
    )
    public PageResult<Item> getActiveItems(
        int page,
        int size,
        String category,
        String search,
        String sort
    ) {
        return itemRepository.findActiveItems(
            page,
            size,
            category,
            normalizeText(search),
            sort
        );
    }

    @Override
    @Cacheable(
        value = "adminVitrine",
        key = "{#page, #size, #category, #search, #sort}"
    )
    public PageResult<Item> getAllItems(
        int page,
        int size,
        String category,
        String search,
        String sort
    ) {
        return itemRepository.findAllItems(page, size, category, search, sort);
    }

    @Override
    @Cacheable(value = "categoryStats", key = "#search != null ? #search : ''")
    public List<CategoryStatsDTO> getCategoryStats(String search) {
        String safeSearch = normalizeText(search);
        return itemRepository.countActiveItemsByCategory(safeSearch);
    }

    private static String normalizeText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase();
    }
}
