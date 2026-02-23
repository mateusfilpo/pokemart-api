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
    @Transactional
    public Order placeOrder(UUID userId, Order order) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        double totalAmount = 0.0; // Criamos a variável do total

        // 1. Validar e Atualizar Estoque
        for (var orderItem : order.getItems()) {
            var item = itemRepository.findById(orderItem.getProductId())
                    .orElseThrow(() -> new RuntimeException("Item indisponível: " + orderItem.getProductId()));
            
            // Regra de negócio bônus: Evitar estoque negativo
            if (item.getStock() < orderItem.getQuantity()) {
                throw new RuntimeException("Estoque insuficiente para: " + item.getName());
            }

            // Baixa o estoque e salva
            item.decreaseStock(orderItem.getQuantity());
            itemRepository.save(item);

            // ⚠️ A CORREÇÃO: Preenchemos os dados que o Front-end não mandou
            orderItem.setName(item.getName());
            orderItem.setPrice(item.getPrice());

            // ⚠️ A CORREÇÃO: Calculamos o total da compra
            totalAmount += item.getPrice() * orderItem.getQuantity();
        }

        // 2. Preparar o Pedido
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("COMPLETED");
        order.setTotalAmount(totalAmount); // ⚠️ Guardamos o total calculado!

        // 3. Salvar o Pedido no Grafo
        return orderRepository.save(order);
    }
}