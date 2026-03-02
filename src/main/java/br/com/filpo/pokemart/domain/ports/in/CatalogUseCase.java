package br.com.filpo.pokemart.domain.ports.in;

import java.util.List;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;

public interface CatalogUseCase {
    Item getItemDetails(UUID id);
    Item createItem(ItemRequestDTO request);
    Item updateItem(UUID id, ItemRequestDTO request);
    void deleteItem(UUID id);
    void toggleItemStatus(UUID id, boolean status);
    PageResult<Item> getActiveItems(int page, int size, String category, String search, String sort);
    PageResult<Item> getAllItems(int page, int size, String category, String search, String sort);
    List<CategoryStatsDTO> getCategoryStats(String search);
}