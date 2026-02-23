package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCase categoryUseCase;

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
}