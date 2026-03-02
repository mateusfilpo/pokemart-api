package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.UserMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository userRepository;

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(UserMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    @Override
    public User save(User user) {
        var node = UserMapper.toNode(user);
        var savedNode = userRepository.save(node);
        return UserMapper.toDomain(savedNode);
    }

    @Override
    public void upsertCartItem(UUID userId, UUID itemId, Integer quantity) {
        userRepository.upsertCartItem(userId, itemId, quantity);
    }

    @Override
    public void removeCartItem(UUID userId, UUID itemId) {
        userRepository.removeCartItem(userId, itemId);
    }

    @Override
    public void clearCart(UUID userId) {
        userRepository.clearCart(userId);
    }
}