package br.com.filpo.pokemart.application.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;
import br.com.filpo.pokemart.domain.exceptions.ResourceNotFoundException;
import br.com.filpo.pokemart.domain.models.CartItem;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.in.CartUseCase;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartService implements CartUseCase {

    private final UserRepositoryPort userRepository;
    private final ItemRepositoryPort itemRepository;

    @Override
    public List<CartItem> getCart(UUID userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"))
            .getCart();
    }

    @Override
    public List<CartItem> updateCartItem(
        UUID userId,
        UUID itemId,
        Integer quantity
    ) {
        if (quantity > 0) {
            Item item = itemRepository
                .findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

            if (quantity > item.getStock()) {
                throw new BusinessRuleException("Requested quantity exceeds available stock");
            }

            userRepository.upsertCartItem(userId, itemId, quantity);
        } else {
            userRepository.removeCartItem(userId, itemId);
        }

        return getCart(userId);
    }

    @Override
    public void clearCart(UUID userId) {
        userRepository.clearCart(userId);
    }
}