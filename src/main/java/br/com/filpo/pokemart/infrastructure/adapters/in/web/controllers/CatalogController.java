package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemResponseDTO;
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
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/items")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "3. Catálogo", description = "Endpoints para visualização pública de produtos e gerenciamento de inventário (exclusivo para Administradores)")
public class CatalogController {

    private final CatalogUseCase catalogUseCase;

    @Operation(summary = "Listar Itens Ativos", description = "Retorna uma lista paginada de todos os itens disponíveis para venda. Permite filtros por categoria, busca textual e ordenação. Rota pública.")
    @ApiResponse(responseCode = "200", description = "Página de itens retornada com sucesso.")
    @GetMapping
    public ResponseEntity<PageResult<ItemResponseDTO>> getActiveItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "price-asc") String sort
    ) {
        PageResult<Item> result = catalogUseCase.getActiveItems(
            page,
            size,
            category,
            search,
            sort
        );

        var dtos = result
            .getData()
            .stream()
            .map(ItemResponseDTO::fromDomain)
            .collect(Collectors.toList());

        var response = PageResult.<ItemResponseDTO>builder()
            .data(dtos)
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .currentPage(result.getCurrentPage())
            .hasNext(result.isHasNext())
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Listar Todos os Itens (Admin)", description = "Retorna uma lista paginada incluindo itens inativos/deletados. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth") 
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Página de itens retornada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @GetMapping("/all")
    public ResponseEntity<PageResult<ItemResponseDTO>> getAllItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "price-asc") String sort
    ) {
        PageResult<Item> result = catalogUseCase.getAllItems(
            page,
            size,
            category,
            search,
            sort
        );

        var dtos = result
            .getData()
            .stream()
            .map(ItemResponseDTO::fromDomain)
            .collect(Collectors.toList());

        var response = PageResult.<ItemResponseDTO>builder()
            .data(dtos)
            .totalElements(result.getTotalElements())
            .totalPages(result.getTotalPages())
            .currentPage(result.getCurrentPage())
            .hasNext(result.isHasNext())
            .build();

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Detalhes do Item", description = "Busca as informações detalhadas de um item específico pelo seu ID. Rota pública.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item encontrado."),
        @ApiResponse(responseCode = "404", description = "Item não encontrado no banco de dados.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> getItemDetails(
        @PathVariable UUID id
    ) {
        var item = catalogUseCase.getItemDetails(id);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(item));
    }

    @Operation(summary = "Criar Novo Item", description = "Adiciona um novo produto ao catálogo. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Item criado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação (ex: preço negativo, nome vazio).", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @PostMapping
    public ResponseEntity<ItemResponseDTO> createItem(
        @RequestBody @Valid ItemRequestDTO request
    ) {
        Item created = catalogUseCase.createItem(request);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.getId())
            .toUri();

        return ResponseEntity.created(uri).body(
            ItemResponseDTO.fromDomain(created)
        );
    }

    @Operation(summary = "Atualizar Item", description = "Substitui completamente os dados de um item existente. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth") 
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação nos campos enviados.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDTO> updateItem(
        @PathVariable UUID id,
        @RequestBody @Valid ItemRequestDTO request
    ) {
        Item updated = catalogUseCase.updateItem(id, request);
        return ResponseEntity.ok(ItemResponseDTO.fromDomain(updated));
    }

    @Operation(summary = "Deletar Item", description = "Remove o item do catálogo (Exclusão lógica / Soft Delete). O registro é mantido no banco para histórico de pedidos. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth") 
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Item removido com sucesso (sem conteúdo no retorno)."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        catalogUseCase.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Alterar Status do Item", description = "Realiza um 'Soft Delete', ativando ou inativando um item para que ele não apareça na vitrine pública. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Status alterado com sucesso (sem conteúdo no retorno)."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> toggleStatus(
        @PathVariable UUID id,
        @RequestParam boolean deleted
    ) {
        catalogUseCase.toggleItemStatus(id, deleted);
        return ResponseEntity.noContent().build();
    }
}