package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.LoginRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    // Busca os dados do Perfil
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable UUID id) {
        User user = userUseCase.getUserById(id);
        return ResponseEntity.ok(UserResponseDTO.fromDomain(user));
    }

    // Busca o Histórico de Pedidos
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable UUID id) {
        List<Order> orders = userUseCase.getUserOrderHistory(id);
        return ResponseEntity.ok(orders);
    }

    // Cria um novo usuário (Para a tela de Cadastro)
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody User user) {
        User createdUser = userUseCase.createUser(user);
        return ResponseEntity.ok(UserResponseDTO.fromDomain(createdUser));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> login(@RequestBody LoginRequestDTO request) {
        try {
            User user = userUseCase.login(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(UserResponseDTO.fromDomain(user));
        } catch (RuntimeException e) {
            // Se a senha estiver errada, devolvemos 401 Não Autorizado!
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}