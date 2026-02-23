package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Node("Order")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderNode {
    @Id
    private UUID id;
    
    private Double totalAmount;
    private LocalDateTime createdAt;
    private String status;

    @Relationship(type = "PLACED_BY", direction = Relationship.Direction.OUTGOING)
    private UserNode user;

    @Relationship(type = "CONTAINS", direction = Relationship.Direction.OUTGOING)
    private List<ItemNode> items; // Simplificando para o Neo4j por enquanto
}