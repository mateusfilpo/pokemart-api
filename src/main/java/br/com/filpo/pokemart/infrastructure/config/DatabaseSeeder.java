package br.com.filpo.pokemart.infrastructure.config;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class DatabaseSeeder {

    @Bean
    CommandLineRunner initDatabase(CategoryRepositoryPort categoryRepository, 
                                   ItemRepositoryPort itemRepository,
                                   UserRepositoryPort userRepository) {
        return args -> {
            
            if (userRepository.findByEmail("ash@pallet.com").isEmpty()) {
                User ash = User.builder()
                        .id(UUID.randomUUID())
                        .name("Ash Ketchum")
                        .email("ash@pallet.com")
                        .build();
                userRepository.save(ash);
                System.out.println("👤 Usuário de teste criado! Copie este ID para testar o Checkout: " + ash.getId());
            }

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
                        .category(pokeballs)
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
            }
        };
    }
}