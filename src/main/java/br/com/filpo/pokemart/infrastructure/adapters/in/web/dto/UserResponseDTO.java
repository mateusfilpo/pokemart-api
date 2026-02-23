package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import java.util.UUID;

import br.com.filpo.pokemart.domain.models.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {
    private UUID id;
    private String name;
    private String email;

    public static UserResponseDTO fromDomain(User user) {
        if (user == null) return null;
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }
}