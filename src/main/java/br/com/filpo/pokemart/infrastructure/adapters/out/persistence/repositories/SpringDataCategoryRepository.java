package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;

@Repository
public interface SpringDataCategoryRepository extends Neo4jRepository<CategoryNode, UUID> {
    Optional<CategoryNode> findByName(String name);
}