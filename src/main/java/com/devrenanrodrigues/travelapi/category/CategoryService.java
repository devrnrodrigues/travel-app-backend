package com.devrenanrodrigues.travelapi.category;

import com.devrenanrodrigues.travelapi.category.dto.CategoryRequestDTO;
import com.devrenanrodrigues.travelapi.category.dto.CategoryResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAllActive() {
        return categoryRepository.findByActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> findAll() {
        return categoryRepository.findAll()
                .stream()
                .map(CategoryResponseDTO::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO findById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada com o id: " + id));
        return CategoryResponseDTO.fromEntity(category);
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO dto) {
        String trimmedName = dto.name().trim();
        String slug = (dto.slug() != null && !dto.slug().isBlank())
                ? generateSlug(dto.slug())
                : generateSlug(trimmedName);

        if (categoryRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já existe com este nome: " + trimmedName);
        }

        if (categoryRepository.existsBySlugIgnoreCase(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já existe com este slug: " + slug);
        }

        Category category = Category.builder()
                .name(trimmedName)
                .slug(slug)
                .icon(dto.icon() != null ? dto.icon().trim() : null)
                .accentColor(dto.accentColor() != null ? dto.accentColor().trim() : null)
                .bgImageUrl(dto.bgImageUrl() != null ? dto.bgImageUrl().trim() : null)
                .sortOrder(dto.sortOrder() != null ? dto.sortOrder() : 0)
                .active(dto.active() != null ? dto.active() : true)
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponseDTO.fromEntity(saved);
    }

    @Transactional
    public CategoryResponseDTO update(UUID id, CategoryRequestDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada com o id: " + id));

        String trimmedName = dto.name().trim();
        String slug = (dto.slug() != null && !dto.slug().isBlank())
                ? generateSlug(dto.slug())
                : generateSlug(trimmedName);

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já existe com este nome: " + trimmedName);
        }

        if (categoryRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Categoria já existe com este slug: " + slug);
        }

        category.setName(trimmedName);
        category.setSlug(slug);
        category.setIcon(dto.icon() != null ? dto.icon().trim() : null);
        category.setAccentColor(dto.accentColor() != null ? dto.accentColor().trim() : null);
        category.setBgImageUrl(dto.bgImageUrl() != null ? dto.bgImageUrl().trim() : null);
        if (dto.sortOrder() != null) {
            category.setSortOrder(dto.sortOrder());
        }
        if (dto.active() != null) {
            category.setActive(dto.active());
        }

        return CategoryResponseDTO.fromEntity(category);
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada com o id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public static String generateSlug(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}", "");
        return withoutAccents.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
