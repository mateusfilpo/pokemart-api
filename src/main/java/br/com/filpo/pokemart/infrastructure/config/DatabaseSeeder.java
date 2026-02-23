package br.com.filpo.pokemart.infrastructure.config;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepositoryPort categoryRepository, ItemRepositoryPort itemRepository) {
        return args -> {
            if (itemRepository.findAll().isEmpty()) {
                System.out.println("🌱 Banco de dados vazio. Iniciando o Seeder de Pokémart...");

                Category pokeballs = Category.builder().id(UUID.randomUUID()).name("Poké Balls").build();
                Category potions = Category.builder().id(UUID.randomUUID()).name("Potions").build();

                categoryRepository.save(pokeballs);
                categoryRepository.save(potions);

                Item pokeBallItem = Item.builder()
                        .id(UUID.randomUUID())
                        .name("Poké Ball")
                        .description("A tool for catching wild Pokémon.")
                        .price(200.0)
                        .imageUrl("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/poke-ball.png")
                        .stock(50)
                        .deleted(false)
                        .category(pokeballs) // Relacionamento no Grafo!
                        .build();

                Item potionItem = Item.builder()
                        .id(UUID.randomUUID())
                        .name("Potion")
                        .description("Restores 20 HP to a Pokémon.")
                        .price(300.0)
                        .imageUrl("https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/items/potion.png")
                        .stock(30)
                        .deleted(false)
                        .category(potions)
                        .build();

                itemRepository.save(pokeBallItem);
                itemRepository.save(potionItem);

                System.out.println("✅ Dados semeados com sucesso no Neo4j AuraDB!");
            } else {
                System.out.println("⚡ O banco de dados já possui informações. Seeder ignorado.");
            }
        };
    }
}