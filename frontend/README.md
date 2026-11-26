# ScentStation

**ScentStation** is an IoT-based perfume dispensing and management platform that allows customers to experience and purchase perfumes through smart dispensing machines.

The platform provides management features for Super Admins and Tenant Admins, including user, brand, location, product, machine, slot, order, and report management.

## 1. Project Overview

ScentStation aims to provide a convenient perfume shopping experience through smart IoT machines while allowing perfume brands to manage their business operations through a centralized platform.

### Main user roles

- **Super Admin:** Manages the overall system, tenants, users, locations, products, machines, slots, orders, and system settings.
- **Tenant Admin:** Manages the operations and resources belonging to their own perfume brand.
- **Customer:** Interacts with perfume dispensing machines to explore and purchase perfume products.

## 2. Technology Stack

### Frontend

- React
- TypeScript
- Vite
- Tailwind CSS
- React Router DOM
- Lucide React

### Backend

Backend development and API integration will be implemented in a subsequent development phase.

## 3. Project Structure

```text
ProjectJava/
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   └── tenant-admin/
│   │   ├── layouts/
│   │   ├── pages/
│   │   │   ├── admin/
│   │   │   │   ├── tenants/
│   │   │   │   ├── users/
│   │   │   │   ├── locations/
│   │   │   │   ├── products/
│   │   │   │   ├── machines/
│   │   │   │   ├── slots/
│   │   │   │   ├── orders/
│   │   │   │   └── settings/
│   │   │   └── tenant-admin/
│   │   │       ├── users/
│   │   │       ├── locations/
│   │   │       ├── products/
│   │   │       ├── machines/
│   │   │       ├── slots/
│   │   │       ├── orders/
│   │   │       ├── reports/
│   │   │       └── settings/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── package.json
│   └── vite.config.ts
│
└── README.md
```

## 4. Requirements

Make sure the following tools are installed:

- Node.js (recommended: Node.js 20 or later)
- npm
- Git
- Visual Studio Code (recommended)

Check the installed versions:

```bash
node -v
npm -v
git --version
```

## 5. Getting Started

Follow these steps to run the Frontend locally.

### Step 1: Clone the repository

```bash
git clone https://github.com/CamTupn/ProjectJava.git
```

### Step 2: Navigate to the Frontend directory

```bash
cd ProjectJava/frontend
```

### Step 3: Install dependencies

```bash
npm install
```

### Step 4: Start the development server

```bash
npm run dev
```

Vite will start the development server. Open the URL displayed in the terminal, usually:

```text
http://localhost:5173/
```

### Step 5: Access the application

The application currently provides the following routes:

| Module | URL |
|---|---|
| Super Admin Dashboard | http://localhost:5173/admin |
| Super Admin Tenants | http://localhost:5173/admin/tenants |
| Super Admin Users | http://localhost:5173/admin/users |
| Super Admin Locations | http://localhost:5173/admin/locations |
| Super Admin Products | http://localhost:5173/admin/products |
| Super Admin Machines | http://localhost:5173/admin/machines |
| Super Admin Slots | http://localhost:5173/admin/slots |
| Super Admin Orders | http://localhost:5173/admin/orders |
| Super Admin Settings | http://localhost:5173/admin/settings |
| Tenant Admin Dashboard | http://localhost:5173/tenant-admin/dashboard |
| Tenant Admin Users | http://localhost:5173/tenant-admin/users |
| Tenant Admin Locations | http://localhost:5173/tenant-admin/locations |
| Tenant Admin Products | http://localhost:5173/tenant-admin/products |
| Tenant Admin Machines | http://localhost:5173/tenant-admin/machines |
| Tenant Admin Slots | http://localhost:5173/tenant-admin/slots |
| Tenant Admin Orders | http://localhost:5173/tenant-admin/orders |
| Tenant Admin Reports | http://localhost:5173/tenant-admin/reports |
| Tenant Admin Settings | http://localhost:5173/tenant-admin/settings |

## 6. Build for Production

To build the Frontend application:

```bash
npm run build
```

To preview the production build locally:

```bash
npm run preview
```

## 7. Development Notes

- The current Frontend uses mock data for UI demonstration and interaction testing.
- Backend APIs have not yet been integrated.
- Data changes made in the current mock interface may not persist after refreshing the page.
- Authentication, authorization, and tenant-level data isolation will be implemented and enforced during Backend integration.
- Machine connection status and perfume slot inventory are currently simulated.

## 8. Git Workflow

We recommend using feature branches for development.

Create a new branch:

```bash
git checkout -b feature/your-task-name
```

Commit your changes:

```bash
git add .
git commit -m "feat: describe your changes"
```

Push your branch:

```bash
git push -u origin feature/your-task-name
```

After pushing, create a Pull Request on GitHub to merge your changes into the main development branch.

---

**Project:** ScentStation  
**Repository:** https://github.com/CamTupn/ProjectJava  
**Frontend Directory:** `frontend/`