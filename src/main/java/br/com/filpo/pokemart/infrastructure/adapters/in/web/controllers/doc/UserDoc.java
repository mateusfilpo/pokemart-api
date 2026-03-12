package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@Tag(name = "2. Usuários", description = "Endpoints para gerenciamento de perfil de treinadores e histórico de pedidos")
public interface UserDoc {

    @Operation(summary = "Buscar Perfil do Usuário", description = "Retorna os detalhes de um Treinador específico. Requer permissão de acesso (próprio usuário ou ADMIN).")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil retornado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Sessão expirada ou não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado (tentativa de visualizar outro perfil).", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<UserResponseDTO> getUserProfile(UUID id, @Parameter(hidden = true) @AuthenticationPrincipal UserNode loggedUser);

    @Operation(summary = "Buscar Histórico de Pedidos", description = "Retorna a lista de pedidos realizados por um Treinador. Requer permissão de acesso (próprio usuário ou ADMIN).")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Histórico retornado com sucesso."),
        @ApiResponse(responseCode = "401", description = "Não autenticado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "403", description = "Acesso negado.", content = @Content(schema = @Schema(implementation = CustomError.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<List<OrderResponseDTO>> getUserOrders(UUID id, @Parameter(hidden = true) @AuthenticationPrincipal UserNode loggedUser);

    @Operation(summary = "Registrar novo Usuário", description = "Cria uma nova conta de Treinador no sistema. Endpoint público.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso."),
        @ApiResponse(responseCode = "422", description = "Erro de validação ou email já cadastrado.", content = @Content(schema = @Schema(implementation = CustomError.class)))
    })
    ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid UserRequestDTO request);
}