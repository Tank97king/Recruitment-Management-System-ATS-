🌐 Hướng Dẫn Deploy Lấy Link Demo Đính Kèm CV (Recruitment ATS Project)
Để có một link demo online chạy thật (live demo) đưa vào CV, bạn cần deploy 3 thành phần của dự án:

Database (PostgreSQL): Cơ sở dữ liệu lưu trữ thông tin.
Backend (Spring Boot): Xử lý logic API.
Frontend (React + Vite): Giao diện người dùng.
Dưới đây là các phương án phổ biến nhất giúp bạn có link demo miễn phí hoặc chi phí cực thấp, kèm theo hướng dẫn từng bước.

🛠️ Phương Án 1: Deploy Miễn Phí 100% (Khuyên Dùng Cho CV)
Đây là cách phổ biến nhất cho các dự án portfolio cá nhân. Chúng ta sẽ kết hợp:

Database: Neon.tech (PostgreSQL Serverless miễn phí vĩnh viễn, không bị xóa sau 90 ngày như Render Free DB).
Backend: Render.com (Hosting Spring Boot miễn phí bằng Dockerfile có sẵn).
Frontend: Vercel (Hosting React miễn phí, tốc độ tải cực nhanh).
Bước 1: Deploy Database lên Neon.tech
Truy cập Neon.tech và đăng ký tài khoản (dùng Github).
Tạo một project mới (ví dụ đặt tên là ats-db).
Chọn database version là PostgreSQL 16.
Sau khi tạo xong, Neon sẽ cung cấp cho bạn một Connection String dạng: postgresql://ats_user:password@ep-cool-shadow-123456.us-east-2.aws.neon.tech/neondb?sslmode=require
Hãy tách chuỗi kết nối này thành các thông tin riêng để cấu hình biến môi trường sau này:
Host: ep-cool-shadow-123456.us-east-2.aws.neon.tech
Port: 5432
Database Name: neondb
Username: ats_user
Password: password
Bước 2: Push code lên GitHub
Đảm bảo toàn bộ mã nguồn của bạn đã được đẩy lên một repository Public (hoặc Private nhưng bạn cấp quyền được) trên GitHub cá nhân.
Kiểm tra xem file Dockerfile đã ở thư mục gốc của project (file này đã được tối ưu hóa Multi-stage build chạy Java 21).
Bước 3: Deploy Backend Spring Boot lên Render.com
Đăng ký tài khoản trên Render.com bằng tài khoản GitHub của bạn.
Nhấn nút New + ở góc trên cùng bên phải và chọn Web Service.
Chọn Repository chứa mã nguồn dự án ATS của bạn.
Cấu hình các thông số cơ bản:
Name: ats-backend (hoặc tên tùy chọn).
Region: Chọn khu vực gần Việt Nam nhất (ví dụ: Singapore hoặc Oregon để có đường truyền tốt).
Branch: main (hoặc nhánh chứa code chính của bạn).
Runtime: Chọn Docker (Render sẽ tự động đọc Dockerfile ở thư mục gốc để build ứng dụng Java 21).
Instance Type: Chọn Free ($0/tháng).
Cuộn xuống phần Environment Variables (Biến môi trường) và thêm các biến sau:
SPRING_PROFILES_ACTIVE = prod
DB_HOST = <Host từ Neon DB của bạn>
DB_PORT = 5432
DB_NAME = neondb
DB_USERNAME = <Username từ Neon DB>
DB_PASSWORD = <Password từ Neon DB>
JWT_SECRET = <Nhập 1 chuỗi ngẫu nhiên dài hơn 32 ký tự, ví dụ: mySuperSecureProductionSecretKeyForAtsSystem2026>
FILE_UPLOAD_DIR = /app/uploads/resumes
CORS_ALLOWED_ORIGINS = * (Hoặc sau khi deploy Frontend xong, bạn sửa lại thành URL của Frontend để bảo mật hơn)
Nhấn Deploy Web Service. Quá trình build và khởi chạy thông thường mất từ 5-10 phút.
Khi deploy thành công, bạn sẽ nhận được một đường link API từ Render (dạng https://ats-backend.onrender.com).
Lưu ý đặc biệt: Với gói Free của Render, nếu không có request nào gửi tới trong vòng 15 phút, app backend sẽ tự động đi vào trạng thái "ngủ" (Spin down). Khi có request mới truy cập, app sẽ mất khoảng 50 giây để khởi động lại. Hãy viết một ghi chú nhỏ này vào CV để nhà tuyển dụng không bất ngờ khi load lần đầu tiên!
Bước 4: Deploy Frontend React lên Vercel
Truy cập Vercel.com và đăng ký bằng GitHub.
Chọn Add New Project, sau đó Import repository dự án ATS của bạn.
Trong cấu hình dự án:
Root Directory: Click chọn và trỏ vào thư mục frontend (Thay vì deploy cả repo).
Framework Preset: Vercel sẽ tự động phát hiện ra Vite (React).
Build and Output Settings: Giữ mặc định (npm run build và thư mục output dist).
Cuộn xuống phần Environment Variables, thêm biến môi trường sau:
VITE_API_URL = <Link backend của bạn trên Render>/api/v1 (Ví dụ: https://ats-backend.onrender.com/api/v1)
Nhấn Deploy. Chỉ mất khoảng 1-2 phút là bạn sẽ có một đường dẫn frontend cực mượt (dạng https://ats-frontend.vercel.app).
Kiểm thử: Truy cập đường link Frontend trên Vercel, đăng nhập bằng tài khoản seed sẵn (admin@example.com / password) để kiểm tra xem hệ thống đã kết nối hoàn chỉnh chưa.
🐳 Phương Án 2: Deploy Bằng Docker Compose Lên VPS (Chuyên Nghiệp Hơn)
Nếu bạn muốn tạo ấn tượng cực mạnh trong mắt nhà tuyển dụng về kỹ năng DevOps / System Admin, hãy deploy toàn bộ dự án lên một máy chủ ảo (VPS) cá nhân.

Chi phí: Khoảng $4 - $6/tháng (Sử dụng Hetzner, DigitalOcean Droplet, Vultr hoặc OVH).
Ưu điểm: Ứng dụng chạy 24/7 cực kỳ mượt mà, không bao giờ bị "ngủ" như Render Free. Có thể dùng tên miền riêng (ví dụ: ats.yourname.dev).
Các bước thực hiện chính:
Thuê VPS: Chọn gói cấu hình tối thiểu 1 vCPU, 2GB RAM (Ubuntu OS).
Trỏ Domain: Mua tên miền giá rẻ hoặc dùng tên miền miễn phí, trỏ bản ghi A về IP của VPS.
Cài đặt môi trường: SSH vào VPS và cài đặt Docker + Docker Compose:
bash

sudo apt update
sudo apt install docker.io docker-compose -y
Clone source code: Clone git repository của bạn về thư mục /var/www/ats-project.
Cấu hình file .env: Tạo file .env ở thư mục gốc của VPS tương tự như file .env.example, đổi giá trị DB_HOST=postgres (vì trong compose, tên service db là postgres).
Khởi chạy container:
bash

docker-compose up -d --build
Cấu hình Reverse Proxy & SSL (Nginx + Let's Encrypt):
Cài đặt Nginx trên VPS để trỏ từ cổng 80/443 vào cổng của Frontend và Backend.
Hoặc sử dụng Nginx Proxy Manager (chạy bằng docker) để có giao diện UI trực quan quản lý SSL chứng chỉ HTTPS miễn phí chỉ với vài click chuột.
🎥 Phương Án 3: Sử Dụng Video Demo & Frontend-Only (Mẹo Vượt Qua Bộ Lọc CV)
Rất nhiều HR hoặc Technical Leader khi lướt CV sẽ không muốn mất thời gian click vào link web, đợi backend Render khởi động, hoặc đăng nhập rồi tự mò mẫm. Do đó, phương án kết hợp sau đây sẽ mang lại tỷ lệ phản hồi cao nhất:

Làm một Video Walkthrough (Khuyên dùng Loom hoặc YouTube):
Dùng Loom.com (miễn phí) hoặc quay màn hình và up lên YouTube chế độ Không công khai (Unlisted).
Quay 1 video dài khoảng 2 - 3 phút: Giới thiệu bản thân, demo nhanh luồng hoạt động ấn tượng nhất của dự án (kéo thả Kanban board chuyển đổi trạng thái ứng viên, upload CV, schedule interview và dashboard biểu đồ).
Video này cho nhà tuyển dụng thấy ngay sản phẩm thực tế chạy mượt thế nào mà không sợ hệ thống bị lỗi database hay sập mạng lúc họ click.
Tạo trang Mock-Demo tĩnh (Frontend-only):
Nếu không muốn tốn chi phí thuê VPS hoặc không muốn backend Render bị chậm, bạn có thể tạo một nhánh riêng trên Git tên là demo-static.
Cấu hình React Frontend sử dụng các file JSON mock dữ liệu ngay ở client (không gọi API thực tế). Deploy nhánh này lên Github Pages hoặc Vercel. Link này mở ra phát ăn ngay, tốc độ tải 0.1 giây.
📝 Cách Viết Thông Tin Demo Vào CV Chuyên Nghiệp
Khi viết vào CV phần Dự án (Projects), bạn nên ghi cấu trúc rõ ràng như sau:

Dự án: Hệ thống quản lý tuyển dụng - ATS (Applicant Tracking System)
Vai trò: Full-stack Developer (Java Spring Boot + React)
Công nghệ sử dụng: Spring Boot 3, Spring Security, JWT, PostgreSQL 16, React 19, Tailwind CSS, Docker.
Github: [Link Github Repo của bạn]
Live Demo: [Link Vercel Frontend] (Lưu ý: Backend miễn phí có thể mất 45s để khởi động ở lần truy cập đầu tiên)
Video Demo (3 Mins): [Link Loom / YouTube video]
API Docs (Swagger): [Link Swagger UI hoặc Postman Document]
