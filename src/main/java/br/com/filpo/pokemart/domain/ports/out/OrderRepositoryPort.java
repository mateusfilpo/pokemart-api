package br.com.filpo.pokemart.domain.ports.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Order;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByUserId(UUID userId);
}