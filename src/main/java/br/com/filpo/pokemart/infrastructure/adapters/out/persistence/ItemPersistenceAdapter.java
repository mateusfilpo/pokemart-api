package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.ItemMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemPersistenceAdapter implements ItemRepositoryPort {

    private final SpringDataItemRepository itemRepository;

    @Override
    public List<Item> findAll() {
        return itemRepository.findAll().stream()
                .map(ItemMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return itemRepository.findById(id).map(ItemMapper::toDomain);
    }

    @Override
    public Item save(Item item) {
        var node = ItemMapper.toNode(item);
        var savedNode = itemRepository.save(node);
        return ItemMapper.toDomain(savedNode);
    }

    @Override
    public void deleteById(UUID id) {
        itemRepository.deleteById(id);
    }
}