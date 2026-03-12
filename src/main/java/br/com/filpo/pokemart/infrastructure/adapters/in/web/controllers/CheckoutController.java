package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.ports.in.CheckoutUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc.CheckoutDoc;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.OrderResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CheckoutController implements CheckoutDoc {

    private final CheckoutUseCase checkoutUseCase;

    @Override
    @PostMapping
    public ResponseEntity<OrderResponseDTO> placeOrder(@AuthenticationPrincipal UserNode user) {
        Order processedOrder = checkoutUseCase.placeOrder(user.getId());

        URI uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/users/{userId}/orders")
                .buildAndExpand(user.getId()) 
                .toUri();

        OrderResponseDTO response = OrderResponseDTO.fromDomain(processedOrder);

        return ResponseEntity.created(uri).body(response);
    }
}