package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

@Tag(name = "4. Categorias", description = "Endpoints para gerenciamento do menu de categorias e estatísticas do catálogo")
public interface CategoryDoc {

    @Operation(summary = "Listar Categorias", description = "Retorna uma lista simples com todas as categorias ativas no sistema. Rota pública.")
    @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso.")
    ResponseEntity<List<CategoryResponseDTO>> listAllCategories();

    @Operation(summary = "Criar Nova Categoria", description = "Adiciona uma nova categoria ao sistema. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth") 
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", 
            content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", 
            content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação (ex: nome já existente).", 
            content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody @Valid CategoryRequestDTO request);

    @Operation(summary = "Estatísticas de Categorias", description = "Retorna a contagem de itens ativos por categoria. Se um termo de busca for fornecido, a contagem refletirá apenas os itens que correspondem à busca. Rota pública.")
    @ApiResponse(responseCode = "200", description = "Estatísticas calculadas e retornadas com sucesso.")
    ResponseEntity<List<CategoryStatsDTO>> getCategoryStats(@RequestParam(required = false) String search);
}