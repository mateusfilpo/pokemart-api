package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;

public class UserMapper {

    public static User toDomain(UserNode node) {
        if (node == null) return null;
        return User.builder()
                .id(node.getId())
                .name(node.getName())
                .email(node.getEmail())
                .build();
    }

    public static UserNode toNode(User domain) {
        if (domain == null) return null;
        return UserNode.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .build();
    }
}