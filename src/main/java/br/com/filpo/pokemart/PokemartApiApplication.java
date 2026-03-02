package br.com.filpo.pokemart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PokemartApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(PokemartApiApplication.class, args);
    }
}
