# 🏢 Recruitment Management System (ATS)

> **Language Navigation / Chuyển đổi ngôn ngữ:**  
> 🇻🇳 [Tiếng Việt](#-hệ-thống-quản-lý-tuyển-dụng-ats---tiếng-việt) | 🇬🇧 [English](#-recruitment-management-system-ats---english)

---

# 🇻🇳 Hệ Thống Quản Lý Tuyển Dụng (ATS) - Tiếng Việt

Một hệ thống Quản lý Tuyển dụng (Applicant Tracking System - ATS) full-stack cấp sản phẩm (production-grade). Repository này chứa cả backend REST API (Java Spring Boot) và frontend SPA (React). Hệ thống được thiết kế để tối ưu hóa toàn bộ quy trình tuyển dụng—từ quản lý công ty và tin tuyển dụng đến sàng lọc hồ sơ ứng viên, xử lý file CV, theo dõi đơn ứng tuyển, xếp lịch phỏng vấn và quản lý pipeline tuyển dụng trực quan.

---

## 📖 Tổng Quan Dự Án

### Bài Toán Kinh Doanh
Các đội ngũ tuyển dụng thường gặp khó khăn do quy trình phân tán:
- CV bị thất lạc trong các luồng email cá nhân hoặc nhóm.
- Không có một nguồn dữ liệu tập trung (single source of truth) cho pipeline tuyển dụng.
- Điều phối lịch phỏng vấn và tổng hợp phản hồi thủ công, tốn thời gian.
- Khó khăn trong việc tuân thủ và theo dõi vết hoạt động do thiếu hệ thống audit log chi tiết.

### Giải Pháp
**Hệ Thống Quản Lý Tuyển Dụng (ATS)** cung cấp một nền tảng tập trung, bảo mật và áp dụng chặt chẽ các quy tắc nghiệp vụ ở từng bước:
- **Recruiter & Hiring Manager**: Đăng tin tuyển dụng, theo dõi trạng thái ứng viên qua bảng Kanban tương tác, xem/tải CV và xếp lịch phỏng vấn.
- **Administrator**: Quản lý toàn bộ người dùng, vai trò (role), phân quyền (permission) và cấu hình hệ thống.

---

## 🚀 Tính Năng Nổi Bật

### 🔐 Xác Thực & Bảo Mật
- **Quản lý phân quyền theo vai trò (RBAC)**: Phân quyền truy cập an toàn cho các vai trò `ADMIN` và `RECRUITER`.
- **Phiên làm việc JWT (Stateless)**: Cấp phát Access Token và cơ chế xoay vòng Refresh Token bảo mật.
- **Mã hóa mật khẩu**: Sử dụng thuật toán BCrypt cho tất cả tài khoản người dùng.

### 🏢 Quản Lý Công Ty
- Quản lý thông tin hồ sơ các công ty đối tác.
- Bảo vệ toàn vẹn dữ liệu: Ngăn xóa mềm (soft-delete) công ty đang có tin tuyển dụng hoạt động.

### 💼 Tin Tuyển Dụng
- Tạo, xuất bản, cập nhật và đóng các tin tuyển dụng.
- Ràng buộc & xác thực dữ liệu: Kiểm tra dải lương hợp lệ, giới hạn hạn nộp hồ sơ và quản lý trạng thái.

### 👤 Hồ Sơ Ứng Viên & Tải CV
- Quản lý thông tin nhân khẩu học, kinh nghiệm làm việc và trình độ học vấn.
- Tải CV trực tiếp (định dạng PDF/DOC/DOCX tối đa 5MB) với cơ chế chuẩn hóa tên file an toàn và endpoint truy xuất bảo mật.

### 📋 Đơn Ứng Tuyển
- Nộp hồ sơ ứng viên trực tiếp vào các tin tuyển dụng đang mở.
- Chuyển đổi trạng thái tuyến tính chặt chẽ: `APPLIED` ➔ `REVIEWING` ➔ `INTERVIEW` ➔ `OFFER` ➔ `HIRED` / `REJECTED`.

### 📅 Điều Phối Phỏng Vấn
- Đặt lịch phỏng vấn `VIDEO_CALL` (yêu cầu xác thực đường dẫn online) hoặc `ONSITE` (yêu cầu chi tiết địa điểm/phòng họp).
- Tự động cập nhật trạng thái đơn ứng tuyển và ghi nhận ghi chú phỏng vấn.

### 📊 Bảng Tuyển Dụng Kanban
- Trực quan hóa tiến trình tuyển dụng với thao tác kéo thả (drag-and-drop) hoặc chọn dropdown.
- Bộ lọc tương tác theo Công ty và Tin tuyển dụng.

### 📈 Bảng Thống Kê (Metrics Dashboard)
- Các chỉ số KPI thời gian thực hiển thị số vị trí đang tuyển, số lượng ứng viên theo từng giai đoạn, tỷ lệ tải CV và lịch sử hoạt động gần đây.

---

## 🔑 Tài Khoản Thử Nghiệm Mặc Định

Hệ thống tự động khởi tạo sẵn các tài khoản dùng thử khi khởi chạy lần đầu:

| Vai Trò (Role) | Email | Mật Khẩu (Password) | Quyền Hạn (Permissions) |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@example.com` | `password` | Toàn quyền hệ thống (Quản lý tài khoản người dùng, phân quyền, cấu hình & nghiệp vụ tuyển dụng) |
| **Recruiter** | `recruiter@example.com` | `password` | Quyền nhà tuyển dụng (Quản lý công ty, đăng tin tuyển dụng, duyệt CV, xếp lịch phỏng vấn, Kanban) |

---

## 🛠️ Công Nghệ Sử Dụng

### Backend
- **Core**: Java 21, Spring Boot 3.3.2
- **Security**: Spring Security, JJWT (JSON Web Token)
- **Database**: PostgreSQL 16
- **Migration**: Flyway DB
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Mapping & Utilities**: MapStruct, Lombok
- **Testing**: JUnit 5, Mockito, MockMvc, Testcontainers

### Frontend
- **Core**: React 19, Vite, React Router 6
- **Styling**: Tailwind CSS 3
- **Icons**: Lucide React
- **Forms & Alerts**: React Hook Form, React Hot Toast
- **HTTP Client**: Axios (đã cấu hình interceptor tự động chèn JWT và xử lý điều hướng khi gặp lỗi 401)

---

## 🏗️ Kiến Trúc Hệ Thống

Hệ thống sử dụng kiến trúc phân tầng rõ ràng (layered architecture) để tách biệt các trách nhiệm:

```
                  ┌───────────────────────────────┐
                  │      React SPA Frontend       │
                  └───────────────┬───────────────┘
                                  │
                                  ▼  [JSON over HTTP / Multipart]
                  ┌───────────────────────────────┐
                  │     Spring RestControllers    │ (JWT & Validation)
                  └───────────────┬───────────────┘
                                  │
                                  ▼
                  ┌───────────────────────────────┐
                  │         Service Layer         │ (Business Logic & States)
                  └───────────────┬───────────────┘
                                  │
                                  ▼
                  ┌───────────────────────────────┐
                  │       Spring Data JPA         │ (Repositories)
                  └───────────────┬───────────────┘
                                  │
                                  ▼  [SQL Queries]
                  ┌───────────────────────────────┐
                  │      PostgreSQL Database      │
                  └───────────────────────────────┘
```

---

## 📂 Cấu Trúc Thư Mục

### Cấu Trúc Backend
```
src/
├── main/
│   ├── java/com/example/ats/
│   │   ├── AtsApplication.java       # Khởi chạy ứng dụng
│   │   ├── config/                   # Cấu hình Security, CORS, Auditing, JWT Filters
│   │   ├── controller/               # API Controllers (routing, versioning, Swagger docs)
│   │   ├── dto/                      # Data Transfer Objects (Requests/Responses)
│   │   ├── entity/                   # Ánh xạ Entity Cơ sở dữ liệu JPA
│   │   ├── enums/                    # Danh mục Enums (Role, JobStatus, Status)
│   │   ├── exception/                # Custom Exceptions & GlobalExceptionAdvice
│   │   ├── repository/               # Interfaces Spring Data JPA Repository
│   │   ├── service/                  # Service interfaces và implementations
│   │   ├── util/                     # Hàm tiện ích Security, JWT và Upload file
│   │   └── validation/               # Custom JSR validations (Khoảng ngày, giới hạn lương)
│   └── resources/
│       ├── db/migration/             # Scripts SQL migration cho Flyway
│       └── application.yml           # Cấu hình ứng dụng theo profile
└── test/                             # Unit tests & MockMvc integration tests
```

### Cấu Trúc Frontend
```
frontend/
├── src/
│   ├── components/
│   │   ├── common/                   # ProtectedRoute, điều hướng, layouts
│   │   └── ui/                       # Reusable elements, badges, modals, tables
│   ├── context/                      # AuthContext quản lý session và JWT người dùng
│   ├── pages/
│   │   ├── auth/                     # Giao diện Đăng nhập
│   │   ├── company/                  # Danh sách, chi tiết và chỉnh sửa Công ty
│   │   ├── jobs/                     # Tạo vị trí tuyển dụng và bộ lọc
│   │   ├── candidates/               # Tải CV và xem chi tiết ứng viên
│   │   ├── applications/             # Form nộp đơn ứng tuyển
│   │   ├── interviews/               # Điều phối phỏng vấn
│   │   ├── pipeline/                 # Các component bảng Kanban
│   │   ├── Profile.jsx               # Quản lý hồ sơ cá nhân nhà tuyển dụng
│   │   └── NotFound.jsx              # Trang lỗi 404
│   ├── routes/                       # React Router AppRoutes cấu hình lazy loading
│   ├── services/                     # Cấu hình Axios client với JWT interceptors
│   ├── index.css                     # Tailwind bindings & fonts
│   └── main.jsx                      # Entry point của ứng dụng SPA
```

---

## ⚙️ Biến Môi Trường (Environment Variables)

Sao chép mẫu từ `.env.example` để tạo file `.env` tại thư mục gốc:

| Biến | Mô Tả | Giá Trị Mặc Định (Dev) |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Spring profile đang hoạt động (`dev` hoặc `prod`) | `dev` |
| `DB_HOST` | Địa chỉ máy chủ Cơ sở dữ liệu | `localhost` |
| `DB_PORT` | Cổng kết nối PostgreSQL | `5432` |
| `DB_NAME` | Tên cơ sở dữ liệu | `ats_db` |
| `DB_USERNAME` | Tài khoản kết nối CSDL | `ats_user` |
| `DB_PASSWORD` | Mật khẩu kết nối CSDL | `your_secure_database_password_here` |
| `JWT_SECRET` | Khóa mã hóa 256-bit để tạo chữ ký JWT | *tạo bằng OpenSSL* |
| `JWT_ACCESS_EXPIRY_MS` | Thời gian sống của Access token (ms) | `3600000` (1 Giờ) |
| `JWT_REFRESH_EXPIRY_MS`| Thời gian sống của Refresh token (ms) | `604800000` (7 Ngày) |
| `SERVER_PORT` | Cổng chạy dịch vụ Backend | `8080` |
| `FILE_UPLOAD_DIR` | Thư mục lưu trữ file CV tải lên | `uploads/resumes` |
| `CORS_ALLOWED_ORIGINS` | Danh sách origin được phép truy cập API | `http://localhost:3000,http://localhost:5173` |

---

## 💻 Hướng Dẫn Cài Đặt & Chạy Cục Bộ

### Yêu Cầu Tiên Quyết
- Java 21 JDK (Amazon Corretto, Temurin, ...)
- Node.js (v18 trở lên)
- PostgreSQL (hoặc chạy qua Docker)

### Bước 1: Clone Repository & Cấu Hình Môi Trường
```bash
git clone https://github.com/your-username/recruitment-ats.git
cd recruitment-ats
cp .env.example .env
```

### Bước 2: Khởi Tạo Cơ Sở Dữ Liệu
Đảm bảo PostgreSQL đang chạy và tạo sẵn cơ sở dữ liệu có tên `ats_db`.

### Bước 3: Khởi Chạy Ứng Dụng

#### Khởi chạy nhanh (Script `.bat` trên Windows):
Kích đúp vào file `run.bat` hoặc chạy trong CMD:
```cmd
run.bat
```
Chọn **Option 1** để tự động khởi chạy cả Backend và Frontend trong 2 cửa sổ riêng biệt.

#### Khởi chạy thủ công:

##### Backend
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```
Máy chủ backend sẽ chạy tại: `http://localhost:8080`.  
Tài liệu OpenAPI Swagger UI truy cập tại: `http://localhost:8080/swagger-ui/index.html`.

##### Frontend
Mở một cửa sổ terminal mới tại thư mục gốc dự án:
```bash
cd frontend
npm install
npm run dev
```
Ứng dụng frontend sẽ chạy tại: `http://localhost:5173`.

---

## 🐳 Hướng Dẫn Triển Khai Với Docker

Để triển khai toàn bộ môi trường (Spring Boot REST App, PostgreSQL database, và local volume) bằng Docker Compose:

1. Cấu hình giá trị biến môi trường trong file `.env`. Đảm bảo `DB_HOST=postgres` trong môi trường Docker.
2. Build và khởi chạy:
   ```bash
   docker compose up -d --build
   ```
3. Kiểm tra trạng thái các container:
   ```bash
   docker compose ps
   ```
4. Dừng toàn bộ hệ thống:
   ```bash
   docker compose down -v
   ```

---

## 🧪 Hướng Dẫn Chạy Kiểm Thử (Testing)

Để thực thi bộ kiểm thử:

```bash
# Chạy Unit Tests
./mvnw test -Dtest=*Test

# Chạy Integration Tests
./mvnw test -Dtest=*IntegrationTests
```

---

## 🖼️ Hình Ảnh Giao Diện

Hình ảnh tham khảo của ATS Portal:

### 1. Bảng Pipeline Tuyển Dụng
*(Placeholder: Chèn hình ảnh bảng Kanban thể hiện thao tác kéo thả ứng viên qua từng giai đoạn tuyển dụng)*

### 2. Bảng Thống Kê Analytics
*(Placeholder: Chèn hình ảnh dashboard hiển thị các chỉ số công việc, ứng viên và phỏng vấn)*

### 3. Giao Diện API Swagger
*(Placeholder: Chèn hình ảnh giao diện tài liệu Swagger UI)*

---

## 🔮 Định Hướng Phát Triển

- **Resume Parsing**: Tích hợp các dịch vụ AI để tự động bóc tách thông tin ứng viên từ CV đã tải lên.
- **Dịch vụ thông báo (Notification Services)**: Tự động gửi email thông báo lịch phỏng vấn cho ứng viên và người phỏng vấn.
- **Audit Logs nâng cao**: Thống kê và phân tích chi tiết lịch sử thao tác tuyển dụng.

---

## 📄 Giấy Phép (License)

Dự án này được phân phối theo Giấy phép MIT - xem file `LICENSE` để biết thêm chi tiết.

---
---

# 🇬🇧 Recruitment Management System (ATS) - English

A production-grade, full-stack Applicant Tracking System (ATS) and Recruitment Management portal. This repository contains both the Java Spring Boot REST API backend and the React SPA frontend. The system is designed to streamline the entire recruitment lifecycle—from company and job management to candidate profile screening, CV document handling, application tracking, interview scheduling, and visual pipeline management.

---

## 📖 Project Overview

### The Business Problem
Recruitment teams often struggle with fragmented processes:
- Resumes lost in shared inbox threads.
- No single source of truth for the hiring pipeline.
- Manual interview coordination and feedback collation.
- Compliance and tracking issues due to a lack of detailed audit logging.

### The Solution
The **Recruitment Management System (ATS)** provides a centralized, secure platform enforcing business rules at every step:
- **Recruiters & Hiring Managers** can post job requirements, track applicant status via an interactive Kanban board, review candidate CVs, and coordinate interviews.
- **Administrators** have complete control over system users, roles, permissions, and system configurations.

---

## 🚀 Features

### 🔐 Authentication & Security
- **Role-Based Access Control (RBAC)**: Secure access mapped to `ADMIN` and `RECRUITER` roles.
- **Stateless JWT Sessions**: Access token issuance with secure rotation using refresh tokens.
- **Password Protection**: BCrypt hashing applied to all credential stores.

### 🏢 Company Management
- Profile tracking for partner organizations.
- Integrity protection: Prevents soft-deleting any company with active job openings.

### 💼 Job Postings
- Create, publish, update, and close job postings.
- Form validations including salary range sanity checks, deadline restrictions, and status controls.

### 👤 Candidate Profiles & CV Upload
- Manage applicant demographics, experience level, and educational background.
- Direct CV upload (PDF/DOC/DOCX up to 5MB) with safe file name serialization and secure retrieval endpoints.

### 📋 Job Applications
- Submit candidate profiles directly to active job openings.
- Strict linear status transitions: `APPLIED` ➔ `REVIEWING` ➔ `INTERVIEW` ➔ `OFFER` ➔ `HIRED` / `REJECTED`.

### 📅 Interview Coordination
- Schedule `VIDEO_CALL` (requires online link validation) or `ONSITE` meetings (requires office location details).
- Automatically updates application status and records notes.

### 📊 Kanban Recruitment Board
- Drag-and-drop or dropdown-based candidate state transition visualizer.
- Interactive filtering by Company and Job opening.

### 📈 Metrics Dashboard
- Real-time KPIs showing active openings, screening stage counts, resume upload ratios, and recent operations.

---

## 🔑 Default Test Accounts

The system automatically seeds default demo user accounts on initial startup:

| Role | Email | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin@example.com` | `password` | Full system access (User & role management, system configs & recruitment ops) |
| **Recruiter** | `recruiter@example.com` | `password` | Recruitment operations (Company & job management, CV screening, interview scheduling, Kanban) |

---

## 🛠️ Technology Stack

### Backend
- **Core**: Java 21, Spring Boot 3.3.2
- **Security**: Spring Security, JJWT (JSON Web Token)
- **Database**: PostgreSQL 16
- **Migration**: Flyway DB
- **Documentation**: SpringDoc OpenAPI (Swagger UI)
- **Mapping & Utilities**: MapStruct, Lombok
- **Testing**: JUnit 5, Mockito, MockMvc, Testcontainers

### Frontend
- **Core**: React 19, Vite, React Router 6
- **Styling**: Tailwind CSS 3
- **Icons**: Lucide React
- **Forms & Alerts**: React Hook Form, React Hot Toast
- **HTTP Client**: Axios (pre-configured interceptors for JWT injection and 401 automatic redirection)

---

## 🏗️ System Architecture

The project utilizes a clear layered architecture separating concerns:

```
                  ┌───────────────────────────────┐
                  │      React SPA Frontend       │
                  └───────────────┬───────────────┘
                                  │
                                  ▼  [JSON over HTTP / Multipart]
                  ┌───────────────────────────────┐
                  │     Spring RestControllers    │ (JWT & Validation)
                  └───────────────┬───────────────┘
                                  │
                                  ▼
                  ┌───────────────────────────────┐
                  │         Service Layer         │ (Business Logic & States)
                  └───────────────┬───────────────┘
                                  │
                                  ▼
                  ┌───────────────────────────────┐
                  │       Spring Data JPA         │ (Repositories)
                  └───────────────┬───────────────┘
                                  │
                                  ▼  [SQL Queries]
                  ┌───────────────────────────────┐
                  │      PostgreSQL Database      │
                  └───────────────────────────────┘
```

---

## 📂 Folders Structure

### Backend Structure
```
src/
├── main/
│   ├── java/com/example/ats/
│   │   ├── AtsApplication.java       # Application Bootstrapping
│   │   ├── config/                   # Security configs, CORS, Auditing, JWT Filters
│   │   ├── controller/               # API Controllers (routing, versioning, Swagger docs)
│   │   ├── dto/                      # Data Transfer Objects (Requests/Responses)
│   │   ├── entity/                   # JPA Database Entity mappings
│   │   ├── enums/                    # Application Enum collections (Role, JobStatus, Status)
│   │   ├── exception/                # Custom Exceptions & GlobalExceptionAdvice
│   │   ├── repository/               # Spring Data JPA Repository interfaces
│   │   ├── service/                  # Service interfaces and implementations
│   │   ├── util/                     # Security, JWT, and upload utility functions
│   │   └── validation/               # Custom JSR validations (Date ranges, salary limits)
│   └── resources/
│       ├── db/migration/             # Flyway SQL migration scripts
│       └── application.yml           # Configuration settings profiles
└── test/                             # Unit tests & MockMvc integrations
```

### Frontend Structure
```
frontend/
├── src/
│   ├── components/
│   │   ├── common/                   # ProtectedRoute, navigation, layouts
│   │   └── ui/                       # Reusable form elements, badges, modals, tables
│   ├── context/                      # AuthContext managing user sessions and JWT
│   ├── pages/
│   │   ├── auth/                     # Login templates
│   │   ├── company/                  # Company records list, details, and editor
│   │   ├── jobs/                     # Positions creation and filters
│   │   ├── candidates/               # Resumes download and details view
│   │   ├── applications/             # Job application submission forms
│   │   ├── interviews/               # Session schedulers
│   │   ├── pipeline/                 # Kanban boards components
│   │   ├── Profile.jsx               # Recruiter personal profile manager
│   │   └── NotFound.jsx              # 404 page handler
│   ├── routes/                       # React Router AppRoutes with lazy loading configuration
│   ├── services/                     # Axios clients configuration with JWT interceptors
│   ├── index.css                     # Tailwind bindings & fonts
│   └── main.jsx                      # Single Page Application entrance point
```

---

## ⚙️ Environment Variables

Copy the template from `.env.example` to create `.env` in the root folder:

| Variable | Description | Default Value (Dev) |
| :--- | :--- | :--- |
| `SPRING_PROFILES_ACTIVE` | Active Spring profile (`dev` or `prod`) | `dev` |
| `DB_HOST` | Database server address | `localhost` |
| `DB_PORT` | Port number of target PostgreSQL instance | `5432` |
| `DB_NAME` | Database catalog name | `ats_db` |
| `DB_USERNAME` | Database connection username | `ats_user` |
| `DB_PASSWORD` | Database user credential | `your_secure_database_password_here` |
| `JWT_SECRET` | 256-bit encryption secret for signature generation | *generate using OpenSSL* |
| `JWT_ACCESS_EXPIRY_MS` | Access token lifespan in milliseconds | `3600000` (1 Hour) |
| `JWT_REFRESH_EXPIRY_MS`| Refresh token lifespan in milliseconds | `604800000` (7 Days) |
| `SERVER_PORT` | Web service server execution port | `8080` |
| `FILE_UPLOAD_DIR` | Host location mapping for resume files | `uploads/resumes` |
| `CORS_ALLOWED_ORIGINS` | Permitted browser API consumer origins list | `http://localhost:3000,http://localhost:5173` |

---

## 💻 Installation & Local Run Guide

### Prerequisites
- Java 21 JDK (Amazon Corretto, Temurin, etc.)
- Node.js (v18 or higher)
- PostgreSQL (or run via Docker)

### Step 1: Clone and Set Environment
```bash
git clone https://github.com/your-username/recruitment-ats.git
cd recruitment-ats
cp .env.example .env
```

### Step 2: Database Setup
Make sure PostgreSQL is running, and create a database named `ats_db`.

### Step 3: Run the Project

#### Quick Launch (Windows `.bat` script):
Double-click `run.bat` or run in CMD:
```cmd
run.bat
```
Select **Option 1** to start both Backend and Frontend automatically in separate windows.

#### Manual Startup:

##### Backend
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```
The server starts on `http://localhost:8080`.  
The OpenAPI documentation is accessible at `http://localhost:8080/swagger-ui/index.html`.

##### Frontend
Open a new terminal window in the project workspace:
```bash
cd frontend
npm install
npm run dev
```
The application will launch on `http://localhost:5173`.

---

## 🐳 Docker Deployment Guide

To deploy the entire environment (Spring Boot REST App, PostgreSQL database instance, and local volumes) using Docker Compose:

1. Setup the environment values in `.env`. Ensure `DB_HOST=postgres` inside the Docker composition block environment.
2. Build and launch:
   ```bash
   docker compose up -d --build
   ```
3. Verify status checks:
   ```bash
   docker compose ps
   ```
4. Shutdown composition:
   ```bash
   docker compose down -v
   ```

---

## 🧪 Testing Guide

To run the test suite:

```bash
# Execute unit tests
./mvnw test -Dtest=*Test

# Execute integration tests
./mvnw test -Dtest=*IntegrationTests
```

---

## 🖼️ User Interface Screenshots

Visual references of the ATS Portal:

### 1. Recruitment Pipeline Board
*(Placeholder: Insert Kanban board screenshot showing the drag-and-drop recruitment pipeline stages)*

### 2. Analytics Dashboard
*(Placeholder: Insert dashboard screenshot showing aggregated job, candidate, and interview metrics)*

### 3. API Swagger Interface
*(Placeholder: Insert Swagger UI documentation screenshot)*

---

## 🔮 Future Enhancements
- **Resume Parsing**: Integration with AI services to extract candidate profile data from uploaded resumes automatically.
- **Notification Services**: Automating email notifications for interviews.
- **Audit Logs**: Deep analytics on historical screening actions.

---

## 📄 License

This project is licensed under the MIT License - see the `LICENSE` file for details.
