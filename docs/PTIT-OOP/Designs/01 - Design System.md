---
title: "01 - Design System"
tags:
  - spec
  - design-system
  - ui-ux
status: active
priority: P0
last_updated: 2026-05-28
---

# 🎨 Design System: Modern Medical

> [!abstract] Tổng quan
> Tài liệu này định nghĩa hệ thống thiết kế (Design System) cho Hệ thống Quản lý Lịch trực Bệnh viện. Phong cách hướng tới là **Modern Medical** - kết hợp sự sạch sẽ, chuyên nghiệp của môi trường y tế với nét hiện đại, công nghệ của các SaaS hàng đầu (như Linear, Notion). 
> **Theme mặc định:** Dark Mode (giúp nổi bật khi demo và phù hợp cho bác sĩ trực ca đêm).

---

## 1. Design Principles (Nguyên tắc thiết kế)

1. **Clarity over Clutter (Rõ ràng thay vì nhồi nhét):** Môi trường y tế cần sự chính xác. Dữ liệu (bảng biểu, lịch) phải cực kỳ dễ đọc. Sử dụng nhiều whitespace.
2. **Intentional Hierarchy (Phân cấp có chủ đích):** Mắt người dùng phải nhìn thấy thông tin quan trọng nhất đầu tiên (Next Shift, Cảnh báo đỏ).
3. **Calm & Focused (Điềm tĩnh & Tập trung):** Sử dụng Dark mode với tone nền xanh navy/xám đen để tạo sự dịu mắt. Tránh dùng màu sắc sặc sỡ bừa bãi.
4. **Fluid Interactions (Tương tác mượt mà):** Micro-animations tinh tế giúp hệ thống có cảm giác "sống động" (alive) và cao cấp (premium).

---

## 2. Color Palette (Bảng màu)

> [!info] 
> Do chọn **Dark mode** làm mặc định, các mã màu dưới đây ưu tiên hiển thị tốt trên nền tối.

### 2.1 Brand & Primary
Màu chủ đạo truyền tải sự chuyên nghiệp, tin cậy của y tế nhưng vẫn hiện đại.
- **Primary:** `Indigo #4F46E5` (Sử dụng cho Primary Buttons, Active States, Links).
- **Primary Hover:** `#4338CA`
- **Primary Subtle (Bg):** `rgba(79, 70, 229, 0.15)` (Dùng cho các tag, badge hoặc background của icon).

### 2.2 Semantic (Trạng thái & Cảnh báo)
Màu sắc mang ý nghĩa sống còn trong nghiệp vụ y tế. Phải đảm bảo độ tương phản (WCAG AA).
- **Success (Hợp lệ, Đã duyệt):** `Emerald #10B981`
- **Warning (Cảnh báo Soft Rule, Burnout vàng):** `Amber #F59E0B`
- **Danger (Vi phạm Hard Rule, Xóa):** `Rose #E11D48` 
- **Info (Gợi ý, Thông tin thêm):** `Sky #0EA5E9`

### 2.3 Neutrals (Nền & Text)
Thay vì dùng màu đen tuyền `#000000`, sử dụng Slate (Xám pha chút xanh blue) để phù hợp với màu Indigo.
- **Background (App):** `Slate 900 #0F172A` (Nền toàn ứng dụng)
- **Surface (Cards, Modals):** `Slate 800 #1E293B` (Nổi lên trên nền app)
- **Surface Hover/Elevated:** `Slate 700 #334155` (Hover states, dropdowns)
- **Border/Divider:** `Slate 600 #475569`
- **Text Primary:** `Slate 50 #F8FAFC` (Heading, Text chính)
- **Text Secondary:** `Slate 400 #94A3B8` (Subtitles, Placeholder, Meta info)

### 2.4 Shift Types (Màu cho Ca trực)
Dùng trên màn hình [[MG-02 Bảng Xếp Lịch (Master Schedule)]] và [[ST-02 Lịch của tôi (My Schedule)]].
- **Ca Sáng (S):** Blue `#3B82F6`
- **Ca Chiều (C):** Orange `#F97316`
- **Ca Đêm (Đ):** Purple `#A855F7`

---

## 3. Typography (Kiểu chữ)

- **Font Family:** `Inter, sans-serif` (Rất hiện đại, hỗ trợ tiếng Việt hoàn hảo, số dễ đọc - cực kỳ quan trọng cho bảng biểu y tế).
- **Base Size:** `14px` (phù hợp cho Dashboard nhiều dữ liệu).

### Scale & Hierarchy
- **H1 (Page Title):** `24px` / SemiBold (600) / Tracking tight (`-0.025em`)
- **H2 (Section/Modal Title):** `20px` / Medium (500)
- **H3 (Card Title):** `16px` / Medium (500)
- **Body (Default):** `14px` / Regular (400) / Line height 1.5
- **Small/Meta:** `12px` / Regular (400) / Text Secondary

> [!tip] Numbers
> Sử dụng tính năng `tabular-nums` của CSS cho các số liệu thống kê, bảng giờ trực để các con số thẳng hàng với nhau.

---

## 4. UI Elements & Tokens

### 4.1 Border Radius (Độ bo góc)
Medium radius tạo cảm giác thân thiện nhưng vẫn giữ được độ sắc nét của enterprise app.
- **Small (Inputs, Badges):** `4px`
- **Medium (Buttons, Modals, Popovers):** `8px`
- **Large (Dashboard Cards):** `12px`

### 4.2 Shadows (Đổ bóng)
Trên Dark mode, shadow khó thấy hơn. Thay vì dùng shadow để phân lớp (elevation), ta kết hợp Shadow đen + Border sáng nhẹ.
- **Card (Level 1):** `box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.5); border: 1px solid #334155;`
- **Modal/Dropdown (Level 2):** `box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.7); border: 1px solid #475569;`

### 4.3 Glassmorphism (Kính mờ) - Điểm nhấn UI
Để tạo cảm giác "Premium", sử dụng hiệu ứng Glassmorphism ở một số thành phần đặc biệt (không lạm dụng):
- **Header/Navbar:** Nền Slate 900 ở mức `80% opacity` + `backdrop-blur(8px)`.
- **Panel Xếp lịch tự động:** Tạo cảm giác layer nổi lên trên bảng lưới.

---

## 5. Components Detail

### 5.1 Buttons
- **Primary:** Background `Indigo #4F46E5`, Text White. Hover: Brighten lên `#4338CA` kèm transition `150ms`. Focus: Ring 2px Indigo.
- **Secondary:** Background `Transparent`, Border `Slate 600`, Text `Slate 50`. Hover: Bg `Slate 700`.
- **Danger:** Background `Rose #E11D48`, Text White. Dùng cho Deactivate/Reject.
- **Ghost/Tertiary:** Không nền, Text `Slate 400`. Hover: Bg `Slate 800`, Text `Slate 50`. Dùng cho hành động phụ.

### 5.2 Badges & Tags
Thiết kế "Subtle" (Nền nhạt + Text đậm) để giao diện không bị quá gắt trên Dark mode.
- **Ví dụ Tag "Ít giờ trực nhất":** Bg `rgba(16, 185, 129, 0.15)` + Text `#34D399` (Emerald 400).
- **Badge Cảnh báo đỏ:** Bg `rgba(225, 29, 72, 0.15)` + Text `#FB7185` (Rose 400) + Icon.

### 5.3 Data Tables (Bảng dữ liệu)
- **Header:** Chữ in hoa (Uppercase), size `12px`, Text `Slate 400`, Tracking wide (`0.05em`). Border bottom cứng.
- **Row:** Border bottom `Slate 700` cực nhạt.
- **Row Hover:** Nổi nền `Slate 800`.
- **Striped:** Không dùng striped, ưu tiên khoảng trắng và border nhạt để phân tách.

---

## 6. Animation & Micro-interactions (Mức độ vừa phải)

> [!important] 
> Mục tiêu: Mượt mà nhưng không làm chậm quá trình thao tác. Transition default: `150ms ease-in-out`.

1. **Hover States:** Tất cả các element tương tác (button, row, card) đều phải có thay đổi màu nền hoặc nâng nhẹ (transform: translateY(-1px)).
2. **Skeleton Loading:** Thay vì dùng Spinner xoay, sử dụng Shimmer Skeleton (hiệu ứng sóng ánh sáng chạy ngang khối xám) cho các Table và Card khi đang fetch dữ liệu.
3. **Modal/Drawer Enter:** 
   - Modal: Fade in + Scale up từ `0.95` -> `1`. (`200ms ease-out`).
   - Drawer: Slide in từ phải sang trái. (`250ms cubic-bezier`).
4. **Scanning Animation (Nghiệp vụ cốt lõi 1):** 
   - Khi chạy Rule Engine trên [[ST-03 Yêu cầu Đổi ca (Smart Shift Swap)]], list ứng viên sẽ hiển thị hiệu ứng "Radar Sweep" hoặc dãy Skeleton nhấp nháy tuần tự từ trên xuống dưới, tạo cảm giác AI đang lọc dữ liệu, kéo dài khoảng 1.5s - 2s trước khi hiện kết quả.
5. **Heatmap Recalculate (Nghiệp vụ cốt lõi 2):**
   - Khi Manager thả (Drop) một ca trực trên [[MG-02 Bảng Xếp Lịch (Master Schedule)]], ô lịch đó và các ô liên quan (cùng bác sĩ) sẽ nháy chớp sáng nhẹ (Flash) màu vàng/đỏ trong `300ms` để thu hút sự chú ý nếu có vi phạm mới.

---

## 7. Layout & Spacing (Hệ thống lưới)

- **Grid System:** 12-column grid.
- **Spacing Scale:** Dựa trên bội số của `4px` (4, 8, 12, 16, 24, 32, 48, 64).
  - Khoảng cách giữa các icon và text: `8px`.
  - Padding trong Button/Input: `8px 16px`.
  - Padding trong Card: `24px`.
  - Gap giữa các section: `32px`.

---

## 8. Trải nghiệm Hội đồng (Demo Focus)

Để buổi bảo vệ đồ án ấn tượng nhất:
1. **Empty States đẹp mắt:** Khi chưa có dữ liệu (Ví dụ: chưa có ca trực nào), hiện hình minh họa (Illustration) mang phong cách line art tinh tế với tone màu Indigo, kèm câu quote/hướng dẫn thay vì chỉ để một màn hình đen thui.
2. **Fairness Score Bar:** Thanh này trên [[MG-01 Dashboard]] và [[MG-02 Bảng Xếp Lịch (Master Schedule)]] nên có hiệu ứng thanh trượt trơn tru (Spring animation) khi số liệu thay đổi từ 60% lên 90%, kèm màu sắc chuyển đổi từ Vàng sang Xanh lá.
3. **Theme Toggle:** Phía trên cùng góc phải luôn có icon 🌙 / ☀️. Nếu hội đồng yêu cầu xem màn hình sáng, có thể switch ngay lập tức (Tất nhiên, CSS Variable cần được setup từ đầu để hỗ trợ việc này).

---

> [!todo] Design Tasks (Figma)
> - [ ] Tạo Global Variables (Colors, Typography, Spacing).
> - [ ] Xây dựng Component Library (Buttons, Inputs, Badges).
> - [ ] Thiết kế Layout Shell (Sidebar, Header, Main Content).
> - [ ] Thiết kế Wireframe -> High Fidelity cho màn ST-03 (Smart Swap).
> - [ ] Thiết kế Wireframe -> High Fidelity cho màn MG-02 (Master Schedule).
