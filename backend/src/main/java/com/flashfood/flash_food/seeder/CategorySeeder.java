package com.flashfood.flash_food.seeder;

import com.flashfood.flash_food.entity.Category;
import com.flashfood.flash_food.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CategorySeeder - creates category data for Postgres.
 *
 * Enabled only when app.db.seed.enabled=true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.db.seed.enabled", havingValue = "true")
public class CategorySeeder implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categoryRepository.count() > 10) {
            log.info("CategorySeeder: categories already exist, skipping seeding.");
            return;
        }

        List<CategorySeed> rootSeeds = buildRootSeeds();
        Map<String, Category> rootBySlug = new HashMap<>();

        List<Category> rootEntities = rootSeeds.stream()
                .map(seed -> Category.builder()
                        .name(seed.name)
                        .slug(seed.slug)
                        .description(seed.description)
                        .iconUrl(seed.iconUrl)
                        .displayOrder(seed.displayOrder)
                        .isActive(true)
                        .build())
                .toList();

        List<Category> savedRoots = categoryRepository.saveAll(rootEntities);
        for (int i = 0; i < rootSeeds.size(); i++) {
            rootBySlug.put(rootSeeds.get(i).slug, savedRoots.get(i));
        }

        List<Category> children = new ArrayList<>();
        for (CategorySeed seed : buildChildSeeds()) {
            Category parent = rootBySlug.get(seed.parentSlug);
            if (parent == null) {
                continue;
            }
            children.add(Category.builder()
                    .name(seed.name)
                    .slug(seed.slug)
                    .description(seed.description)
                    .iconUrl(seed.iconUrl)
                    .displayOrder(seed.displayOrder)
                    .isActive(true)
                    .parent(parent)
                    .build());
        }

        List<Category> savedChildren = categoryRepository.saveAll(children);
        log.info("CategorySeeder: seeded {} root categories and {} child categories.",
                savedRoots.size(), savedChildren.size());
    }

    private List<CategorySeed> buildRootSeeds() {
        List<CategorySeed> seeds = new ArrayList<>();
        seeds.add(new CategorySeed(
                "Ready Meals",
                "ready-meals",
                "Rice dishes, noodles, soups, and daily sets.",
                "https://placehold.co/128x128?text=Meals",
                1,
                null));
        seeds.add(new CategorySeed(
                "Bakery",
                "bakery",
                "Bread, pastries, and baked snacks.",
                "https://placehold.co/128x128?text=Bakery",
                2,
                null));
        seeds.add(new CategorySeed(
                "Snacks",
                "snacks",
                "Grab-and-go snacks and street food.",
                "https://placehold.co/128x128?text=Snacks",
                3,
                null));
        seeds.add(new CategorySeed(
                "Drinks",
                "drinks",
                "Coffee, tea, and bottled drinks.",
                "https://placehold.co/128x128?text=Drinks",
                4,
                null));
        seeds.add(new CategorySeed(
                "Fresh Produce",
                "fresh-produce",
                "Fruits, vegetables, and fresh items.",
                "https://placehold.co/128x128?text=Produce",
                5,
                null));
        seeds.add(new CategorySeed(
                "Desserts",
                "desserts",
                "Sweet treats and chilled desserts.",
                "https://placehold.co/128x128?text=Dessert",
                6,
                null));
        return seeds;
    }

    private List<CategorySeed> buildChildSeeds() {
        List<CategorySeed> seeds = new ArrayList<>();
        seeds.add(new CategorySeed(
                "Rice Dishes",
                "rice-dishes",
                "Com tam, fried rice, and rice bowls.",
                "https://placehold.co/128x128?text=Rice",
                1,
                "ready-meals"));
        seeds.add(new CategorySeed(
                "Noodles",
                "noodles",
                "Pho, bun, and noodle soups.",
                "https://placehold.co/128x128?text=Noodles",
                2,
                "ready-meals"));
        seeds.add(new CategorySeed(
                "Soups",
                "soups",
                "Light soups and broths.",
                "https://placehold.co/128x128?text=Soup",
                3,
                "ready-meals"));
        seeds.add(new CategorySeed(
                "Vegetarian",
                "vegetarian",
                "Vegetarian meals and bowls.",
                "https://placehold.co/128x128?text=Veg",
                4,
                "ready-meals"));
        seeds.add(new CategorySeed(
                "Bread",
                "bread",
                "Banh mi, baguettes, and buns.",
                "https://placehold.co/128x128?text=Bread",
                1,
                "bakery"));
        seeds.add(new CategorySeed(
                "Pastry",
                "pastry",
                "Croissants and sweet pastries.",
                "https://placehold.co/128x128?text=Pastry",
                2,
                "bakery"));
        seeds.add(new CategorySeed(
                "Street Food",
                "street-food",
                "Skewers, rolls, and quick bites.",
                "https://placehold.co/128x128?text=Street",
                1,
                "snacks"));
        seeds.add(new CategorySeed(
                "Chips",
                "chips",
                "Chips and crunchy snacks.",
                "https://placehold.co/128x128?text=Chips",
                2,
                "snacks"));
        seeds.add(new CategorySeed(
                "Coffee",
                "coffee",
                "Hot and iced coffee.",
                "https://placehold.co/128x128?text=Coffee",
                1,
                "drinks"));
        seeds.add(new CategorySeed(
                "Tea",
                "tea",
                "Milk tea and herbal tea.",
                "https://placehold.co/128x128?text=Tea",
                2,
                "drinks"));
        seeds.add(new CategorySeed(
                "Juice",
                "juice",
                "Fresh juices and smoothies.",
                "https://placehold.co/128x128?text=Juice",
                3,
                "drinks"));
        seeds.add(new CategorySeed(
                "Fruits",
                "fruits",
                "Seasonal fruits and packs.",
                "https://placehold.co/128x128?text=Fruits",
                1,
                "fresh-produce"));
        seeds.add(new CategorySeed(
                "Vegetables",
                "vegetables",
                "Leafy greens and daily vegetables.",
                "https://placehold.co/128x128?text=Veg",
                2,
                "fresh-produce"));
        seeds.add(new CategorySeed(
                "Cakes",
                "cakes",
                "Cakes and slices.",
                "https://placehold.co/128x128?text=Cake",
                1,
                "desserts"));
        seeds.add(new CategorySeed(
                "Pudding",
                "pudding",
                "Pudding cups and chilled desserts.",
                "https://placehold.co/128x128?text=Pudding",
                2,
                "desserts"));
        return seeds;
    }

    private static final class CategorySeed {
        private final String name;
        private final String slug;
        private final String description;
        private final String iconUrl;
        private final int displayOrder;
        private final String parentSlug;

        private CategorySeed(String name, String slug, String description,
                             String iconUrl, int displayOrder, String parentSlug) {
            this.name = name;
            this.slug = slug;
            this.description = description;
            this.iconUrl = iconUrl;
            this.displayOrder = displayOrder;
            this.parentSlug = parentSlug;
        }
    }
}
