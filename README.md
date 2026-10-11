# ScentStation

**A Multi-Brand IoT Perfume Experience and Management Platform**  
*Nền tảng IoT đa thương hiệu cho máy trải nghiệm và quản lý nước hoa*

ScentStation là dự án xây dựng nền tảng cho phép khách hàng trải nghiệm nước hoa tại kiosk tự phục vụ, đồng thời cung cấp hệ thống quản lý tập trung cho nhiều thương hiệu, nhân viên vận hành và bộ phận kỹ thuật.

> **Trạng thái dự án:** Đang trong giai đoạn phân tích yêu cầu và thiết kế.  
> **Kế hoạch:** 7 Sprint, dự kiến mỗi Sprint 1 tuần.  
> **Nhóm thực hiện:** 7 thành viên.

---

## 1. Mục tiêu dự án

- Cho phép khách hàng chọn nước hoa tại kiosk, thanh toán bằng QR và yêu cầu máy phun một lần.
- Hỗ trợ nhiều thương hiệu trên cùng nền tảng, đồng thời cách ly dữ liệu giữa các thương hiệu.
- Cung cấp công cụ quản lý sản phẩm, máy/khe chứa nước hoa, đơn hàng, tồn kho, bảo trì và báo cáo.
- Theo dõi trạng thái kiosk và nhận biết các tình huống cần xử lý như mất kết nối, hết nước hoa hoặc lỗi thiết bị.
- Đảm bảo quy trình thanh toán và phun nước hoa an toàn, có thể kiểm tra và đối soát.

## 2. Phạm vi chức năng

### Trải nghiệm khách hàng tại kiosk
- Xem và chọn thương hiệu, sản phẩm nước hoa.
- Tạo đơn hàng và thanh toán QR.
- Theo dõi trạng thái yêu cầu phun.
- Hiển thị kết quả thành công hoặc thông báo khi cần hỗ trợ.

### Cổng quản trị và vận hành
- Đăng nhập, phân quyền theo vai trò.
- Quản lý thương hiệu, sản phẩm, máy và các khe chứa nước hoa.
- Quản lý đơn hàng, giao dịch thanh toán và lịch sử phun.
- Theo dõi tồn kho, tiếp nước hoa và lịch sử thay đổi tồn kho.
- Quản lý cảnh báo, bảo trì và sự cố thiết bị.
- Xem dashboard, báo cáo và nhật ký kiểm toán.

### IoT và giám sát thiết bị
- Giao tiếp giữa backend và kiosk/gateway thông qua MQTT.
- Theo dõi trạng thái kết nối, trạng thái máy và khe chứa.
- Gửi lệnh phun có xác thực và ghi nhận sự kiện phản hồi.
- Trong giai đoạn phát triển, có thể dùng simulator để mô phỏng thiết bị trước khi tích hợp phần cứng thật.

## 3. Vai trò người dùng

| Vai trò | Trách nhiệm chính |
|---|---|
| Platform Super Admin | Quản lý nền tảng và các thương hiệu |
| Brand Admin | Quản lý dữ liệu, sản phẩm và hoạt động thuộc thương hiệu được cấp quyền |
| Operations Manager | Theo dõi đơn hàng, máy và hoạt động vận hành |
| Technician | Kiểm tra sự cố, bảo trì và xử lý thiết bị |
| Inventory Staff | Quản lý tồn kho, tiếp nước hoa và ghi nhận thao tác kho |
| Customer / Kiosk User | Chọn nước hoa và thực hiện trải nghiệm tại kiosk |

Quyền cụ thể của từng vai trò phải tuân theo SRS và cơ chế phân quyền được triển khai. Người dùng của một thương hiệu không được truy cập dữ liệu của thương hiệu khác nếu không có quyền phù hợp.

## 4. Công nghệ dự kiến

Các công nghệ dưới đây là định hướng hiện tại của dự án. Nhóm cần thống nhất trước khi cố định phiên bản và bắt đầu phát triển các module phụ thuộc.

| Thành phần | Công nghệ dự kiến |
|---|---|
| Backend | Java 21 LTS, Spring Boot 3.5.x, Maven 3.9.x |
| Frontend | React, TypeScript, Vite, Node.js 24 LTS |
| Database | MySQL 8.4 LTS |
| Giao tiếp thiết bị | MQTT; dùng simulator trước, ESP32 nếu có phần cứng |
| API | REST/JSON, tiền tố `/api/v1` |
| Backend testing | JUnit 5, Mockito, Spring Boot Test |
| API testing | Postman |
| Frontend testing | Vitest, React Testing Library |
| IoT testing | MQTT Explorer và simulator |
| Quản lý mã nguồn, công việc | Git, GitHub Issues, GitHub Projects, GitHub Milestones |

**Lưu ý:** Phiên bản vá cụ thể và cấu hình môi trường sẽ được ghi trong các file cấu hình của từng module khi chúng được khởi tạo.

## 5. Quy tắc nghiệp vụ quan trọng

Các quy tắc sau là yêu cầu an toàn cốt lõi, cần được phản ánh trong thiết kế, code và kiểm thử:

1. Một đơn hàng đã thanh toán không được dẫn tới nhiều hơn **một lần phun thành công**.
2. Chỉ backend xác nhận thanh toán thành công theo luồng đã thiết kế mới được cho phép tiếp tục quy trình phun.
3. Không phun khi máy đang bảo trì, cửa mở, khe hết nước hoa hoặc máy có lỗi nghiêm trọng.
4. Lệnh phun phải được xác thực, chống xử lý trùng và hướng tới đúng máy/khe.
5. Nếu kết quả phun không xác định, phải ghi nhận để xử lý/đối soát; không tự động gửi lệnh phun lần hai.
6. Chỉ cập nhật tồn kho theo kết quả phun được xác nhận theo quy tắc hệ thống.
7. Không nhận đơn mới nếu máy không thể giao tiếp với backend theo điều kiện vận hành đã thống nhất.
8. Các thao tác quan trọng cần có lịch sử phù hợp để hỗ trợ kiểm tra và đối soát.

## 6. Kiến trúc mức cao

```text
Customer
   |
   v
Kiosk / Frontend  <---->  Backend REST API
                              |
              +---------------+----------------+
              |               |                |
              v               v                v
          MySQL DB       Payment Flow      Admin Portal
                              |
                              v
                       MQTT / IoT Gateway
                              |
                              v
                       Kiosk / Device Simulator
```

Sơ đồ trên thể hiện các thành phần ở mức khái quát. Luồng thanh toán, xác nhận kết quả phun, xử lý sự kiện MQTT và đồng bộ tồn kho phải được mô tả chi tiết hơn trong tài liệu kiến trúc và thiết kế API trước khi triển khai.

## 7. Cấu trúc repository

```text
ProjectJava/
├── README.md
├── CONTRIBUTING.md
├── .gitignore
├── .github/PULL_REQUEST_TEMPLATE.md
├── docs/
│   ├── project-brief-scentStation.md
│   ├── software-requirement-specification.md
│   ├── architecture/
│   ├── diagrams/
│   ├── database/
│   ├── api/
│   ├── test-reports/
│   └── meeting-notes/
├── backend/
│   └── README.md
├── frontend/
│   └── README.md
└── iot/
    ├── README.md
    ├── simulator/
    ├── protocol/
    └── firmware/
```

Một số thư mục/module có thể chưa chứa mã nguồn ở giai đoạn hiện tại. Chỉ bổ sung cấu trúc chi tiết khi có nhu cầu thực tế.

## 8. Tài liệu dự án

- [Quy chuẩn làm việc nhóm — CONTRIBUTING.md](CONTRIBUTING.md)
- [Project Brief](docs/project-brief-scentStation.md)
- [Software Requirements Specification (SRS)](docs/software-requirement-specification.md)

Tài liệu thiết kế kiến trúc, sơ đồ, database, API và báo cáo kiểm thử sẽ được bổ sung vào các thư mục tương ứng khi nhóm hoàn thiện.

## 9. Chuẩn bị môi trường phát triển

Cài đặt các công cụ cần thiết theo module bạn tham gia:

- Git và tài khoản GitHub có quyền truy cập repository.
- Java >= 21 và Maven 3.9.x cho backend.
- Node.js 24 LTS và npm cho frontend.
- MySQL 8.4 LTS cho cơ sở dữ liệu.
- MQTT broker và MQTT Explorer khi làm phần IoT.
- Postman để kiểm tra API.

Kiểm tra các công cụ cơ bản sau khi cài đặt:

```bash
git --version
java -version
mvn -version
node --version
npm --version
```

### Clone repository

```bash
git clone https://github.com/CamTupn/ProjectJava.git
cd ProjectJava
```

### Chạy dự án

Hiện repository đang trong giai đoạn chuẩn bị và hoàn thiện tài liệu; các module có thể chưa được khởi tạo đầy đủ. Khi backend, frontend và IoT simulator đã có cấu hình chạy thống nhất, hướng dẫn chạy chi tiết sẽ được bổ sung vào README của từng module và cập nhật tại đây.

Không tự tạo file `.env` với thông tin thật rồi commit lên GitHub. Có thể cung cấp `.env.example` chỉ chứa tên biến và giá trị mẫu không nhạy cảm.

## 10. Quy trình đóng góp

1. Đọc `CONTRIBUTING.md` và tài liệu SRS trước khi bắt đầu.
2. Thống nhất phạm vi và đầu ra của công việc với nhóm.
3. Tạo branch riêng từ `main`, không làm trực tiếp trên `main` đối với công việc thông thường.
4. Commit theo quy ước đã thống nhất và push branch lên GitHub.
5. Tạo Pull Request, liên kết Issue khi nhóm bắt đầu quản lý công việc bằng Issues, và mô tả cách kiểm tra.
6. Nhờ thành viên khác review; chỉ merge khi đạt tiêu chí nghiệm thu.
7. Cập nhật tài liệu và trạng thái công việc sau khi merge.

Xem chi tiết quy ước branch, commit, Pull Request, kiểm thử và Definition of Done tại [`CONTRIBUTING.md`](CONTRIBUTING.md).

## 11. Kiểm thử và tiêu chí hoàn thành

Mỗi chức năng cần có tiêu chí nghiệm thu và bằng chứng kiểm tra phù hợp. Không chỉ kiểm thử luồng thành công; cần chú ý các trường hợp lỗi, xử lý trùng, mất kết nối và kết quả không xác định.

Đặc biệt, luồng thanh toán – phun – tồn kho cần kiểm thử tối thiểu:
- Callback thanh toán bị gửi lặp.
- Lệnh phun bị gửi lặp hoặc gửi sai máy/khe.
- Máy mất kết nối hoặc không phản hồi.
- Máy ở trạng thái bảo trì, cửa mở, khe hết nước hoa hoặc có lỗi nghiêm trọng.
- Kết quả phun không xác định và yêu cầu đối soát.
- Tồn kho chỉ thay đổi khi có kết quả phun được xác nhận.

Một công việc chỉ hoàn thành khi đáp ứng tiêu chí nghiệm thu, được review/merge theo quy trình nhóm và có tài liệu cần thiết.

## 12. Trạng thái hiện tại và kế hoạch tiếp theo

- [x] Khởi tạo repository và README ban đầu.
- [x] Bổ sung Project Brief.
- [x] Bổ sung SRS.
- [x] Bổ sung quy chuẩn làm việc nhóm.
- [ ] Hoàn thiện và thống nhất tài liệu kiến trúc.
- [ ] Thiết kế ERD và schema MySQL.
- [ ] Thiết kế API và luồng thanh toán/phun.
- [ ] Khởi tạo bộ khung backend, frontend và IoT simulator.
- [ ] Phân công module, phát triển, tích hợp và kiểm thử theo 7 Sprint.

Các mục được đánh dấu hoàn thành phản ánh trạng thái tài liệu/repository hiện tại; không có nghĩa các chức năng sản phẩm đã được triển khai.

---

**Repository:** https://github.com/CamTupn/ProjectJava  
**Tên dự án:** ScentStation  
**Quy chuẩn đóng góp:** [`CONTRIBUTING.md`](CONTRIBUTING.md)
