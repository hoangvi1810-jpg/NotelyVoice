# Tiến độ dự án Notely Voice (bản tiếng Việt AI) — 8/9/2026

Ghi lại toàn bộ để bạn xem lại sau 2-3 ngày, không cần hỏi lại từ đầu.

## TIN MỚI: App đã chạy thử thành công trên máy tính (Android emulator)

Không cần chờ cáp iPhone nữa để **xem app chạy** — đã cài JDK + Android SDK + emulator
ngay trên máy này, build APK, chạy thử, chụp màn hình xác nhận: theme màu be/caramel
đúng, giao diện 4 tab AI Note/Highlights/Summary/Transcript hoạt động, Note Template
bottom sheet hoạt động, không crash. Nhân tiện test còn **tìm ra và sửa 1 bug thật**
(màn Model Selection hiện sai dung lượng tải cho tiếng Việt — 465MB thay vì 547MB thật).

Cáp/adapter vẫn cần cho việc **cài lên iPhone thật** (sideload) — hai việc độc lập nhau.

---

## Việc cần làm ngay khi bạn có cáp/adapter sống (cài lên iPhone thật)

1. Cắm iPhone vào máy → mở khoá → bấm **Trust** nếu điện thoại hỏi "Trust This Computer?"
2. Mở Sideloadly (đã cài sẵn tại `C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`,
   hoặc tìm "Sideloadly" trong Start Menu)
3. Kéo file này vào giữa cửa sổ Sideloadly:
   `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely-ios-build\notely-voice-unsigned-ipa\NotelyVoice-unsigned.ipa`
   (file này build từ commit cũ hơn 1 chút, thiếu bản vá Model Selection nói trên — không
   ảnh hưởng gì lớn, chỉ là hiện sai số MB ở 1 màn hình phụ. Nếu muốn bản mới nhất, xem
   mục "Build lại" bên dưới trước khi sideload)
4. Gõ Apple ID + mật khẩu vào 2 ô bên dưới (nên dùng Apple ID phụ, không phải ID chính)
5. Bấm **Start**, chờ 1-3 phút
6. Trên iPhone: **Cài đặt → Cài đặt chung → VPN & Quản lý thiết bị** → bấm vào Apple ID
   vừa dùng → **Trust**
7. Mở app "Notely Voice" từ màn hình chính

**Lưu ý:** Apple ID miễn phí → app hết hạn sau **7 ngày**, phải mở lại Sideloadly ký lại
(sau lần cắm cáp đầu tiên này, các lần ký lại sau có thể làm qua Wi-Fi, không cần cáp nữa
— nhớ bật "Sync over Wi-Fi" trong Sideloadly/iTunes sau khi cắm cáp lần đầu thành công).

## Muốn test ngay bây giờ trên máy tính (không cần cáp, không cần iPhone)

Emulator Android đã cài sẵn tên `NotelyTest`. Chạy lại bất cứ lúc nào bằng lệnh (terminal,
Git Bash):

```
export ANDROID_HOME="/c/dev-tools/android-sdk"
"$ANDROID_HOME/emulator/emulator.exe" -avd NotelyTest &
```

Đợi máy ảo khởi động xong (1-2 phút), app "Notely Voice" đã có sẵn trong đó (icon giống
app thường). Bấm mở như điện thoại thường. Muốn cài lại bản mới nhất, xem mục dưới.

## Việc cần làm sau khi mở app lên (test thử, dù trên emulator hay iPhone thật)

1. **Lấy API key OpenRouter mới** (key cũ đã dán vào chat trước đó nên coi như đã lộ, cần
   đổi). Vào [openrouter.ai/keys](https://openrouter.ai/keys) tạo key mới, **gõ thẳng vào
   app** (Settings → cuộn xuống mục "AI (OpenRouter)"), không dán vào chat.
2. Settings → Transcription Language → chọn **Vietnamese**
3. Bấm vào mục Model Selection → chọn "Optimized model (large-v3-turbo)" → tải (~547MB,
   nên qua Wi-Fi; trên emulator mạng ảo có thể chậm hơn máy thật)
4. Ghi âm thử vài câu tiếng Việt (trên emulator dùng mic ảo hoặc mic thật của laptop nếu
   bật quyền) → kiểm tra transcript có đúng dấu/thanh điệu không
5. Mở note → 4 tab **AI Note / Highlights / Summary / Transcript** → bấm "Tạo với AI" →
   sau khi có API key thật, kiểm tra nội dung AI tạo ra có đúng, có tiếng Việt tự nhiên
   không
6. Thử đổi Note Template (Classic/Brainstorm/Meeting/Lecture/Journaling) → xem nội dung AI
   Note có đổi theo đúng văn phong từng loại không
7. Thử xuất file → menu 3 chấm góc trên → "Export as Markdown"
8. **Báo lại nếu có gì bất thường**, đặc biệt: iOS Keychain (SecureKeyStore.ios.kt — phần
   duy nhất chưa test được, kể cả trên iPhone thật lẫn CI, vì code chỉ chạy thật lần đầu
   khi app khởi động trên thiết bị iOS) và bất kỳ chỗ nào nội dung AI trả về nhìn "sượng"
   (là do cần chỉnh lại prompt, không phải lỗi kỹ thuật).

---

## Build lại app (khi có sửa code mới)

**Android (nhanh, chạy trên máy này, ~15-30s nếu chỉ sửa code, không sửa C++):**
```
cd /c/dev/notely-repo
export JAVA_HOME="/c/dev-tools/jdk-17.0.13+11"
export ANDROID_HOME="/c/dev-tools/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew.bat :shared:assembleDebug --console=plain
```
APK ra ở: `shared/build/outputs/apk/debug/shared-debug.apk`

Cài vào emulator đang chạy:
```
export MSYS_NO_PATHCONV=1
"$ANDROID_HOME/platform-tools/adb.exe" install -r "C:/dev/notely-repo/shared/build/outputs/apk/debug/shared-debug.apk"
```

**Quan trọng:** code làm việc thật nằm ở `C:\dev\notely-repo` (copy ra ngoài OneDrive, vì
đường dẫn có dấu cách/OneDrive làm vỡ build C++ của thư viện Whisper — xem phần "Lỗi thật
đã sửa" bên dưới). Repo gốc trong `OneDrive\Desktop\CLAUDE CODE\notely repo` vẫn còn
nhưng **đã cũ hơn** bản ở `C:\dev\notely-repo` — nếu sửa code, sửa ở `C:\dev\notely-repo`.

**iOS (chạy trên GitHub Actions, cần ~20-25 phút, tốn phút Actions miễn phí):**
```
cd /c/dev/notely-repo
git add -A && git commit -m "mô tả thay đổi"
git push fork feature/vietnamese-ai-notes:main
gh workflow run build-ios-unsigned.yml --repo hoangvi1810-jpg/NotelyVoice --ref main
```
Xem tiến trình: `gh run watch <run-id> --repo hoangvi1810-jpg/NotelyVoice --exit-status`
Tải kết quả: `gh run download <run-id> --repo hoangvi1810-jpg/NotelyVoice --dir <thư mục>`

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

### Lỗi thật đã tìm và sửa (không phải đoán mò, đều verify bằng dữ liệu/thiết bị thật)
- Pod `ffmpegkit-kmp-ios` trong Podfile đã bị CocoaPods gỡ bỏ (FFmpegKit khai tử 4/2025)
  — xoá khỏi Podfile, xác nhận không dùng ở đâu trong code iOS.
- Thư viện Ktor bản 3.5.2 (tôi chọn ban đầu) không tương thích với Kotlin 2.2.0 của
  project — tải và kiểm tra thật nhiều bản Ktor, chốt dùng bản 3.2.3.
- Model OpenRouter alias đúng là `~google/gemini-flash-latest` (có dấu ngã ở đầu, không
  phải `google/gemini-flash-latest` như tôi tra web lúc đầu) — đã test bằng key thật,
  xác nhận hoạt động.
- Build Android trên Windows bị vỡ vì đường dẫn project có dấu cách (`CLAUDE CODE`,
  `notely repo`) và nằm trong OneDrive — cờ `-ffile-prefix-map` của trình biên dịch C++
  (dùng để build thư viện Whisper) bị Clang tách sai thành nhiều tham số. Đã copy toàn bộ
  code sang `C:\dev\notely-repo` (không dấu cách, ngoài OneDrive) để build được.
- Màn hình `ModelSelectionScreen.kt` (khác với card tóm tắt trong Settings) vẫn hiện sai
  dung lượng model cho tiếng Việt (465MB thay vì 547MB thật) — phát hiện khi bấm thử trên
  emulator, đã sửa.

### Đã test trực tiếp trên Android emulator (ảnh chụp màn hình thật, không phải suy đoán)
- Theme be/caramel lên đúng toàn bộ app, kể cả các chỗ tím ẩn
- Onboarding, Home, Settings, Model Selection: không crash
- Giao diện 4 tab AI Note/Highlights/Summary/Transcript: hoạt động đúng
- Banner lỗi "Chưa cấu hình OpenRouter API key" hiện đúng khi bấm "Tạo với AI" mà chưa có
  key
- Note Template bottom sheet: cả 5 template hiện đúng, tiếng Việt có dấu render sạch

### Thông tin kỹ thuật (để tham khảo khi cần)
- Repo fork của bạn: https://github.com/hoangvi1810-jpg/NotelyVoice (nhánh `main`)
- Commit mới nhất: `7d8e9fa` — "Fix Model Selection screen showing wrong size..."
- Lần build iOS thành công: run `34192470017` (24 phút 16 giây, **chưa có bản vá Model
  Selection** — nếu muốn bản mới nhất phải build lại, xem mục "Build lại" ở trên)
  → https://github.com/hoangvi1810-jpg/NotelyVoice/actions/runs/34192470017
- File `.ipa` (bản cũ hơn 1 commit) đã tải sẵn tại:
  `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely-ios-build\notely-voice-unsigned-ipa\NotelyVoice-unsigned.ipa`
- Sideloadly đã cài sẵn tại: `C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`
- Android SDK + JDK + emulator cài tại `C:\dev-tools\` (JDK: `jdk-17.0.13+11`, SDK:
  `android-sdk`), AVD tên `NotelyTest` (Pixel 6, Android 14)
- Code làm việc thật: `C:\dev\notely-repo` (bản copy ngoài OneDrive, không dấu cách —
  dùng bản này để build, không dùng bản trong OneDrive nữa)

## Việc còn thiếu / chưa làm (thành thật, không giấu)
- **Tag AI sinh ra** (2-3 từ khoá mỗi note) có lưu vào máy nhưng **chưa hiển thị** ở màn
  hình danh sách note — cần làm thêm UI riêng nếu muốn thấy tag.
- **Chưa test được việc gọi AI thật** (cần API key thật, key cũ đã lộ nên chưa dùng lại
  để test) — mọi thứ về UI/luồng lỗi đã xác nhận đúng, chỉ còn thiếu bước gọi API thành
  công thực tế.
- **Chưa test trên iPhone thật** — phần Keychain (SecureKeyStore.ios.kt) chỉ chạy thật
  lần đầu khi mở app trên thiết bị iOS, đây là rủi ro lớn nhất còn lại.
- Nút "tóm tắt nhanh" trên màn hình ghi âm (khác tab Summary) vẫn dùng công nghệ cũ,
  không hỗ trợ tiếng Việt — cố ý giữ vì đổi sẽ tốn thêm tiền gọi API cho một tính năng
  phụ, đã ghi rõ lý do trong code.
