package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;

@Repository
public interface SpringDataItemRepository extends Neo4jRepository<ItemNode, UUID> {
    @Query("MATCH (i:Item {id: $id}) SET i.deleted = $status")
    void updateStatus(@Param("id") UUID id, @Param("status") boolean status);
}