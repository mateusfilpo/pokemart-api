package br.com.filpo.pokemart.infrastructure.config;

import java.io.InputStream;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.User;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepositoryPort userRepository;
    private final ItemRepositoryPort itemRepository;
    private final CategoryRepositoryPort categoryRepository;
    
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedItemsFromJson();
    }

    private void seedUsers() {
        if (userRepository.findByEmail("ash@pallet.com").isEmpty()) {
            User ash = User.builder()
                    .id(UUID.randomUUID())
                    .name("Ash Ketchum")
                    .email("ash@pallet.com")
                    .password("Pikachu123@")
                    .build();
            userRepository.save(ash);
            System.out.println("Usuário Ash criado com sucesso!");
        }
    }

    private void seedItemsFromJson() {
        try {
            if (!itemRepository.findAll().isEmpty()) {
                System.out.println("O banco já possui itens. Pulando o Seeder de JSON.");
                return;
            }

            InputStream inputStream = new ClassPathResource("items.json").getInputStream();
            JsonNode itemsJson = objectMapper.readTree(inputStream);

            for (JsonNode node : itemsJson) {
                String name = node.get("name").asString();
                String description = node.get("description").asString();
                String categoryName = node.get("category").asString();
                double price = node.get("price").asDouble();
                int stock = node.get("stock").asInt();
                String image = node.get("image").asString();
                Boolean deleted = node.get("deleted").asBoolean();

                Category categoria = categoryRepository.findByName(categoryName)
                        .orElseGet(() -> {
                            Category novaCat = Category.builder()
                                    .id(UUID.randomUUID())
                                    .name(categoryName)
                                    .build();
                            return categoryRepository.save(novaCat);
                        });

                Item item = Item.builder()
                        .id(UUID.randomUUID())
                        .name(name)
                        .description(description)
                        .price(price)
                        .stock(stock)
                        .imageUrl(image)
                        .category(categoria)
                        .deleted(deleted)
                        .build();
                
                itemRepository.save(item);
            }
            System.out.println("Catálogo importado do JSON com sucesso!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar itens do JSON: " + e.getMessage());
        }
    }
}