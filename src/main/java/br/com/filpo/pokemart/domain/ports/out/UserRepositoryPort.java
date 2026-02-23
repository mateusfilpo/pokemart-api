package br.com.filpo.pokemart.domain.ports.out;

import java.util.Optional;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.User;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    User save(User user);
}