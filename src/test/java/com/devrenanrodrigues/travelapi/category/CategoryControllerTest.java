package com.devrenanrodrigues.travelapi.category;

import com.devrenanrodrigues.travelapi.category.dto.CategoryRequestDTO;
import com.devrenanrodrigues.travelapi.category.dto.CategoryResponseDTO;
import com.devrenanrodrigues.travelapi.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/categories - Deve retornar categorias ativas por padrão com 200 OK")
    void shouldReturnActiveCategories() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponseDTO dto = new CategoryResponseDTO(id, "Praia", "praia", "beach", "#FFF", null, 1, true, false);

        when(categoryService.findAllActive()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Praia"))
                .andExpect(jsonPath("$[0].slug").value("praia"));
    }

    @Test
    @DisplayName("GET /api/categories/{id} - Deve retornar categoria por ID com 200 OK")
    void shouldReturnCategoryById() throws Exception {
        UUID id = UUID.randomUUID();
        CategoryResponseDTO dto = new CategoryResponseDTO(id, "Cultura", "cultura", "museum", "#000", null, 2, true, true);

        when(categoryService.findById(id)).thenReturn(dto);

        mockMvc.perform(get("/api/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cultura"))
                .andExpect(jsonPath("$.isPrimary").value(true));
    }

    @Test
    @DisplayName("POST /api/categories - Deve criar categoria com status 201 Created")
    void shouldCreateCategoryAndReturn201() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO("Aventura", "aventura", "compass", "#FF5500", null, 3, true, false);
        CategoryResponseDTO response = new CategoryResponseDTO(UUID.randomUUID(), "Aventura", "aventura", "compass", "#FF5500", null, 3, true, false);

        when(categoryService.create(any(CategoryRequestDTO.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Aventura"));
    }

    @Test
    @DisplayName("DELETE /api/categories/{id} - Deve remover categoria com status 204 No Content")
    void shouldDeleteCategoryAndReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(categoryService).delete(id);

        mockMvc.perform(delete("/api/categories/{id}", id))
                .andExpect(status().isNoContent());
    }
}
