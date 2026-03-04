import http from "k6/http";
import { sleep } from "k6";

export const options = {
  vus: 100, // 100 user ảo
  duration: "30s", // chạy 30 giây
};

export default function () {
  http.get(
    "http://localhost:8080/api/v1/stores/nearby?lat=10.7769&lon=106.7009&radius=3.0",
  );
  sleep(1);
}
