package br.com.filpo.pokemart.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.models.UserRole;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final OrderRepositoryPort orderRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User user) {
        userRepository.findByEmail(user.getEmail()).ifPresent(u -> {
            throw new BusinessRuleException("Email is already in use.");
        });
        
        user.setId(UUID.randomUUID());
        
        String encryptedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encryptedPassword);
        
        user.setRole(UserRole.USER);
        
        return userRepository.save(user);
    }
    
    @Override
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Override
    public List<Order> getUserOrderHistory(UUID userId) {
        return orderRepository.findByUserId(userId);
    }
}