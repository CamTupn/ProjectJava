# ScentStation Use Case Specification

Tài liệu này hoàn thiện phần mô tả use case cho TASK-003. Dùng `use-case-diagram-reference.svg` làm ảnh tham khảo khi dựng sơ đồ chỉnh sửa được `use-case-diagram.drawio`. Actor và luồng được tổng hợp từ SRS; quyền truy cập thực tế luôn phải được backend kiểm tra theo vai trò và phạm vi thương hiệu.

## Actors

| Actor | Mã | Mục tiêu / vai trò |
|---|---|---|
| Platform Super Admin | PSA | Quản trị nền tảng, thương hiệu và cấu hình chung |
| Brand Admin | BA | Quản lý dữ liệu, sản phẩm và hoạt động trong phạm vi thương hiệu được giao |
| Operations Manager | OM | Theo dõi máy, cảnh báo, bảo trì và hoạt động vận hành |
| Technician | TECH | Chẩn đoán, bảo trì và thực hiện kiểm tra được cấp quyền |
| Inventory Staff | INV | Quản lý cartridge, slot, tồn kho và refill |
| Customer / Kiosk User | CUST | Tìm, chọn sản phẩm, thanh toán và nhận kết quả trải nghiệm |
| Device / Gateway | DEV | Gửi telemetry, nhận command và trả ACK/kết quả |
| Payment Provider | PAY | Gửi thông báo giao dịch để hệ thống xác minh |

## Use cases

### UC-01 — View Available Fragrances

- **Actor chính:** Customer / Kiosk User.
- **Mục tiêu:** Xem các sản phẩm đang có thể phục vụ trên kiosk.
- **Điều kiện trước:** Kiosk có thể truy cập hệ thống.
- **Luồng chính:** 1) Khách mở danh sách; 2) hệ thống trả các sản phẩm đang hoạt động được gán vào slot khả dụng; 3) kiosk hiển thị thông tin và giá.
- **Ngoại lệ:** Nếu không tải được dữ liệu, kiosk báo lỗi; nếu không có slot khả dụng thì không hiển thị sản phẩm như có thể đặt.
- **Tham chiếu:** SRS-PROD-001, SRS-MACH-002, BR-003.

### UC-02 — Select Fragrance

- **Actor chính:** Customer / Kiosk User.
- **Mục tiêu:** Chọn sản phẩm/slot cho một lần trải nghiệm.
- **Điều kiện trước:** Sản phẩm đang được hiển thị là khả dụng.
- **Luồng chính:** 1) Khách chọn sản phẩm; 2) hệ thống hiển thị chi tiết và giá; 3) khách xác nhận lựa chọn để tiếp tục tạo đơn.
- **Ngoại lệ:** Nếu sản phẩm, máy hoặc slot vừa trở nên không khả dụng, hệ thống từ chối lựa chọn và cập nhật danh sách.
- **Tham chiếu:** SRS-ORDER-001, SRS-MACH-002, BR-003.

### UC-03 — Create Experience Order

- **Actor chính:** Customer / Kiosk User.
- **Mục tiêu:** Tạo đơn chờ thanh toán cho đúng máy và slot.
- **Điều kiện trước:** Khách đã chọn sản phẩm; máy và slot có thể hoạt động.
- **Luồng chính:** 1) Khách xác nhận đặt; 2) backend kiểm tra lại máy, slot, tồn kho và giá; 3) backend lưu đơn cùng snapshot sản phẩm/giá, thời hạn thanh toán và trạng thái chờ thanh toán; 4) hệ thống trả mã đơn và thông tin thanh toán.
- **Ngoại lệ:** Máy offline, slot hết hàng, sản phẩm ngừng cung cấp hoặc lỗi lưu đơn thì không trả kết quả thành công và không tạo đơn giả.
- **Tham chiếu:** SRS-ORDER-001, BR-003.

### UC-04 — Make QR Payment

- **Actor chính:** Customer / Kiosk User. **Actor hỗ trợ:** Payment Provider.
- **Mục tiêu:** Thanh toán đơn bằng phương thức QR được cấu hình.
- **Điều kiện trước:** Đơn tồn tại, còn hạn và đang chờ thanh toán.
- **Luồng chính:** 1) Khách mở thông tin QR của đơn; 2) khách thực hiện thanh toán qua provider; 3) provider gửi kết quả giao dịch; 4) UC-05 xác minh kết quả trước khi hệ thống chuyển trạng thái đơn.
- **Ngoại lệ:** Giao dịch thất bại/hết hạn thì đơn không được coi là đã thanh toán; callback trùng không được tạo xử lý lặp.
- **Tham chiếu:** SRS-ORDER-001, SRS-PAY-001.

### UC-05 — Process Callback

- **Actor chính:** Payment Provider.
- **Mục tiêu:** Chỉ xác nhận thanh toán khi giao dịch được xác minh hợp lệ.
- **Điều kiện trước:** Hệ thống nhận được thông báo giao dịch từ provider hoặc simulator đáng tin cậy.
- **Luồng chính:** 1) Backend xác minh nguồn và dữ liệu giao dịch; 2) đối chiếu mã đơn, số tiền, trạng thái và thời hạn; 3) nếu hợp lệ, cập nhật trạng thái thanh toán một cách idempotent; 4) phát sinh sự kiện cho quy trình dispensing.
- **Ngoại lệ:** Nguồn không đáng tin cậy, sai mã đơn/số tiền, đơn hết hạn hoặc callback trùng thì từ chối hoặc xử lý theo trạng thái hiện tại, không xác nhận thành công sai.
- **Tham chiếu:** SRS-PAY-001, NFR-SEC-001.

### UC-06 — Dispense Fragrance

- **Actor chính:** Customer / Kiosk User. **Actor hỗ trợ:** Device / Gateway.
- **Mục tiêu:** Thực hiện tối đa một lần xịt cho đơn đã thanh toán.
- **Điều kiện trước:** Thanh toán đã được xác minh; máy/slot đúng đích và đủ điều kiện hoạt động.
- **Luồng chính:** 1) Backend kiểm tra lại đơn và trạng thái máy/slot; 2) tạo command ID duy nhất gắn với đơn, máy, slot và thời hạn; 3) thiết bị xác minh command và điều kiện an toàn; 4) thiết bị thực hiện tối đa một lần xịt rồi trả ACK/kết quả; 5) backend lưu kết quả, cập nhật đơn và tồn kho nếu thành công.
- **Ngoại lệ:** Máy offline, cửa mở (nếu có cảm biến), slot trống, máy bảo trì, command sai/hết hạn/trùng thì thiết bị từ chối; kết quả không rõ ràng được chuyển sang cần xem xét, không tự động gửi command mới có thể gây xịt lần hai.
- **Tham chiếu:** SRS-DISP-001, SRS-DISP-002, SRS-INV-001, BR-007, BR-008, BR-014.

### UC-07 — View Dispensing Result

- **Actor chính:** Customer / Kiosk User.
- **Mục tiêu:** Xem trạng thái cuối cùng của đơn trải nghiệm.
- **Điều kiện trước:** Đơn đã được tạo.
- **Luồng chính:** 1) Kiosk hiển thị trạng thái thanh toán và dispensing của đơn; 2) khi có kết quả từ thiết bị, hệ thống hiển thị thành công hoặc thất bại cùng hướng dẫn phù hợp.
- **Ngoại lệ:** Khi chưa có kết quả xác định, hiển thị trạng thái đang xử lý/cần hỗ trợ thay vì báo thành công.
- **Tham chiếu:** SRS-ORDER-001, SRS-PAY-001, SRS-DISP-001.

### UC-08 — Log In

- **Actor chính:** PSA, BA, OM, TECH, INV.
- **Mục tiêu:** Tạo phiên đăng nhập cho người dùng quản trị hợp lệ.
- **Điều kiện trước:** Người dùng có tài khoản được cấp.
- **Luồng chính:** 1) Người dùng nhập thông tin đăng nhập; 2) backend xác minh thông tin và trạng thái tài khoản; 3) hệ thống tạo phiên/token; 4) giao diện điều hướng theo vai trò.
- **Ngoại lệ:** Sai thông tin, tài khoản bị khóa/vô hiệu hóa hoặc backend không khả dụng thì từ chối; không giả lập đăng nhập thành công.
- **Tham chiếu:** SRS-AUTH-001.

### UC-09 — Manage Brand

- **Actor chính:** Platform Super Admin.
- **Mục tiêu:** Tạo, xem, cập nhật hoặc ngừng hoạt động thương hiệu.
- **Điều kiện trước:** Actor đã đăng nhập và có quyền quản trị nền tảng.
- **Luồng chính:** 1) Super Admin mở danh sách thương hiệu; 2) chọn tạo mới hoặc chỉnh sửa; 3) nhập thông tin; 4) backend kiểm tra dữ liệu/quyền, lưu thay đổi và ghi audit log.
- **Ngoại lệ:** Dữ liệu không hợp lệ hoặc actor không đủ quyền thì thay đổi bị từ chối.
- **Tham chiếu:** SRS-BRAND-001, SRS-AUDIT-001.

### UC-10 — Manage Products

- **Actor chính:** Platform Super Admin, Brand Admin.
- **Mục tiêu:** Quản lý danh mục sản phẩm trong phạm vi được phép.
- **Điều kiện trước:** Actor đã đăng nhập.
- **Luồng chính:** 1) Actor mở danh sách sản phẩm; 2) tạo hoặc cập nhật thông tin, giá và trạng thái; 3) backend xác minh dữ liệu, vai trò và phạm vi thương hiệu; 4) hệ thống lưu sản phẩm.
- **Ngoại lệ:** Giá không hợp lệ hoặc thương hiệu ngoài phạm vi quyền thì từ chối; sản phẩm chỉ xuất hiện ở kiosk khi được gán vào slot khả dụng.
- **Tham chiếu:** SRS-PROD-001, SRS-AUTH-002, BR-003.

### UC-11 — Manage Machines and Slots

- **Actor chính:** PSA, BA, OM, TECH, INV (theo thao tác được cấp quyền).
- **Mục tiêu:** Đăng ký máy, quản lý thông tin máy, slot và trạng thái sẵn sàng.
- **Điều kiện trước:** Actor đã đăng nhập và có quyền trong phạm vi thương hiệu/máy tương ứng.
- **Luồng chính:** 1) Actor chọn máy; 2) hệ thống hiển thị máy/slot trong phạm vi quyền; 3) actor tạo/cập nhật thông tin máy hoặc gán sản phẩm, cartridge và trạng thái slot; 4) backend kiểm tra định danh, quan hệ máy-slot, tenant và quyền; 5) lưu thay đổi và ghi audit log khi cần.
- **Ngoại lệ:** Mã máy trùng, slot không thuộc máy, thương hiệu sai phạm vi hoặc máy/slot không đủ điều kiện thì từ chối hoặc đánh dấu không khả dụng.
- **Tham chiếu:** SRS-MACH-001, SRS-MACH-002, SRS-AUTH-002, BR-003.

### UC-12 — Manage Inventory

- **Actor chính:** Inventory Staff, Brand Admin.
- **Mục tiêu:** Xem và quản lý cartridge, slot và lượng tồn kho ước tính.
- **Điều kiện trước:** Actor đã đăng nhập và có quyền với máy/slot.
- **Luồng chính:** 1) Actor chọn máy/slot; 2) hệ thống hiển thị cartridge và tồn kho; 3) actor ghi nhận gán/thay cartridge hoặc điều chỉnh; 4) backend xác minh dữ liệu/quyền; 5) cập nhật tồn kho và lịch sử.
- **Ngoại lệ:** Dữ liệu không hợp lệ hoặc ngoài phạm vi quyền thì không lưu; điều chỉnh phải có lý do và người thực hiện.
- **Tham chiếu:** SRS-INV-001, SRS-AUTH-002.

### UC-13 — Record Refill

- **Actor chính:** Inventory Staff.
- **Mục tiêu:** Ghi nhận việc nạp thêm hoặc thay cartridge.
- **Điều kiện trước:** Actor đã đăng nhập và slot/máy hợp lệ.
- **Luồng chính:** 1) Nhân viên chọn máy/slot; 2) nhập thời gian, loại thao tác, lượng nạp/thông tin cartridge và ghi chú; 3) backend kiểm tra quyền và trường bắt buộc; 4) lưu bản ghi refill, cập nhật tồn kho và audit log.
- **Ngoại lệ:** Thiếu dữ liệu bắt buộc, máy/slot không hợp lệ hoặc không đủ quyền thì từ chối và không cập nhật tồn kho.
- **Tham chiếu:** SRS-INV-002, SRS-AUDIT-001.

### UC-14 — Monitor Device Status

- **Actor chính:** Operations Manager, Technician. **Actor hỗ trợ:** Device / Gateway.
- **Mục tiêu:** Theo dõi trạng thái kết nối và telemetry của máy.
- **Điều kiện trước:** Thiết bị được đăng ký và người xem có quyền trong phạm vi tương ứng.
- **Luồng chính:** 1) Thiết bị gửi heartbeat/telemetry; 2) backend xác minh thiết bị và lưu trạng thái; 3) dashboard hiển thị trạng thái hiện tại, thời điểm liên lạc gần nhất và sự kiện liên quan; 4) thiết bị quá hạn heartbeat được đánh dấu offline theo cấu hình.
- **Ngoại lệ:** Thiết bị không hợp lệ hoặc payload sai bị từ chối và ghi log; mất kết nối được thể hiện là offline, không suy đoán là hoạt động bình thường.
- **Tham chiếu:** SRS-IOT-001, SRS-IOT-002, SRS-MACH-001.

### UC-15 — Manage Alerts

- **Actor chính:** Operations Manager, Technician, Inventory Staff.
- **Mục tiêu:** Xem và cập nhật xử lý cảnh báo vận hành.
- **Điều kiện trước:** Cảnh báo đã được hệ thống tạo; actor đã đăng nhập và có quyền xử lý.
- **Luồng chính:** 1) Hệ thống tạo/cập nhật cảnh báo từ điều kiện vận hành; 2) người dùng xem danh sách và chi tiết; 3) người có quyền cập nhật trạng thái, ghi chú xử lý; 4) hệ thống lưu lịch sử.
- **Ngoại lệ:** Actor không đủ quyền hoặc cảnh báo không tồn tại thì từ chối cập nhật.
- **Tham chiếu:** SRS-OPS-001, SRS-INV-003, SRS-IOT-002.

### UC-16 — Manage Maintenance

- **Actor chính:** Operations Manager, Technician.
- **Mục tiêu:** Tạo, giao xử lý và hoàn tất phiếu bảo trì.
- **Điều kiện trước:** Actor đã đăng nhập và có quyền với máy.
- **Luồng chính:** 1) Operations Manager tạo phiếu từ máy/cảnh báo và giao người phụ trách; 2) máy có thể được chuyển sang chế độ bảo trì; 3) Technician kiểm tra, ghi nhận kết quả và biện pháp khắc phục; 4) người có quyền xác nhận checklist; 5) hệ thống lưu lịch sử và audit log, sau đó mới cho máy hoạt động lại khi đủ điều kiện.
- **Ngoại lệ:** Máy đang bảo trì không nhận đơn mới; thiếu bước kiểm tra bắt buộc thì không được đưa máy về hoạt động.
- **Tham chiếu:** SRS-MAINT-001, BR-015.

### UC-17 — View Dashboard and Reports

- **Actor chính:** PSA, BA, OM.
- **Mục tiêu:** Xem tổng quan vận hành trong phạm vi dữ liệu được cấp.
- **Điều kiện trước:** Actor đã đăng nhập và được phép xem báo cáo.
- **Luồng chính:** 1) Actor mở dashboard; 2) backend xác định phạm vi theo vai trò/thương hiệu; 3) hệ thống tổng hợp máy online/offline, giao dịch, dispensing, cảnh báo và bảo trì; 4) giao diện hiển thị số liệu và bộ lọc được hỗ trợ.
- **Ngoại lệ:** Lỗi truy xuất dữ liệu được thông báo rõ; không trả dữ liệu ngoài phạm vi thương hiệu.
- **Tham chiếu:** SRS-REPORT-001, SRS-AUTH-002, BR-013.

## Ghi chú phạm vi và quan hệ

- UC-04 bao gồm bước UC-05 xác minh kết quả thanh toán.
- UC-06 chỉ được thực hiện sau khi thanh toán đã được xác minh; điều kiện này là tiền đề nghiệp vụ, không phải một lệnh xịt tự động được kích hoạt bởi việc tạo đơn.
- Việc gán actor trong sơ đồ thể hiện tương tác chức năng, không thay thế ma trận quyền chi tiết hoặc kiểm tra quyền ở backend.
- Mã use case UC-01–UC-17 được dùng nhất quán giữa tài liệu mô tả và sơ đồ tham khảo.