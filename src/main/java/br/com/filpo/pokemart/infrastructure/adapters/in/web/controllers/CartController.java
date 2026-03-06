package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.ports.in.CartUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CartItemRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CartResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
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
public class CartController {

    private final CartUseCase cartUseCase;

    @GetMapping
    public ResponseEntity<List<CartResponseDTO>> getCart(
        @AuthenticationPrincipal UserNode user
    ) {
        var cart = cartUseCase.getCart(user.getId());
        return ResponseEntity.ok(mapToResponse(cart));
    }

    @PostMapping
    public ResponseEntity<List<CartResponseDTO>> updateCart(
        @AuthenticationPrincipal UserNode user,
        @RequestBody CartItemRequestDTO request
    ) {
        var updatedCart = cartUseCase.updateCartItem(
            user.getId(),
            request.itemId(),
            request.quantity()
        );
        return ResponseEntity.ok(mapToResponse(updatedCart));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(
        @AuthenticationPrincipal UserNode user
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
