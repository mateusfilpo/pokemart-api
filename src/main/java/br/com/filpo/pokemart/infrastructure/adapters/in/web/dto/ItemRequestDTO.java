package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import lombok.Data;

@Data
public class ItemRequestDTO {
    private String name;
    private String description;
    private String category;
    private double price;
    private int stock;
    private String image;
    private Boolean deleted;
}