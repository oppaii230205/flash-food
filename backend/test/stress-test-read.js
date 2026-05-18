import http from "k6/http";
import { check, sleep } from "k6";

// Cấu hình Stress Test: Tăng tải theo từng bậc để tìm điểm gãy
export const options = {
  stages: [
    { duration: "1m", target: 500 }, // Bậc 1: Lên 500 users (mức an toàn từ bài test trước)
    { duration: "1m", target: 500 }, // Giữ 500 users để ổn định
    { duration: "1m", target: 1500 }, // Bậc 2: Ép xung lên 1500 users (Bắt đầu nghẽn)
    { duration: "1m", target: 1500 }, // Giữ tải 1500 users
    { duration: "1m", target: 2500 }, // Bậc 3: Đẩy lên 2500 users (Điểm gãy thường xuất hiện ở đây)
    { duration: "1m", target: 2500 }, // Cố gắng duy trì mức tối đa
    { duration: "1m", target: 0 }, // Xả tải
  ],
  // Trong Stress Test, chúng ta kỳ vọng test sẽ fail,
  // nên không cần đặt thresholds quá khắt khe để ngắt script giữa chừng.
};

export default function () {
  const lat = 10.7769;
  const lon = 106.7009;
  const port = 80;

  // 1. API Tìm cửa hàng lân cận
  const storesRes = http.get(
    `http://localhost:${port}/api/v1/stores/nearby?lat=${lat}&lon=${lon}&radius=100`,
    { tags: { name: "GET_nearby_stores" } }, // Đánh tag để dễ đọc report
  );

  check(storesRes, {
    "GET nearby stores status is 200": (r) => r.status === 200,
  });

  // Nghỉ 1-2 giây
  sleep(Math.random() * 1 + 1);

  // 2. Lấy Menu cửa hàng
  const storesBody = storesRes.json();
  const stores = storesBody && storesBody.data ? storesBody.data : [];

  if (stores.length > 0) {
    const randomStore = stores[Math.floor(Math.random() * stores.length)];
    const menuRes = http.get(
      `http://localhost:${port}/api/v1/food-items/store/${randomStore.id}?page=0&size=20`,
      { tags: { name: "GET_store_menu" } },
    );

    check(menuRes, {
      "GET store menu status is 200": (r) => r.status === 200,
    });
  }

  // Nghỉ 1-3 giây
  sleep(Math.random() * 2 + 1);
}
