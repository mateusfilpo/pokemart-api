package br.com.filpo.pokemart.domain.ports.in;

import java.util.List;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;

public interface CatalogUseCase {
    List<Item> listAllAvailableItems();
    Item getItemDetails(UUID id);
    Item createItem(ItemRequestDTO request);
    Item updateItem(UUID id, ItemRequestDTO request);
    void deleteItem(UUID id);
    void toggleItemStatus(UUID id, boolean status);
}