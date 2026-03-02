package br.com.filpo.pokemart.domain.ports.out;

import java.util.Optional;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.User;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    User save(User user);
    void upsertCartItem(UUID userId, UUID itemId, Integer quantity);
    void removeCartItem(UUID userId, UUID itemId);
    void clearCart(UUID userId);
}