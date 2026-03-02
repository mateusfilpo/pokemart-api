package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CartItemRelationship;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toDomain(UserNode node) {
        if (node == null) return null;

        User user = User.builder()
            .id(node.getId())
            .name(node.getName())
            .email(node.getEmail())
            .password(node.getPassword())
            .role(node.getRole())
            .build();

        if (node.getCartItems() != null) {
            user.setCart(
                node
                    .getCartItems()
                    .stream()
                    .map(rel ->
                        CartItem.builder()
                            .item(ItemMapper.toDomain(rel.getItem()))
                            .quantity(rel.getQuantity())
                            .build()
                    )
                    .collect(Collectors.toList())
            );
        } else {
            user.setCart(new ArrayList<>());
        }

        return user;
    }

    public static UserNode toNode(User domain) {
        if (domain == null) return null;

        UserNode node = UserNode.builder()
            .id(domain.getId())
            .name(domain.getName())
            .email(domain.getEmail())
            .password(domain.getPassword())
            .role(domain.getRole())
            .build();

        if (domain.getCart() != null) {
            node.setCartItems(
                domain
                    .getCart()
                    .stream()
                    .map(cartItem ->
                        CartItemRelationship.builder()
                            .item(ItemMapper.toNode(cartItem.getItem()))
                            .quantity(cartItem.getQuantity())
                            .build()
                    )
                    .collect(Collectors.toList())
            );
        } else {
            node.setCartItems(new ArrayList<>());
        }

        return node;
    }
}
