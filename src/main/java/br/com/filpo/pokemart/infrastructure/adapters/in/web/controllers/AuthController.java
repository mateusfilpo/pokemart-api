package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.AuthenticationRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.LoginResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.UserResponseDTO;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.security.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var userNode = (UserNode) auth.getPrincipal();
        var token = tokenService.generateToken(userNode);

        return ResponseEntity.ok(new LoginResponseDTO(
            token, 
            userNode.getRole().name(), 
            userNode.getId()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getMe(@AuthenticationPrincipal UserNode loggedUser) {
        UserResponseDTO response = UserResponseDTO.builder()
                .id(loggedUser.getId())
                .name(loggedUser.getName())
                .email(loggedUser.getEmail())
                .role(loggedUser.getRole().name())
                .build();

        return ResponseEntity.ok(response);
    }
}