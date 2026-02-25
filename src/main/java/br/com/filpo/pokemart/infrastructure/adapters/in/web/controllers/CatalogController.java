package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemResponseDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*") 
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogUseCase catalogUseCase;

    @GetMapping
    public ResponseEntity<List<ItemResponseDTO>> listAllItems() {
        List<ItemResponseDTO> items = catalogUseCase.listAllAvailableItems()
                .stream()
                .map(ItemResponseDTO::fromDomain)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(items);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemDetails(@PathVariable UUID id) {
        var item = catalogUseCase.getItemDetails(id);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(item));
    }

    // ⚠️ NOVO: Criar Produto
    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@RequestBody ItemRequestDTO request) {
        Item created = catalogUseCase.createItem(request);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(created));
    }

    // ⚠️ NOVO: Editar Produto
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(@PathVariable UUID id, @RequestBody ItemRequestDTO request) {
        Item updated = catalogUseCase.updateItem(id, request);
        System.out.println("Updated Item: " + updated);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(updated));
    }

    // ⚠️ NOVO: Deletar Produto (Soft Delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        catalogUseCase.deleteItem(id);
        return ResponseEntity.noContent().build(); // Retorna 204 (Sucesso sem corpo)
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id, @RequestParam boolean deleted) {
        catalogUseCase.toggleItemStatus(id, deleted);
        return ResponseEntity.noContent().build();
    }
}