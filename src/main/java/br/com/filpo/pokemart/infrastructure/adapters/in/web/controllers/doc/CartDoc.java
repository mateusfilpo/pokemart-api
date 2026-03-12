package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CartItemRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CartResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.CustomError;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "5. Carrinho", description = "Endpoints para gerenciamento do carrinho de compras do Treinador")
public interface CartDoc {

    @Operation(summary = "Visualizar Carrinho", description = "Retorna a lista de itens atualmente no carrinho do Treinador autenticado.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrinho retornado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<List<CartResponseDTO>> getCart(@Parameter(hidden = true) @AuthenticationPrincipal UserNode user);

    @Operation(summary = "Adicionar ou Atualizar Item no Carrinho", 
               description = "Adiciona um novo item ou atualiza a quantidade de um item existente no carrinho. Se a quantidade for 0, o item é removido.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrinho atualizado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Item não encontrado no catálogo.", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação (ex: estoque insuficiente).", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<List<CartResponseDTO>> updateCart(
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode user, 
        @RequestBody CartItemRequestDTO request
    );

    @Operation(summary = "Limpar Carrinho", description = "Remove todos os itens do carrinho do Treinador autenticado.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Carrinho esvaziado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<Void> clearCart(@Parameter(hidden = true) @AuthenticationPrincipal UserNode user);
}