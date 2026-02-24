package br.com.filpo.pokemart.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final OrderRepositoryPort orderRepository;

    @Override
    public User createUser(User user) {
        // Verifica se já existe um usuário com esse email (regra de negócio)
        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        });
        
        user.setId(UUID.randomUUID());
        return userRepository.save(user);
    }

    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + id));
    }

    @Override
    public List<Order> getUserOrderHistory(UUID userId) {
        // Busca os pedidos e atrela ao usuário para retornar ao front-end
        return orderRepository.findByUserId(userId);
    }

    @Override
    public User login(String email, String password) {
        // 1. Busca o usuário pelo e-mail
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        // 2. Confere se a senha bate (Lembre-se: no futuro usaremos BCrypt aqui!)
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Credenciais inválidas");
        }

        // 3. Se deu tudo certo, devolve o usuário
        return user;
    }
}