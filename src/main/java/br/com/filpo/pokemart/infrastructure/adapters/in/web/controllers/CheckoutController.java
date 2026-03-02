package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.ports.in.CheckoutUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutUseCase checkoutUseCase;

    @PostMapping
    public ResponseEntity<Order> placeOrder(
        @AuthenticationPrincipal UserNode user
    ) {
        Order processedOrder = checkoutUseCase.placeOrder(user.getId());

        return ResponseEntity.ok(processedOrder);
    }
}
