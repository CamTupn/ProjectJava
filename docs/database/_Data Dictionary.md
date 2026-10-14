\# Từ điển dữ liệu (Data Dictionary) — ScentStation Platform

\- \*\*Đường dẫn tệp:\*\* \`docs/database/data-dictionary.md\`  
\- \*\*Hệ quản trị CSDL:\*\* MySQL 8.0+  
\- \*\*Quy ước chung:\*\*  
  \- Kiểu khóa chính (\`PK\`): \`BIGINT UNSIGNED AUTO\_INCREMENT\`.  
  \- Kiểu khóa ngoại (\`FK\`): \`BIGINT UNSIGNED\`, có thiết lập tường minh \`ON DELETE CASCADE\` hoặc \`RESTRICT\` nhằm bảo vệ tính toàn vẹn tham chiếu.  
  \- Quy ước đặt tên: \`snake\_case\` cho toàn bộ tên bảng và tên cột.  
  \- Thời gian: Sử dụng kiểu \`TIMESTAMP\`, mặc định \`CURRENT\_TIMESTAMP\` cho \`created\_at\` và \`CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP\` cho \`updated\_at\`.

\---

\#\# I. Nhóm Thương hiệu & Tài khoản (Tenant & Users)

\#\#\# 1\. Bảng \`tenants\` (Quản lý thương hiệu / Người thuê đa cấp)  
Mỗi thương hiệu nước hoa hoạt động trong một không gian dữ liệu độc lập (Multi-tenant isolation)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh duy nhất thương hiệu |  
| \`name\` | VARCHAR(150) | NOT NULL | \- | Tên thương hiệu nước hoa (VD: Chanel, Dior) |  
| \`code\` | VARCHAR(50) | NOT NULL, UNIQUE | \- | Mã định danh ngắn phục vụ phân tách phạm vi tenant |  
| \`status\` | ENUM('ACTIVE', 'SUSPENDED', 'INACTIVE') | NOT NULL, DEFAULT 'ACTIVE' | \- | Trạng thái hoạt động của tenant |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm tạo bản ghi |  
| \`updated\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP | \- | Thời điểm cập nhật bản ghi |

\#\#\# 2\. Bảng \`users\` (Quản lý tài khoản người dùng hệ thống)  
Hỗ trợ phân quyền theo vai trò (RBAC) cho các đối tượng như Quản trị hệ thống, Quản trị thương hiệu, Quản lý vận hành, Kỹ thuật viên và Nhân viên kho\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh người dùng |  
| \`tenant\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`tenants.id\`) | Thuộc tenant nào (NULL nếu là Super Admin toàn hệ thống)\[cite: 1\] |  
| \`username\` | VARCHAR(100) | NOT NULL, UNIQUE | \- | Tên đăng nhập hệ thống |  
| \`email\` | VARCHAR(150) | NOT NULL, UNIQUE | \- | Email liên hệ và nhận thông báo |  
| \`password\_hash\` | VARCHAR(255) | NOT NULL | \- | Mật khẩu mã hóa (Bcrypt) |  
| \`role\` | ENUM('SUPER\_ADMIN', 'BRAND\_ADMIN', 'OPS\_MANAGER', 'TECHNICIAN', 'INVENTORY\_STAFF') | NOT NULL | \- | Vai trò phân quyền người dùng\[cite: 1\] |  
| \`status\` | ENUM('ACTIVE', 'LOCKED', 'INACTIVE') | NOT NULL, DEFAULT 'ACTIVE' | \- | Trạng thái tài khoản |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm tạo bản ghi |  
| \`updated\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP | \- | Thời điểm cập nhật bản ghi |  
\*(Chỉ mục bổ sung: \`INDEX idx\_tenant\_role (tenant\_id, role)\`)\*\[cite: 1\]

\---

\#\# II. Nhóm Sản phẩm, Máy Kiosk & Khe chứa (Products, Machines & Slots)

\#\#\# 3\. Bảng \`products\` (Quản lý danh mục sản phẩm nước hoa)

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh sản phẩm nước hoa |  
| \`tenant\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`tenants.id\`) | Thương hiệu sở hữu sản phẩm\[cite: 1\] |  
| \`name\` | VARCHAR(150) | NOT NULL | \- | Tên dòng nước hoa |  
| \`fragrance\_notes\`| TEXT | DEFAULT NULL | \- | Thành phần tầng hương (Top, Middle, Base notes)\[cite: 1\] |  
| \`price\_per\_spray\`| DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 | \- | Giá tiền niêm yết cho mỗi lần xịt chuẩn |  
| \`image\_url\` | VARCHAR(255) | DEFAULT NULL | \- | Đường dẫn hình ảnh hiển thị trên giao diện Kiosk\[cite: 1\] |  
| \`status\` | ENUM('AVAILABLE', 'DISCONTINUED') | NOT NULL, DEFAULT 'AVAILABLE' | \- | Trạng thái kinh doanh sản phẩm |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm tạo bản ghi |

\#\#\# 4\. Bảng \`machines\` (Quản lý thiết bị máy Kiosk IoT)  
Mỗi máy kiosk tại một thời điểm thuộc đúng một thương hiệu nước hoa (BR-01)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh máy Kiosk |  
| \`tenant\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`tenants.id\`) | Thương hiệu quản lý thiết bị (BR-01)\[cite: 1\] |  
| \`serial\_number\` | VARCHAR(100) | NOT NULL, UNIQUE | \- | Mã phần cứng định danh thiết bị (Device Identity)\[cite: 1\] |  
| \`name\` | VARCHAR(150) | NOT NULL | \- | Tên gợi nhớ/Vị trí lắp đặt |  
| \`location\_address\`| TEXT | NOT NULL | \- | Địa chỉ lắp đặt thực tế của máy\[cite: 1\] |  
| \`status\` | ENUM('ONLINE', 'OFFLINE', 'MAINTENANCE', 'DISABLED', 'ERROR') | NOT NULL, DEFAULT 'OFFLINE' | \- | Trạng thái vận hành thiết bị\[cite: 1\] |  
| \`firmware\_version\`| VARCHAR(50) | DEFAULT NULL | \- | Phiên bản firmware hiện tại trên thiết bị\[cite: 1\] |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm tạo bản ghi |  
| \`updated\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP | \- | Thời điểm cập nhật bản ghi |  
\*(Chỉ mục bổ sung: \`INDEX idx\_tenant\_status (tenant\_id, status)\`)\*

\#\#\# 5\. Bảng \`machine\_slots\` (Quản lý các khe/khay xịt trên máy)  
Mỗi khe chứa quản lý độc lập một chai nước hoa (BR-02)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh khe xịt |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Thuộc máy Kiosk nào\[cite: 1\] |  
| \`slot\_number\` | INT | NOT NULL | \- | Số thứ tự khe trên máy (VD: 1, 2, 3, 4)\[cite: 1\] |  
| \`product\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`products.id\`) | Sản phẩm nước hoa đang được gắn trong slot này (BR-02)\[cite: 1\] |  
| \`capacity\_ml\` | DECIMAL(8,2) | NOT NULL | \- | Sức chứa tối đa của bình chứa (ml) |  
| \`current\_volume\_ml\`| DECIMAL(8,2) | NOT NULL | \- | Lượng nước hoa ước tính còn lại trong khe (ml) |  
| \`status\` | ENUM('AVAILABLE', 'LOW\_STOCK', 'EMPTY', 'DISABLED', 'ERROR') | NOT NULL, DEFAULT 'AVAILABLE' | \- | Trạng thái khe (BR-09)\[cite: 1\] |  
\*(Ràng buộc duy nhất: \`UNIQUE KEY uk\_machine\_slot (machine\_id, slot\_number)\`)\*

\---

\#\# III. Nhóm Đơn hàng & Thanh toán (Orders & Payments)

\#\#\# 6\. Bảng \`orders\` (Quản lý đơn hàng trải nghiệm nước hoa)  
Sản phẩm và giá tiền của đơn hàng được cố định tại thời điểm tạo đơn (BR-04)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh đơn hàng |  
| \`order\_code\` | VARCHAR(64) | NOT NULL, UNIQUE | \- | Mã giao dịch đơn hàng hiển thị trên Kiosk |  
| \`tenant\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`tenants.id\`) | Ràng buộc phạm vi tenant\[cite: 1\] |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy thực hiện giao dịch\[cite: 1\] |  
| \`slot\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machine\_slots.id\`) | Khe nước hoa khách hàng lựa chọn\[cite: 1\] |  
| \`product\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`products.id\`) | Sản phẩm tương ứng (Cố định theo BR-04)\[cite: 1\] |  
| \`amount\` | DECIMAL(12,2) | NOT NULL | \- | Tổng số tiền thanh toán |  
| \`status\` | ENUM('PENDING\_PAYMENT', 'PAID', 'DISPENSING', 'DISPENSED', 'FAILED', 'EXPIRED', 'REFUNDED', 'REVIEW') | NOT NULL, DEFAULT 'PENDING\_PAYMENT' | \- | Trạng thái đơn hàng\[cite: 1\] |  
| \`expires\_at\` | TIMESTAMP | NOT NULL | \- | Thời gian hết hạn nếu chưa thanh toán QR\[cite: 1\] |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm khởi tạo đơn hàng |  
\*(Chỉ mục bổ sung: \`INDEX idx\_order\_status\_created (status, created\_at)\`)\*

\#\#\# 7\. Bảng \`payments\` (Lưu thông tin giao dịch thanh toán QR)  
Chỉ backend mới được xác thực thanh toán thành công thông qua webhook từ cổng thanh toán (BR-05)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh bản ghi thanh toán |  
| \`order\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`orders.id\`) | Đơn hàng liên kết\[cite: 1\] |  
| \`transaction\_no\` | VARCHAR(100) | DEFAULT NULL, UNIQUE | \- | Mã giao dịch trả về từ cổng thanh toán trung gian |  
| \`gateway\_provider\`| VARCHAR(50) | NOT NULL | \- | Tên cổng thanh toán (VD: VNPay, Momo, Mock) |  
| \`amount\` | DECIMAL(12,2) | NOT NULL | \- | Số tiền thực tế thanh toán thành công |  
| \`status\` | ENUM('PENDING', 'SUCCESS', 'FAILED', 'CANCELLED', 'EXPIRED', 'REFUNDED') | NOT NULL, DEFAULT 'PENDING' | \- | Trạng thái thanh toán\[cite: 1\] |  
| \`raw\_response\` | TEXT | DEFAULT NULL | \- | Dữ liệu JSON thô phản hồi từ webhook thanh toán |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm khởi tạo yêu cầu thanh toán |  
| \`updated\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP | \- | Thời điểm cập nhật trạng thái webhook |  
\*(Chỉ mục bổ sung: \`INDEX idx\_payment\_order (order\_id, status)\`)\*

\---

\#\# IV. Nhóm Nhả hàng Phần cứng (Secure Dispensing)

\#\#\# 8\. Bảng \`dispense\_commands\` (Lệnh điều khiển nhả nước hoa)  
Đảm bảo quy tắc nghiệp vụ cốt lõi: Một đơn hàng thanh toán thành công không được tạo quá một lần xịt thành công (BR-07)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh lệnh nhả hàng |  
| \`command\_uuid\` | VARCHAR(64) | NOT NULL, UNIQUE | \- | UUID duy nhất chống trùng lặp lệnh (Idempotency key)\[cite: 1\] |  
| \`order\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`orders.id\`) | Đơn hàng đã được xác nhận thanh toán\[cite: 1\] |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy Kiosk nhận lệnh qua MQTT\[cite: 1\] |  
| \`slot\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machine\_slots.id\`) | Khe cần kích hoạt bơm định lượng\[cite: 1\] |  
| \`dosage\_ml\` | DECIMAL(5,2) | NOT NULL, DEFAULT 1.00 | \- | Định lượng nước hoa cho một lần xịt chuẩn (ml) |  
| \`status\` | ENUM('CREATED', 'SENT', 'ACKED', 'EXECUTED', 'FAILED', 'EXPIRED', 'REJECTED', 'REVIEW') | NOT NULL, DEFAULT 'CREATED' | \- | Trạng thái vòng đời lệnh xịt\[cite: 1\] |  
| \`failure\_code\` | VARCHAR(50) | DEFAULT NULL | \- | Mã lỗi chi tiết nếu thiết bị từ chối hoặc thực thi thất bại |  
| \`executed\_at\` | TIMESTAMP | NULL DEFAULT NULL | \- | Thời điểm thiết bị phản hồi hoàn tất xịt thực tế |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm khởi tạo lệnh điều phối |  
\*(Ràng buộc duy nhất quan trọng chống xịt 2 lần: \`UNIQUE KEY uk\_order\_command (order\_id)\`)\*\[cite: 1\]

\---

\#\# V. Nhóm Tồn kho & Refill (Inventory & Refill)

\#\#\# 9\. Bảng \`inventory\_transactions\` (Lịch sử biến động tồn kho)  
Chỉ dispensing đã được xác nhận thành công mới làm giảm tồn kho bán hàng; xịt kiểm tra (diagnostic spray) phải được tách riêng (BR-11)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh giao dịch tồn kho |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy Kiosk\[cite: 1\] |  
| \`slot\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machine\_slots.id\`) | Khe xịt tương ứng\[cite: 1\] |  
| \`product\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`products.id\`) | Sản phẩm nước hoa\[cite: 1\] |  
| \`user\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`users.id\`) | Nhân viên thực hiện (nếu là refill/điều chỉnh thủ công) |  
| \`transaction\_type\`| ENUM('CUSTOMER\_SALE', 'REFILL', 'ADJUSTMENT', 'DIAGNOSTIC\_SPRAY', 'DISPOSAL') | NOT NULL | \- | Loại biến động kho (BR-11, BR-12)\[cite: 1\] |  
| \`quantity\_delta\_ml\`| DECIMAL(8,2) | NOT NULL | \- | Lượng thay đổi dung tích (+/- ml) |  
| \`balance\_after\_ml\` | DECIMAL(8,2) | NOT NULL | \- | Dung tích tồn kho còn lại sau giao dịch (ml) |  
| \`reference\_id\` | BIGINT UNSIGNED | DEFAULT NULL | \- | Tham chiếu ID đơn hàng hoặc ID phiếu bảo trì |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm ghi nhận giao dịch tồn kho |  
\*(Chỉ mục bổ sung: \`INDEX idx\_machine\_slot\_time (machine\_id, slot\_id, created\_at)\`)\*

\#\#\# 10\. Bảng \`refills\` (Lịch sử tiếp liệu và thay thế bình chứa)

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh phiếu tiếp liệu |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy Kiosk được bảo dưỡng refill\[cite: 1\] |  
| \`slot\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machine\_slots.id\`) | Khe thay thế bình chứa\[cite: 1\] |  
| \`user\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`users.id\`) | Nhân viên kho/kỹ thuật thực hiện |  
| \`old\_product\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`products.id\`) | Sản phẩm cũ trước khi thay (nếu có đổi loại) |  
| \`new\_product\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`products.id\`) | Sản phẩm mới được nạp đầy |  
| \`added\_volume\_ml\`| DECIMAL(8,2) | NOT NULL | \- | Dung tích nước hoa bổ sung vào khe (ml) |  
| \`notes\` | TEXT | DEFAULT NULL | \- | Ghi chú chi tiết của nhân viên vận hành |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm thực hiện tiếp liệu |

\---

\#\# VI. Nhóm IoT, Cảnh báo & Vận hành (Telemetry, Alerts & Maintenance)

\#\#\# 11\. Bảng \`sensor\_readings\` (Dữ liệu telemetry từ thiết bị MQTT)

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh bản ghi telemetry |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy Kiosk gửi dữ liệu\[cite: 1\] |  
| \`slot\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`machine\_slots.id\`) | Khe tương ứng (nếu đo tải trọng cụ thể)\[cite: 1\] |  
| \`sensor\_type\` | VARCHAR(50) | NOT NULL | \- | Loại cảm biến: \`LOAD\_CELL\`, \`DOOR\_SENSOR\`, \`POWER\`, \`LEAK\_DETECTOR\`\[cite: 1\] |  
| \`reading\_value\` | DECIMAL(10,2) | NOT NULL | \- | Giá trị đo được từ cảm biến (Trọng lượng gram, điện áp, v.v.) |  
| \`unit\` | VARCHAR(20) | NOT NULL | \- | Đơn vị đo (\`g\`, \`V\`, \`status\`) |  
| \`recorded\_at\` | TIMESTAMP | NOT NULL | \- | Thời gian thiết bị ghi nhận dữ liệu |  
\*(Chỉ mục bổ sung: \`INDEX idx\_machine\_sensor\_time (machine\_id, sensor\_type, recorded\_at)\`)\*

\#\#\# 12\. Bảng \`alerts\` (Quản lý cảnh báo thiết bị tự động)

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh cảnh báo hệ thống |  
| \`tenant\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`tenants.id\`) | Thương hiệu quản lý\[cite: 1\] |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy phát sinh cảnh báo\[cite: 1\] |  
| \`alert\_type\` | VARCHAR(100) | NOT NULL | \- | Loại lỗi: \`OFFLINE\`, \`LOW\_STOCK\`, \`DOOR\_OPEN\`, \`LEAKAGE\_SUSPECTED\`, \`SENSOR\_FAILURE\`, \`DISPENSING\_FAIL\`\[cite: 1\] |  
| \`severity\` | ENUM('INFO', 'WARNING', 'CRITICAL') | NOT NULL, DEFAULT 'WARNING' | \- | Mức độ nghiêm trọng của cảnh báo\[cite: 1\] |  
| \`status\` | ENUM('ACTIVE', 'ACKNOWLEDGED', 'RESOLVED') | NOT NULL, DEFAULT 'ACTIVE' | \- | Trạng thái xử lý cảnh báo |  
| \`message\` | TEXT | NOT NULL | \- | Nội dung chi tiết thông điệp cảnh báo |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm phát sinh cảnh báo |  
| \`resolved\_at\` | TIMESTAMP | NULL DEFAULT NULL | \- | Thời điểm sự cố được khắc phục xong |

\#\#\# 13\. Bảng \`maintenance\_tickets\` (Quản lý phiếu bảo trì, sự cố kỹ thuật)

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh phiếu bảo trì |  
| \`machine\_id\` | BIGINT UNSIGNED | NOT NULL | FK (\`machines.id\`) | Máy Kiosk cần bảo dưỡng\[cite: 1\] |  
| \`technician\_id\`| BIGINT UNSIGNED | DEFAULT NULL | FK (\`users.id\`) | Kỹ thuật viên phụ trách xử lý\[cite: 1\] |  
| \`status\` | ENUM('OPEN', 'ASSIGNED', 'IN\_PROGRESS', 'WAITING', 'COMPLETED', 'CLOSED') | NOT NULL, DEFAULT 'OPEN' | \- | Trạng thái phiếu bảo trì (BR-15)\[cite: 1\] |  
| \`issue\_description\`| TEXT | NOT NULL | \- | Mô tả sự cố ban đầu |  
| \`resolution\_notes\`| TEXT | DEFAULT NULL | \- | Báo cáo phương án xử lý và linh kiện thay thế của kỹ thuật viên\[cite: 1\] |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm tạo phiếu bảo trì |  
| \`updated\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP | \- | Thời điểm cập nhật phiếu |

\#\#\# 14\. Bảng \`audit\_logs\` (Nhật ký truy vết thao tác nhạy cảm)  
Mọi thay đổi nhạy cảm (hoàn tiền, đổi giá, phân quyền, override tồn kho) đều phải ghi nhận vào audit log và không được phép chỉnh sửa/xóa bởi người dùng thông thường (BR-12, BR-16)\[cite: 1\].

| Cột | Kiểu dữ liệu MySQL | Ràng buộc | Khóa | Mô tả chi tiết |  
| :--- | :--- | :--- | :--- | :--- |  
| \`id\` | BIGINT UNSIGNED | NOT NULL | PK | Định danh bản ghi audit log |  
| \`tenant\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`tenants.id\`) | Tenant liên quan (nếu có)\[cite: 1\] |  
| \`user\_id\` | BIGINT UNSIGNED | DEFAULT NULL | FK (\`users.id\`) | Người thực hiện thao tác (NULL nếu hệ thống tự động)\[cite: 1\] |  
| \`action\` | VARCHAR(150) | NOT NULL | \- | Hành động thực hiện: \`REFUND\_ORDER\`, \`CHANGE\_PRICE\`, \`OVERRIDE\_INVENTORY\`, \`UPDATE\_PERMISSION\`, \`MAINTENANCE\_OVERRIDE\`\[cite: 1\] |  
| \`target\_entity\`| VARCHAR(100) | NOT NULL | \- | Đối tượng bảng tác động (VD: \`orders\`, \`products\`, \`machines\`) |  
| \`target\_id\` | BIGINT UNSIGNED | NOT NULL | \- | ID của đối tượng bị tác động |  
| \`old\_value\` | JSON | DEFAULT NULL | \- | Dữ liệu cũ trước khi thay đổi |  
| \`new\_value\` | JSON | DEFAULT NULL | \- | Dữ liệu mới sau khi thay đổi |  
| \`ip\_address\` | VARCHAR(45) | DEFAULT NULL | \- | Địa chỉ IP của người thực hiện |  
| \`created\_at\` | TIMESTAMP | NOT NULL, DEFAULT CURRENT\_TIMESTAMP | \- | Thời điểm ghi nhận (Bất biến theo BR-12)\[cite: 1\] |  
\*(Chỉ mục bổ sung: \`INDEX idx\_audit\_action\_time (action, created\_at)\`)\*

\---

\#\# VII. Tổng kết kiểm soát Ràng buộc & Trạng thái Nghiệp vụ

1\. \*\*Cô lập dữ liệu đa thương hiệu (Tenant Isolation \- BR-13):\*\*  
   \- Các bảng chính (\`users\`, \`products\`, \`machines\`, \`orders\`, \`alerts\`, \`audit\_logs\`) đều chứa cột \`tenant\_id\`, đảm bảo tầng Backend Spring Boot luôn lọc dữ liệu theo phạm vi thương hiệu tương ứng\[cite: 1\].  
2\. \*\*Kiểm soát chống xịt lặp (One-to-One Dispensing Control \- BR-07):\*\*  
   \- Ràng buộc \`UNIQUE\` trên cột \`order\_id\` trong bảng \`dispense\_commands\` đảm bảo mỗi đơn hàng thành công tuyệt đối chỉ sinh ra một lệnh điều khiển phần cứng duy nhất\[cite: 1\].  
3\. \*\*An toàn phần cứng (Hardware Safety \- BR-09):\*\*  
   \- Các trạng thái khe (\`AVAILABLE\`, \`EMPTY\`, \`ERROR\`) và máy (\`MAINTENANCE\`, \`ERROR\`) được kiểm tra đồng thời với cảm biến cửa mở / tải trọng trước khi phần mềm tạo lệnh xịt\[cite: 1\].  
