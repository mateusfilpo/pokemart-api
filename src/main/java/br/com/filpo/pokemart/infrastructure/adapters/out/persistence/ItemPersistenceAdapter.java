package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.ItemMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ItemPersistenceAdapter implements ItemRepositoryPort {

    private final SpringDataItemRepository itemRepository;
    private final SpringDataCategoryRepository categoryRepository; // ⚠️ A PEÇA QUE FALTAVA!

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
        if (item.getId() != null) {
            Optional<ItemNode> existingNodeOpt = itemRepository.findById(item.getId());

            if (existingNodeOpt.isPresent()) {
                // ⚠️ É um UPDATE (Reativar, Pausar ou Editar)
                ItemNode existingNode = existingNodeOpt.get();
                existingNode.setName(item.getName());
                existingNode.setDescription(item.getDescription());
                existingNode.setPrice(item.getPrice());
                existingNode.setImageUrl(item.getImageUrl());
                existingNode.setStock(item.getStock());
                existingNode.setDeleted(item.getDeleted()); // Agora vai salvar 100%

                // Linka a Categoria de forma segura buscando o nó "oficial" do banco
                if (item.getCategory() != null) {
                    categoryRepository.findById(item.getCategory().getId())
                            .ifPresent(existingNode::setCategory);
                }

                var savedNode = itemRepository.save(existingNode);
                return ItemMapper.toDomain(savedNode);
            }
        }

        // ⚠️ É um INSERT (Criar novo item)
        var node = ItemMapper.toNode(item);
        if (item.getCategory() != null) {
            categoryRepository.findById(item.getCategory().getId())
                    .ifPresent(node::setCategory);
        }
        
        var savedNode = itemRepository.save(node);
        return ItemMapper.toDomain(savedNode);
    }

    @Override
    public void deleteById(UUID id) {
        itemRepository.deleteById(id);
    }

    @Override
    public void updateStatus(UUID id, boolean status) {
        itemRepository.updateStatus(id, status);
    }

    @Override
    public void saveAll(List<Item> items) {
        var nodes = items.stream()
                .map(ItemMapper::toNode)
                .collect(Collectors.toList());
        itemRepository.saveAll(nodes);
    }
}