package com.devrenanrodrigues.travelapi.category;

import com.devrenanrodrigues.travelapi.category.dto.CategoryRequestDTO;
import com.devrenanrodrigues.travelapi.category.dto.CategoryResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("Deve listar apenas categorias ativas ordenadas por sortOrder")
    void shouldFindAllActiveCategories() {
        Category cat = Category.builder()
                .id(UUID.randomUUID())
                .name("Praia")
                .slug("praia")
                .active(true)
                .sortOrder(1)
                .build();

        when(categoryRepository.findByActiveTrueOrderBySortOrderAsc()).thenReturn(List.of(cat));

        List<CategoryResponseDTO> result = categoryService.findAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().name()).isEqualTo("Praia");
    }

    @Test
    @DisplayName("Deve buscar categoria por ID com sucesso")
    void shouldFindCategoryById() {
        UUID id = UUID.randomUUID();
        Category cat = Category.builder().id(id).name("Montanha").slug("montanha").active(true).build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(cat));

        CategoryResponseDTO result = categoryService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(id);
        assertThat(result.name()).isEqualTo("Montanha");
    }

    @Test
    @DisplayName("Deve lançar 404 quando categoria não existir")
    void shouldThrow404WhenCategoryNotFound() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(id))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
                });
    }

    @Test
    @DisplayName("Deve criar categoria gerando slug automaticamente a partir do nome")
    void shouldCreateCategorySuccessfully() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Ecoturismo & Natureza", null, "tree", "#00FF00", null, 1, true, false);

        when(categoryRepository.existsByNameIgnoreCase("Ecoturismo & Natureza")).thenReturn(false);
        when(categoryRepository.existsBySlugIgnoreCase(any())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            return Category.builder()
                    .id(UUID.randomUUID())
                    .name(c.getName())
                    .slug(c.getSlug())
                    .icon(c.getIcon())
                    .accentColor(c.getAccentColor())
                    .active(c.getActive())
                    .build();
        });

        CategoryResponseDTO result = categoryService.create(dto);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Ecoturismo & Natureza");
        assertThat(result.slug()).isEqualTo("ecoturismo-natureza");
    }

    @Test
    @DisplayName("Deve lançar 409 quando nome da categoria já existir")
    void shouldThrow409WhenCategoryNameAlreadyExists() {
        CategoryRequestDTO dto = new CategoryRequestDTO("Praia", null, null, null, null, null, null, null);
        when(categoryRepository.existsByNameIgnoreCase("Praia")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(dto))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException rse = (ResponseStatusException) ex;
                    assertThat(rse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                });
    }

    @Test
    @DisplayName("Deve deletar categoria existente")
    void shouldDeleteCategorySuccessfully() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.existsById(id)).thenReturn(true);

        categoryService.delete(id);

        verify(categoryRepository).deleteById(id);
    }
}
