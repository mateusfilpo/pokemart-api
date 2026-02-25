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

        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User ash = User.builder()
                    .id(UUID.randomUUID())
                    .name("Professor Carvalho")
                    .email("admin@admin.com")
                    .password("Senha123@")
                    .build();
            userRepository.save(ash);
            System.out.println("Usuário Professor Carvalho criado com sucesso!");
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

            // 1. 🚀 CACHE DE CATEGORIAS: Busca todas de uma vez para evitar 50 findByName
            java.util.Map<String, Category> categoryCache = categoryRepository.findAll()
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(Category::getName, c -> c));

            // 2. 🚀 LISTA PARA BATCH: Preparar todos os itens na memória
            java.util.List<Item> itemsToSave = new java.util.ArrayList<>();

            for (JsonNode node : itemsJson) {
                String categoryName = node.get("category").asString();

                // Busca no cache local primeiro
                Category categoria = categoryCache.get(categoryName);

                if (categoria == null) {
                    // Se não existe nem no cache, cria e salva na hora para ter o ID
                    Category novaCat = Category.builder()
                            .id(UUID.randomUUID())
                            .name(categoryName)
                            .build();
                    categoria = categoryRepository.save(novaCat);
                    categoryCache.put(categoryName, categoria); // Guarda no cache para o próximo item
                }

                Item item = Item.builder()
                        .id(UUID.randomUUID())
                        .name(node.get("name").asString())
                        .description(node.get("description").asString())
                        .price(node.get("price").asDouble())
                        .stock(node.get("stock").asInt())
                        .imageUrl(node.get("image").asString())
                        .category(categoria)
                        .deleted(node.get("deleted").asBoolean())
                        .build();
                
                itemsToSave.add(item);
            }

            // 3. 🚀 SALVAMENTO EM LOTE: Uma única viagem ao banco para todos os itens!
            // Certifique-se de que a sua Porta/Repository tenha o método saveAll
            itemRepository.saveAll(itemsToSave); 
            
            System.out.println("Catálogo importado do JSON com sucesso (" + itemsToSave.size() + " itens)!");

        } catch (Exception e) {
            System.err.println("Erro ao carregar itens do JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}