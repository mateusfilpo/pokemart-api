package br.com.filpo.pokemart.domain.ports.in;

import java.util.List;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Item;

public interface CatalogUseCase {
    List<Item> listAllAvailableItems();
    Item getItemDetails(UUID id);
}