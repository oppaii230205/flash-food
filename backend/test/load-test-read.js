import http from "k6/http";
import { check, sleep } from "k6";

// Thiết lập cấu hình Load Test: Duy trì tải ổn định trong thời gian dài
export const options = {
  stages: [
    { duration: "30s", target: 200 }, // Ramping up: Tăng dần lên 200 users trong 30 giây
    { duration: "3m", target: 200 }, // Steady state: Duy trì liên tục 200 users trong 3 phút
    { duration: "30s", target: 0 }, // Ramping down: Giảm dần về 0
  ],
  thresholds: {
    // Đặt ngưỡng thất bại cho test: 95% request phải phản hồi dưới 300ms
    http_req_duration: ["p(95)<300"],
    http_req_failed: ["rate<0.01"], // Tỉ lệ lỗi phải dưới 1%
  },
};

export default function () {
  // 1. Lấy danh sách cửa hàng lân cận (Giả lập tọa độ tại trung tâm TP.HCM)
  const lat = 10.7769;
  const lon = 106.7009;
  const storesRes = http.get(
    `http://localhost:8080/api/v1/stores/nearby?lat=${lat}&lon=${lon}&radius=100`,
  );

  check(storesRes, {
    "GET nearby stores status is 200": (r) => r.status === 200,
    "GET nearby stores < 300ms": (r) => r.timings.duration < 300,
  });

  // Mô phỏng thời gian người dùng lướt xem danh sách cửa hàng (think time)
  sleep(Math.random() * 2 + 1); // Nghỉ ngẫu nhiên từ 1 đến 3 giây

  // 2. Chọn một cửa hàng thực tế từ response để xem thực đơn
  const storesBody = storesRes.json();
  const stores = storesBody && storesBody.data ? storesBody.data : [];

  if (stores.length === 0) {
    sleep(1);
    return;
  }

  const store = stores[Math.floor(Math.random() * stores.length)];
  const menuRes = http.get(
    `http://localhost:8080/api/v1/food-items/store/${store.id}?page=0&size=20`,
  );

  check(menuRes, {
    "GET store menu status is 200": (r) => r.status === 200,
    "GET store menu < 200ms": (r) => r.timings.duration < 200,
  });

  // Mô phỏng thời gian người dùng suy nghĩ chọn món
  sleep(Math.random() * 3 + 2); // Nghỉ ngẫu nhiên từ 2 đến 5 giây
}
