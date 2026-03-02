package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CatalogUseCase catalogUseCase;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listAllCategories() {
        List<CategoryResponseDTO> categories = categoryUseCase.listAllCategories().stream()
                .map(CategoryResponseDTO::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody Category category) {
        Category createdCategory = categoryUseCase.createCategory(category);
        return ResponseEntity.ok(CategoryResponseDTO.fromDomain(createdCategory));
    }

    @GetMapping("/stats")
    public ResponseEntity<List<CategoryStatsDTO>> getCategoryStats(
            @RequestParam(required = false) String search) {
        
        List<CategoryStatsDTO> stats = catalogUseCase.getCategoryStats(search);
        return ResponseEntity.ok(stats);
    }
}