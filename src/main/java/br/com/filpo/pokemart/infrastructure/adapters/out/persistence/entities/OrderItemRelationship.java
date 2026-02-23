package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities;

import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@RelationshipProperties
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRelationship {

    @RelationshipId
    private Long id;

    private Integer quantity;

    @TargetNode
    private ItemNode item;
}