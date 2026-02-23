package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities;

import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Node("Item")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemNode {
    @Id
    private UUID id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Integer stock;
    private Boolean deleted;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private CategoryNode category;
}