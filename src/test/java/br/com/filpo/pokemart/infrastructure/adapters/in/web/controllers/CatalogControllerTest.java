package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.models.Item;
import br.com.filpo.pokemart.domain.models.PageResult;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.ItemRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(
    controllers = CatalogController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class},
    excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.filpo.pokemart.infrastructure.(adapters.out.security|adapters.in.web.RateLimit|config).*")
)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CatalogUseCase catalogUseCase;

    @MockitoBean
    private CacheManager cacheManager;

    private UUID mockItemId;
    private Item mockItem;
    private PageResult<Item> mockPageResult;

    @BeforeEach
    void setUp() {
        mockItemId = UUID.randomUUID();
        
        Category mockCategory = new Category(UUID.randomUUID(), "Potions");
        
        mockItem = Item.builder()
            .id(mockItemId)
            .name("Hyper Potion")
            .description("Heals 200 HP.")
            .price(1200.0)
            .stock(50)
            .category(mockCategory)
            .deleted(false)
            .imageUrl("hyper_potion.png")
            .build();

        mockPageResult = PageResult.<Item>builder()
            .data(List.of(mockItem))
            .totalElements(1L)
            .totalPages(1)
            .currentPage(0)
            .hasNext(false)
            .build();
    }

    @Test
    @DisplayName("GET /api/items: Deve retornar itens paginados com status 200 OK")
    void shouldGetActiveItemsPaginates() throws Exception {
        when(catalogUseCase.getActiveItems(0, 10, null, null, "price-asc")).thenReturn(mockPageResult);

        mockMvc.perform(get("/api/items")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "price-asc")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.data[0].name").value("Hyper Potion"))
            .andExpect(jsonPath("$.data[0].category").value("Potions"));

        verify(catalogUseCase, times(1)).getActiveItems(0, 10, null, null, "price-asc");
    }

    @Test
    @DisplayName("GET /api/items/all: Deve retornar todos os itens (incluindo inativos) com status 200 OK")
    void shouldGetAllItemsPaginates() throws Exception {
        // Arrange
        when(catalogUseCase.getAllItems(0, 10, null, null, "price-asc")).thenReturn(mockPageResult);

        // Act & Assert
        mockMvc.perform(get("/api/items/all")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "price-asc")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.data[0].name").value("Hyper Potion"));

        verify(catalogUseCase, times(1)).getAllItems(0, 10, null, null, "price-asc");
    }

    @Test
    @DisplayName("GET /api/items/{id}: Deve retornar os detalhes do item com status 200 OK")
    void shouldGetItemDetails() throws Exception {
        when(catalogUseCase.getItemDetails(mockItemId)).thenReturn(mockItem);

        mockMvc.perform(get("/api/items/{id}", mockItemId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(mockItemId.toString()))
            .andExpect(jsonPath("$.name").value("Hyper Potion"));

        verify(catalogUseCase, times(1)).getItemDetails(mockItemId);
    }

    @Test
    @DisplayName("POST /api/items: Deve criar um item e retornar 201 Created com cabeçalho Location")
    void shouldCreateItemSuccessfully() throws Exception {
        ItemRequestDTO requestDTO = new ItemRequestDTO();
        requestDTO.setName("Hyper Potion");
        requestDTO.setDescription("Heals 200 HP.");
        requestDTO.setCategory("Potions");
        requestDTO.setPrice(1200.0);
        requestDTO.setStock(50);
        requestDTO.setImage("hyper_potion.png");

        when(catalogUseCase.createItem(any(ItemRequestDTO.class))).thenReturn(mockItem);

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isCreated())
            .andExpect(header().exists("Location"))
            .andExpect(jsonPath("$.name").value("Hyper Potion"));

        verify(catalogUseCase, times(1)).createItem(any(ItemRequestDTO.class));
    }

    @Test
    @DisplayName("POST /api/items: Deve falhar no @Valid com 422 se o preço for negativo e o nome vazio")
    void shouldReturn422WhenItemRequestIsInvalid() throws Exception {
        ItemRequestDTO invalidRequest = new ItemRequestDTO();
        invalidRequest.setName("");
        invalidRequest.setDescription("Description");
        invalidRequest.setCategory("Potions");
        invalidRequest.setPrice(-50.0); 
        invalidRequest.setStock(10);

        mockMvc.perform(post("/api/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.status").value(422))
            .andExpect(jsonPath("$.error").value("Invalid data"))
            .andExpect(jsonPath("$.errors[?(@.fieldName == 'name')]").exists())
            .andExpect(jsonPath("$.errors[?(@.fieldName == 'price')]").exists());

        verify(catalogUseCase, never()).createItem(any());
    }

    @Test
    @DisplayName("PUT /api/items/{id}: Deve atualizar um item e retornar 200 OK")
    void shouldUpdateItemSuccessfully() throws Exception {
        ItemRequestDTO requestDTO = new ItemRequestDTO();
        requestDTO.setName("Max Potion");
        requestDTO.setDescription("Heals all HP.");
        requestDTO.setCategory("Potions");
        requestDTO.setPrice(2500.0);
        requestDTO.setStock(20);

        Item updatedItem = Item.builder().id(mockItemId).name("Max Potion").price(2500.0).build();
        when(catalogUseCase.updateItem(eq(mockItemId), any(ItemRequestDTO.class))).thenReturn(updatedItem);

        mockMvc.perform(put("/api/items/{id}", mockItemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Max Potion"))
            .andExpect(jsonPath("$.price").value(2500.0));

        verify(catalogUseCase, times(1)).updateItem(eq(mockItemId), any(ItemRequestDTO.class));
    }

    @Test
    @DisplayName("DELETE /api/items/{id}: Deve deletar um item e retornar 204 No Content")
    void shouldDeleteItemSuccessfully() throws Exception {
        mockMvc.perform(delete("/api/items/{id}", mockItemId))
            .andExpect(status().isNoContent());

        verify(catalogUseCase, times(1)).deleteItem(mockItemId);
    }

    @Test
    @DisplayName("PATCH /api/items/{id}/status: Deve alterar o status do item e retornar 204 No Content")
    void shouldToggleItemStatusSuccessfully() throws Exception {
        mockMvc.perform(patch("/api/items/{id}/status", mockItemId)
                .param("deleted", "true")) 
            .andExpect(status().isNoContent());

        verify(catalogUseCase, times(1)).toggleItemStatus(mockItemId, true);
    }
}