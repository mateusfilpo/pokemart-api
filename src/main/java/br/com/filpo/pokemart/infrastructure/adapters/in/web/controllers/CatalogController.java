package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemResponseDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*") // Permite que o seu frontend local faça requisições sem erro de CORS
@RequiredArgsConstructor
public class CatalogController {

    // Injetamos a Interface (Porta de Entrada), e não a implementação direta!
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
}