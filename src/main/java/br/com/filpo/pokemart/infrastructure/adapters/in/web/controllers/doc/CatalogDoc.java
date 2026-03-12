package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.filpo.pokemart.domain.models.PageResult;
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

@Tag(name = "3. Catálogo", description = "Endpoints para visualização pública de produtos e gerenciamento de inventário (exclusivo para Administradores)")
public interface CatalogDoc {

    @Operation(summary = "Listar Itens Ativos", description = "Retorna uma lista paginada de todos os itens disponíveis para venda. Permite filtros por categoria, busca textual e ordenação. Rota pública.")
    @ApiResponse(responseCode = "200", description = "Página de itens retornada com sucesso.")
    ResponseEntity<PageResult<ItemResponseDTO>> getActiveItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "price-asc") String sort
    );

    @Operation(summary = "Listar Todos os Itens (Admin)", description = "Retorna uma lista paginada incluindo itens inativos/deletados. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Página de itens retornada com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<PageResult<ItemResponseDTO>> getAllItems(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "price-asc") String sort
    );

    @Operation(summary = "Detalhes do Item", description = "Busca as informações detalhadas de um item específico pelo seu ID. Rota pública.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item encontrado."),
        @ApiResponse(responseCode = "404", description = "Item não encontrado no banco de dados.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<ItemResponseDTO> getItemDetails(UUID id);

    @Operation(summary = "Criar Novo Item", description = "Adiciona um novo produto ao catálogo. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Item criado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado. Apenas administradores.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<ItemResponseDTO> createItem(@RequestBody @Valid ItemRequestDTO request);

    @Operation(summary = "Atualizar Item", description = "Substitui completamente os dados de um item existente. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso."),
        @ApiResponse(responseCode = "404", description = "Item não encontrado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<ItemResponseDTO> updateItem(UUID id, @RequestBody @Valid ItemRequestDTO request);

    @Operation(summary = "Deletar Item", description = "Remove o item do catálogo (Exclusão lógica). Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "204", description = "Item removido com sucesso.")
    ResponseEntity<Void> deleteItem(UUID id);

    @Operation(summary = "Alterar Status do Item", description = "Ativa ou inativa um item no catálogo. Requer privilégios de ADMIN.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "204", description = "Status alterado com sucesso.")
    ResponseEntity<Void> toggleStatus(UUID id, boolean deleted);
}