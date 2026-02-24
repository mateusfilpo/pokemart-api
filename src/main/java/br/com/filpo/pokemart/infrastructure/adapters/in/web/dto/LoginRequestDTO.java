package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}