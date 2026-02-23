package br.com.filpo.pokemart.application.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogUseCase {

    private final ItemRepositoryPort itemRepository;

    @Override
    public List<Item> listAllAvailableItems() {
        return itemRepository.findAll().stream()
                .filter(item -> !item.getDeleted())
                .collect(Collectors.toList());
    }

    @Override
    public Item getItemDetails(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado: " + id));
    }
}