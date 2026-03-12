package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc.UserDoc;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.OrderResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserController implements UserDoc {

    private final UserUseCase userUseCase;

    @Override
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserProfile(@PathVariable UUID id, @AuthenticationPrincipal UserNode loggedUser) {
        checkPermission(id, loggedUser);
        User user = userUseCase.getUserById(id);
        return ResponseEntity.ok(UserResponseDTO.fromDomain(user));
    }

    @Override
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(@PathVariable UUID id, @AuthenticationPrincipal UserNode loggedUser) {
        checkPermission(id, loggedUser);
        List<Order> orders = userUseCase.getUserOrderHistory(id);
        List<OrderResponseDTO> response = orders.stream()
                .map(OrderResponseDTO::fromDomain)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO request) {
        User createdUser = userUseCase.createUser(request.toDomain());
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getId())
                .toUri();
        return ResponseEntity.created(uri).body(UserResponseDTO.fromDomain(createdUser));
    }

    private void checkPermission(UUID targetId, UserNode loggedUser) {
        if (!loggedUser.getId().equals(targetId) && !loggedUser.getRole().name().equals("ADMIN")) {
            throw new AccessDeniedException("Acesso negado. Você não tem permissão para visualizar estes dados.");
        }
    }
}