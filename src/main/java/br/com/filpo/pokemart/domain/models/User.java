package br.com.filpo.pokemart.domain.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private UserRole role;
    
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    private List<CartItem> cart = new ArrayList<>();
}