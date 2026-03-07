package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.ports.in.CartUseCase;
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
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "5. Carrinho", description = "Endpoints para gerenciamento do carrinho de compras do Treinador")
public class CartController {

    private final CartUseCase cartUseCase;

    @Operation(summary = "Visualizar Carrinho", description = "Retorna a lista de itens atualmente no carrinho do Treinador autenticado.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrinho retornado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @GetMapping
    public ResponseEntity<List<CartResponseDTO>> getCart(
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode user 
    ) {
        var cart = cartUseCase.getCart(user.getId());
        return ResponseEntity.ok(mapToResponse(cart));
    }

    @Operation(summary = "Adicionar ou Atualizar Item no Carrinho", description = "Adiciona um novo item ou atualiza a quantidade de um item existente no carrinho. Se a quantidade for 0, o item é removido.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Carrinho atualizado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Item não encontrado no catálogo.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de validação (ex: quantidade solicitada maior que o estoque disponível).", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @PostMapping
    public ResponseEntity<List<CartResponseDTO>> updateCart(
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode user, 
        @RequestBody CartItemRequestDTO request
    ) {
        var updatedCart = cartUseCase.updateCartItem(
            user.getId(),
            request.itemId(),
            request.quantity()
        );
        return ResponseEntity.ok(mapToResponse(updatedCart));
    }

    @Operation(summary = "Limpar Carrinho", description = "Remove todos os itens do carrinho do Treinador autenticado.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Carrinho esvaziado com sucesso (sem conteúdo no retorno)."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode user
    ) {
        cartUseCase.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }

    private List<CartResponseDTO> mapToResponse(List<CartItem> cartItems) {
        return cartItems
            .stream()
            .map(ci ->
                CartResponseDTO.builder()
                    .itemId(ci.getItem().getId())
                    .name(ci.getItem().getName())
                    .image(ci.getItem().getImageUrl())
                    .price(ci.getItem().getPrice())
                    .quantity(ci.getQuantity())
                    .stock(ci.getItem().getStock())
                    .deleted(ci.getItem().getDeleted())
                    .build()
            )
            .collect(Collectors.toList());
    }
}