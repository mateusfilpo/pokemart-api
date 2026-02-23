package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.OrderNode;

@Repository
public interface SpringDataOrderRepository extends Neo4jRepository<OrderNode, UUID> {
}