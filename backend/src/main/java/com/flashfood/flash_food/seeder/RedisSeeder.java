package com.flashfood.flash_food.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * RedisSeeder — populates Redis geo-spatial indexes with synthetic data.
 *
 * <p>Activated only when {@code app.redis.seed.enabled=true} is set, so it never
 * fires in production unless explicitly requested (e.g. during load-test setup).
 *
 * <h3>What is seeded</h3>
 * <ul>
 *   <li>{@code geo:stores} — 50 000 fake store locations scattered within ~50 km
 *       of Ho Chi Minh City centre (the area exercised by the k6 search test).</li>
 *   <li>{@code geo:users} — optionally seeded; disabled by default
 *       ({@code app.redis.seed.users=false}).</li>
 * </ul>
 *
 * <h3>Performance</h3>
 * All {@code GEOADD} commands are issued through Redis pipelining in batches of
 * {@value #BATCH_SIZE}, keeping the total round-trip count to
 * {@code ceil(50000 / BATCH_SIZE)} instead of 50 000.  On a local Redis instance
 * this completes in roughly 2–5 seconds.
 *
 * <h3>Idempotency</h3>
 * If the target key already has members the seeder logs a warning and skips it,
 * so restarting the application with the flag enabled does not duplicate data.
 * To force a re-seed, flush the key manually ({@code DEL geo:stores}) and restart.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.redis.seed.enabled", havingValue = "true")
public class RedisSeeder implements ApplicationRunner {

    private final RedisTemplate<String, Object> redisTemplate;

    // -----------------------------------------------------------------
    // Redis key constants (must match RedisGeoService)
    // -----------------------------------------------------------------
    private static final String STORE_GEO_KEY = "geo:stores";
    private static final String USER_GEO_KEY  = "geo:users";

    // -----------------------------------------------------------------
    // Seeding parameters
    // -----------------------------------------------------------------
    private static final int    TOTAL_RECORDS = 50_000;
    private static final int    BATCH_SIZE    = 500;

    // Ho Chi Minh City centre — matches the k6 load-test coordinates
    private static final double BASE_LAT   = 10.7769;
    private static final double BASE_LON   = 106.7009;

    // ±0.45° ≈ ±50 km; keeps all points within Greater HCMC / Binh Duong / Long An
    private static final double LAT_SPREAD = 0.45;
    private static final double LON_SPREAD = 0.45;

    // -----------------------------------------------------------------
    // Configurable flags
    // -----------------------------------------------------------------
    @Value("${app.redis.seed.stores:true}")
    private boolean seedStores;

    @Value("${app.redis.seed.users:false}")
    private boolean seedUsers;

    @Value("${app.redis.seed.random-seed:42}")
    private long randomSeed;

    // -----------------------------------------------------------------
    // ApplicationRunner entry point
    // -----------------------------------------------------------------

    @Override
    public void run(ApplicationArguments args) {
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  RedisSeeder — starting ({} records target)  ║", TOTAL_RECORDS);
        log.info("╚══════════════════════════════════════════════╝");

        long wallStart = System.currentTimeMillis();

        if (seedStores) {
            seedIfEmpty(STORE_GEO_KEY, TOTAL_RECORDS, 1L, "Store");
        }

        if (seedUsers) {
            seedIfEmpty(USER_GEO_KEY, TOTAL_RECORDS, 1L, "User");
        }

        long elapsed = System.currentTimeMillis() - wallStart;
        log.info("╔══════════════════════════════════════════════╗");
        log.info("║  RedisSeeder — finished in {} ms             ║", elapsed);
        log.info("╚══════════════════════════════════════════════╝");
    }

    // -----------------------------------------------------------------
    // Internal helpers
    // -----------------------------------------------------------------

    /**
     * Seeds {@code count} geo-entries into {@code redisKey} only when the key is
     * empty.  If the key already contains data a warning is emitted and seeding
     * is skipped to avoid duplicates.
     */
    private void seedIfEmpty(String redisKey, int count, long idOffset, String label) {
        long existing = zsetSize(redisKey);
        if (existing > 0) {
            log.warn("[{}] '{}' already contains {} members — skipping. "
                    + "DEL the key and restart to force a re-seed.",
                    label, redisKey, existing);
            return;
        }
        seedGeoKey(redisKey, count, idOffset, label);
    }

    /**
     * Bulk-inserts {@code count} synthetic geo-locations into {@code redisKey}
     * using Redis pipelining.
     *
     * <p>Coordinates are generated with a fixed random seed so that the same
     * dataset is reproduced on every clean run, making performance comparisons
     * repeatable.
     *
     * @param redisKey the sorted-set / geo key (e.g. {@code "geo:stores"})
     * @param count    total number of members to insert
     * @param idOffset first member ID; members are named {@code idOffset},
     *                 {@code idOffset+1}, … {@code idOffset+count-1}
     * @param label    human-readable label used in log output
     */
    private void seedGeoKey(String redisKey, int count, long idOffset, String label) {
        int batches = (count + BATCH_SIZE - 1) / BATCH_SIZE;
        log.info("[{}] Seeding {} entries into '{}' — {} batches × {} (pipeline)",
                label, count, redisKey, batches, BATCH_SIZE);

        Random rng    = new Random(randomSeed);
        byte[] rawKey = redisKey.getBytes(StandardCharsets.UTF_8);
        long   tStart = System.currentTimeMillis();

        for (int batch = 0; batch < batches; batch++) {
            int  fromIdx = batch * BATCH_SIZE;
            int  toIdx   = Math.min(fromIdx + BATCH_SIZE, count);
            int  items   = toIdx - fromIdx;
            long baseId  = idOffset + fromIdx;

            // Pre-generate coordinates outside the pipeline callback
            double[] lons = new double[items];
            double[] lats = new double[items];
            for (int i = 0; i < items; i++) {
                lons[i] = BASE_LON + (rng.nextDouble() * 2 - 1) * LON_SPREAD;
                lats[i] = BASE_LAT + (rng.nextDouble() * 2 - 1) * LAT_SPREAD;
            }

            // Issue all GEOADD commands in a single pipeline flush
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (int i = 0; i < items; i++) {
                    byte[] member = String.valueOf(baseId + i)
                            .getBytes(StandardCharsets.UTF_8);
                    connection.geoCommands()
                            .geoAdd(rawKey, new Point(lons[i], lats[i]), member);
                }
                return null; // required by RedisCallback contract; result ignored
            });

            // Log progress every 10 batches (every 5 000 records)
            if ((batch + 1) % 10 == 0 || (batch + 1) == batches) {
                long elapsed = System.currentTimeMillis() - tStart;
                log.info("[{}] Progress: {}/{} batches — {} / {} entries inserted ({}ms elapsed)",
                        label, batch + 1, batches, toIdx, count, elapsed);
            }
        }

        long total = System.currentTimeMillis() - tStart;
        log.info("[{}] Done — {} entries seeded into '{}' in {} ms (~{} entries/sec).",
                label, count, redisKey, total,
                total > 0 ? (count * 1000L / total) : count);
    }

    /**
     * Returns the number of members in a Redis sorted-set (geo keys are backed
     * by sorted sets internally).
     */
    private long zsetSize(String key) {
        Long size = redisTemplate.opsForZSet().size(key);
        return size != null ? size : 0L;
    }
}
