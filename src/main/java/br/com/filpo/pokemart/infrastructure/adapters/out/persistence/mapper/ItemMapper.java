package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;

public class ItemMapper {

    public static Item toDomain(ItemNode node) {
        if (node == null) return null;

        Category domainCategory = null;
        if (node.getCategory() != null) {
            domainCategory = Category.builder()
                    .id(node.getCategory().getId())
                    .name(node.getCategory().getName())
                    .build();
        }

        return Item.builder()
                .id(node.getId())
                .name(node.getName())
                .description(node.getDescription())
                .price(node.getPrice())
                .imageUrl(node.getImageUrl())
                .stock(node.getStock())
                .deleted(node.getDeleted())
                .category(domainCategory)
                .build();
    }

    public static ItemNode toNode(Item domain) {
        if (domain == null) return null;

        CategoryNode categoryNode = null;
        if (domain.getCategory() != null) {
            categoryNode = CategoryNode.builder()
                    .id(domain.getCategory().getId())
                    .name(domain.getCategory().getName())
                    .build();
        }

        return ItemNode.builder()
                .id(domain.getId())
                .name(domain.getName())
                .description(domain.getDescription())
                .price(domain.getPrice())
                .imageUrl(domain.getImageUrl())
                .stock(domain.getStock())
                .deleted(domain.getDeleted())
                .category(categoryNode)
                .build();
    }
}