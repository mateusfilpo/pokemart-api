package br.com.filpo.pokemart.application.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.filpo.pokemart.domain.models.Order;
import br.com.filpo.pokemart.domain.models.OrderItem;
import br.com.filpo.pokemart.domain.ports.in.CheckoutUseCase;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.OrderRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService implements CheckoutUseCase {

    private final ItemRepositoryPort itemRepository;
    private final OrderRepositoryPort orderRepository;
    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    @CacheEvict(value = {"vitrine", "adminVitrine", "categoryStats"}, allEntries = true)
    public Order placeOrder(UUID userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        var cart = user.getCart();
        if (cart == null || cart.isEmpty()) {
            throw new RuntimeException("Não é possível finalizar a compra com o carrinho vazio.");
        }

        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (var cartItem : cart) {
            var item = cartItem.getItem();
            var quantity = cartItem.getQuantity();

            if (item.getStock() < quantity) {
                throw new RuntimeException("Estoque insuficiente para: " + item.getName());
            }

            item.decreaseStock(quantity);
            try {
                itemRepository.save(item);
            } catch (OptimisticLockingFailureException e) {
                throw new RuntimeException("O estoque de " + item.getName() + " sofreu alteração por outro treinador! Atualize a página e tente novamente.");
            }
            
            var orderItem = OrderItem.builder()
                    .productId(item.getId())
                    .name(item.getName())
                    .price(item.getPrice())
                    .quantity(quantity)
                    .build();
            
            orderItems.add(orderItem);
            totalAmount += item.getPrice() * quantity;
        }

        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("COMPLETED");

        Order savedOrder = orderRepository.save(order);

        userRepository.clearCart(userId);

        return savedOrder;
    }
}