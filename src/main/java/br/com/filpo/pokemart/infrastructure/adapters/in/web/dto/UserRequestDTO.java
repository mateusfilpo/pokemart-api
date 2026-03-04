package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import br.com.filpo.pokemart.domain.models.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserRequestDTO(
        @NotBlank(message = "Name is required.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "Password is required.")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
            message = "Password must contain at least 8 characters, one uppercase, one lowercase, one number and one special character."
        )
        String password
) {
    public User toDomain() {
        User user = new User();
        user.setName(this.name());
        user.setEmail(this.email());
        user.setPassword(this.password());
        return user;
    }
}