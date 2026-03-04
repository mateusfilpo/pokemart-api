package br.com.filpo.pokemart.infrastructure.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ItemRequestDTO {

    @NotBlank(message = "Item name is required.")
    @Size(max = 100, message = "Name cannot exceed 100 characters.")
    private String name;

    @NotBlank(message = "Description is required.")
    private String description;

    @NotBlank(message = "Category is required.")
    private String category;

    @Positive(message = "Price must be greater than zero.")
    private double price;

    @PositiveOrZero(message = "Stock cannot be negative.")
    private int stock;

    private String image;
    
    private Boolean deleted;
}