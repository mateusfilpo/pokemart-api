package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

public record LoginResponseDTO(String token, String role, UUID id) {
}