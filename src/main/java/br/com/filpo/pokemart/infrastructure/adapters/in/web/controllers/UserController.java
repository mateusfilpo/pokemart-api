package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.OrderResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController {

    private final UserUseCase userUseCase;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserProfile(
        @PathVariable UUID id
    ) {
        User user = userUseCase.getUserById(id);
        return ResponseEntity.ok(UserResponseDTO.fromDomain(user));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable UUID id) {
        
        List<Order> orders = userUseCase.getUserOrderHistory(id);
        
        List<OrderResponseDTO> response = orders.stream()
                .map(OrderResponseDTO::fromDomain)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO request) {        
        User userToCreate = request.toDomain(); 
        
        User createdUser = userUseCase.createUser(userToCreate);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        
        return ResponseEntity.created(uri).body(UserResponseDTO.fromDomain(createdUser));
    }
}
