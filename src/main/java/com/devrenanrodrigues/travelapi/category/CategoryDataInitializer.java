package com.devrenanrodrigues.travelapi.category;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final EntityManager entityManager;

    @Override
    @Transactional
    public void run(String... args) {
        seedCategories();
        migrateExistingDestinationsCategories();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> defaultCategories = List.of(
                Category.builder().name("Florestas").slug("florestas").icon("leaf").accentColor("#4CAF50").sortOrder(0).active(true).build(),
                Category.builder().name("Praias").slug("praias").icon("waves").accentColor("#00B4D8").bgImageUrl("https://images.pexels.com/photos/1007657/pexels-photo-1007657.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(1).active(true).build(),
                Category.builder().name("Montanhas").slug("montanhas").icon("image-filter-hdr").accentColor("#FFA726").bgImageUrl("https://images.pexels.com/photos/933054/pexels-photo-933054.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(2).active(true).build(),
                Category.builder().name("Cachoeiras").slug("cachoeiras").icon("waterfall").accentColor("#80DEEA").bgImageUrl("https://images.pexels.com/photos/14659324/pexels-photo-14659324.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(3).active(true).build(),
                Category.builder().name("Deserto").slug("deserto").icon("white-balance-sunny").accentColor("#FF7043").bgImageUrl("https://images.pexels.com/photos/1001435/pexels-photo-1001435.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(4).active(true).build(),
                Category.builder().name("Neve").slug("neve").icon("snowflake").accentColor("#E0F7FA").bgImageUrl("https://images.pexels.com/photos/839462/pexels-photo-839462.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(5).active(true).build(),
                Category.builder().name("Histórico").slug("historico").icon("pillar").accentColor("#D4AF37").bgImageUrl("https://images.pexels.com/photos/2044434/pexels-photo-2044434.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(6).active(true).build(),
                Category.builder().name("Urbano").slug("urbano").icon("city-variant-outline").accentColor("#90CAF9").bgImageUrl("https://images.pexels.com/photos/15271798/pexels-photo-15271798.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(7).active(true).build(),
                Category.builder().name("Ilhas").slug("ilhas").icon("island").accentColor("#26A69A").bgImageUrl("https://images.pexels.com/photos/1450360/pexels-photo-1450360.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(8).active(true).build(),
                Category.builder().name("Interior").slug("interior").icon("home-variant-outline").accentColor("#AED581").bgImageUrl("https://images.pexels.com/photos/16725824/pexels-photo-16725824.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(9).active(true).build()
        );

        categoryRepository.saveAll(defaultCategories);
    }

    private void migrateExistingDestinationsCategories() {
        try {
            entityManager.createNativeQuery(
                    "INSERT INTO destination_categories (destination_id, category_id) " +
                    "SELECT d.id, c.id " +
                    "FROM destinations d, " +
                    "     unnest(d.categories) AS cat_name " +
                    "JOIN categories c ON LOWER(TRIM(c.name)) = LOWER(TRIM(cat_name)) " +
                    "ON CONFLICT DO NOTHING"
            ).executeUpdate();
        } catch (Exception ignored) {
        }
    }
}
