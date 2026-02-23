package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;

@Repository
public interface SpringDataUserRepository extends Neo4jRepository<UserNode, UUID> {
    Optional<UserNode> findByEmail(String email);
}