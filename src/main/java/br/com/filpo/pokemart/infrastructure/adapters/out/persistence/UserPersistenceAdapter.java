package br.com.filpo.pokemart.infrastructure.adapters.out.persistence;

import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper.UserMapper;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
}