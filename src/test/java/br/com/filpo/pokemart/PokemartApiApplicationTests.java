package br.com.filpo.pokemart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "org.neo4j.migrations.enabled=false")
class PokemartApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
