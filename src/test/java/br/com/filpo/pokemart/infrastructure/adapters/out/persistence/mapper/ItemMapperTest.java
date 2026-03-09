package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.mapper;

import static org.junit.jupiter.api.Assertions.*;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.CategoryNode;
import br.com.filpo.pokemart.infrastructure.adapters.out.persistence.entities.ItemNode;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ItemMapperTest {

    @Test
    @DisplayName("toDomain: Deve converter ItemNode completo para Item de Domínio com sucesso")
    void shouldMapItemNodeToDomainSuccessfully() {
        // Arrange
        UUID itemId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        CategoryNode categoryNode = CategoryNode.builder()
            .id(categoryId)
            .name("Poké Balls")
            .build();

        ItemNode itemNode = ItemNode.builder()
            .id(itemId)
            .name("Ultra Ball")
            .description("A great ball.")
            .price(1200.0)
            .imageUrl("url_img")
            .stock(50)
            .deleted(false)
            .category(categoryNode)
            .version(1L)
            .normalizedSearch("ultra ball a great ball.")
            .build();

        // Act
        Item result = ItemMapper.toDomain(itemNode);

        // Assert
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Ultra Ball", result.getName());
        assertEquals("A great ball.", result.getDescription());
        assertEquals(1200.0, result.getPrice());
        assertEquals("url_img", result.getImageUrl());
        assertEquals(50, result.getStock());
        assertEquals(false, result.getDeleted());
        assertEquals(1L, result.getVersion());
        
        assertNotNull(result.getCategory());
        assertEquals(categoryId, result.getCategory().getId());
        assertEquals("Poké Balls", result.getCategory().getName());
    }

    @Test
    @DisplayName("toDomain: Deve retornar null quando ItemNode for null")
    void shouldReturnNullWhenItemNodeIsNull() {
        // Act
        Item result = ItemMapper.toDomain(null);

        // Assert
        assertNull(result, "Deveria retornar null para evitar NullPointerException");
    }

    @Test
    @DisplayName("toDomain: Deve mapear com sucesso mesmo se a Categoria for null")
    void shouldMapItemNodeToDomainWithNullCategory() {
        // Arrange
        ItemNode itemNode = ItemNode.builder()
            .id(UUID.randomUUID())
            .name("Potion")
            .category(null)
            .build();

        // Act
        Item result = ItemMapper.toDomain(itemNode);

        // Assert
        assertNotNull(result);
        assertEquals("Potion", result.getName());
        assertNull(result.getCategory(), "A categoria do domínio deveria ser null");
    }

    @Test
    @DisplayName("toNode: Deve converter Item de Domínio completo para ItemNode gerando a string de busca normalizada")
    void shouldMapDomainToItemNodeAndGenerateNormalizedSearch() {
        // Arrange
        UUID itemId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        Category category = Category.builder()
            .id(categoryId)
            .name("Healing")
            .build();

        Item itemDomain = Item.builder()
            .id(itemId)
            .name("Mãx Révîvê")
            .description("Restaura TUDO!")
            .price(4000.0)
            .imageUrl("url_revive")
            .stock(10)
            .deleted(false)
            .category(category)
            .version(2L)
            .build();

        // Act
        ItemNode result = ItemMapper.toNode(itemDomain);

        // Assert
        assertNotNull(result);
        assertEquals(itemId, result.getId());
        assertEquals("Mãx Révîvê", result.getName());
        assertEquals("Restaura TUDO!", result.getDescription());
        assertEquals(4000.0, result.getPrice());
        assertEquals("url_revive", result.getImageUrl());
        assertEquals(10, result.getStock());
        assertEquals(false, result.getDeleted());
        assertEquals(2L, result.getVersion());
        
        assertEquals("max revive restaura tudo!", result.getNormalizedSearch());

        assertNotNull(result.getCategory());
        assertEquals(categoryId, result.getCategory().getId());
        assertEquals("Healing", result.getCategory().getName());
    }

    @Test
    @DisplayName("toNode: Deve retornar null quando o Item de Domínio for null")
    void shouldReturnNullWhenItemDomainIsNull() {
        // Act
        ItemNode result = ItemMapper.toNode(null);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("toNode: Deve mapear com sucesso e criar busca normalizada mesmo sem Categoria")
    void shouldMapDomainToItemNodeWithNullCategory() {
        // Arrange
        Item itemDomain = Item.builder()
            .id(UUID.randomUUID())
            .name("Item Solto")
            .description("Sem categoria")
            .category(null)
            .build();

        // Act
        ItemNode result = ItemMapper.toNode(itemDomain);

        // Assert
        assertNotNull(result);
        assertEquals("Item Solto", result.getName());
        assertEquals("item solto sem categoria", result.getNormalizedSearch());
        assertNull(result.getCategory());
    }
}