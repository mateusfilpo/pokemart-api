package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.OrderItem;
import br.com.filpo.pokemart.domain.ports.in.CheckoutUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CheckoutRequestDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/checkout")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutUseCase checkoutUseCase;

    @PostMapping
    public ResponseEntity<Order> placeOrder(@RequestBody CheckoutRequestDTO request) {
        
        Order orderToProcess = Order.builder()
                .items(request.getItems().stream()
                        .map(dto -> OrderItem.builder()
                                .productId(dto.getProductId())
                                .quantity(dto.getQuantity())
                                .build())
                        .collect(Collectors.toList()))
                .build();

        Order processedOrder = checkoutUseCase.placeOrder(request.getUserId(), orderToProcess);

        return ResponseEntity.ok(processedOrder);
    }
}