// TODO: This class is currently disabled because the scheduled jobs are not yet needed in the MVP. It can be re-enabled once we have a critical mass of food items and need to automate status transitions.
// package com.flashfood.flash_food.scheduler;

// import com.flashfood.flash_food.entity.FoodItemStatus;
// import com.flashfood.flash_food.repository.FoodItemRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDateTime;

// /**
//  * Scheduled tasks that keep food-item statuses in sync with wall-clock time.
//  *
//  * Two jobs run periodically:
//  *  1. {@link #activatePendingItems()} — promote PENDING items whose sale window has opened.
//  *  2. {@link #expireStaleItems()} — expire AVAILABLE/PENDING items whose sale window has closed.
//  *
//  * Both jobs use bulk UPDATE queries instead of loading individual entities so
//  * that status transitions are applied in a single round-trip to the database,
//  * regardless of how many items are affected.
//  *
//  * Requires {@code @EnableScheduling} on a {@code @Configuration} class (already set
//  * in {@link com.flashfood.flash_food.config.AsyncConfig}).
//  */
// @Slf4j
// @Component
// @RequiredArgsConstructor
// public class StatusUpdateScheduler {

//     private final FoodItemRepository foodItemRepository;

//     // -------------------------------------------------------------------------
//     // PENDING → AVAILABLE  (every 2 minutes)
//     // -------------------------------------------------------------------------

//     /**
//      * Promotes PENDING items to AVAILABLE once their {@code saleStartTime} has
//      * elapsed and they still have stock. Runs every 2 minutes so that flash-sale
//      * items become visible to buyers within a short window after their declared
//      * start time.
//      */
//     @Scheduled(cron = "0 */2 * * * *")
//     @Transactional
//     public void activatePendingItems() {
//         LocalDateTime now = LocalDateTime.now();
//         int count = foodItemRepository.bulkActivatePendingItems(
//                 FoodItemStatus.AVAILABLE,
//                 FoodItemStatus.PENDING,
//                 now);

//         if (count > 0) {
//             log.info("[Scheduler] Activated {} PENDING food item(s) → AVAILABLE", count);
//         } else {
//             log.debug("[Scheduler] activatePendingItems: no items to activate at {}", now);
//         }
//     }

//     // -------------------------------------------------------------------------
//     // AVAILABLE / PENDING → EXPIRED  (every 5 minutes)
//     // -------------------------------------------------------------------------

//     /**
//      * Expires AVAILABLE and PENDING items that have passed their {@code saleEndTime}.
//      * Only these two statuses are transitioned; SOLD_OUT, CANCELLED, and DELETED
//      * items are intentionally left unchanged.
//      */
//     @Scheduled(cron = "0 */5 * * * *")
//     @Transactional
//     public void expireStaleItems() {
//         LocalDateTime now = LocalDateTime.now();
//         int count = foodItemRepository.bulkExpireItems(
//                 FoodItemStatus.EXPIRED,
//                 now,
//                 FoodItemStatus.AVAILABLE,
//                 FoodItemStatus.PENDING);

//         if (count > 0) {
//             log.info("[Scheduler] Expired {} food item(s) past their sale end time", count);
//         } else {
//             log.debug("[Scheduler] expireStaleItems: no items to expire at {}", now);
//         }
//     }
// }
