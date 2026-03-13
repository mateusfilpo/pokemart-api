package br.com.filpo.pokemart.infrastructure.adapters.in.web.controllers;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.filpo.pokemart.domain.models.Category;
import br.com.filpo.pokemart.domain.ports.in.CatalogUseCase;
import br.com.filpo.pokemart.domain.ports.in.CategoryUseCase;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryRequestDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.dto.CategoryStatsDTO;
import br.com.filpo.pokemart.infrastructure.adapters.in.web.exceptions.GlobalExceptionHandler;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = CategoryController.class, excludeAutoConfiguration = {
        SecurityAutoConfiguration.class }, excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "br.com.filpo.pokemart.infrastructure.(adapters.out.security|adapters.in.web.RateLimit|config).*"))
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryUseCase categoryUseCase;

    @MockitoBean
    private CatalogUseCase catalogUseCase;

    @MockitoBean
    private CacheManager cacheManager;

    private UUID categoryId;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        mockCategory = new Category(categoryId, "Poké Balls");
    }

    @Test
    @DisplayName("GET /api/categories: Deve listar todas as categorias com status 200 OK")
    void shouldListAllCategories() throws Exception {
        // Arrange
        when(categoryUseCase.listAllCategories()).thenReturn(List.of(mockCategory));

        // Act & Assert
        mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].id").value(categoryId.toString()))
            .andExpect(jsonPath("$[0].name").value("Poké Balls"));

        verify(categoryUseCase, times(1)).listAllCategories();
    }

    @Test
    @DisplayName("POST /api/categories: Deve criar categoria, retornar 201 Created e o Header Location")
    void shouldCreateCategorySuccessfully() throws Exception {
        // Arrange
        CategoryRequestDTO requestDTO = new CategoryRequestDTO("Potions");
        Category savedCategory = new Category(UUID.randomUUID(), "Potions");

        when(categoryUseCase.createCategory(any(Category.class))).thenReturn(savedCategory);

        // Act & Assert
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/api/categories/" + savedCategory.getId())))
                .andExpect(jsonPath("$.id").value(savedCategory.getId().toString()))
                .andExpect(jsonPath("$.name").value("Potions"));

        verify(categoryUseCase, times(1)).createCategory(any(Category.class));
    }

    @Test
    @DisplayName("POST /api/categories: Deve falhar no @Valid e retornar 422 Unprocessable Content se o nome for vazio")
    void shouldReturn422WhenCategoryNameIsBlank() throws Exception {
        // Arrange
        CategoryRequestDTO invalidRequestDTO = new CategoryRequestDTO("");

        // Act & Assert
        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequestDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Invalid data"))
                .andExpect(jsonPath("$.errors[0].fieldName").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Category name is required."));

        verify(categoryUseCase, never()).createCategory(any());
    }

    @Test
    @DisplayName("GET /api/categories/stats: Deve retornar as estatísticas com status 200 OK")
    void shouldGetCategoryStats() throws Exception {
        // Arrange
        CategoryStatsDTO statsDTO = new CategoryStatsDTO("Poké Balls", 42L);
        when(catalogUseCase.getCategoryStats(null)).thenReturn(List.of(statsDTO));

        // Act & Assert
        mockMvc.perform(get("/api/categories/stats")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].category").value("Poké Balls"))
                .andExpect(jsonPath("$[0].count").value(42));

        verify(catalogUseCase, times(1)).getCategoryStats(null);
    }

    @Test
    @DisplayName("GET /api/categories/stats: Deve repassar o Query Param 'search' corretamente")
    void shouldGetCategoryStatsWithSearchParam() throws Exception {
        // Arrange
        CategoryStatsDTO statsDTO = new CategoryStatsDTO("Healing", 5L);
        when(catalogUseCase.getCategoryStats("heal")).thenReturn(List.of(statsDTO));

        // Act & Assert
        mockMvc.perform(get("/api/categories/stats")
                .param("search", "heal")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Healing"))
                .andExpect(jsonPath("$[0].count").value(5));

        verify(catalogUseCase, times(1)).getCategoryStats("heal");
    }
}