package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.net.URI;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc.CatalogDoc;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CatalogController implements CatalogDoc {

    private final CatalogUseCase catalogUseCase;

    @Override
    @GetMapping
    public ResponseEntity<PageResult<ItemResponseDTO>> getActiveItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "price-asc") String sort
    ) {
        PageResult<Item> result = catalogUseCase.getActiveItems(page, size, category, search, sort);
        return ResponseEntity.ok(mapToPageResultDTO(result));
    }

    @Override
    @GetMapping("/all")
    public ResponseEntity<PageResult<ItemResponseDTO>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "price-asc") String sort
    ) {
        PageResult<Item> result = catalogUseCase.getAllItems(page, size, category, search, sort);
        return ResponseEntity.ok(mapToPageResultDTO(result));
    }

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemDetails(@PathVariable UUID id) {
        var item = catalogUseCase.getItemDetails(id);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(item));
    }

    @Override
    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(@RequestBody @Valid ItemRequestDTO request) {
        Item created = catalogUseCase.createItem(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(uri).body(ItemResponseDTO.fromDomain(created));
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(@PathVariable UUID id, @RequestBody @Valid ItemRequestDTO request) {
        Item updated = catalogUseCase.updateItem(id, request);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(updated));
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        catalogUseCase.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(@PathVariable UUID id, @RequestParam boolean deleted) {
        catalogUseCase.toggleItemStatus(id, deleted);
        return ResponseEntity.noContent().build();
    }

    private PageResult<ItemResponseDTO> mapToPageResultDTO(PageResult<Item> result) {
        var dtos = result.getData().stream()
                .map(ItemResponseDTO::fromDomain)
                .collect(Collectors.toList());

        return PageResult.<ItemResponseDTO>builder()
                .data(dtos)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .currentPage(result.getCurrentPage())
                .hasNext(result.isHasNext())
                .build();
    }
}