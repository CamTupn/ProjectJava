# Frontend — ScentStation

**Vai trò:** Giao diện kiosk cho khách hàng và cổng quản trị/vận hành.

**Công nghệ dự kiến:** React, TypeScript, Vite, Node.js 24 LTS, Vitest, React Testing Library.

## Cấu trúc mục tiêu sau khi khởi tạo
```text
frontend/
├── package.json
├── package-lock.json
├── index.html
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── app/
    ├── assets/
    ├── components/
    ├── features/
    │   ├── auth/
    │   ├── brands/
    │   ├── products/
    │   ├── kiosk/
    │   ├── orders/
    │   ├── machines/
    │   ├── inventory/
    │   └── maintenance/
    ├── layouts/
    ├── lib/
    ├── services/
    ├── types/
    └── tests/
```

Đây là cấu trúc mục tiêu. Chưa tạo hàng loạt file rỗng; khởi tạo bằng Vite khi nhóm sẵn sàng. Không commit `node_modules/` hoặc `dist/`.
