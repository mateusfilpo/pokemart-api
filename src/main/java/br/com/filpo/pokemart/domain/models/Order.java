package br.com.filpo.pokemart.domain.models;

import java.time.LocalDateTime;
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
public class Order {
    private UUID id;
    private User user;

    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    
    private Double totalAmount;
    private LocalDateTime createdAt;
    private String status;
}