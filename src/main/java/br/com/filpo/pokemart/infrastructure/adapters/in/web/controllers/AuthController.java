package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

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

import br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers.doc.AuthDoc;
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
@CrossOrigin(origins = "*")
public class AuthController implements AuthDoc {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Override
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid AuthenticationRequestDTO data) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var userNode = (UserNode) auth.getPrincipal();
        var token = tokenService.generateToken(userNode);

        ResponseCookie jwtCookie = ResponseCookie.from("pokemart_token", token)
            .httpOnly(true)
            .secure(false) 
            .path("/")
            .maxAge(2 * 60 * 60)
            .sameSite("Lax")
            .build();

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
            .body(new LoginResponseDTO(userNode.getRole().name(), userNode.getId()));
    }

    @Override
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

    @Override
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