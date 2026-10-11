# Quy chuẩn làm việc nhóm ScentStation

**Dự án:** ScentStation — A Multi-Brand IoT Perfume Experience and Management Platform  
**Phiên bản:** 1.0  
**Trạng thái:** Bản đề xuất để nhóm thống nhất  
**Quy mô:** 7 thành viên  
**Kế hoạch:** 7 Sprint, dự kiến mỗi Sprint 1 tuần

Tài liệu này quy định cách nhóm giao việc, tổ chức thư mục, đặt tên branch/commit, nộp sản phẩm, review và cập nhật tiến độ. Tất cả thành viên cần đọc và tuân thủ sau khi nhóm thống nhất tài liệu.

---

## 1. Nguyên tắc chung

1. Mọi công việc cần được ghi nhận bằng GitHub Issue và đưa vào GitHub Project.
2. Mỗi Issue phải nêu mục tiêu, đầu việc, đầu ra cần nộp và tiêu chí nghiệm thu.
3. Không làm trực tiếp trên `main` đối với công việc thông thường. Tạo branch riêng và gửi Pull Request (PR).
4. Không chuyển nhiệm vụ sang `Done` chỉ vì đã push; cần đạt tiêu chí nghiệm thu và được review.
5. Không commit mật khẩu, token, API key, dữ liệu nhạy cảm hoặc file `.env` chứa bí mật.
6. Khi thay đổi yêu cầu, API, schema dữ liệu hoặc cách chạy, cập nhật tài liệu liên quan.

## 2. Công cụ quản lý

| Công cụ | Mục đích | Quy định |
|---|---|---|
| GitHub Repository | Lưu source code và tài liệu | Lưu file thuộc dự án; không lưu file build, thư viện sinh tự động hoặc bí mật |
| GitHub Issues | Giao nhiệm vụ | Mỗi nhiệm vụ có mã TASK, người phụ trách và tiêu chí nghiệm thu |
| GitHub Projects | Theo dõi trạng thái, người phụ trách, ưu tiên | Cập nhật trạng thái khi công việc thay đổi |
| GitHub Milestones | Gom Issue theo Sprint | Gắn mỗi Issue vào Sprint dự kiến hoàn thành |
| Branches | Tách biệt công việc | Mỗi nhiệm vụ tạo branch riêng |
| Pull Requests | Nộp kết quả và review | Thay đổi vào `main` phải qua PR, trừ ngoại lệ được nhóm thống nhất |

### Trạng thái công việc

- **Todo:** đã ghi nhận, chưa bắt đầu.
- **In Progress:** đang thực hiện.
- **Review:** đã gửi PR hoặc chờ kiểm tra.
- **Done:** đã review, merge và đạt tiêu chí nghiệm thu.

Nếu bị chặn, người phụ trách phải thông báo trong Issue, nêu lý do và hỗ trợ cần thiết.

## 3. Quy ước mã định danh

| Loại | Mẫu | Ví dụ | Sử dụng |
|---|---|---|---|
| Task | `TASK-xxx` | `TASK-001` | Mã nhiệm vụ do nhóm quy ước |
| Functional Requirement | `FR-xxx` | `FR-001` | Yêu cầu chức năng |
| Non-functional Requirement | `NFR-xxx` | `NFR-001` | Yêu cầu phi chức năng |
| Business Rule | `BR-xxx` | `BR-001` | Quy tắc nghiệp vụ |
| Test Case | `TC-xxx` | `TC-001` | Trường hợp kiểm thử |

`TASK-001` là mã do nhóm quy ước, còn `#1` là số Issue GitHub tự cấp; hai mã không nhất thiết giống nhau. Không tái sử dụng mã đã cấp.

## 4. Quy ước đặt tên branch

Mẫu:

```text
<type>/task-<id>-<short-description>
```

| Loại | Dùng cho | Ví dụ |
|---|---|---|
| `docs/` | Tài liệu, sơ đồ, đặc tả | `docs/task-001-srs` |
| `feat/` | Chức năng mới | `feat/task-010-user-auth` |
| `fix/` | Sửa lỗi | `fix/task-025-payment-validation` |
| `test/` | Kiểm thử | `test/task-030-order-flow` |
| `refactor/` | Tái cấu trúc | `refactor/task-040-order-service` |
| `chore/` | Cấu hình, bảo trì | `chore/task-002-repo-setup` |

Tên branch dùng chữ thường, không dấu, không khoảng trắng; các từ cách nhau bằng dấu gạch ngang. Mỗi branch nên phục vụ một Issue chính.

Ví dụ tạo branch mới từ `main` đã cập nhật:

```bash
git switch main
git pull origin main
git switch -c docs/task-001-srs
```

## 5. Quy ước commit

Mẫu:

```text
<type>(TASK-xxx): <mô tả ngắn>
```

| Type | Ý nghĩa | Ví dụ |
|---|---|---|
| `docs` | Tài liệu | `docs(TASK-001): complete ScentStation SRS` |
| `feat` | Chức năng mới | `feat(TASK-010): add user authentication` |
| `fix` | Sửa lỗi | `fix(TASK-025): validate payment callback` |
| `test` | Kiểm thử | `test(TASK-030): cover order state transitions` |
| `refactor` | Tái cấu trúc | `refactor(TASK-040): separate order service logic` |
| `chore` | Cấu hình/bảo trì | `chore(TASK-002): add repository guidelines` |

Mỗi commit nên chứa một thay đổi có ý nghĩa. Dùng mô tả ngắn, rõ ràng và nhất quán. Có thể thêm `(refs #1)` để tham chiếu Issue, ví dụ:

```bash
git add docs/software-requirement-specification.md
git commit -m "docs(TASK-001): update SRS (refs #1)"
```

Chỉ dùng `Closes #1` trong mô tả PR khi PR thực sự hoàn tất Issue đó.

## 6. Quy trình thực hiện nhiệm vụ

1. Đọc Issue: mục tiêu, phạm vi, đầu ra, tiêu chí nghiệm thu, Assignee, Priority và Milestone.
2. Làm rõ yêu cầu trong Issue trước khi tự giả định.
3. Cập nhật `main`, sau đó tạo branch riêng.
4. Thực hiện thay đổi trong phạm vi nhiệm vụ.
5. Tự kiểm tra tài liệu/code và chạy test phù hợp.
6. Commit theo quy ước.
7. Push branch lên GitHub.
8. Mở PR, liên kết Issue, mô tả thay đổi và cách kiểm tra.
9. Xử lý phản hồi review. (nhóm trưởng hoặc người được phân công thực hiện)
10. Sau khi merge, cập nhật Issue và Project; chuyển sang `Done` khi đạt tiêu chí nghiệm thu.(nhóm trưởng hoặc người được phân công thực hiện)

Lệnh mẫu:

```bash
git status
git add <file-hoac-thu-muc>
git commit -m "docs(TASK-001): update SRS"
git push -u origin docs/task-001-srs
```

Không dùng `git push --force` lên `main`. Nếu push bị từ chối hoặc có xung đột, xử lý nguyên nhân trước.

## 7. Quy định Pull Request

Mỗi PR cần có:
- Tiêu đề theo mẫu `[TASK-xxx] Mô tả ngắn`.
- Liên kết Issue liên quan.
- Tóm tắt nội dung thay đổi và file/module bị ảnh hưởng.
- Hướng dẫn kiểm tra hoặc lệnh test đã chạy.
- Ảnh chụp, kết quả test hoặc liên kết tài liệu khi phù hợp.
- Những việc chưa hoàn thành hoặc rủi ro còn lại.

### Checklist trước khi gửi PR

- [ ] PR liên kết đúng Issue.
- [ ] Thay đổi nằm trong phạm vi nhiệm vụ.
- [ ] Không chứa mật khẩu, token, API key hoặc dữ liệu nhạy cảm.
- [ ] Đã tự kiểm tra.
- [ ] Đã cập nhật tài liệu liên quan.
- [ ] Không có file build, file tạm hoặc file cá nhân không cần thiết.
- [ ] Có bằng chứng kiểm tra phù hợp.

Người thực hiện không nên tự duyệt thay reviewer khác.
Chỉ người được phân công mới có quyền merge và số reviewer tối thiểu trước khi phát triển chính thức.

## 8. Cấu trúc thư mục và nơi nộp sản phẩm

Cấu trúc đích được đề xuất; chỉ tạo thư mục khi cần.

```text
ProjectJava/
├── README.md
├── CONTRIBUTING.md
├── .gitignore
├── .github/
│   ├── ISSUE_TEMPLATE/
│   └── PULL_REQUEST_TEMPLATE.md
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
    └── README.md
```

| Đầu ra | Nơi nộp |
|---|---|
| Brief, SRS, đặc tả | `docs/` |
| Use Case, Activity, Sequence, ERD, kiến trúc | `docs/diagrams/` |
| Thiết kế database, schema | `docs/database/` |
| API specification, request/response mẫu | `docs/api/` |
| Báo cáo test, bằng chứng kiểm thử | `docs/test-reports/` |
| Biên bản họp, quyết định nhóm | `docs/meeting-notes/` |
| Backend | `backend/` |
| Frontend | `frontend/` |
| Code thiết bị, gateway hoặc simulator IoT | `iot/` |

Đặt tên file mô tả nội dung, dùng chữ thường và dấu gạch ngang khi phù hợp. Tránh tên mơ hồ như `final-final-v3`. Nếu có file nguồn sơ đồ chỉnh sửa được, lưu cùng ảnh xuất khi phù hợp. Cập nhật tài liệu này nếu nhóm thống nhất cấu trúc khác.

## 9. Yêu cầu tối thiểu cho sản phẩm

### Tài liệu
- Có tiêu đề và trạng thái/phiên bản khi phù hợp.
- Dùng thuật ngữ thống nhất với SRS.
- Có mã tham chiếu khi liên quan đến yêu cầu hoặc nhiệm vụ.
- Liên kết nội bộ hoạt động.

### Code
- Tuân theo quy ước của ngôn ngữ/framework mà nhóm thống nhất.
- Có hướng dẫn cấu hình và chạy trong README của module khi cần.
- Không commit file build, thư viện sinh tự động hoặc bí mật.
- Có kiểm thử phù hợp với rủi ro của thay đổi.

### Kiểm thử
- Ghi điều kiện đầu vào, bước thực hiện và kết quả mong đợi.
- Ghi kết quả thực tế, trạng thái đạt/không đạt và lỗi phát hiện.
- Với luồng quan trọng, lưu bằng chứng và liên kết Issue liên quan.

## 10. Quy tắc nghiệp vụ quan trọng của ScentStation

Các thay đổi phải tuân thủ yêu cầu đã thống nhất trong SRS. Đặc biệt:
- Một đơn hàng đã thanh toán không được dẫn tới nhiều hơn một lần phun thành công.
- Chỉ backend xác nhận thanh toán thành công theo luồng đã thiết kế.
- Không phun khi máy đang bảo trì, cửa mở, khe hết nước hoa hoặc có lỗi nghiêm trọng.
- Lệnh phun phải được xác thực, chống lặp và hướng tới đúng máy/khe.
- Nếu kết quả phun không chắc chắn, ghi nhận để xử lý/đối soát; không tự động phun lần hai.
- Chỉ cập nhật tồn kho theo kết quả phun được xác nhận theo quy tắc hệ thống.

Thay đổi ảnh hưởng các quy tắc này cần cập nhật SRS, kiểm thử liên quan và được review trước khi merge.

## 11. Giao tiếp và xử lý vướng mắc

- Trao đổi về nhiệm vụ nên ghi trong Issue/PR để giữ lịch sử.
- Khi bị chặn, thông báo sớm vấn đề, việc đã thử, thông tin lỗi và hỗ trợ cần thiết.
- Không tự thay đổi API, schema dữ liệu hoặc quy tắc nghiệp vụ ảnh hưởng module khác mà không trao đổi với người liên quan.
- Ghi lại quyết định ảnh hưởng kiến trúc, công nghệ hoặc phạm vi trong tài liệu/Issue.
- Không đưa thông tin đăng nhập, token hoặc dữ liệu nhạy cảm vào Issue, commit, log hay ảnh chụp.

## 12. Definition of Done (DoD)

Một nhiệm vụ chỉ được xem là hoàn thành khi các điều kiện phù hợp đã đạt:

- [ ] Hoàn thành đầu việc trong Issue.
- [ ] Đầu ra nằm đúng thư mục và có thể truy cập.
- [ ] Đạt tiêu chí nghiệm thu.
- [ ] Đã tự kiểm tra hoặc chạy test phù hợp.
- [ ] Tài liệu liên quan đã cập nhật.
- [ ] PR được review và merge vào `main`.
- [ ] Issue và Project được cập nhật.
- [ ] Không còn lỗi nghiêm trọng hoặc bí mật trong repository.

Nếu một tiêu chí không áp dụng, ghi rõ lý do trong Issue/PR.

## 13. Nội dung nhóm cần xác nhận

13.1. Phương pháp phát triển

Dự án sử dụng Lightweight Scrum, tổ chức theo 7 Sprint đã lập lịch. GitHub Milestones đại diện cho Sprint; GitHub Projects dùng để theo dõi trạng thái, người phụ trách và Priority. Các thay đổi yêu cầu phải được ghi nhận trong Issue và tài liệu liên quan.

13.2. Công nghệ

Backend: Java 21 LTS, Spring Boot 3.5.x, Maven 3.9.x.

Frontend: React, TypeScript, Vite, Node.js 24 LTS.

Database: MySQL 8.4 LTS.

IoT: MQTT; sử dụng simulator trước và ESP32 khi có thiết bị.

API: REST/JSON, prefix /api/v1.

Backend testing: JUnit 5, Mockito, Spring Boot Test.

API testing: Postman.

Frontend testing: Vitest và React Testing Library.

IoT testing: MQTT Explorer và simulator.

Phiên bản vá cụ thể phải được ghi trong file cấu hình dự án và không được tự ý thay đổi nếu chưa thống nhất.

13.3. Quy ước đặt tên

Java package: chữ thường, theo tên miền đảo ngược, ví dụ com.scentstation.product.

Java class: PascalCase, ví dụ ProductController.

Biến và method: camelCase, ví dụ productId, getProductById.

REST endpoint: danh từ số nhiều, chữ thường; dùng HTTP method phù hợp.

Database: bảng và cột dùng snake_case chữ thường, ví dụ payment_transactions, created_at.

Mã nhiệm vụ: TASK-xxx; mã yêu cầu chức năng: FR-xxx; mã yêu cầu phi chức năng: NFR-xxx; mã quy tắc nghiệp vụ: BR-xxx; mã test: TC-xxx.

13.4. Review và merge

Người review và người có quyền merge là nhóm trưởng hoặc thành viên được nhóm trưởng phân công. Người thực hiện không tự phê duyệt PR của mình. Mọi thay đổi thông thường vào main phải qua Pull Request, được review và đạt tiêu chí nghiệm thu.

13.5. Kiểm thử và nghiệm thu

Mỗi Issue phải có tiêu chí nghiệm thu phù hợp. Người thực hiện phải tự kiểm tra, chạy test phù hợp, cập nhật tài liệu liên quan và đính kèm bằng chứng khi cần. Chỉ chuyển Issue sang Done sau khi đầu ra được review, merge và đáp ứng tiêu chí nghiệm thu.

Các luồng thanh toán, dispensing và tồn kho phải kiểm thử trường hợp lỗi, xử lý trùng và kết quả không xác định; không chỉ kiểm thử luồng thành công.