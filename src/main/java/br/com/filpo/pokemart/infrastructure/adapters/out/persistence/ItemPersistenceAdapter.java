package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.ItemMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.CategoryStatsProjection;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataCategoryRepository;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataItemRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ItemPersistenceAdapter implements ItemRepositoryPort {

    private final SpringDataItemRepository itemRepository;
    private final SpringDataCategoryRepository categoryRepository;

    @Override
    public PageResult<Item> findActiveItems(
        int page,
        int size,
        String category,
        String search,
        String sort
    ) {
        Page<ItemNode> pageNode = itemRepository.findActiveItemsWithFilters(
            category,
            search,
            buildPageRequest(page, size, sort, search)
        );
        return mapToPageResult(pageNode);
    }

    @Override
    public Optional<Item> findById(UUID id) {
        return itemRepository.findById(id).map(ItemMapper::toDomain);
    }

    @Override
    public Item save(Item item) {
        if (item.getId() != null) {
            Optional<ItemNode> existingNodeOpt = itemRepository.findById(
                item.getId()
            );

            if (existingNodeOpt.isPresent()) {
                ItemNode existingNode = existingNodeOpt.get();
                existingNode.setName(item.getName());
                existingNode.setDescription(item.getDescription());
                existingNode.setPrice(item.getPrice());
                existingNode.setImageUrl(item.getImageUrl());
                existingNode.setStock(item.getStock());
                existingNode.setDeleted(item.getDeleted());

                if (item.getCategory() != null) {
                    categoryRepository
                        .findById(item.getCategory().getId())
                        .ifPresent(existingNode::setCategory);
                }

                var savedNode = itemRepository.save(existingNode);
                return ItemMapper.toDomain(savedNode);
            }
        }

        var node = ItemMapper.toNode(item);
        if (item.getCategory() != null) {
            categoryRepository
                .findById(item.getCategory().getId())
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
        var nodes = items
            .stream()
            .map(ItemMapper::toNode)
            .collect(Collectors.toList());
        itemRepository.saveAll(nodes);
    }

    @Override
    public PageResult<Item> findAllItems(
        int page,
        int size,
        String category,
        String search,
        String sort
    ) {
        Page<ItemNode> pageNode = itemRepository.findAllItemsWithFilters(
            category,
            search,
            buildPageRequest(page, size, sort, search)
        );
        return mapToPageResult(pageNode);
    }

    @Override
    public long count() {
        return itemRepository.count();
    }

    @Override
    public List<CategoryStatsDTO> countActiveItemsByCategory(String search) {
        List<Map<String, Object>> results =
            itemRepository.countActiveItemsByCategory(search);

        return results
            .stream()
            .map(row ->
                CategoryStatsDTO.builder()
                    .category((String) row.get("category"))
                    .count((Long) row.get("count"))
                    .build()
            )
            .collect(Collectors.toList());
    }

    private PageRequest buildPageRequest(
        int page,
        int size,
        String sort,
        String search
    ) {
        Sort sortObj = Sort.unsorted();

        if (search != null && !search.trim().isEmpty()) {
            sortObj = Sort.by(Sort.Direction.DESC, "score");
        }

        if (sort != null && !sort.isEmpty()) {
            Sort userSort = Sort.unsorted();
            switch (sort) {
                case "price-asc":
                    userSort = Sort.by(Sort.Direction.ASC, "i.price");
                    break;
                case "price-desc":
                    userSort = Sort.by(Sort.Direction.DESC, "i.price");
                    break;
                case "name-asc":
                    userSort = Sort.by(Sort.Direction.ASC, "i.name");
                    break;
                case "name-desc":
                    userSort = Sort.by(Sort.Direction.DESC, "i.name");
                    break;
            }
            sortObj = sortObj.and(userSort);
        }

        return PageRequest.of(page, size, sortObj);
    }

    private PageResult<Item> mapToPageResult(Page<ItemNode> pageNode) {
        var items = pageNode
            .getContent()
            .stream()
            .map(ItemMapper::toDomain)
            .collect(Collectors.toList());

        return PageResult.<Item>builder()
            .data(items)
            .totalElements(pageNode.getTotalElements())
            .totalPages(pageNode.getTotalPages())
            .currentPage(pageNode.getNumber())
            .hasNext(pageNode.hasNext())
            .build();
    }
}
