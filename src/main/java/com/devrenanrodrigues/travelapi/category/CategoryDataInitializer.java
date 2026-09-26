package com.devrenanrodrigues.travelapi.category;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CategoryDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedCategories();
    }

    private void seedCategories() {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<Category> defaultCategories = List.of(
                Category.builder().name("Florestas").slug("florestas").icon("leaf").accentColor("#4CAF50").bgImageUrl("https://images.pexels.com/photos/6040064/pexels-photo-6040064.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(0).active(true).build(),
                Category.builder().name("Praias").slug("praias").icon("waves").accentColor("#00B4D8").bgImageUrl("https://images.pexels.com/photos/10392591/pexels-photo-10392591.jpeg?auto=compress&cs=tinysrgb&fit=crop&w=1260&h=750&dpr=1").sortOrder(1).active(true).build(),
                Category.builder().name("Montanhas").slug("montanhas").icon("image-filter-hdr").accentColor("#FFA726").bgImageUrl("https://images.pexels.com/photos/933054/pexels-photo-933054.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(2).active(true).build(),
                Category.builder().name("Ilhas").slug("ilhas").icon("island").accentColor("#26A69A").bgImageUrl("https://images.pexels.com/photos/1450360/pexels-photo-1450360.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(3).active(true).build(),
                Category.builder().name("Cachoeiras").slug("cachoeiras").icon("waterfall").accentColor("#80DEEA").bgImageUrl("https://images.pexels.com/photos/28404799/pexels-photo-28404799.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(4).active(true).build(),
                Category.builder().name("Desertos").slug("deserto").icon("white-balance-sunny").accentColor("#FF7043").bgImageUrl("https://images.pexels.com/photos/1001435/pexels-photo-1001435.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(5).active(true).build(),
                Category.builder().name("Cânions").slug("canions").icon("triangle-outline").accentColor("#D35400").bgImageUrl("https://images.pexels.com/photos/13641027/pexels-photo-13641027.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(6).active(true).build(),
                Category.builder().name("Penhascos").slug("penhascos").icon("trending-up-outline").accentColor("#8E44AD").bgImageUrl("https://images.pexels.com/photos/6175595/pexels-photo-6175595.jpeg?auto=compress&cs=tinysrgb&fit=crop&w=1260&h=750&dpr=1").sortOrder(7).active(true).build(),
                Category.builder().name("Vulcões").slug("vulcoes").icon("flame-outline").accentColor("#E74C3C").bgImageUrl("https://images.pexels.com/photos/38262907/pexels-photo-38262907.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(8).active(true).build(),
                Category.builder().name("Neve").slug("neve").icon("snowflake").accentColor("#E0F7FA").bgImageUrl("https://images.pexels.com/photos/35636196/pexels-photo-35636196.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(9).active(true).build(),
                Category.builder().name("Geleiras").slug("geleiras").icon("snow-outline").accentColor("#5DADE2").bgImageUrl("https://images.pexels.com/photos/12071906/pexels-photo-12071906.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(10).active(true).build(),
                Category.builder().name("Cavernas").slug("cavernas").icon("moon-outline").accentColor("#34495E").bgImageUrl("https://images.pexels.com/photos/14924492/pexels-photo-14924492.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(11).active(true).build(),
                Category.builder().name("Vales").slug("vales").icon("map-outline").accentColor("#27AE60").bgImageUrl("https://images.pexels.com/photos/39556872/pexels-photo-39556872.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(12).active(true).build(),
                Category.builder().name("Histórico").slug("historico").icon("pillar").accentColor("#D4AF37").bgImageUrl("https://images.pexels.com/photos/2044434/pexels-photo-2044434.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(13).active(true).build(),
                Category.builder().name("Surf").slug("surf").icon("surfing").accentColor("#00ACC1").bgImageUrl("https://images.pexels.com/photos/15036515/pexels-photo-15036515.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(14).active(true).build(),
                Category.builder().name("Cultural").slug("cultural").icon("color-palette-outline").accentColor("#E91E63").bgImageUrl("https://images.pexels.com/photos/31267774/pexels-photo-31267774.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(15).active(true).build(),
                Category.builder().name("Lagos").slug("lagos").icon("water-outline").accentColor("#3498DB").bgImageUrl("https://images.pexels.com/photos/36103492/pexels-photo-36103492.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(16).active(true).build(),
                Category.builder().name("Rios").slug("rios").icon("navigate-outline").accentColor("#2980B9").bgImageUrl("https://images.pexels.com/photos/37931564/pexels-photo-37931564.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(17).active(true).build(),
                Category.builder().name("Interior").slug("interior").icon("home-variant-outline").accentColor("#AED581").bgImageUrl("https://images.pexels.com/photos/16725824/pexels-photo-16725824.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(18).active(true).build(),
                Category.builder().name("Aventura").slug("aventura").icon("bicycle-outline").accentColor("#F1C40F").bgImageUrl("https://images.pexels.com/photos/3098647/pexels-photo-3098647.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(19).active(true).build(),
                Category.builder().name("Costeiro").slug("costeiro").icon("compass-outline").accentColor("#00A8CC").bgImageUrl("https://images.pexels.com/photos/32041610/pexels-photo-32041610.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(20).active(true).build(),
                Category.builder().name("Rural").slug("rural").icon("home-outline").accentColor("#795548").bgImageUrl("https://images.pexels.com/photos/15111268/pexels-photo-15111268.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(21).active(true).build(),
                Category.builder().name("Arquitetônico").slug("arquitetonico").icon("business-outline").accentColor("#7F8C8D").bgImageUrl("https://images.pexels.com/photos/7316991/pexels-photo-7316991.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(22).active(true).build(),
                Category.builder().name("Arqueológico").slug("arqueologico").icon("hourglass-outline").accentColor("#A569BD").bgImageUrl("https://images.pexels.com/photos/35417009/pexels-photo-35417009.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(23).active(true).build(),
                Category.builder().name("Recifes").slug("recifes").icon("fish-outline").accentColor("#16A085").bgImageUrl("https://images.pexels.com/photos/8395909/pexels-photo-8395909.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(24).active(true).build(),
                Category.builder().name("Pântanos").slug("pantanos").icon("leaf-outline").accentColor("#1E8449").bgImageUrl("https://images.pexels.com/photos/28869097/pexels-photo-28869097.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(25).active(true).build(),
                Category.builder().name("Termal").slug("termal").icon("flame-outline").accentColor("#E67E22").bgImageUrl("https://images.pexels.com/photos/23322327/pexels-photo-23322327.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(26).active(true).build(),
                Category.builder().name("Urbano").slug("urbano").icon("city-variant-outline").accentColor("#90CAF9").bgImageUrl("https://images.pexels.com/photos/15271798/pexels-photo-15271798.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(27).active(true).build(),
                Category.builder().name("Parques Nacionais").slug("parques-nacionais").icon("pine-tree").accentColor("#2E7D32").bgImageUrl("https://images.pexels.com/photos/11702774/pexels-photo-11702774.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(28).active(true).build(),
                Category.builder().name("Safári").slug("safari").icon("paw").accentColor("#C2843A").bgImageUrl("https://images.pexels.com/photos/9185432/pexels-photo-9185432.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(29).active(true).build(),
                Category.builder().name("Resorts").slug("resorts").icon("umbrella-beach").accentColor("#00BCD4").bgImageUrl("https://images.pexels.com/photos/6821435/pexels-photo-6821435.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(30).active(true).build(),
                Category.builder().name("Vinhedos").slug("vinhedos").icon("glass-wine").accentColor("#880E4F").bgImageUrl("https://images.pexels.com/photos/23441099/pexels-photo-23441099.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(31).active(true).build(),
                Category.builder().name("Castelos").slug("castelos").icon("castle").accentColor("#5C6BC0").bgImageUrl("https://images.pexels.com/photos/11348493/pexels-photo-11348493.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(32).active(true).build(),
                Category.builder().name("Mergulho").slug("mergulho").icon("diving-scuba").accentColor("#0288D1").bgImageUrl("https://images.pexels.com/photos/7169290/pexels-photo-7169290.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(33).active(true).build(),
                Category.builder().name("Gastronomia").slug("gastronomia").icon("silverware-fork-knife").accentColor("#FF5722").bgImageUrl("https://images.pexels.com/photos/27381531/pexels-photo-27381531.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(34).active(true).build(),
                Category.builder().name("Mirantes").slug("mirantes").icon("binoculars").accentColor("#3F51B5").bgImageUrl("https://images.pexels.com/photos/28516035/pexels-photo-28516035.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(35).active(true).build(),
                Category.builder().name("Romântico").slug("romantico").icon("heart-outline").accentColor("#E91E63").bgImageUrl("https://images.pexels.com/photos/39064478/pexels-photo-39064478.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(36).active(true).build(),
                Category.builder().name("Entretenimento").slug("entretenimento").icon("ticket-outline").accentColor("#9C27B0").bgImageUrl("https://images.pexels.com/photos/10807092/pexels-photo-10807092.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(37).active(true).build(),
                Category.builder().name("Trilhas").slug("trilhas").icon("hiking").accentColor("#43A047").bgImageUrl("https://images.pexels.com/photos/35525826/pexels-photo-35525826.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(38).active(true).build(),
                Category.builder().name("Cidades Fluviais").slug("cidades-fluviais").icon("ferry").accentColor("#0097A7").bgImageUrl("https://images.pexels.com/photos/6152717/pexels-photo-6152717.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1").sortOrder(39).active(true).build()
        );

        categoryRepository.saveAll(defaultCategories);
    }
}
