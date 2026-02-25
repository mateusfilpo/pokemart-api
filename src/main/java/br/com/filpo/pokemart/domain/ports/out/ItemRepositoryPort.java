package br.com.filpo.pokemart.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Item;

public interface ItemRepositoryPort {
    List<Item> findAll();
    Optional<Item> findById(UUID id);
    Item save(Item item);
    void deleteById(UUID id);
    void updateStatus(UUID id, boolean status);
    void saveAll(List<Item> items);
}