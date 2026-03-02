package br.com.filpo.pokemart.domain.ports.in;

import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Order;

public interface CheckoutUseCase {
    Order placeOrder(UUID userId);
}