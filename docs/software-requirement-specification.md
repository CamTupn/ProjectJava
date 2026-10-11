# TÀI LIỆU ĐẶC TẢ YÊU CẦU PHẦN MỀM (SOFTWARE REQUIREMENTS SPECIFICATION — SRS)

## DỰ ÁN: SCENTSTATION – NỀN TẢNG IoT ĐA THƯƠNG HIỆU CHO MÁY TRẢI NGHIỆM VÀ QUẢN LÝ NƯỚC HOA

| Thông tin | Chi tiết |
|---|---|
| **Tên dự án (EN)** | ScentStation: A Multi-Brand IoT Perfume Experience and Management Platform |
| **Tên dự án (VN)** | ScentStation: Nền tảng IoT đa thương hiệu cho máy trải nghiệm và quản lý nước hoa |
| **Viết tắt** | ScentStation |
| **Loại tài liệu** | Software Requirements Specification (SRS) |
| **Phiên bản tài liệu** | 1.1 |
| **Trạng thái** | Bản nháp để nhóm rà soát (Draft for Review) |
| **Thời gian dự kiến** | 7 tuần |
| **Nguồn yêu cầu chính** | Project Brief ScentStation do giảng viên cung cấp |
| **Tài liệu liên quan** | `ScentStation_Project_Analysis_Document.docx` |
| **Tham chiếu cấu trúc** | Cấu trúc SRS theo phong cách IEEE 830; nhóm cần xác nhận nếu giảng viên yêu cầu tuân thủ chuẩn cụ thể |

> **Lưu ý:** Nội dung chức năng và quy tắc nghiệp vụ được diễn giải từ Project Brief ScentStation. Các lựa chọn như React/Next.js, PostgreSQL/MySQL, MQTT, ESP32, cách chia sprint, API và test case là đề xuất triển khai của nhóm nếu Project Brief không quy định rõ. Những nội dung chưa được xác nhận phải được nhóm đánh dấu là đề xuất, không coi là yêu cầu nguyên văn của giảng viên.

---

## MỤC LỤC

1. [Giới thiệu](#1-giới-thiệu)
2. [Mô tả tổng quan hệ thống](#2-mô-tả-tổng-quan-hệ-thống)
3. [Yêu cầu giao diện bên ngoài](#3-yêu-cầu-giao-diện-bên-ngoài)
4. [Đặc tả yêu cầu chức năng](#4-đặc-tả-yêu-cầu-chức-năng)
5. [Yêu cầu phi chức năng](#5-yêu-cầu-phi-chức-năng)
6. [Yêu cầu cơ sở dữ liệu](#6-yêu-cầu-cơ-sở-dữ-liệu)
7. [Ràng buộc thiết kế](#7-ràng-buộc-thiết-kế)
8. [Ma trận truy xuất yêu cầu và mã nhiệm vụ](#8-ma-trận-truy-xuất-yêu-cầu-và-mã-nhiệm-vụ)
9. [Phụ lục](#9-phụ-lục)

---

# 1. Giới thiệu

## 1.1. Mục đích tài liệu

Tài liệu SRS mô tả các yêu cầu chức năng, yêu cầu phi chức năng, giao diện, dữ liệu và ràng buộc nghiệp vụ của ScentStation. Tài liệu là căn cứ chung để nhóm:
- Phân tích và thống nhất phạm vi hệ thống.
- Thiết kế Use Case, kiến trúc, cơ sở dữ liệu và API.
- Phát triển Web App, backend và phần giao tiếp IoT.
- Xây dựng test plan, test case và kiểm thử tích hợp.
- Theo dõi công việc trên GitHub Issues/Projects.
- Đánh giá mức độ hoàn thành và chuẩn bị demo/nghiệm thu.

## 1.2. Phạm vi hệ thống

ScentStation là nền tảng trải nghiệm nước hoa kết nối IoT, hỗ trợ nhiều thương hiệu nước hoa trên cùng nền tảng. Khách hàng tương tác với kiosk để xem và chọn nước hoa, thanh toán bằng QR theo luồng được hỗ trợ, sau đó hệ thống điều phối thiết bị thực hiện tối đa một lần xịt thành công cho đơn hàng đã thanh toán.

Nhân sự vận hành sử dụng Web App quản trị để quản lý thương hiệu, sản phẩm, máy, slot, tồn kho, cảnh báo, bảo trì, giao dịch và báo cáo.

### 1.2.1. Phạm vi chức năng

- Xác thực người dùng, quản lý vai trò và quyền truy cập (RBAC).
- Phân tách dữ liệu giữa các thương hiệu (tenant isolation).
- Quản lý thương hiệu, sản phẩm nước hoa, máy và slot.
- Tạo đơn hàng, xác minh thanh toán và theo dõi trạng thái đơn.
- Gửi lệnh dispensing đến đúng thiết bị/slot và tiếp nhận kết quả.
- Theo dõi kết nối thiết bị, heartbeat, telemetry và sự kiện.
- Quản lý cartridge/chai, tồn kho, refill và test spray.
- Cảnh báo vận hành và phiếu bảo trì.
- Dashboard, báo cáo và audit log.
- Cung cấp hướng dẫn chạy, kiểm thử và tài liệu kỹ thuật.

### 1.2.2. Ngoài phạm vi hoặc cần xác nhận

Các chức năng như thanh toán qua nhà cung cấp thật, gửi thông báo qua email/SMS, phân tích nâng cao, quản lý firmware từ xa và triển khai cloud production phụ thuộc nguồn lực và yêu cầu cuối cùng của giảng viên. Nếu chưa triển khai thật, nhóm phải ghi rõ phần mô phỏng trong báo cáo và demo.

## 1.3. Đối tượng đọc tài liệu

| Đối tượng | Mục đích |
|---|---|
| Developers | Hiểu yêu cầu để thiết kế và lập trình |
| Testers | Xây dựng test plan và test case |
| Project Manager/Team Leader | Theo dõi phạm vi, tiến độ và phụ thuộc |
| Thành viên phụ trách IoT | Hiểu luồng command, telemetry, safety và kết quả dispensing |
| Giảng viên/Stakeholders | Rà soát, xác nhận và đánh giá yêu cầu |

## 1.4. Định nghĩa, từ viết tắt và thuật ngữ

| Thuật ngữ | Định nghĩa |
|---|---|
| **Kiosk** | Giao diện/thiết bị nơi khách xem, chọn và trải nghiệm nước hoa |
| **Tenant / Brand** | Không gian dữ liệu thuộc một thương hiệu nước hoa |
| **Machine** | Máy trải nghiệm nước hoa được định danh trong hệ thống |
| **Slot** | Khe chứa/cấp một loại nước hoa trên máy |
| **Cartridge** | Chai hoặc hộp chứa nước hoa được gán vào slot |
| **Dispensing** | Quá trình máy thực hiện một lần xịt |
| **Telemetry** | Dữ liệu trạng thái hoặc đo lường được thiết bị gửi về |
| **Heartbeat** | Thông điệp định kỳ cho biết thiết bị còn liên lạc |
| **Command** | Lệnh backend gửi đến thiết bị, ví dụ yêu cầu dispensing |
| **Acknowledgement (ACK)** | Phản hồi xác nhận thiết bị đã nhận lệnh |
| **RBAC** | Role-Based Access Control – phân quyền dựa trên vai trò |
| **Idempotency** | Xử lý thông điệp/yêu cầu lặp mà không tạo tác động nghiệp vụ lặp ngoài ý muốn |
| **Audit log** | Nhật ký thao tác quan trọng để kiểm tra và truy vết |
| **Diagnostic/Test spray** | Lần xịt kiểm tra phục vụ bảo trì, không phải giao dịch khách hàng |
| **MVP** | Minimum Viable Product – phiên bản tối thiểu đủ thể hiện luồng giá trị cốt lõi |
| **SRS** | Software Requirements Specification – tài liệu đặc tả yêu cầu phần mềm |

## 1.5. Tài liệu tham chiếu

| STT | Tài liệu | Mục đích |
|---|---|---|
| 1 | Project Brief ScentStation do giảng viên cung cấp | Nguồn yêu cầu gốc |
| 2 | `ScentStation_Project_Analysis_Document.docx` | Tài liệu phân tích dự án và kế hoạch 7 tuần của nhóm |
| 3 | `software-requirement-specifications.md` (tài liệu BICAP mẫu do người dùng cung cấp) | Tham khảo cách tổ chức và mức độ chi tiết trình bày SRS; không phải nguồn yêu cầu nghiệp vụ ScentStation |
| 4 | GitHub Issues/Projects của nhóm | Theo dõi nhiệm vụ và tiến độ triển khai |

---

# 2. Mô tả tổng quan hệ thống

## 2.1. Bối cảnh và tầm nhìn

Trải nghiệm thử nước hoa tại điểm bán thường cần nhân viên hỗ trợ và khó theo dõi tập trung nếu có nhiều thương hiệu hoặc nhiều máy. ScentStation hướng tới việc cung cấp một hệ thống kiosk tự phục vụ, đồng thời giúp đơn vị vận hành quản lý máy, sản phẩm, tồn kho và sự cố từ một nền tảng thống nhất.

### Vấn đề cần giải quyết
- Khách cần một giao diện rõ ràng để chọn mùi và hoàn thành trải nghiệm.
- Đơn hàng, thanh toán và thao tác xịt phải được liên kết chính xác.
- Máy có thể mất kết nối, hết nước hoa hoặc gặp trạng thái không an toàn.
- Đơn vị vận hành cần theo dõi tình trạng máy, tồn kho, refill và bảo trì.
- Nền tảng đa thương hiệu cần ngăn người dùng xem hoặc sửa dữ liệu ngoài phạm vi của mình.

### Giải pháp
ScentStation kết hợp Web App/Kiosk UI, backend, cơ sở dữ liệu và giao tiếp IoT. Backend xác minh thanh toán, quản lý trạng thái đơn, điều phối lệnh dispensing và lưu lịch sử. Thiết bị gửi trạng thái/telemetry và phản hồi kết quả. Cổng quản trị hỗ trợ hoạt động đa thương hiệu theo quyền được cấp.

## 2.2. Các giải pháp cốt lõi

| Mã | Giải pháp | Mô tả |
|---|---|---|
| GP-01 | Trải nghiệm kiosk | Khách xem danh mục, chọn nước hoa, thanh toán và nhận kết quả |
| GP-02 | Quản lý đa thương hiệu | Quản lý brand, sản phẩm, máy và dữ liệu theo tenant |
| GP-03 | Dispensing có kiểm soát | Chỉ phát lệnh sau khi xác nhận thanh toán và kiểm tra điều kiện an toàn |
| GP-04 | Giám sát IoT | Theo dõi heartbeat, telemetry, trạng thái máy và lỗi |
| GP-05 | Quản lý tồn kho/refill | Ghi nhận cartridge, refill, test spray và ước tính tồn kho |
| GP-06 | Vận hành và bảo trì | Quản lý cảnh báo, phiếu bảo trì và lịch sử khắc phục |
| GP-07 | Truy vết và báo cáo | Dashboard, báo cáo và audit log cho các thao tác quan trọng |

## 2.3. Kiến trúc tổng quan đề xuất

```text
┌───────────────────────────────────────────────────────────┐
│                      SCENTSTATION                         │
│                                                           │
│  ┌───────────────────┐       ┌────────────────────────┐  │
│  │ Kiosk / Customer  │       │ Admin / Operations Web │  │
│  │ Web App           │       │ Portal                 │  │
│  └─────────┬─────────┘       └────────────┬───────────┘  │
│            └────────────────┬─────────────┘              │
│                             ▼                            │
│                  ┌─────────────────────┐                 │
│                  │ Backend API         │                 │
│                  │ Java Spring Boot    │                 │
│                  └──────┬────────┬─────┘                 │
│                         │        │                       │
│                 ┌───────▼───┐ ┌──▼──────────────────┐    │
│                 │ Relational│ │ IoT Communication   │    │
│                 │ Database  │ │ MQTT / Gateway*     │    │
│                 └───────────┘ └─────────┬───────────┘    │
│                                         ▼                │
│                              ┌──────────────────────┐    │
│                              │ Device / Simulator   │    │
│                              │ ≥ 4 controlled slots │    │
│                              └──────────────────────┘    │
│                                                           │
│  Payment Provider / Payment Simulator*                    │
└───────────────────────────────────────────────────────────┘
* Thành phần/công nghệ cần được nhóm chốt theo điều kiện triển khai.
```

### Mô tả thành phần
- **Kiosk/Customer UI:** hiển thị sản phẩm và trạng thái trải nghiệm.
- **Admin Portal:** giao diện quản trị theo vai trò.
- **Backend API:** xử lý nghiệp vụ, phân quyền, thanh toán, đơn hàng, dispensing, tồn kho và audit.
- **Database:** lưu dữ liệu có cấu trúc và lịch sử giao dịch.
- **IoT communication:** trao đổi lệnh, ACK, kết quả, heartbeat và telemetry.
- **Device/Simulator:** thiết bị thật hoặc chương trình mô phỏng phục vụ phát triển và kiểm thử.
- **Payment Provider/Simulator:** nguồn xác nhận thanh toán. Mọi mô phỏng phải được nhận diện rõ trong demo/tài liệu.

## 2.4. Vai trò người dùng (Actors)

| Vai trò | Mã | Nền tảng | Mô tả |
|---|---|---|---|
| Platform Super Admin | PSA | Admin Portal | Quản trị toàn nền tảng, thương hiệu và cấu hình chung |
| Brand Admin | BA | Admin Portal | Quản lý dữ liệu và hoạt động thuộc thương hiệu được giao |
| Operations Manager | OM | Admin Portal | Theo dõi máy, giao dịch, cảnh báo và hoạt động vận hành |
| Technician | TECH | Admin Portal | Kiểm tra, chẩn đoán, bảo trì và thực hiện thao tác kiểm tra được cấp quyền |
| Inventory Staff | INV | Admin Portal | Quản lý cartridge, slot, refill và điều chỉnh tồn kho |
| Customer/Kiosk User | CUST | Kiosk/Web App | Xem nước hoa, chọn sản phẩm, thanh toán và trải nghiệm |
| Device/Gateway | DEV | IoT channel | Gửi telemetry/event, nhận command và trả ACK/kết quả |
| Payment Provider | PAY | External integration | Gửi thông tin giao dịch để backend xác minh |

## 2.5. Luồng nghiệp vụ chính

### 2.5.1. Luồng trải nghiệm nước hoa
1. Khách mở kiosk và xem danh sách nước hoa sẵn có.
2. Khách chọn sản phẩm/slot.
3. Backend kiểm tra máy, slot và điều kiện vận hành.
4. Hệ thống tạo đơn và thông tin thanh toán.
5. Khách thực hiện thanh toán QR.
6. Backend xác minh thông báo thanh toán đáng tin cậy.
7. Backend tạo command dispensing duy nhất cho đúng máy/slot.
8. Thiết bị kiểm tra lệnh và các điều kiện an toàn.
9. Thiết bị thực hiện hoặc từ chối lệnh, sau đó trả kết quả.
10. Backend cập nhật trạng thái đơn, tồn kho và audit log.
11. Kiosk hiển thị kết quả cho khách.

### 2.5.2. Luồng xử lý kết quả không rõ ràng
1. Backend gửi command và chờ kết quả.
2. Nếu thiết bị mất kết nối hoặc không thể xác nhận kết quả, đơn được chuyển sang trạng thái cần xem xét.
3. Hệ thống không tự động gửi một command xịt mới cho cùng đơn.
4. Người có quyền kiểm tra log/thiết bị và quyết định hướng xử lý theo chính sách được thống nhất.

### 2.5.3. Luồng refill
1. Nhân viên tồn kho chọn máy/slot cần refill.
2. Hệ thống ghi nhận cartridge hiện tại và thao tác thay/refill.
3. Nhân viên nhập lượng nạp hoặc thông tin cartridge theo thiết kế.
4. Hệ thống cập nhật ước tính tồn kho, thời điểm và người thực hiện.
5. Hệ thống ghi audit log và kiểm tra ngưỡng cảnh báo.

## 2.6. Giả định và phụ thuộc

### Giả định
- Nhóm phát triển Web App và backend trước hoặc song song với phần cứng.
- Nếu thiết bị thật chưa sẵn sàng, simulator có thể được dùng để phát triển sớm.
- Nhóm sẽ thống nhất công nghệ database, frontend và giao tiếp IoT trước khi triển khai.
- Dữ liệu thử nghiệm và tài khoản demo được chuẩn bị riêng, không dùng thông tin bí mật thật trong repository.

### Phụ thuộc
- Prototype phần cứng hoặc simulator có thể nhận command và trả kết quả.
- Nếu dùng MQTT, cần broker và thông tin cấu hình phù hợp.
- Nếu dùng payment provider thật, cần tài khoản, tài liệu tích hợp và cơ chế xác minh callback/webhook.
- Yêu cầu tối thiểu về prototype bốn slot phải được đối chiếu khi nghiệm thu; simulator không tự động thay thế phần cứng nếu đề bài yêu cầu thiết bị thật.

---

# 3. Yêu cầu giao diện bên ngoài

## 3.1. Giao diện người dùng (User Interface)

### 3.1.1. Kiosk / Customer Web App

| Thuộc tính | Đặc tả đề xuất |
|---|---|
| Mục đích | Hỗ trợ khách xem, chọn và trải nghiệm nước hoa |
| Nội dung | Tên/hình ảnh/mô tả sản phẩm, giá, trạng thái sẵn có |
| Luồng thao tác | Chọn sản phẩm → thanh toán → chờ kết quả → hiển thị kết quả |
| Trạng thái | Sẵn sàng, đang tạo đơn, chờ thanh toán, đang dispensing, thành công, thất bại/cần hỗ trợ |
| Phản hồi lỗi | Hiển thị thông báo ngắn gọn, không để khách hiểu nhầm đã xịt thành công |
| Bảo mật | Không cho client tự đánh dấu thanh toán thành công hoặc tự gửi lệnh trực tiếp đến phần cứng |

### 3.1.2. Admin / Operations Portal

| Thuộc tính | Đặc tả đề xuất |
|---|---|
| Mục đích | Quản lý hệ thống theo vai trò |
| Chức năng | Brand/product, machine/slot, order, inventory, alerts, maintenance, dashboard, audit |
| Điều hướng | Menu theo nhóm chức năng và quyền người dùng |
| Bộ lọc | Theo thương hiệu, máy, slot, trạng thái và thời gian khi phù hợp |
| Phản hồi | Thông báo rõ thao tác thành công/thất bại |
| Tenant isolation | Chỉ hiển thị dữ liệu thuộc phạm vi được cấp |

### 3.1.3. Quy tắc giao diện chung
- Các trường bắt buộc phải được đánh dấu và kiểm tra.
- Không chỉ dựa vào ẩn nút trên giao diện; backend phải xác minh quyền.
- Trạng thái thanh toán và dispensing phải được trình bày riêng biệt.
- Các trạng thái `PENDING`, `SUCCESS`, `FAILED`, `EXPIRED`, `REVIEW_REQUIRED` cần được giải thích nhất quán.

## 3.2. Giao diện phần cứng (Hardware Interface)

| Thành phần | Giao diện/chức năng | Mục đích |
|---|---|---|
| Bộ điều khiển | Theo thiết kế prototype, ví dụ ESP32 | Nhận lệnh và điều khiển slot |
| Cơ cấu xịt | Driver/bơm/cơ cấu chấp hành theo thiết kế | Thực hiện một lần xịt có kiểm soát |
| Cảm biến cửa | GPIO hoặc giao diện phù hợp | Phát hiện cửa mở nếu phần cứng hỗ trợ |
| Cảm biến slot/tồn kho | Theo phần cứng thực tế | Hỗ trợ xác định slot trống hoặc ước tính tồn kho |
| Kết nối mạng | Wi-Fi/Ethernet theo thiết kế | Giao tiếp với backend/gateway |
| Device simulator | Giao diện phần mềm | Mô phỏng slot, trạng thái an toàn, lỗi, ACK và kết quả |

> Tên linh kiện và giao diện phần cứng cụ thể cần được chốt sau khi nhóm xác định prototype thực tế.

## 3.3. Giao diện phần mềm (Software Interface)

| Hệ thống/thành phần | Giao thức đề xuất | Mục đích |
|---|---|---|
| Kiosk/Admin → Backend | HTTPS REST API, JSON | Đọc/ghi nghiệp vụ theo quyền |
| Backend → Database | JDBC/ORM phù hợp | Lưu dữ liệu nghiệp vụ |
| Backend ↔ Device/Gateway | MQTT hoặc giao thức được nhóm chọn | Command, ACK, telemetry, event và result |
| Backend ↔ Payment Provider | HTTPS callback/webhook/API | Xác minh trạng thái thanh toán |
| Backend ↔ Payment Simulator | API nội bộ trong môi trường demo | Mô phỏng trạng thái thanh toán có kiểm soát |

## 3.4. Giao diện truyền thông (Communication Interface)

- API có xác thực và kiểm tra quyền với các endpoint được bảo vệ.
- Dữ liệu truyền giữa các thành phần phải dùng giao thức phù hợp và không để lộ secret.
- Command dispensing cần có mã định danh, máy đích, slot đích và thời hạn.
- Device event cần liên kết được với máy và command liên quan.
- Backend phải kiểm tra tính hợp lệ của callback thanh toán; không tin trạng thái do trình duyệt/kiosk tự khai báo.
- Nếu thiết bị offline, hệ thống phải ngăn đơn mới cho máy đó theo quy tắc nghiệp vụ.

---

# 4. Đặc tả yêu cầu chức năng

## 4.1. Quy ước đặc tả

Mỗi yêu cầu chức năng có mã `SRS-<MODULE>-<NNN>`. Các mã `TASK-xxx` trong Mục 8 liên kết nhóm yêu cầu với nhiệm vụ GitHub. Mức ưu tiên đề xuất:
- **High:** cốt lõi hoặc bắt buộc để đảm bảo an toàn/tính đúng đắn.
- **Medium:** cần cho vận hành MVP nhưng có thể triển khai sau luồng cốt lõi.
- **Low:** phần mở rộng, tùy thời gian.

## 4.2. Module Authentication, Users and RBAC

### SRS-AUTH-001: Đăng nhập người dùng quản trị

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-AUTH-001 |
| **Tham chiếu TASK** | TASK-008 |
| **Tên** | Đăng nhập người dùng quản trị |
| **Mô tả** | Hệ thống cho phép người dùng được cấp tài khoản đăng nhập vào Admin Portal |
| **Độ ưu tiên** | High |
| **Actor** | Platform Super Admin, Brand Admin, Operations Manager, Technician, Inventory Staff |

**Đầu vào (Input):**
- Email/tên đăng nhập: bắt buộc, đúng định dạng theo thiết kế.
- Mật khẩu: bắt buộc.

**Đầu ra (Output):**
- Thành công: phiên đăng nhập/token theo phương án xác thực nhóm chọn và thông tin vai trò.
- Thất bại: thông báo chung, không tiết lộ tài khoản có tồn tại hay không nếu chính sách bảo mật yêu cầu.

**Luồng chính (Main Flow):**
1. Người dùng mở trang đăng nhập.
2. Nhập thông tin đăng nhập.
3. Backend xác minh thông tin.
4. Backend kiểm tra trạng thái tài khoản.
5. Hệ thống tạo phiên đăng nhập/token.
6. Giao diện điều hướng đến màn hình phù hợp với vai trò.

**Luồng ngoại lệ (Exception Flow):**
- E1: Sai thông tin → từ chối đăng nhập.
- E2: Tài khoản bị vô hiệu hóa/khóa → từ chối và hiển thị hướng dẫn phù hợp.
- E3: Backend không khả dụng → thông báo lỗi hệ thống, không giả lập đăng nhập thành công.

**Quy tắc nghiệp vụ:**
- Chỉ tài khoản hợp lệ mới được đăng nhập.
- Quyền phải được kiểm tra ở backend cho từng thao tác được bảo vệ.

### SRS-AUTH-002: Phân quyền theo vai trò và phạm vi thương hiệu

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-AUTH-002 |
| **Tham chiếu TASK** | TASK-008, TASK-021 |
| **Tên** | Kiểm tra RBAC và tenant isolation |
| **Mô tả** | Hệ thống giới hạn chức năng và dữ liệu mà người dùng có thể truy cập theo vai trò và thương hiệu được giao |
| **Độ ưu tiên** | High |
| **Actor** | Tất cả người dùng quản trị |

**Đầu vào:** danh tính người dùng, vai trò, thương hiệu/phạm vi được gán và yêu cầu truy cập.

**Đầu ra:** cho phép thao tác nếu hợp lệ; nếu không, trả lỗi truy cập phù hợp.

**Luồng chính:**
1. Người dùng gọi chức năng hoặc API.
2. Backend xác thực danh tính.
3. Backend kiểm tra quyền thao tác.
4. Backend kiểm tra phạm vi thương hiệu của dữ liệu.
5. Hệ thống chỉ trả về dữ liệu được phép.

**Luồng ngoại lệ:**
- E1: Chưa đăng nhập → từ chối truy cập.
- E2: Không có quyền → trả lỗi truy cập.
- E3: Dữ liệu thuộc tenant khác → từ chối, không trả dữ liệu nhạy cảm.

**Quy tắc nghiệp vụ:**
- Người dùng của một thương hiệu không được xem/sửa dữ liệu của thương hiệu khác.
- Ẩn chức năng trên giao diện không thay thế kiểm tra quyền ở backend.

## 4.3. Module Brand and Product Management

### SRS-BRAND-001: Quản lý thương hiệu

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-BRAND-001 |
| **Tham chiếu TASK** | TASK-009 |
| **Tên** | Tạo và quản lý thương hiệu |
| **Mô tả** | Platform Super Admin có thể tạo, xem, cập nhật hoặc ngừng hoạt động một thương hiệu |
| **Độ ưu tiên** | High |
| **Actor** | Platform Super Admin |

**Đầu vào:** tên thương hiệu (bắt buộc), mô tả/liên hệ (theo thiết kế), trạng thái.

**Đầu ra:** thông tin thương hiệu đã lưu và thông báo kết quả.

**Luồng chính:**
1. Super Admin mở màn hình thương hiệu.
2. Hệ thống hiển thị danh sách thương hiệu.
3. Super Admin chọn tạo mới hoặc chỉnh sửa.
4. Nhập thông tin và gửi yêu cầu.
5. Backend kiểm tra dữ liệu và quyền.
6. Hệ thống lưu thay đổi và ghi audit log.

**Ngoại lệ:**
- E1: Tên/mã định danh không hợp lệ hoặc trùng theo chính sách → từ chối.
- E2: Người dùng không phải Super Admin → từ chối thao tác.

### SRS-PROD-001: Quản lý sản phẩm nước hoa

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-PROD-001 |
| **Tham chiếu TASK** | TASK-009 |
| **Tên** | Quản lý danh mục nước hoa |
| **Mô tả** | Người có quyền quản lý thông tin sản phẩm thuộc thương hiệu được giao |
| **Độ ưu tiên** | High |
| **Actor** | Platform Super Admin, Brand Admin |

**Đầu vào (Input):**
- Tên sản phẩm: bắt buộc.
- Thương hiệu: bắt buộc, phải nằm trong phạm vi quyền.
- Mô tả/hình ảnh/nốt hương: theo phạm vi triển khai.
- Giá mỗi lần xịt: số dương, đơn vị tiền tệ được thống nhất.
- Trạng thái: hoạt động/ngừng cung cấp.

**Đầu ra:** danh sách/chi tiết sản phẩm và thông báo lưu thành công hoặc lỗi.

**Luồng chính:**
1. Người dùng mở quản lý sản phẩm.
2. Hệ thống hiển thị sản phẩm thuộc phạm vi được phép.
3. Người dùng tạo hoặc cập nhật sản phẩm.
4. Backend kiểm tra dữ liệu, giá và tenant.
5. Hệ thống lưu sản phẩm.
6. Sản phẩm chỉ xuất hiện ở kiosk khi được gán vào slot khả dụng và thỏa điều kiện vận hành.

**Ngoại lệ:**
- E1: Giá không hợp lệ → từ chối.
- E2: Thương hiệu ngoài phạm vi quyền → từ chối.
- E3: Sản phẩm ngừng cung cấp hoặc không có slot khả dụng → không cho đặt đơn mới.

**Quy tắc nghiệp vụ:**
- Giá và thông tin sản phẩm của đơn cần được lưu cố định tại thời điểm tạo đơn.
- Không hiển thị sản phẩm như sẵn sàng nếu máy/slot không khả dụng.

## 4.4. Module Machine and Slot Management

### SRS-MACH-001: Quản lý máy

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-MACH-001 |
| **Tham chiếu TASK** | TASK-010 |
| **Tên** | Đăng ký và quản lý máy |
| **Mô tả** | Người có quyền có thể đăng ký máy, liên kết máy với thương hiệu/địa điểm và theo dõi trạng thái |
| **Độ ưu tiên** | High |
| **Actor** | Platform Super Admin, Brand Admin, Operations Manager |

**Đầu vào:** mã máy duy nhất, thương hiệu, địa điểm và trạng thái vận hành.

**Đầu ra:** thông tin máy, trạng thái hiện tại và thời điểm liên lạc gần nhất nếu có.

**Luồng chính:**
1. Người dùng mở quản lý máy.
2. Hệ thống hiển thị danh sách máy thuộc phạm vi quyền.
3. Người dùng tạo hoặc cập nhật máy.
4. Backend kiểm tra mã máy và quyền tenant.
5. Hệ thống lưu máy và ghi audit log.

**Ngoại lệ:** mã máy trùng, thương hiệu không hợp lệ hoặc người dùng không đủ quyền thì thao tác bị từ chối.

### SRS-MACH-002: Quản lý slot và điều kiện sẵn sàng

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-MACH-002 |
| **Tham chiếu TASK** | TASK-010, TASK-013 |
| **Tên** | Quản lý slot nước hoa |
| **Mô tả** | Hệ thống quản lý slot độc lập, sản phẩm được gán và trạng thái sẵn sàng |
| **Độ ưu tiên** | High |
| **Actor** | Brand Admin, Operations Manager, Inventory Staff, Technician |

**Đầu vào:** máy, mã slot, sản phẩm/cartridge, trạng thái, ngưỡng tồn kho nếu có.

**Đầu ra:** thông tin slot và trạng thái khả dụng.

**Luồng chính:**
1. Người dùng chọn máy.
2. Hệ thống hiển thị các slot.
3. Người dùng gán/cập nhật sản phẩm hoặc trạng thái slot theo quyền.
4. Backend xác minh máy, slot, sản phẩm và tenant.
5. Hệ thống lưu thay đổi.

**Ngoại lệ:**
- E1: Slot không thuộc máy được chọn → từ chối.
- E2: Cartridge/slot trống → đánh dấu không khả dụng.
- E3: Máy đang bảo trì hoặc có lỗi nghiêm trọng → không cho slot phục vụ đơn mới.

**Quy tắc nghiệp vụ:**
- Mỗi slot chỉ có một cartridge đang hoạt động tại một thời điểm.
- Deliverable của Project Brief yêu cầu prototype có tối thiểu bốn slot được điều khiển độc lập.

## 4.5. Module Order and Payment

### SRS-ORDER-001: Tạo đơn trải nghiệm

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-ORDER-001 |
| **Tham chiếu TASK** | TASK-011 |
| **Tên** | Tạo đơn trải nghiệm nước hoa |
| **Mô tả** | Khách có thể tạo đơn cho một sản phẩm đang sẵn có trên kiosk |
| **Độ ưu tiên** | High |
| **Actor** | Customer/Kiosk User |

**Đầu vào:** sản phẩm/slot được chọn, mã máy và thông tin cần thiết theo thiết kế.

**Đầu ra:** mã đơn, số tiền, thời hạn thanh toán và trạng thái ban đầu.

**Luồng chính:**
1. Khách xem danh sách sản phẩm sẵn có.
2. Khách chọn một sản phẩm.
3. Backend kiểm tra trạng thái máy/slot, tồn kho và giá.
4. Backend tạo đơn ở trạng thái chờ thanh toán.
5. Hệ thống trả thông tin đơn và dữ liệu thanh toán.

**Luồng ngoại lệ:**
- E1: Máy offline → không tạo đơn mới.
- E2: Slot hết nước hoa/không sẵn sàng → từ chối và cập nhật hiển thị.
- E3: Sản phẩm ngừng cung cấp → từ chối.
- E4: Lỗi lưu đơn → không hiển thị đơn như đã tạo thành công.

**Quy tắc nghiệp vụ:**
- Đơn phải lưu snapshot giá/sản phẩm cần thiết tại thời điểm tạo.
- Một đơn chỉ được gắn với đúng máy và slot đích.

### SRS-PAY-001: Xác minh thanh toán

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-PAY-001 |
| **Tham chiếu TASK** | TASK-011 |
| **Tên** | Xác minh trạng thái thanh toán |
| **Mô tả** | Backend tiếp nhận và xác minh trạng thái thanh toán từ nguồn đáng tin cậy |
| **Độ ưu tiên** | High |
| **Actor** | Payment Provider, Backend |

**Đầu vào:** mã tham chiếu đơn/giao dịch, trạng thái giao dịch và dữ liệu xác minh từ provider hoặc simulator.

**Đầu ra:** trạng thái thanh toán được cập nhật và sự kiện nghiệp vụ tương ứng.

**Luồng chính:**
1. Khách thực hiện thanh toán.
2. Provider hoặc simulator gửi kết quả.
3. Backend xác minh nguồn và dữ liệu giao dịch.
4. Backend đối chiếu mã đơn, số tiền và trạng thái.
5. Nếu hợp lệ, đơn được chuyển sang trạng thái đã thanh toán.
6. Hệ thống phát sinh yêu cầu dispensing theo quy trình kiểm soát.

**Luồng ngoại lệ:**
- E1: Callback không hợp lệ → từ chối.
- E2: Mã đơn/số tiền không khớp → không xác nhận thành công.
- E3: Thông báo trùng → không tạo xử lý nghiệp vụ trùng.
- E4: Đơn hết hạn hoặc đã xử lý → áp dụng quy tắc trạng thái, không phát lệnh ngoài ý muốn.

**Quy tắc nghiệp vụ:**
- Chỉ backend/cơ chế xác minh đáng tin cậy mới xác nhận thanh toán thành công.
- Ảnh chụp màn hình do khách cung cấp không phải bằng chứng xác minh thanh toán.

## 4.6. Module Secure Dispensing

### SRS-DISP-001: Phát lệnh dispensing

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-DISP-001 |
| **Tham chiếu TASK** | TASK-012, TASK-013 |
| **Tên** | Phát lệnh xịt sau khi thanh toán |
| **Mô tả** | Backend tạo lệnh có định danh để yêu cầu đúng máy/slot thực hiện một lần xịt |
| **Độ ưu tiên** | High |
| **Actor** | Backend, Device/Gateway |

**Đầu vào:** đơn đã thanh toán, mã máy, mã slot, command ID và thời hạn lệnh.

**Đầu ra:** command được phát, trạng thái ACK/result hoặc trạng thái cần xem xét.

**Luồng chính:**
1. Backend xác nhận đơn đã thanh toán.
2. Backend kiểm tra trạng thái máy/slot lần cuối.
3. Backend tạo command ID duy nhất và gắn với đơn.
4. Command được gửi đến đúng thiết bị/slot.
5. Thiết bị phản hồi ACK và kết quả thực thi.
6. Backend ghi nhận kết quả và cập nhật đơn.

**Luồng ngoại lệ:**
- E1: Máy không kết nối → không phát lệnh hoặc chuyển xử lý theo trạng thái đã định nghĩa.
- E2: Command hết hạn/không đúng đích → thiết bị từ chối.
- E3: ACK/result không nhận được hoặc không rõ ràng → chuyển `REVIEW_REQUIRED`, không tự động gửi lệnh xịt mới.
- E4: Command trùng → không được gây ra lần xịt thành công thứ hai.

**Quy tắc nghiệp vụ:**
- Một đơn đã thanh toán thành công không được dẫn đến quá một lần xịt thành công.
- Command cần có ID duy nhất, máy/slot đích và thời hạn.
- Kết quả không chắc chắn phải được xem xét; không tự động retry theo cách có thể tạo thêm lần xịt.

### SRS-DISP-002: Kiểm tra điều kiện an toàn trước khi xịt

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-DISP-002 |
| **Tham chiếu TASK** | TASK-012, TASK-013 |
| **Tên** | Kiểm tra điều kiện an toàn |
| **Mô tả** | Thiết bị phải từ chối dispensing nếu một trong các điều kiện an toàn bắt buộc không đáp ứng |
| **Độ ưu tiên** | High |
| **Actor** | Device/Gateway |

**Điều kiện từ chối tối thiểu:**
- Máy đang ở chế độ bảo trì.
- Cửa đang mở, nếu có cảm biến cửa trong prototype.
- Slot trống.
- Có lỗi phần cứng nghiêm trọng.
- Command không hợp lệ, hết hạn, trùng hoặc gửi sai thiết bị.

**Luồng chính:**
1. Thiết bị nhận command.
2. Kiểm tra command ID, thời hạn và đích.
3. Kiểm tra trạng thái máy, cửa, slot và lỗi.
4. Nếu tất cả điều kiện đạt, thực hiện tối đa một lần xịt cho command.
5. Gửi kết quả và trạng thái lỗi nếu có.

**Quy tắc nghiệp vụ:** thiết bị phải fail-safe theo điều kiện được thiết kế; backend không được coi lệnh đã gửi là bằng chứng xịt thành công.

## 4.7. Module IoT Monitoring and Communication

### SRS-IOT-001: Heartbeat và trạng thái kết nối

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-IOT-001 |
| **Tham chiếu TASK** | TASK-013 |
| **Tên** | Theo dõi heartbeat và online/offline |
| **Mô tả** | Hệ thống tiếp nhận heartbeat và xác định trạng thái kết nối máy |
| **Độ ưu tiên** | High |
| **Actor** | Device/Gateway, Operations Manager |

**Đầu vào:** mã thiết bị, thời điểm gửi và thông tin trạng thái được hỗ trợ.

**Đầu ra:** trạng thái online/offline và thời điểm liên lạc gần nhất.

**Luồng chính:**
1. Thiết bị gửi heartbeat theo chu kỳ.
2. Backend xác thực thiết bị.
3. Hệ thống lưu thời điểm và trạng thái.
4. Dashboard hiển thị trạng thái mới nhất.
5. Nếu quá thời gian quy định không nhận heartbeat, hệ thống đánh dấu offline theo cấu hình.

**Ngoại lệ:** thiết bị không hợp lệ hoặc payload sai phải bị từ chối và ghi log phù hợp.

### SRS-IOT-002: Tiếp nhận telemetry và event

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-IOT-002 |
| **Tham chiếu TASK** | TASK-013, TASK-015 |
| **Tên** | Tiếp nhận dữ liệu telemetry/event |
| **Mô tả** | Hệ thống lưu dữ liệu trạng thái và sự kiện thiết bị được hỗ trợ |
| **Độ ưu tiên** | Medium |
| **Actor** | Device/Gateway, Operations Manager, Technician |

**Dữ liệu có thể gồm:** trạng thái cửa, trạng thái slot, lỗi cơ cấu, kết nối, thời điểm event và mã command liên quan, tùy cảm biến/phần cứng thực tế.

**Luồng chính:**
1. Thiết bị gửi telemetry/event.
2. Backend xác minh thiết bị và cấu trúc payload.
3. Hệ thống lưu bản ghi.
4. Các trạng thái quan trọng được cập nhật trên dashboard.
5. Event phù hợp có thể kích hoạt cảnh báo.

## 4.8. Module Inventory and Refill

### SRS-INV-001: Quản lý cartridge và tồn kho

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-INV-001 |
| **Tham chiếu TASK** | TASK-014 |
| **Tên** | Theo dõi cartridge và lượng nước hoa |
| **Mô tả** | Hệ thống theo dõi cartridge, slot đang gán và lượng tồn kho ước tính |
| **Độ ưu tiên** | High |
| **Actor** | Inventory Staff, Brand Admin |

**Đầu vào:** máy/slot, cartridge, lượng nạp hoặc dữ liệu hiệu chuẩn theo thiết kế.

**Đầu ra:** trạng thái cartridge, lượng tồn kho ước tính và lịch sử thay đổi.

**Luồng chính:**
1. Nhân viên chọn máy/slot.
2. Hệ thống hiển thị cartridge và tồn kho hiện tại.
3. Nhân viên ghi nhận gán/thay cartridge hoặc điều chỉnh.
4. Backend xác minh quyền và dữ liệu.
5. Hệ thống cập nhật tồn kho và ghi lịch sử.

**Quy tắc nghiệp vụ:**
- Chỉ dispensing khách hàng được xác nhận thành công mới giảm tồn kho bán hàng.
- Test/diagnostic spray được ghi riêng.
- Điều chỉnh tồn kho phải có lý do và người thực hiện.

### SRS-INV-002: Ghi nhận refill

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-INV-002 |
| **Tham chiếu TASK** | TASK-014 |
| **Tên** | Ghi nhận hoạt động refill |
| **Mô tả** | Nhân viên có quyền ghi nhận việc nạp thêm hoặc thay cartridge |
| **Độ ưu tiên** | Medium |
| **Actor** | Inventory Staff |

**Đầu vào:** máy/slot, thời gian, loại thao tác, lượng nạp/thông tin cartridge và ghi chú nếu có.

**Đầu ra:** bản ghi refill, tồn kho được cập nhật và audit log.

**Ngoại lệ:** slot/máy không hợp lệ hoặc người dùng không đủ quyền thì từ chối; dữ liệu thiếu trường bắt buộc không được lưu.

### SRS-INV-003: Cảnh báo tồn kho thấp

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-INV-003 |
| **Tham chiếu TASK** | TASK-014, TASK-015 |
| **Tên** | Cảnh báo tồn kho thấp/slot trống |
| **Mô tả** | Hệ thống tạo cảnh báo khi tồn kho ước tính xuống dưới ngưỡng hoặc slot không còn khả dụng |
| **Độ ưu tiên** | Medium |
| **Actor** | Inventory Staff, Operations Manager |

**Luồng chính:**
1. Hệ thống cập nhật tồn kho.
2. So sánh với ngưỡng cấu hình.
3. Tạo/cập nhật cảnh báo nếu điều kiện đạt.
4. Người có quyền xem và cập nhật trạng thái xử lý.

## 4.9. Module Alerts and Maintenance

### SRS-OPS-001: Quản lý cảnh báo vận hành

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-OPS-001 |
| **Tham chiếu TASK** | TASK-015 |
| **Tên** | Xem và xử lý cảnh báo |
| **Mô tả** | Hệ thống tổng hợp cảnh báo như máy offline, tồn kho thấp, cửa mở, lỗi cảm biến và dispensing thất bại |
| **Độ ưu tiên** | Medium |
| **Actor** | Operations Manager, Technician, Inventory Staff |

**Thông tin cảnh báo:** mã cảnh báo, máy/slot, loại, mức độ, thời điểm phát sinh, mô tả và trạng thái xử lý.

**Luồng chính:**
1. Hệ thống phát hiện điều kiện cảnh báo.
2. Tạo hoặc cập nhật cảnh báo.
3. Người vận hành xem danh sách và chi tiết.
4. Người có quyền cập nhật trạng thái xử lý và ghi chú.
5. Lịch sử xử lý được lưu lại.

### SRS-MAINT-001: Quản lý phiếu bảo trì

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-MAINT-001 |
| **Tham chiếu TASK** | TASK-016 |
| **Tên** | Tạo và xử lý phiếu bảo trì |
| **Mô tả** | Người có quyền tạo phiếu bảo trì, giao việc và ghi nhận kết quả khắc phục |
| **Độ ưu tiên** | Medium |
| **Actor** | Operations Manager, Technician |

**Đầu vào:** máy, mô tả sự cố, mức độ ưu tiên, người phụ trách và ghi chú.

**Đầu ra:** mã phiếu, trạng thái và lịch sử bảo trì.

**Luồng chính:**
1. Người vận hành tạo phiếu từ máy hoặc cảnh báo.
2. Hệ thống ghi nhận sự cố và người phụ trách.
3. Máy có thể được chuyển sang chế độ bảo trì.
4. Kỹ thuật viên kiểm tra, ghi kết quả và hành động khắc phục.
5. Thực hiện checklist/test spray chẩn đoán nếu được phép.
6. Người có quyền xác nhận máy đủ điều kiện hoạt động trở lại.
7. Hệ thống lưu lịch sử và audit log.

**Quy tắc nghiệp vụ:**
- Máy đang bảo trì không nhận đơn mới.
- Test spray được ghi riêng, không tính là giao dịch khách hàng.
- Chỉ cho máy trở lại hoạt động sau các bước kiểm tra được yêu cầu.

## 4.10. Module Dashboard, Reporting and Audit

### SRS-REPORT-001: Dashboard vận hành

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-REPORT-001 |
| **Tham chiếu TASK** | TASK-018 |
| **Tên** | Dashboard tổng quan |
| **Mô tả** | Cung cấp số liệu vận hành phù hợp với vai trò và phạm vi thương hiệu |
| **Độ ưu tiên** | Medium |
| **Actor** | Platform Super Admin, Brand Admin, Operations Manager |

**Thông tin đề xuất:** tổng số máy, máy online/offline, giao dịch theo trạng thái, dispensing thành công/thất bại, cảnh báo tồn kho và phiếu bảo trì.

**Luồng chính:**
1. Người dùng mở dashboard.
2. Backend xác định phạm vi dữ liệu theo quyền.
3. Hệ thống truy xuất số liệu.
4. Giao diện hiển thị tổng quan và bộ lọc được hỗ trợ.

### SRS-AUDIT-001: Ghi audit log

| Thuộc tính | Chi tiết |
|---|---|
| **Mã yêu cầu** | SRS-AUDIT-001 |
| **Tham chiếu TASK** | TASK-018 |
| **Tên** | Ghi nhật ký thao tác quan trọng |
| **Mô tả** | Hệ thống lưu vết các thao tác nhạy cảm và sự kiện nghiệp vụ quan trọng |
| **Độ ưu tiên** | High |
| **Actor** | Backend, người dùng quản trị được cấp quyền xem log |

**Các sự kiện tối thiểu:** thay đổi quyền, thay đổi giá, thanh toán, dispensing, điều chỉnh tồn kho, refill, test spray, thay đổi trạng thái máy và thao tác bảo trì.

**Thông tin log đề xuất:** thời gian, actor/service, hành động, đối tượng, kết quả và mã tương quan nếu có.

**Quy tắc nghiệp vụ:**
- Người dùng thông thường không được tự ý sửa/xóa audit log.
- Không ghi mật khẩu, secret hoặc dữ liệu nhạy cảm không cần thiết vào log.

## 4.11. Ma trận yêu cầu chức năng theo module

| Module | Dải mã yêu cầu | Ưu tiên chung | Mã nhiệm vụ |
|---|---|---|---|
| Authentication/RBAC | SRS-AUTH-001–002 | High | TASK-008, TASK-021 |
| Brand/Product | SRS-BRAND-001, SRS-PROD-001 | High | TASK-009 |
| Machine/Slot | SRS-MACH-001–002 | High | TASK-010 |
| Order/Payment | SRS-ORDER-001, SRS-PAY-001 | High | TASK-011 |
| Secure Dispensing | SRS-DISP-001–002 | High | TASK-012, TASK-013 |
| IoT Monitoring | SRS-IOT-001–002 | High/Medium | TASK-013 |
| Inventory/Refill | SRS-INV-001–003 | High/Medium | TASK-014, TASK-015 |
| Alerts/Maintenance | SRS-OPS-001, SRS-MAINT-001 | Medium | TASK-015, TASK-016 |
| Dashboard/Audit | SRS-REPORT-001, SRS-AUDIT-001 | Medium/High | TASK-018 |

---

# 5. Yêu cầu phi chức năng

Các yêu cầu dưới đây là mục tiêu cần rà soát. Ngưỡng định lượng cụ thể phải được nhóm thống nhất hoặc được giảng viên yêu cầu; không tự xem các ngưỡng chưa chốt là tiêu chí nghiệm thu chính thức.

| Mã | Nhóm | Yêu cầu |
|---|---|---|
| NFR-SEC-001 | Bảo mật | API được bảo vệ phải xác thực người dùng/thiết bị và kiểm tra quyền |
| NFR-SEC-002 | Tenant isolation | Người dùng không được truy cập dữ liệu thương hiệu ngoài phạm vi |
| NFR-SEC-003 | Secret management | Không hard-code mật khẩu, token, private key hoặc thông tin kết nối thật trong source code |
| NFR-REL-001 | Độ tin cậy | Xử lý payment callback và command theo cách hạn chế tác động lặp |
| NFR-SAFE-001 | An toàn | Không dispensing khi điều kiện an toàn không đáp ứng |
| NFR-TRACE-001 | Truy vết | Giao dịch và thao tác nhạy cảm phải có thể truy vết |
| NFR-USE-001 | Khả dụng | Kiosk phải chỉ rõ trạng thái thanh toán và dispensing, tránh thông báo gây hiểu nhầm |
| NFR-MAINT-001 | Bảo trì phần mềm | Các module backend, giao diện và IoT nên tách biệt để dễ kiểm thử |
| NFR-DATA-001 | Toàn vẹn dữ liệu | Giá và thông tin quan trọng của đơn được lưu ổn định tại thời điểm tạo |
| NFR-AVAIL-001 | Vận hành | Máy offline/không an toàn không được nhận đơn mới |
| NFR-SCALE-001 | Mở rộng | Thiết kế hỗ trợ nhiều thương hiệu, máy và slot trong phạm vi dự án |
| NFR-TEST-001 | Kiểm thử | Các luồng cốt lõi cần có test case và bằng chứng kết quả |

---

# 6. Yêu cầu cơ sở dữ liệu

Đây là mô hình dữ liệu mức yêu cầu, không phải ERD cuối cùng. Tên bảng, kiểu dữ liệu, khóa và quan hệ chi tiết sẽ được chốt trong tài liệu thiết kế cơ sở dữ liệu.

## 6.1. Các thực thể dự kiến

| Thực thể | Mục đích | Thuộc tính tiêu biểu |
|---|---|---|
| `users` | Tài khoản người dùng quản trị | id, email, password_hash, status |
| `roles` | Danh mục vai trò | id, name |
| `user_roles` | Gán vai trò | user_id, role_id |
| `brands` | Thương hiệu/tenant | id, name, status |
| `brand_memberships` | Liên kết người dùng với thương hiệu | user_id, brand_id, role/scope |
| `products` | Sản phẩm nước hoa | id, brand_id, name, description, price, status |
| `machines` | Máy trải nghiệm | id, brand_id, location, status, last_seen_at |
| `slots` | Khe trên máy | id, machine_id, product_id, cartridge_id, status |
| `cartridges` | Chai/cartridge | id, product_id, status, estimated_remaining |
| `orders` | Đơn trải nghiệm | id, product/slot/machine reference, amount, status, expires_at |
| `payments` | Thông tin thanh toán | id, order_id, provider_reference, amount, status |
| `dispense_commands` | Lệnh dispensing | id, order_id, machine_id, slot_id, status, expires_at |
| `dispense_results` | Kết quả thực hiện | id, command_id, result, error_code, completed_at |
| `device_events` | Event/telemetry | id, machine_id, event_type, payload, received_at |
| `inventory_transactions` | Thay đổi tồn kho | id, slot/cartridge, type, quantity, reason, actor_id |
| `alerts` | Cảnh báo | id, machine_id, slot_id, type, severity, status |
| `maintenance_tickets` | Phiếu bảo trì | id, machine_id, assignee_id, status, description |
| `audit_logs` | Nhật ký thao tác | id, actor, action, target, timestamp, metadata |

## 6.2. Yêu cầu dữ liệu

- Mỗi thực thể cần khóa định danh ổn định.
- Dữ liệu có quan hệ phải được kiểm tra tính toàn vẹn.
- Dữ liệu theo thương hiệu cần có liên kết tenant rõ ràng và được kiểm tra trong mọi truy vấn được bảo vệ.
- Đơn hàng phải lưu thông tin cần thiết để xác định đúng sản phẩm, giá, máy và slot tại thời điểm tạo.
- Payment reference và command ID cần có ràng buộc chống xử lý trùng phù hợp.
- Audit log và kết quả dispensing cần được lưu đủ để truy vết sự cố.
- Không lưu mật khẩu dạng văn bản thuần.
- Payload telemetry cần được thiết kế có giới hạn kích thước và định dạng được xác thực.

## 6.3. Quan hệ khái quát

- Một brand có nhiều product, machine và thành viên được gán quyền.
- Một machine có nhiều slot.
- Một slot có thể được gán một product và một cartridge đang hoạt động.
- Một order có thông tin thanh toán và tối đa một luồng dispensing thành công.
- Một dispense command có thể có ACK/result liên quan.
- Một machine có nhiều event, alert và maintenance ticket.
- Một cartridge có nhiều inventory transaction theo thời gian.

---

# 7. Ràng buộc thiết kế

## 7.1. Ràng buộc công nghệ
- **Backend:** Java Spring Boot theo yêu cầu đã ghi nhận từ project brief/yêu cầu môn học.
- **Web App:** là sản phẩm bắt buộc; framework frontend cần nhóm chốt.
- **Database:** dùng cơ sở dữ liệu quan hệ; MySQL là lựa chọn đề xuất nếu chưa có yêu cầu bắt buộc.
- **IoT protocol:** MQTT là lựa chọn đề xuất, cần thống nhất với prototype.
- **Hardware:** prototype cần đối chiếu yêu cầu tối thiểu bốn slot được điều khiển độc lập.
- **Payment:** tích hợp thật hoặc mô phỏng phải được mô tả trung thực.

## 7.2. Quy tắc nghiệp vụ bắt buộc

| Mã | Quy tắc |
|---|---|
| BR-001 | Mỗi máy thuộc đúng một thương hiệu tại một thời điểm |
| BR-002 | Mỗi slot chỉ có một cartridge đang hoạt động tại một thời điểm |
| BR-003 | Chỉ sản phẩm sẵn có trên máy/slot hoạt động mới được khách lựa chọn |
| BR-004 | Giá và thông tin cần thiết của đơn được cố định tại thời điểm tạo đơn |
| BR-005 | Chỉ backend/cơ chế xác minh đáng tin cậy mới xác nhận thanh toán thành công |
| BR-006 | Ảnh chụp màn hình do khách cung cấp không phải bằng chứng xác minh thanh toán |
| BR-007 | Một đơn thanh toán thành công không được dẫn đến quá một lần xịt thành công |
| BR-008 | Command sai, hết hạn, trùng hoặc gửi sai thiết bị phải bị từ chối |
| BR-009 | Không dispensing khi máy bảo trì, cửa mở, slot trống hoặc có lỗi phần cứng nghiêm trọng |
| BR-010 | Không nhận đơn mới nếu máy không thể giao tiếp với backend để thực hiện giao dịch an toàn |
| BR-011 | Chỉ dispensing khách hàng thành công đã xác nhận mới giảm tồn kho bán hàng |
| BR-012 | Diagnostic/test spray phải được ghi nhận riêng |
| BR-013 | Dữ liệu giữa các thương hiệu phải được cách ly |
| BR-014 | Kết quả dispensing không rõ ràng phải được xem xét; không tự động phát lệnh xịt lần hai |
| BR-015 | Máy chỉ trở lại hoạt động sau các bước kiểm tra sau bảo trì được yêu cầu |

---

# 8. Ma trận truy xuất yêu cầu và mã nhiệm vụ

## 8.1. Quy ước mã

| Tiền tố | Ý nghĩa | Ví dụ |
|---|---|---|
| `SRS-<MODULE>-NNN` | Yêu cầu chức năng chi tiết | `SRS-DISP-001` |
| `NFR-...` | Yêu cầu phi chức năng | `NFR-SEC-001` |
| `BR-NNN` | Quy tắc nghiệp vụ | `BR-007` |
| `TASK-NNN` | Mã tham chiếu nhiệm vụ do nhóm quy ước | `TASK-012` |
| `TC-NNN` | Mã test case do nhóm quy ước | `TC-001` |
| GitHub Issue `#N` | Số Issue do GitHub tự sinh | Ví dụ `#12` |

> `TASK-xxx` không phải số Issue GitHub. Đặt mã `TASK-xxx` trong tiêu đề để tìm kiếm/đối chiếu; GitHub tự sinh số Issue thật.

## 8.2. Danh sách nhiệm vụ tham chiếu

| Task ID | Nhiệm vụ | Yêu cầu liên quan | Tuần dự kiến | Kết quả bàn giao |
|---|---|---|---|---|
| TASK-001 | Rà soát Project Brief và chốt phạm vi MVP | Toàn bộ SRS | 1 | Phạm vi và câu hỏi cần xác nhận |
| TASK-002 | Hoàn thiện SRS | Các SRS/NFR/BR | 1 | SRS được nhóm review |
| TASK-003 | Xác định actor và vẽ Use Case Diagram | Các module | 1 | Use Case Diagram và mô tả use case |
| TASK-004 | Thiết kế ERD và mô hình dữ liệu | Mục 6, BR-001–BR-015 | 1–2 | ERD và data dictionary |
| TASK-005 | Thiết kế kiến trúc hệ thống | Mục 2, Mục 7 | 2 | Architecture Diagram |
| TASK-006 | Đặc tả API contract | Các yêu cầu chức năng | 2–3 | API specification |
| TASK-007 | Khởi tạo Spring Boot và cấu hình database | NFR-MAINT-001, NFR-DATA-001 | 2 | Backend chạy và kết nối DB |
| TASK-008 | Đăng nhập, user và RBAC | SRS-AUTH-001–002 | 2–3 | Xác thực và phân quyền |
| TASK-009 | Quản lý brand và product | SRS-BRAND-001, SRS-PROD-001 | 3 | API/giao diện brand-product |
| TASK-010 | Quản lý machine và slot | SRS-MACH-001–002 | 3 | API/giao diện máy-slot |
| TASK-011 | Tạo order và xử lý payment | SRS-ORDER-001, SRS-PAY-001 | 3–4 | Luồng đơn và thanh toán |
| TASK-012 | Xây dựng dispensing state machine và command | SRS-DISP-001–002 | 4–5 | Command/ACK/result và chống xử lý trùng |
| TASK-013 | Tích hợp MQTT và device simulator/ESP32 | SRS-IOT-001–002, SRS-DISP-001–002 | 4–5 | Luồng thiết bị |
| TASK-014 | Inventory và refill | SRS-INV-001–002 | 5 | Quản lý tồn kho/refill |
| TASK-015 | Alerts và cảnh báo tồn kho | SRS-INV-003, SRS-OPS-001 | 5–6 | Cảnh báo và xử lý |
| TASK-016 | Maintenance ticket và checklist | SRS-MAINT-001 | 5–6 | Quy trình bảo trì |
| TASK-017 | Xây dựng Kiosk UI và Admin Portal | Mục 3, các API liên quan | 3–6 | Giao diện tích hợp |
| TASK-018 | Dashboard và audit log | SRS-REPORT-001, SRS-AUDIT-001 | 5–6 | Dashboard và audit |
| TASK-019 | Lập test plan/test cases từ sớm | Tất cả SRS/NFR/BR | 1–6 | Test plan và test cases |
| TASK-020 | Kiểm thử tích hợp end-to-end | Order, payment, dispensing, inventory | 6–7 | Kết quả test luồng chính |
| TASK-021 | Kiểm thử RBAC và tenant isolation | SRS-AUTH-002, NFR-SEC-001–002, BR-013 | 3–7 | Bằng chứng kiểm thử phân quyền |
| TASK-022 | Viết hướng dẫn cài đặt và sử dụng | Deliverables | 6–7 | README/user guide/technical docs |
| TASK-023 | Chuẩn bị demo, nghiệm thu và bàn giao | Acceptance criteria | 7 | Demo, checklist và source code |

## 8.3. Ví dụ liên kết yêu cầu – nhiệm vụ – kiểm thử

| Yêu cầu | Nhiệm vụ | Test case mẫu | Kết quả mong đợi |
|---|---|---|---|
| SRS-AUTH-002 | TASK-008, TASK-021 | TC-001: Brand A truy cập dữ liệu Brand B | Bị từ chối, không lộ dữ liệu |
| SRS-PAY-001 | TASK-011, TASK-020 | TC-002: Callback thanh toán giả mạo | Không xác nhận thành công |
| SRS-DISP-001 | TASK-012, TASK-020 | TC-003: Gửi lại cùng command | Không có lần xịt thành công thứ hai |
| SRS-DISP-002 | TASK-012, TASK-013 | TC-004: Cửa mở khi nhận command | Thiết bị từ chối dispensing |
| SRS-INV-001 | TASK-014, TASK-020 | TC-005: Dispensing thất bại | Không giảm tồn kho bán hàng |
| SRS-IOT-001 | TASK-013, TASK-020 | TC-006: Thiết bị ngừng heartbeat | Máy được đánh dấu offline theo cấu hình |

## 8.4. Cách sử dụng trên GitHub

Ví dụ tiêu đề Issue:
- `[TASK-003] Vẽ Use Case Diagram`
- `[TASK-008] Implement authentication and RBAC`
- `[TASK-012] Implement secure dispensing command`
- `[TASK-020] E2E test: payment to dispensing`

Trong phần mô tả Issue, ghi:
- **Requirement IDs:** `SRS-DISP-001`, `SRS-DISP-002`, `BR-007`, `BR-008`, `BR-014`
- **Acceptance criteria:** điều kiện để xem nhiệm vụ hoàn thành.
- **Test evidence:** liên kết test case, ảnh chụp hoặc log phù hợp.

---

# 9. Phụ lục

## 9.1. Tiêu chí nghiệm thu MVP đề xuất

1. Người dùng quản trị đăng nhập được và chỉ truy cập chức năng được phép.
2. Tenant isolation ngăn truy cập dữ liệu giữa các thương hiệu.
3. Quản lý được thương hiệu, sản phẩm, máy và tối thiểu bốn slot theo phạm vi demo.
4. Khách xem sản phẩm sẵn có và tạo đơn trải nghiệm được.
5. Luồng thanh toán thật hoặc mô phỏng thể hiện được thành công, thất bại và hết hạn.
6. Backend chỉ phát command sau khi thanh toán được xác nhận.
7. Callback/command gửi lặp không gây thêm lần xịt thành công.
8. Máy/slot không an toàn từ chối dispensing.
9. Kết quả dispensing được ghi nhận và trạng thái đơn cập nhật đúng.
10. Tồn kho chỉ giảm khi dispensing khách hàng thành công đã xác nhận; test spray được ghi riêng.
11. Người vận hành xem được trạng thái máy, tồn kho và cảnh báo cơ bản.
12. Các thao tác quan trọng có audit log.
13. Có test case và kết quả kiểm thử cho luồng chính.
14. Có source code, hướng dẫn chạy, tài liệu kỹ thuật và hướng dẫn demo.

## 9.2. Quản lý thay đổi

- Thay đổi yêu cầu cần ghi trong GitHub Issue hoặc lịch sử quyết định của nhóm.
- Khi thay đổi một yêu cầu, rà soát ảnh hưởng đến Use Case, ERD, API, UI, IoT và test case.
- Không xóa mã yêu cầu đã được tham chiếu mà không cập nhật tài liệu liên quan.
- Sau khi nhóm review, cập nhật phiên bản tài liệu và ghi rõ ngày thay đổi.

## 9.4. Trạng thái tài liệu

Tài liệu này là **bản nháp SRS 1.1 để nhóm review**. Sau khi thống nhất yêu cầu và các lựa chọn kỹ thuật còn mở, nhóm có thể chốt bản baseline rồi tiếp tục `system-specification.md`, Use Case Diagram, ERD, API specification và test plan.
