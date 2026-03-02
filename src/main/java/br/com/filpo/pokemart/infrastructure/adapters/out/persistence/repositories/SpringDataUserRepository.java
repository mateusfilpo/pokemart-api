package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.UserNode;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataUserRepository
    extends Neo4jRepository<UserNode, UUID>
{
    Optional<UserNode> findByEmail(String email);

    @Query(
        "MATCH (u:User {id: $userId}) MATCH (i:Item {id: $itemId}) " +
            "MERGE (u)-[r:HAS_IN_CART]->(i) SET r.quantity = $quantity"
    )
    void upsertCartItem(UUID userId, UUID itemId, Integer quantity);

    @Query(
        "MATCH (u:User {id: $userId})-[r:HAS_IN_CART]->(i:Item {id: $itemId}) DELETE r"
    )
    void removeCartItem(UUID userId, UUID itemId);

    @Query("MATCH (u:User {id: $userId})-[r:HAS_IN_CART]->() DELETE r")
    void clearCart(UUID userId);
}
