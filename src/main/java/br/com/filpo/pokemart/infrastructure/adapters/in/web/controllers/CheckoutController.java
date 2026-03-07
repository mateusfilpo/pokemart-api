package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.ports.in.CheckoutUseCase;
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

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "6. Checkout", description = "Endpoints para processamento de compras e finalização de pedidos")
public class CheckoutController {

    private final CheckoutUseCase checkoutUseCase;

    @Operation(summary = "Finalizar Compra (Checkout)", description = "Lê o carrinho atual do Treinador autenticado, valida o estoque de cada item, gera um novo pedido com os preços congelados, esvazia o carrinho e retorna os detalhes do pedido gerado.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Compra finalizada com sucesso. Retorna o pedido no corpo e a URI de acesso no cabeçalho Location."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "422", description = "Erro de regra de negócio (ex: Carrinho vazio, estoque insuficiente, item desativado no momento da compra).", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode user
    ) {
        Order processedOrder = checkoutUseCase.placeOrder(user.getId());

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{userId}/orders")
                .buildAndExpand(user.getId()) 
                .toUri();

        OrderResponseDTO response = OrderResponseDTO.fromDomain(processedOrder);

        return ResponseEntity.created(uri).body(response);
    }
}