package br.com.filpo.pokemart.application.services;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.filpo.pokemart.domain.models.Order;
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
    @Transactional // Garante que se um item falhar, nada é salvo (Atomicidade)
    public Order placeOrder(UUID userId, Order order) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 1. Validar e Atualizar Estoque (Lógica do seu CartPage.js)
        order.getItems().forEach(orderItem -> {
            var item = itemRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Item indisponível: " + orderItem.getName()));
            
            item.decreaseStock(orderItem.getQuantity());
            itemRepository.save(item); // Persiste o novo estoque no Neo4j
        });

        // 2. Preparar o Pedido
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("COMPLETED");

        // 3. Salvar o Pedido no Grafo
        return orderRepository.save(order);
    }
}