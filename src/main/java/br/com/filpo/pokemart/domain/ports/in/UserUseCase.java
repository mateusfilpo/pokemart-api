package br.com.filpo.pokemart.domain.ports.in;

import java.util.List;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;

public interface UserUseCase {
    User createUser(User user);
    User getUserById(UUID id);
    List<Order> getUserOrderHistory(UUID userId);
    User login(String email, String password);
}