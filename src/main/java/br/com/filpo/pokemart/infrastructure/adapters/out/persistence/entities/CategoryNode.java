package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities;

import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Node("Category")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryNode {
    @Id
    private UUID id;
    private String name;
}