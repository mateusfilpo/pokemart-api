package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc;

import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.OrderResponseDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "6. Checkout", description = "Endpoints para processamento de compras e finalização de pedidos")
public interface CheckoutDoc {

    @Operation(
        summary = "Finalizar Compra (Checkout)", 
        description = "Lê o carrinho atual do Treinador autenticado, valida o estoque de cada item, gera um novo pedido com os preços congelados, esvazia o carrinho e retorna os detalhes do pedido gerado."
    )
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Compra finalizada com sucesso. Retorna o pedido no corpo e a URI de acesso no cabeçalho Location."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de regra de negócio (ex: Carrinho vazio, estoque insuficiente).", 
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<OrderResponseDTO> placeOrder(@Parameter(hidden = true) @AuthenticationPrincipal UserNode user);
}