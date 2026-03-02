package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RelationshipProperties
public class CartItemRelationship {

    @Id
    @GeneratedValue
    private String id;

    private Integer quantity;

    @TargetNode
    private ItemNode item;
}