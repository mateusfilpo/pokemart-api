package br.com.filpo.pokemart.domain.models;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.filpo.pokemart.domain.exceptions.BusinessRuleException;

class ItemTest {

    @Test
    @DisplayName("hasStock: Deve retornar true quando o estoque for maior que a quantidade pedida")
    void shouldReturnTrueWhenStockIsGreaterThanQuantity() {
        // Arrange
        Item item = Item.builder().stock(10).build();

        // Act
        boolean result = item.hasStock(5);

        // Assert
        assertTrue(result, "Deveria retornar true pois 10 é maior que 5");
    }

    @Test
    @DisplayName("hasStock: Deve retornar true quando o estoque for exatamente igual à quantidade pedida")
    void shouldReturnTrueWhenStockIsExactlyTheQuantity() {
        // Arrange
        Item item = Item.builder().stock(10).build();

        // Act
        boolean result = item.hasStock(10);

        // Assert
        assertTrue(result, "Deveria retornar true pois 10 é igual a 10");
    }

    @Test
    @DisplayName("hasStock: Deve retornar false quando o estoque for menor que a quantidade pedida")
    void shouldReturnFalseWhenStockIsLessThanQuantity() {
        // Arrange
        Item item = Item.builder().stock(3).build();

        // Act
        boolean result = item.hasStock(5);

        // Assert
        assertFalse(result, "Deveria retornar false pois 3 é menor que 5");
    }

    @Test
    @DisplayName("hasStock: Deve retornar false quando o estoque for nulo (evitando NullPointerException)")
    void shouldReturnFalseWhenStockIsNull() {
        // Arrange
        Item item = Item.builder().stock(null).build();

        // Act
        boolean result = item.hasStock(1);

        // Assert
        assertFalse(result, "Deveria retornar false (e não quebrar) quando o estoque for null");
    }

    @Test
    @DisplayName("decreaseStock: Deve subtrair o estoque corretamente quando houver quantidade suficiente")
    void shouldDecreaseStockSuccessfully() {
        // Arrange
        Item item = Item.builder().stock(20).build();

        // Act
        item.decreaseStock(5);

        // Assert
        assertEquals(15, item.getStock(), "O estoque deveria ter caído de 20 para 15");
    }

    @Test
    @DisplayName("decreaseStock: Deve zerar o estoque quando a quantidade pedida for igual ao total")
    void shouldDecreaseStockToZero() {
        // Arrange
        Item item = Item.builder().stock(5).build();

        // Act
        item.decreaseStock(5);

        // Assert
        assertEquals(0, item.getStock(), "O estoque deveria ter zerado");
    }

    @Test
    @DisplayName("decreaseStock: Deve lançar BusinessRuleException quando o estoque for insuficiente")
    void shouldThrowExceptionWhenDecreasingWithInsufficientStock() {
        // Arrange
        Item item = Item.builder().name("Potion").stock(2).build();

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class, 
            () -> item.decreaseStock(5)
        );

        assertEquals("Estoque insuficiente para o item: Potion", exception.getMessage());
        assertEquals(2, item.getStock(), "O estoque não deve ser alterado se a operação falhar");
    }

    @Test
    @DisplayName("decreaseStock: Deve lançar BusinessRuleException quando o estoque for nulo")
    void shouldThrowExceptionWhenDecreasingWithNullStock() {
        // Arrange
        Item item = Item.builder().name("Rare Candy").stock(null).build();

        // Act & Assert
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> item.decreaseStock(1)
        );

        assertEquals("Estoque insuficiente para o item: Rare Candy", exception.getMessage());
        assertNull(item.getStock(), "O estoque deve continuar nulo");
    }
}