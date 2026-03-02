package br.com.filpo.pokemart.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;

public interface ItemRepositoryPort {
    Optional<Item> findById(UUID id);
    Item save(Item item);
    void deleteById(UUID id);
    void updateStatus(UUID id, boolean status);
    void saveAll(List<Item> items);
    PageResult<Item> findActiveItems(int page, int size, String category, String search, String sort);
    PageResult<Item> findAllItems(int page, int size, String category, String search, String sort);
    long count();
    List<CategoryStatsDTO> countActiveItemsByCategory(String search);
}