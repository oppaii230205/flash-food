package com.flashfood.flash_food.seeder;

import com.flashfood.flash_food.entity.Profile;
import com.flashfood.flash_food.entity.Store;
import com.flashfood.flash_food.entity.StoreStatus;
import com.flashfood.flash_food.entity.StoreType;
import com.flashfood.flash_food.entity.User;
import com.flashfood.flash_food.entity.UserRole;
import com.flashfood.flash_food.entity.UserStatus;
import com.flashfood.flash_food.repository.ProfileRepository;
import com.flashfood.flash_food.repository.StoreRepository;
import com.flashfood.flash_food.repository.UserRepository;
import com.flashfood.flash_food.service.RedisGeoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * StoreSeeder - creates realistic store data for Postgres and Redis Geo.
 *
 * Enabled only when app.db.seed.enabled=true.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.db.seed.enabled", havingValue = "true")
public class StoreSeeder implements ApplicationRunner {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final RedisGeoService redisGeoService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.db.seed.random-seed:42}")
    private long randomSeed;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (storeRepository.count() > 10) {
            log.info("StoreSeeder: stores already exist, skipping seeding.");
            return;
        }

        List<User> owners = ensureOwners();
        if (owners.isEmpty()) {
            log.warn("StoreSeeder: no owners available, skipping store seeding.");
            return;
        }

        List<StoreSeed> seeds = buildStoreSeeds();
        Random rng = new Random(randomSeed);

        List<Store> stores = new ArrayList<>();
        for (int i = 0; i < seeds.size(); i++) {
            StoreSeed seed = seeds.get(i);
            User owner = owners.get(i % owners.size());

            Store store = Store.builder()
                    .name(seed.name)
                    .address(seed.address)
                    .phoneNumber(seed.phoneNumber)
                    .latitude(seed.latitude)
                    .longitude(seed.longitude)
                    .type(seed.type)
                    .description(seed.description)
                    .imageUrl(seed.imageUrl)
                    .openTime(seed.openTime)
                    .closeTime(seed.closeTime)
                    .flashSaleTime(seed.flashSaleTime)
                    .status(StoreStatus.ACTIVE)
                    .rating(seed.baseRating + rng.nextDouble() * 0.7)
                    .totalRatings(seed.baseRatings + rng.nextInt(200))
                    .owner(owner)
                    .build();

            stores.add(store);
        }

        List<Store> savedStores = storeRepository.saveAll(stores);
        savedStores.forEach(store ->
                redisGeoService.addStoreLocation(store.getId(), store.getLongitude(), store.getLatitude()));

        log.info("StoreSeeder: seeded {} stores and indexed Redis geo.", savedStores.size());
    }

    private List<User> ensureOwners() {
        List<OwnerSeed> ownerSeeds = buildOwnerSeeds();
        List<User> owners = new ArrayList<>();

        for (OwnerSeed seed : ownerSeeds) {
            User owner = userRepository.findByEmail(seed.email).orElseGet(() -> {
                User created = User.builder()
                        .email(seed.email)
                        .password(passwordEncoder.encode(seed.rawPassword))
                        .roles(Set.of(UserRole.STORE_OWNER))
                        .status(UserStatus.ACTIVE)
                        .build();

                User saved = userRepository.save(created);

                Profile profile = Profile.builder()
                        .user(saved)
                        .fullName(seed.fullName)
                        .phoneNumber(seed.phoneNumber)
                        .address(seed.address)
                        .avatarUrl(seed.avatarUrl)
                        .build();

                Profile savedProfile = profileRepository.save(profile);
                saved.setProfile(savedProfile);
                return userRepository.save(saved);
            });

            owners.add(owner);
        }

        return owners;
    }

    private List<OwnerSeed> buildOwnerSeeds() {
        List<OwnerSeed> seeds = new ArrayList<>();
        seeds.add(new OwnerSeed(
                "owner1@flashfood.local",
                "Minh Tran",
                "0901000001",
                "12 Nguyen Hue, District 1, HCMC",
                "https://placehold.co/200x200?text=Owner",
                "Password123!"));
        seeds.add(new OwnerSeed(
                "owner2@flashfood.local",
                "Linh Nguyen",
                "0901000002",
                "85 Vo Van Tan, District 3, HCMC",
                "https://placehold.co/200x200?text=Owner",
                "Password123!"));
        seeds.add(new OwnerSeed(
                "owner3@flashfood.local",
                "Huy Le",
                "0901000003",
                "220 Nguyen Tri Phuong, District 10, HCMC",
                "https://placehold.co/200x200?text=Owner",
                "Password123!"));
        seeds.add(new OwnerSeed(
                "owner4@flashfood.local",
                "Thao Pham",
                "0901000004",
                "15 Nguyen Van Linh, District 7, HCMC",
                "https://placehold.co/200x200?text=Owner",
                "Password123!"));
        seeds.add(new OwnerSeed(
                "owner5@flashfood.local",
                "Khanh Do",
                "0901000005",
                "64 Phan Xich Long, Phu Nhuan, HCMC",
                "https://placehold.co/200x200?text=Owner",
                "Password123!"));
        return seeds;
    }

    private List<StoreSeed> buildStoreSeeds() {
        List<StoreSeed> seeds = new ArrayList<>();

        seeds.add(new StoreSeed(
                "Ben Thanh Bites",
                "45 Le Loi, District 1, HCMC",
                "0902000001",
                10.7734,
                106.6980,
                StoreType.RESTAURANT,
                "Local favorites with quick pickup for office lunch.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 30),
                LocalTime.of(22, 0),
                LocalTime.of(20, 30),
                4.1,
                320));

        seeds.add(new StoreSeed(
                "Saigon Fresh Bakery",
                "12 Pasteur, District 1, HCMC",
                "0902000002",
                10.7756,
                106.7022,
                StoreType.BAKERY,
                "Breads and pastries baked every morning.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(5, 30),
                LocalTime.of(20, 0),
                LocalTime.of(19, 30),
                4.4,
                180));

        seeds.add(new StoreSeed(
                "Nguyen Hue Cafe",
                "88 Nguyen Hue, District 1, HCMC",
                "0902000003",
                10.7739,
                106.7045,
                StoreType.CAFE,
                "Cold brew, milk tea, and light snacks.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(23, 0),
                LocalTime.of(21, 0),
                4.3,
                260));

        seeds.add(new StoreSeed(
                "District 3 Kitchen",
                "210 Nguyen Dinh Chieu, District 3, HCMC",
                "0902000004",
                10.7846,
                106.6875,
                StoreType.RESTAURANT,
                "Family recipes with rotating daily sets.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 0),
                LocalTime.of(21, 30),
                LocalTime.of(20, 0),
                4.0,
                140));

        seeds.add(new StoreSeed(
                "Vo Van Tan Noodles",
                "120 Vo Van Tan, District 3, HCMC",
                "0902000005",
                10.7820,
                106.6860,
                StoreType.RESTAURANT,
                "Noodle bowls with quick service for students.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                LocalTime.of(19, 0),
                3.9,
                95));

        seeds.add(new StoreSeed(
                "Pham Ngu Lao Banh Mi",
                "180 Pham Ngu Lao, District 1, HCMC",
                "0902000006",
                10.7698,
                106.6926,
                StoreType.FAST_FOOD,
                "Classic banh mi with fresh herbs.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 0),
                LocalTime.of(23, 30),
                LocalTime.of(21, 30),
                4.2,
                410));

        seeds.add(new StoreSeed(
                "Cho Lon Dumplings",
                "320 Tran Hung Dao, District 5, HCMC",
                "0902000007",
                10.7537,
                106.6633,
                StoreType.RESTAURANT,
                "Handmade dumplings and soups.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(20, 0),
                4.1,
                210));

        seeds.add(new StoreSeed(
                "District 10 Hotpot",
                "15 Su Van Hanh, District 10, HCMC",
                "0902000008",
                10.7748,
                106.6670,
                StoreType.BUFFET,
                "Hotpot buffet with late-night flash deals.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(10, 0),
                LocalTime.of(23, 0),
                LocalTime.of(21, 30),
                4.0,
                175));

        seeds.add(new StoreSeed(
                "Crescent Mall Deli",
                "101 Ton Dat Tien, District 7, HCMC",
                "0902000009",
                10.7287,
                106.7216,
                StoreType.CONVENIENCE_STORE,
                "Ready-to-eat meals and pantry staples.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(22, 0),
                LocalTime.of(20, 0),
                3.8,
                120));

        seeds.add(new StoreSeed(
                "Phu My Hung Market",
                "20 Nguyen Van Linh, District 7, HCMC",
                "0902000010",
                10.7299,
                106.7229,
                StoreType.CONVENIENCE_STORE,
                "Fresh produce with nightly bundles.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 0),
                LocalTime.of(21, 0),
                LocalTime.of(19, 30),
                4.1,
                150));

        seeds.add(new StoreSeed(
                "Binh Thanh Rice Bowl",
                "58 Dien Bien Phu, Binh Thanh, HCMC",
                "0902000011",
                10.8021,
                106.7199,
                StoreType.RESTAURANT,
                "Rice bowls and grilled meats for office crowd.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 30),
                LocalTime.of(22, 0),
                LocalTime.of(20, 30),
                4.2,
                190));

        seeds.add(new StoreSeed(
                "Go Vap Corner Cafe",
                "14 Quang Trung, Go Vap, HCMC",
                "0902000012",
                10.8350,
                106.6671,
                StoreType.CAFE,
                "Small batch coffee and sweet pastries.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 30),
                LocalTime.of(22, 30),
                LocalTime.of(20, 0),
                4.0,
                140));

        seeds.add(new StoreSeed(
                "Tan Binh Quick Mart",
                "77 Truong Son, Tan Binh, HCMC",
                "0902000013",
                10.8012,
                106.6529,
                StoreType.CONVENIENCE_STORE,
                "Snacks, drinks, and late pickup.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(23, 0),
                LocalTime.of(21, 30),
                3.7,
                80));

        seeds.add(new StoreSeed(
                "Phu Nhuan Pho",
                "98 Phan Xich Long, Phu Nhuan, HCMC",
                "0902000014",
                10.7994,
                106.6798,
                StoreType.RESTAURANT,
                "Pho and broken rice with late-night deals.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 0),
                LocalTime.of(22, 0),
                LocalTime.of(20, 30),
                4.1,
                220));

        seeds.add(new StoreSeed(
                "Thu Duc Bento",
                "12 Vo Van Ngan, Thu Duc, HCMC",
                "0902000015",
                10.8518,
                106.7662,
                StoreType.RESTAURANT,
                "Bento sets for campus and offices.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(21, 0),
                LocalTime.of(19, 30),
                3.9,
                110));

        seeds.add(new StoreSeed(
                "Tan Phu Grill",
                "190 Luong The Vinh, Tan Phu, HCMC",
                "0902000016",
                10.7920,
                106.6270,
                StoreType.FAST_FOOD,
                "Grilled plates and combo boxes.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                LocalTime.of(20, 30),
                3.8,
                90));

        seeds.add(new StoreSeed(
                "Binh Tan Food Hub",
                "23 Kinh Duong Vuong, Binh Tan, HCMC",
                "0902000017",
                10.7489,
                106.6067,
                StoreType.BUFFET,
                "Buffet options with family packs.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(11, 0),
                LocalTime.of(23, 0),
                LocalTime.of(21, 0),
                3.7,
                70));

        seeds.add(new StoreSeed(
                "District 11 Rice",
                "55 Lac Long Quan, District 11, HCMC",
                "0902000018",
                10.7672,
                106.6433,
                StoreType.RESTAURANT,
                "Rice plates and seasonal specials.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 30),
                LocalTime.of(21, 0),
                LocalTime.of(19, 0),
                4.0,
                130));

        seeds.add(new StoreSeed(
                "Binh Chanh Meal Prep",
                "10 Nguyen Van Linh, Binh Chanh, HCMC",
                "0902000019",
                10.7138,
                106.5347,
                StoreType.RESTAURANT,
                "Daily meal prep with bulk bundles.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(6, 0),
                LocalTime.of(20, 30),
                LocalTime.of(19, 0),
                3.6,
                60));

        seeds.add(new StoreSeed(
                "Nha Be Seafood",
                "2 Nguyen Binh, Nha Be, HCMC",
                "0902000020",
                10.6768,
                106.7347,
                StoreType.RESTAURANT,
                "Seafood rice sets and grilled items.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(9, 0),
                LocalTime.of(21, 30),
                LocalTime.of(20, 0),
                3.9,
                85));

        seeds.add(new StoreSeed(
                "Hoc Mon Banh Bao",
                "8 Phan Van Hon, Hoc Mon, HCMC",
                "0902000021",
                10.8890,
                106.5966,
                StoreType.BAKERY,
                "Warm buns and savory rolls.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(5, 30),
                LocalTime.of(19, 0),
                LocalTime.of(18, 0),
                3.8,
                55));

        seeds.add(new StoreSeed(
                "Cu Chi Farm Kitchen",
                "20 Tinh Lo 8, Cu Chi, HCMC",
                "0902000022",
                10.9731,
                106.4939,
                StoreType.RESTAURANT,
                "Farm-to-table sets with flash bundles.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(20, 0),
                LocalTime.of(19, 0),
                3.7,
                40));

        seeds.add(new StoreSeed(
                "Go Vap Night Snacks",
                "112 Phan Van Tri, Go Vap, HCMC",
                "0902000023",
                10.8299,
                106.6736,
                StoreType.FAST_FOOD,
                "Late night snacks and combos.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(16, 0),
                LocalTime.of(23, 59),
                LocalTime.of(22, 30),
                3.9,
                95));

        seeds.add(new StoreSeed(
                "Saigon Riverside Cafe",
                "10 Ton Duc Thang, District 1, HCMC",
                "0902000024",
                10.7804,
                106.7069,
                StoreType.CAFE,
                "Riverside drinks with evening specials.",
                "https://placehold.co/640x420?text=Store",
                LocalTime.of(7, 0),
                LocalTime.of(23, 0),
                LocalTime.of(21, 30),
                4.2,
                160));

        return seeds;
    }

    private static final class OwnerSeed {
        private final String email;
        private final String fullName;
        private final String phoneNumber;
        private final String address;
        private final String avatarUrl;
        private final String rawPassword;

        private OwnerSeed(String email, String fullName, String phoneNumber,
                          String address, String avatarUrl, String rawPassword) {
            this.email = email;
            this.fullName = fullName;
            this.phoneNumber = phoneNumber;
            this.address = address;
            this.avatarUrl = avatarUrl;
            this.rawPassword = rawPassword;
        }
    }

    private static final class StoreSeed {
        private final String name;
        private final String address;
        private final String phoneNumber;
        private final double latitude;
        private final double longitude;
        private final StoreType type;
        private final String description;
        private final String imageUrl;
        private final LocalTime openTime;
        private final LocalTime closeTime;
        private final LocalTime flashSaleTime;
        private final double baseRating;
        private final int baseRatings;

        private StoreSeed(String name, String address, String phoneNumber,
                          double latitude, double longitude, StoreType type,
                          String description, String imageUrl,
                          LocalTime openTime, LocalTime closeTime,
                          LocalTime flashSaleTime, double baseRating, int baseRatings) {
            this.name = name;
            this.address = address;
            this.phoneNumber = phoneNumber;
            this.latitude = latitude;
            this.longitude = longitude;
            this.type = type;
            this.description = description;
            this.imageUrl = imageUrl;
            this.openTime = openTime;
            this.closeTime = closeTime;
            this.flashSaleTime = flashSaleTime;
            this.baseRating = baseRating;
            this.baseRatings = baseRatings;
        }
    }
}
