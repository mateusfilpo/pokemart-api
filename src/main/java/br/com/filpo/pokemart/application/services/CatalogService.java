package br.com.filpo.pokemart.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogUseCase {

    private final ItemRepositoryPort itemRepository;
    private final CategoryRepositoryPort categoryRepository; // ⚠️ Injetado para lidar com Categorias!

    @Override
    public List<Item> listAllAvailableItems() {
        return itemRepository.findAll(); // Temporário para o Admin ver tudo!

        // return itemRepository.findAll().stream()
        //         // Proteção contra o NPE que sofremos antes!
        //         .filter(item -> item.getDeleted() != null && !item.getDeleted())
        //         .collect(Collectors.toList());
    }

    @Override
    public Item getItemDetails(UUID id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item não encontrado: " + id));
    }

    @Override
    public Item createItem(ItemRequestDTO request) {
        Category category = resolveCategory(request.getCategory());

        Item newItem = Item.builder()
                .id(UUID.randomUUID())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImage())
                .category(category)
                .deleted(false)
                .build();

        return itemRepository.save(newItem);
    }

    @Override
    @Transactional
    public Item updateItem(UUID id, ItemRequestDTO request) {
        Item existingItem = getItemDetails(id);
        Category category = resolveCategory(request.getCategory());
// ⚠️ NOSSOS ESPIÕES:
        System.out.println("--- DEBUG UPDATE ITEM ---");
        System.out.println("Status atual no Banco: " + existingItem.getDeleted());
        System.out.println("Status recebido do Front: " + request.getDeleted());
        System.out.println("category: " + category);
        System.out.println("Request: " + request);

        Boolean newDeletedStatus = request.getDeleted() != null ? request.getDeleted() : existingItem.getDeleted();
        
        System.out.println("Status final que vamos tentar salvar: " + newDeletedStatus);
        // Recriamos o Item mantendo o ID original
        Item updatedItem = Item.builder()
                .id(existingItem.getId())
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .imageUrl(request.getImage())
                .category(category)
                .deleted(existingItem.getDeleted() != null ? existingItem.getDeleted() : false)
                .build();

        return itemRepository.save(updatedItem);
    }

    @Override
    public void toggleItemStatus(UUID id, boolean status) {
        itemRepository.updateStatus(id, status);
    }

    @Override
    public void deleteItem(UUID id) {
        // O Soft Delete agora usa a nossa rota ultrarrápida do Cypher!
        toggleItemStatus(id, true); 
    }

    // @Override
    // public void deleteItem(UUID id) {
    //     Item existingItem = getItemDetails(id);
        
    //     // Soft Delete: Mantém o ID e os dados, mas marca como deletado para o front-end ignorar
    //     Item deletedItem = Item.builder()
    //             .id(existingItem.getId())
    //             .name(existingItem.getName())
    //             .description(existingItem.getDescription())
    //             .price(existingItem.getPrice())
    //             .stock(existingItem.getStock())
    //             .imageUrl(existingItem.getImageUrl())
    //             .category(existingItem.getCategory())
    //             .deleted(true) // ⚠️ O item some da loja, mas os pedidos antigos não quebram!
    //             .build();
                
    //     itemRepository.save(deletedItem);
    // }

    // Método auxiliar (igual ao do Seeder) para buscar ou criar a Categoria
    private Category resolveCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseGet(() -> {
                    Category newCat = Category.builder()
                            .id(UUID.randomUUID())
                            .name(categoryName)
                            .build();
                    return categoryRepository.save(newCat);
                });
    }
}