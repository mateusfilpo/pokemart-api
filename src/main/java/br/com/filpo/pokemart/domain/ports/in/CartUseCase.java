package br.com.filpo.pokemart.domain.ports.in;

import java.util.List;
import java.util.UUID;

import br.com.filpo.pokemart.domain.models.CartItem;

public interface CartUseCase {
    List<CartItem> getCart(UUID userId);
    
    List<CartItem> updateCartItem(UUID userId, UUID itemId, Integer quantity);
    
    void clearCart(UUID userId);
}