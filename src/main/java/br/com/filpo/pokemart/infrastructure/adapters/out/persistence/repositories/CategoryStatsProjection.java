package br.com.filpo.pokemart.infrastructure.adapters.out.persistence.repositories;

public interface CategoryStatsProjection {
    String getCategory();
    Long getCount();
}