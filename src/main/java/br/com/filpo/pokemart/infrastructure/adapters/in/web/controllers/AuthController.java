package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.AuthenticationRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.LoginResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.CustomError;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content; 
import io.swagger.v3.oas.annotations.media.Schema; 
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "1. Autenticação", description = "Endpoints para gerenciamento de login, logout e sessão via Cookies HttpOnly")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Operation(summary = "Realizar Login", description = "Autentica um Treinador e retorna um Cookie HttpOnly seguro contendo o JWT.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login realizado com sucesso. Cookie definido."),
        @ApiResponse(
            responseCode = "401", 
            description = "Credenciais inválidas (Email ou Senha incorretos).",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = CustomError.class))
        )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
        @RequestBody @Valid AuthenticationRequestDTO data
    ) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(
            data.email(),
            data.password()
        );
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var userNode = (UserNode) auth.getPrincipal();
        var token = tokenService.generateToken(userNode);

        ResponseCookie jwtCookie = ResponseCookie.from("pokemart_token", token)
            .httpOnly(true)
            .secure(false) // Mudar para 'true' em Produção quando tiver HTTPS
            .path("/")
            .maxAge(2 * 60 * 60)
            .sameSite("Lax")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(
                new LoginResponseDTO(
                    userNode.getRole().name(),
                    userNode.getId()
                )
            );
    }

    @Operation(summary = "Realizar Logout", description = "Invalida a sessão atual apagando o Cookie HttpOnly do navegador.")
    @ApiResponse(responseCode = "204", description = "Logout realizado com sucesso. Cookie removido.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie deleteCookie = ResponseCookie.from("pokemart_token", "")
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(0)
            .sameSite("Lax")
            .build();

        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
            .build();
    }

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
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(
        @Parameter(hidden = true) @AuthenticationPrincipal UserNode loggedUser 
    ) {
        UserResponseDTO response = UserResponseDTO.builder()
            .id(loggedUser.getId())
            .name(loggedUser.getName())
            .email(loggedUser.getEmail())
            .role(loggedUser.getRole().name())
            .build();

        return ResponseEntity.ok(response);
    }
}