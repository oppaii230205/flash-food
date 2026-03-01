package com.flashfood.flash_food.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message published to the flash-sale exchange when a food item becomes active.
 *
 * <p>The consumer uses {@code storeLongitude}/{@code storeLatitude} together
 * with {@code radiusKm} to query Redis Geo for nearby user IDs, then persists
 * a {@link com.flashfood.flash_food.entity.Notification} row for each one.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlashSaleMessage {

    private Long   foodItemId;
    private Long   storeId;
    private String storeName;
    private String itemName;
    private String itemImageUrl;

    /** Original (full) price in the smallest currency unit (e.g. VND). */
    private Integer originalPrice;
    /** Discounted flash price. */
    private Integer flashPrice;
    /** Pre-computed discount percentage (0–100). */
    private Integer discountPercentage;

    /** Store's longitude — used for Redis Geo radius search. */
    private Double storeLongitude;
    /** Store's latitude — used for Redis Geo radius search. */
    private Double storeLatitude;
    /** Radius in kilometres within which users should be notified. */
    private Double radiusKm;
}
