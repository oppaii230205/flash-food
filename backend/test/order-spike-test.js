import http from "k6/http";
import { check, sleep } from "k6";

// Định nghĩa cấu hình các giai đoạn test
export const options = {
  stages: [
    { duration: "10s", target: 50 }, // Ramping up: Tăng dần lên 50 user trong 10 giây
    { duration: "1m", target: 50 }, // Bình ổn: Giữ nguyên 50 user trong 1 phút để làm nóng hệ thống
    { duration: "10s", target: 500 }, // Spike: Đột ngột tăng vọt lên 500 user (giả lập khoảnh khắc mở bán)
    { duration: "30s", target: 500 }, // Giữ tải cao trong 30 giây để xem database có bị treo không
    { duration: "10s", target: 0 }, // Ramping down: Giảm dần về 0
  ],
};

// 1. Need to set mockToken (15min)
// 2. Need to change storeId and foodItemId in payload to match your seeded data (huge quantity)
export default function () {
  const port = 80;
  const url = `http://localhost:${port}/api/v1/orders`; // Đổi thành URL API nội bộ của bạn
  const mockToken =
    "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyNUBleGFtcGxlLmNvbSIsInJvbGVzIjpbIlJPTEVfQ1VTVE9NRVIiXSwiaWF0IjoxNzc5MTExNzYxLCJleHAiOjE3NzkxMTI2NjF9.LPLPr3YNx_g8-3qhYb-auZL-O5YBvXuPwzbZWdCaVLmY2F_8kvX13uy8v7o6gaEFjjYFCxMR1hTKX0xVUE-pHw";
  const payload = JSON.stringify({
    storeId: 1,
    items: [
      {
        foodItemId: 49925,
        quantity: 1,
      }, //,
      // {
      //   "foodItemId": 2,
      //   "quantity": 1
      // }
    ],
    paymentMethod: "cash",
    pickupTime: "2026-03-01T19:30:00",
    specialInstructions: "Không hành, ít ớt",
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      // Có thể tạo một hàm sinh mock token đơn giản để bypass logic auth phức tạp khi test
      Authorization: `Bearer ${mockToken}`,
    },
  };

  const res = http.post(url, payload, params);

  // Kiểm tra xem request có thành công không (HTTP status 201) và tốc độ có đảm bảo không
  check(res, {
    "is status 201": (r) => r.status === 201,
    "transaction time < 500ms": (r) => r.timings.duration < 500,
  });

  // Nghỉ 1 giây để mô phỏng thời gian chờ thực tế của con người giữa các thao tác
  sleep(1);
}
