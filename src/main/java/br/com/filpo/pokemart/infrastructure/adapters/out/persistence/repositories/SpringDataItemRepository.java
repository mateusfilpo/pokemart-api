package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataItemRepository
    extends Neo4jRepository<ItemNode, UUID>
{
    @Query("MATCH (i:Item {id: $id}) SET i.deleted = $status")
    void updateStatus(@Param("id") UUID id, @Param("status") boolean status);

    @Query(
        value = "MATCH (i:Item)-[r:BELONGS_TO]->(c:Category) " +
            "WHERE (i.deleted = false OR i.deleted IS NULL) " +
            "AND ($category IS NULL OR $category = '' OR c.name = $category) " +
            "AND ($search IS NULL OR $search = '' OR i.normalizedSearch CONTAINS $search) " +
            "RETURN i, collect(r), collect(c), " +
            "(CASE WHEN $search IS NOT NULL AND $search <> '' AND toLower(i.name) CONTAINS toLower($search) THEN 2 ELSE 1 END) AS score " +
            ":#{orderBy(#pageable.sort)} SKIP $skip LIMIT $limit",
        countQuery = "MATCH (i:Item)-[:BELONGS_TO]->(c:Category) " +
            "WHERE (i.deleted = false OR i.deleted IS NULL) " +
            "AND ($category IS NULL OR $category = '' OR c.name = $category) " +
            "AND ($search IS NULL OR $search = '' OR i.normalizedSearch CONTAINS $search) " +
            "RETURN count(i)"
    )
    Page<ItemNode> findActiveItemsWithFilters(
        @Param("category") String category,
        @Param("search") String search,
        Pageable pageable
    );

    @Query(
        value = "MATCH (i:Item)-[r:BELONGS_TO]->(c:Category) " +
            "WHERE ($category IS NULL OR $category = '' OR c.name = $category) " +
            "AND ($search IS NULL OR $search = '' OR toLower(i.name) CONTAINS toLower($search) OR toString(i.id) CONTAINS toLower($search)) " +
            "RETURN i, collect(r), collect(c), " +
            "(CASE WHEN $search IS NOT NULL AND $search <> '' AND toLower(i.name) CONTAINS toLower($search) THEN 2 ELSE 1 END) AS score " +
            ":#{orderBy(#pageable.sort)} SKIP $skip LIMIT $limit",
        countQuery = "MATCH (i:Item)-[:BELONGS_TO]->(c:Category) " +
            "WHERE ($category IS NULL OR $category = '' OR c.name = $category) " +
            "AND ($search IS NULL OR $search = '' OR toLower(i.name) CONTAINS toLower($search) OR toString(i.id) CONTAINS toLower($search)) " +
            "RETURN count(i)"
    )
    Page<ItemNode> findAllItemsWithFilters(
        @Param("category") String category,
        @Param("search") String search,
        Pageable pageable
    );

    @Query(
        "MATCH (i:Item)-[:BELONGS_TO]->(c:Category) " +
            "WHERE (i.deleted = false OR i.deleted IS NULL) " +
            "AND ($search IS NULL OR $search = '' OR i.normalizedSearch CONTAINS $search) " +
            "WITH c.name AS catName, count(i) AS itemCount " +
            "RETURN { category: catName, count: itemCount }"
    )
    List<java.util.Map<String, Object>> countActiveItemsByCategory(
        @Param("search") String search
    );
}
