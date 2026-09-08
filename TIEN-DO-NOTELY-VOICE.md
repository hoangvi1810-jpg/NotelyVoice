# Tiến độ dự án Notely Voice (bản tiếng Việt AI) — 8/9/2026

Ghi lại toàn bộ để bạn xem lại sau 2-3 ngày, không cần hỏi lại từ đầu.

## Tình trạng hiện tại: CÒN 1 BƯỚC CUỐI — kẹt vì cổng Type-C hư

Mọi thứ đã xong xuôi: code viết xong, build iOS thành công, file `.ipa` đã có sẵn,
Sideloadly đã cài sẵn trên máy. **Chỉ còn thiếu 1 sợi cáp/adapter sống để cắm điện thoại
vào máy 1 lần duy nhất** (bắt buộc phải cắm cáp cho lần ghép nối đầu — đã tra kỹ, không
có cách nào bỏ qua bước này, kể cả Sideloadly lẫn AltStore).

---

## Việc cần làm ngay khi bạn có cáp/adapter sống

1. Cắm iPhone vào máy → mở khoá → bấm **Trust** nếu điện thoại hỏi "Trust This Computer?"
2. Mở Sideloadly (đã cài sẵn tại `C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`,
   hoặc tìm "Sideloadly" trong Start Menu)
3. Kéo file này vào giữa cửa sổ Sideloadly:
   `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely-ios-build\notely-voice-unsigned-ipa\NotelyVoice-unsigned.ipa`
4. Gõ Apple ID + mật khẩu vào 2 ô bên dưới (nên dùng Apple ID phụ, không phải ID chính)
5. Bấm **Start**, chờ 1-3 phút
6. Trên iPhone: **Cài đặt → Cài đặt chung → VPN & Quản lý thiết bị** → bấm vào Apple ID
   vừa dùng → **Trust**
7. Mở app "Notely Voice" từ màn hình chính

**Lưu ý:** Apple ID miễn phí → app hết hạn sau **7 ngày**, phải mở lại Sideloadly ký lại
(sau lần cắm cáp đầu tiên này, các lần ký lại sau có thể làm qua Wi-Fi, không cần cáp nữa
— nhớ bật "Sync over Wi-Fi" trong Sideloadly/iTunes sau khi cắm cáp lần đầu thành công).

## Việc cần làm sau khi cài app xong (test thử)

1. **Đổi API key OpenRouter trước tiên** — key cũ đã bị dán vào đoạn chat trước đó nên
   coi như đã lộ. Vào [openrouter.ai/keys](https://openrouter.ai/keys) xoá key cũ, tạo
   key mới, **chỉ gõ key mới thẳng vào app** (Settings → mục "AI (OpenRouter)"), không
   dán vào chat nữa.
2. Vào Settings → chọn ngôn ngữ **Vietnamese** → vào phần chọn model → tải model
   `large-v3-turbo` (~547MB, nên tải qua Wi-Fi)
3. Ghi âm thử vài câu tiếng Việt → kiểm tra transcript có đúng dấu/thanh điệu không (so
   với trước đây dùng model base/small)
4. Mở note vừa ghi → thử 4 tab: **AI Note / Highlights / Summary / Transcript** → bấm
   "Tạo với AI" → xem có tạo được không
5. Thử đổi Note Template (Classic/Brainstorm/Meeting/Lecture/Journaling) → xem nội dung
   AI Note có đổi theo không
6. Thử xuất file → menu 3 chấm góc trên → "Export as Markdown"
7. Kiểm tra màu giao diện đã đổi sang tông be/nâu caramel (không còn tím) chưa
8. **Quan trọng nhất — báo lại nếu có crash hoặc lỗi bất thường**, đặc biệt:
   - Lỗi liên quan tới lưu API key (phần Keychain trên iOS là phần duy nhất tôi **không**
     compile-check được trước khi build, do không có Mac — nếu có lỗi ở đây là điều dễ
     xảy ra nhất, tôi sẽ sửa ngay khi biết)
   - Lỗi layout ở 4 tab mới
   - App bị crash khi mở note

---

## Tóm tắt những gì đã làm trong phiên này (để tham khảo, không cần đọc kỹ)

### Code (6 giai đoạn theo kế hoạch ban đầu)
1. **Tiếng Việt nghe tốt hơn**: thêm model `ggml-large-v3-turbo-q5_0` (547MB), tự động
   chọn cho ngôn ngữ "vi" thay vì model base/small yếu.
2. **Lớp AI qua OpenRouter**: package `ai/` mới — gọi API, 5 prompt template tiếng Việt,
   cache kết quả vào SQLDelight, lưu API key mã hoá bằng Android Keystore/iOS Keychain
   (không dùng thư viện `security-crypto` vì đã bị Google khai tử).
3. **Giao diện 4 tab**: AI Note / Highlights / Summary / Transcript + bottom sheet chọn
   Note Template, giống app mẫu bạn gửi ảnh.
4. **Đổi màu tím → be/caramel**: toàn bộ theme, tìm ra và sửa cả những chỗ tím ẩn không
   nằm trong 3 file theme chính.
5. **Export & tự đặt tên**: thêm export Markdown, AI tự sinh tiêu đề + tag sau lần tạo
   AI Note đầu tiên.
6. **CI build iOS**: workflow GitHub Actions build file `.ipa` chưa ký trên máy ảo macOS,
   để sideload từ Windows không cần Mac.

### Lỗi thật đã tìm và sửa khi build thử (không phải đoán mò, đều verify bằng dữ liệu thật)
- Pod `ffmpegkit-kmp-ios` trong Podfile đã bị CocoaPods gỡ bỏ (FFmpegKit khai tử 4/2025)
  — xoá khỏi Podfile, xác nhận không dùng ở đâu trong code iOS.
- Thư viện Ktor bản 3.5.2 (tôi chọn ban đầu) không tương thích với Kotlin 2.2.0 của
  project — tải và kiểm tra thật nhiều bản Ktor, chốt dùng bản 3.2.3.
- Model OpenRouter alias đúng là `~google/gemini-flash-latest` (có dấu ngã ở đầu, không
  phải `google/gemini-flash-latest` như tôi tra web lúc đầu) — đã test bằng key thật,
  xác nhận hoạt động.

### Thông tin kỹ thuật (để tham khảo khi cần)
- Repo fork của bạn: https://github.com/hoangvi1810-jpg/NotelyVoice (nhánh `main`)
- Commit mới nhất: `5936714` — "Downgrade Ktor 3.5.2 -> 3.2.3..."
- Lần build iOS thành công: run `34192470017` (24 phút 16 giây)
  → https://github.com/hoangvi1810-jpg/NotelyVoice/actions/runs/34192470017
- File `.ipa` đã tải sẵn tại:
  `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely-ios-build\notely-voice-unsigned-ipa\NotelyVoice-unsigned.ipa`
- Sideloadly đã cài sẵn tại: `C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`
- Nếu muốn build lại (sau khi sửa code): vào thư mục
  `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely repo`, commit + push lên
  `fork main`, rồi chạy: `gh workflow run build-ios-unsigned.yml --repo hoangvi1810-jpg/NotelyVoice --ref main`

## Việc còn thiếu / chưa làm (thành thật, không giấu)
- **Tag AI sinh ra** (2-3 từ khoá mỗi note) có lưu vào máy nhưng **chưa hiển thị** ở màn
  hình danh sách note — cần làm thêm UI riêng nếu muốn thấy tag.
- Toàn bộ app **chưa test lần nào trên điện thoại thật** — mọi thứ ở trên là dựa vào
  build thành công + đọc code kỹ, chưa có ai bấm thử.
- Nút "tóm tắt nhanh" trên màn hình ghi âm (khác tab Summary) vẫn dùng công nghệ cũ,
  không hỗ trợ tiếng Việt — cố ý giữ vì đổi sẽ tốn thêm tiền gọi API cho một tính năng
  phụ, đã ghi rõ lý do trong code.
