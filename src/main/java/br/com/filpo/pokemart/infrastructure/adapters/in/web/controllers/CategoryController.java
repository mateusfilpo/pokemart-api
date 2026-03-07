package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.CustomError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "4. Categorias", description = "Endpoints para gerenciamento do menu de categorias e estatísticas do catálogo")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CatalogUseCase catalogUseCase;

    @Operation(summary = "Listar Categorias", description = "Retorna uma lista simples com todas as categorias ativas no sistema. Rota pública.")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso.")
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> listAllCategories() {
        List<CategoryResponseDTO> categories = categoryUseCase
            .listAllCategories()
            .stream()
            .map(CategoryResponseDTO::fromDomain)
            .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Criar Nova Categoria", description = "Adiciona uma nova categoria ao sistema. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth") 
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação (ex: nome da categoria em branco ou já existente).", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
        @RequestBody @Valid CategoryRequestDTO request
    ) {
        Category categoryToCreate = request.toDomain();

        Category createdCategory = categoryUseCase.createCategory(
            categoryToCreate
        );

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdCategory.getId())
            .toUri();

        return ResponseEntity.created(uri).body(
            CategoryResponseDTO.fromDomain(createdCategory)
        );
    }

    @Operation(summary = "Estatísticas de Categorias", description = "Retorna a contagem de itens ativos por categoria. Se um termo de busca for fornecido, a contagem refletirá apenas os itens que correspondem à busca. Rota pública.")
    @ApiResponse(responseCode = "200", description = "Estatísticas calculadas e retornadas com sucesso.")
    @GetMapping("/stats")
    public ResponseEntity<List<CategoryStatsDTO>> getCategoryStats(
        @RequestParam(required = false) String search
    ) {
        List<CategoryStatsDTO> stats = catalogUseCase.getCategoryStats(search);
        return ResponseEntity.ok(stats);
    }
}