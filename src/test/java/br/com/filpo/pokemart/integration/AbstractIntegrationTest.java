package br.com.filpo.pokemart.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(
        DockerImageName.parse("neo4j:5.20.0")
    )
        .withoutAuthentication() 
        .withEnv("NEO4J_apoc_export_file_enabled", "true")
        .withEnv("NEO4J_apoc_import_file_enabled", "true")
        .withEnv("NEO4J_apoc_import_file_use__neo4j__config", "true");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(
        DockerImageName.parse("redis:7.2.4-alpine")
    ).withExposedPorts(6379);

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.neo4j.uri", neo4jContainer::getBoltUrl);
        registry.add("spring.neo4j.authentication.username", () -> "neo4j");
        registry.add("spring.neo4j.authentication.password", () -> "");

        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add(
            "spring.data.redis.port",
            redisContainer::getFirstMappedPort
        );
    }
}
