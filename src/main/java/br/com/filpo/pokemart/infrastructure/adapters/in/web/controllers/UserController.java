package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.in.UserUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.OrderResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.CustomError;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Tag(name = "2. Usuários", description = "Endpoints para gerenciamento de perfil de treinadores e histórico de pedidos")
public class UserController {

    private final UserUseCase userUseCase;

    @Operation(summary = "Buscar Perfil do Usuário", description = "Retorna os detalhes de um Treinador específico. Requer permissão de acesso (próprio usuário ou ADMIN).")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado (tentativa de visualizar outro usuário).", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserProfile(
        @PathVariable UUID id,
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode loggedUser 
    ) {
        checkPermission(id, loggedUser);

        User user = userUseCase.getUserById(id);
        return ResponseEntity.ok(UserResponseDTO.fromDomain(user));
    }

    @Operation(summary = "Buscar Histórico de Pedidos", description = "Retorna a lista de pedidos realizados por um Treinador. Requer permissão de acesso (próprio usuário ou ADMIN).")
    @SecurityRequirement(name = "cookieAuth") 
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico de pedidos retornado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou usuário não autenticado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado (tentativa de visualizar pedidos de outro usuário).", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponseDTO>> getUserOrders(
        @PathVariable UUID id,
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode loggedUser 
    ) {
        checkPermission(id, loggedUser);

        List<Order> orders = userUseCase.getUserOrderHistory(id);

        List<OrderResponseDTO> response = orders
            .stream()
            .map(OrderResponseDTO::fromDomain)
            .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Registrar novo Usuário", description = "Cria uma nova conta de Treinador no sistema. Endpoint público.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso."),
        @ApiResponse(responseCode = "422", description = "Erro de validação nos dados enviados ou regra de negócio (ex: email já cadastrado).", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)))
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
        @RequestBody @Valid UserRequestDTO request
    ) {
        User userToCreate = request.toDomain();

        User createdUser = userUseCase.createUser(userToCreate);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdUser.getId())
            .toUri();

        return ResponseEntity.created(uri).body(
            UserResponseDTO.fromDomain(createdUser)
        );
    }

    private void checkPermission(UUID targetId, UserNode loggedUser) {
        if (
            !loggedUser.getId().equals(targetId) &&
            !loggedUser.getRole().name().equals("ADMIN")
        ) {
            throw new AccessDeniedException(
                "Acesso negado. Você não tem permissão para visualizar estes dados."
            );
        }
    }
}