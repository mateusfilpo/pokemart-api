package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.AuthenticationRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.LoginResponseDTO;
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

@Tag(name = "1. Autenticação", description = "Endpoints para gerenciamento de login, logout e sessão via Cookies HttpOnly")
public interface AuthDoc {

    @Operation(summary = "Realizar Login", description = "Autentica um Treinador e retorna um Cookie HttpOnly seguro contendo o JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso. Cookie definido."),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciais inválidas (Email ou Senha incorretos).",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))
        )
    })
    ResponseEntity<LoginResponseDTO> login(@RequestBody AuthenticationRequestDTO data);

    @Operation(summary = "Realizar Logout", description = "Invalida a sessão atual apagando o Cookie HttpOnly do navegador.")
    @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso. Cookie removido.")
    ResponseEntity<Void> logout();

    @Operation(summary = "Obter sessão ativa", description = "Retorna os dados do Treinador autenticado com base no Cookie enviado na requisição.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sessão válida. Dados retornados."),
        @ApiResponse(
            responseCode = "401", 
            description = "Sessão expirada ou usuário não autenticado.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class)) 
        )
    })
    ResponseEntity<UserResponseDTO> getMe(@Parameter(hidden = true) @AuthenticationPrincipal UserNode loggedUser);
}