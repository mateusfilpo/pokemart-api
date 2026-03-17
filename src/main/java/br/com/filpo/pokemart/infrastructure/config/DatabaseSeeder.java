package br.com.filpo.pokemart.infrastructure.config;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.ports.out.CategoryRepositoryPort;
import br.com.filpo.pokemart.domain.ports.out.ItemRepositoryPort;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final ItemRepositoryPort itemRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        seedItemsFromJson();
    }

    private String normalizeText(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase();
    }

    private void seedItemsFromJson() {
        try {
            if (itemRepository.count() > 0) {
                System.out.println(
                    "O banco já possui itens. Pulando o Seeder de JSON."
                );
                return;
            }

            InputStream inputStream = new ClassPathResource(
                "items.json"
            ).getInputStream();
            JsonNode itemsJson = objectMapper.readTree(inputStream);

            Map<String, Category> categoryCache = categoryRepository
                .findAll()
                .stream()
                .collect(
                    Collectors.toMap(Category::getName, c -> c)
                );

            List<Item> itemsToSave = new ArrayList<>();

            for (JsonNode node : itemsJson) {
                String categoryName = node.get("category").asString();
                Category categoria = categoryCache.get(categoryName);

                if (categoria == null) {
                    Category novaCat = Category.builder()
                        .id(UUID.randomUUID())
                        .name(categoryName)
                        .build();
                    categoria = categoryRepository.save(novaCat);
                    categoryCache.put(categoryName, categoria);
                }

                String name = node.get("name").asString();
                String description = node.get("description").asString();

                String searchString = normalizeText(name + " " + description);

                Item item = Item.builder()
                    .id(UUID.randomUUID())
                    .name(name)
                    .description(description)
                    .price(node.get("price").asDouble())
                    .stock(node.get("stock").asInt())
                    .imageUrl(node.get("image").asString())
                    .category(categoria)
                    .deleted(node.get("deleted").asBoolean())
                    .normalizedSearch(searchString)
                    .build();

                itemsToSave.add(item);
            }

            itemRepository.saveAll(itemsToSave);

            System.out.println(
                "Catálogo importado do JSON com sucesso (" +
                    itemsToSave.size() +
                    " itens)!"
            );
        } catch (Exception e) {
            System.err.println(
                "Erro ao carregar itens do JSON: " + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
