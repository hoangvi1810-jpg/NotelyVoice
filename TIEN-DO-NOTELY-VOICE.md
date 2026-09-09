# Tiến độ dự án Notely Voice (bản tiếng Việt AI) — 8/9/2026 → 9/9/2026

Ghi lại toàn bộ để bạn xem lại sau 2-3 ngày, không cần hỏi lại từ đầu.

## KẾ HOẠCH 3 ĐỢT ĐANG CHẠY (biến app thành sổ ghi chú thật sự)

Kế hoạch đầy đủ nằm ở file plan riêng; tóm tắt để nhớ:

- **Đợt 1 — XONG** (đã test trên máy ảo, đang build IPA): sửa copy/share/PDF theo đúng tab
  đang xem (trước đây chỉ copy được Transcript); ô tiêu đề hiện sẵn đầu note thay cho menu
  "Rename note"; note mới mở thẳng vào chỗ gõ chữ; nút 3 gạch → nút "+" tạo note.
  Sửa kèm 1 lỗi thật: tiêu đề bị nội dung ghi đè mỗi lần gõ.
- **Đợt 2 — chưa làm**: sổ tay (notebook) phân loại note, mỗi note thuộc 1 sổ.
  **Bắt buộc có file migration `1.sqm`** (xem cảnh báo dưới đây), và phải test nâng cấp:
  cài bản cũ → tạo note → cài đè bản mới → note cũ phải còn nguyên.
- **Đợt 3 — chưa làm**: đính kèm ảnh/video/PDF, hiện thành dãy thẻ dưới phần chữ.

**Cảnh báo kỹ thuật quan trọng cho Đợt 2/3:** dự án dùng SQLDelight 1.5.5 và **chưa từng có
file migration nào**, nên `Schema.version` vĩnh viễn = 1 và hàm `migrate()` rỗng. Nếu thêm
bảng/cột mới mà không kèm file `.sqm`, máy nào đã cài app từ trước sẽ **không** được tạo
bảng mới → lỗi `no such table/column` lúc chạy. Migration đầu tiên nên dùng
`CREATE TABLE IF NOT EXISTS` để vá luôn bảng `noteAiContentEntity` (bảng này thêm hồi trước
cũng không có migration).

---

## TIN MỚI NHẤT (9/9): Đổi logo, thêm đổi tên note, sửa lỗi khung soạn thảo

Ba việc bạn yêu cầu, cả ba đã làm xong và **verify trực tiếp trên emulator** (không suy đoán):

1. **Logo mới**: lấy file `logo notely.jpg` (con chó cam tư thế hú, nền kem) bạn để trong
   thư mục repo, tự tạo lại toàn bộ icon Android (mọi độ phân giải, cả icon "adaptive" 2 lớp
   nền+hình) và icon iOS (toàn bộ kích thước trong `AppIcon.appiconset`). Đã cài thử trên
   emulator, icon mới lên đúng ở màn hình chính, ngăn kéo app, và màn splash.
2. **Đổi tên note tách khỏi nội dung**: trước đây tiêu đề note = y hệt nội dung, gõ gì vào
   note thì tiêu đề đổi theo đó, không có cách nào đặt tên riêng. Giờ vào note → bấm menu 3
   chấm góc trên → **"Rename note"** → gõ tên tuỳ ý → Save. Tên này tách biệt hẳn khỏi nội
   dung, gõ thêm vào note không làm mất tên đã đặt nữa. Test trực tiếp: đổi tên, quay lại
   danh sách, tên mới hiện đúng và giữ nguyên.
3. **Sửa lỗi khung soạn thảo bị "nở" thành khoảng trắng** (đúng như video bạn gửi): nguyên
   nhân là code có 2 lớp cuộn (scroll) lồng nhau trong màn Transcript/nội dung note — một
   lớp bọc ngoài không cần thiết khiến Compose tính sai kích thước, làm con trỏ gõ chữ bị
   "lạc" vào vùng trống khi bàn phím hiện lên. Xoá lớp cuộn thừa. Test lại đúng kịch bản
   trong video: gõ nội dung dài nhiều dòng, bấm vào giữa đoạn văn khi bàn phím đang mở — con
   trỏ và nội dung giờ hiện đúng vị trí, gõ thêm chữ vào giữa vẫn thấy rõ, không còn khoảng
   trắng nào nữa.

Đã đẩy cả 3 lên GitHub. Build iOS mới (có đủ cả 3 thứ này) — xem mục "Thông tin kỹ thuật"
để lấy run ID/link mới nhất khi build xong.

---

## TIN CŨ HƠN (8/9): Thiết kế lại toàn bộ giao diện (tím) + tìm ra và sửa 1 crash thật trên iOS

Sau bản be/caramel ban đầu, đã **thiết kế lại toàn bộ giao diện sang tông tím** (theo ảnh
mẫu app AirPods bạn gửi) — thẻ trắng nổi trên nền tím nhạt, bo góc lớn, bóng đổ mềm, nút
bấm có cảm giác "giọt nước" (nhún + nảy nhẹ khi bấm). Đã làm hết toàn bộ màn hình: danh
sách note, chi tiết note (4 tab AI), ghi âm, transcript, settings, onboarding, splash. Đã
build APK, cài lên emulator Android, chụp ảnh xác nhận cả sáng lẫn tối.

**Quan trọng — đã tìm ra và sửa 1 crash thật trên iOS**: bạn gửi video cho thấy app đen
màn hình ngay khi cuộn vào Settings, trước khi kịp gõ gì vào ô API key. Đúng là bug thật —
màn Settings tự động đọc Keychain (`aiRepository.getApiKey()`) ngay khi phần "AI
(OpenRouter)" xuất hiện trên màn hình (không cần bấm gì), và code Keychain trên iOS
(`SecureKeyStore.ios.kt`) có lỗi cân bằng retain/release khiến app crash ở bước đó.

Bug này thật ra **đã được tìm ra và sửa ở một phiên làm việc trước** (dựa đúng bằng chứng
video tương tự), nhưng bản sửa bị kẹt ở một bản copy code khác trên máy, chưa từng được
đẩy lên GitHub — nên bản `.ipa` bạn sideload trước đó vẫn mang lỗi cũ. Đã ghép bản sửa vào
đúng bản code hiện tại (bản có giao diện tím mới), đẩy lên GitHub, và **build lại `.ipa`
mới đã xong thành công** (xem mục "Việc cần làm ngay" bên dưới để lấy file mới).

**Lưu ý thành thật:** code Keychain trên iOS chưa từng được biên dịch/chạy thật trên
thiết bị (máy này không có Mac/Xcode) — bản build mới sẽ là lần đầu tiên nó thực sự chạy
trên iOS thật. Nếu bạn sideload xong mà vẫn thấy crash y hệt ở Settings, báo lại ngay kèm
video mới — đó sẽ là manh mối quan trọng để tìm tiếp.

Cáp/adapter vẫn cần cho việc **cài lên iPhone thật** (sideload) — độc lập với việc build.

---

## Việc cần làm ngay khi bạn có cáp/adapter sống (cài lên iPhone thật)

1. Cắm iPhone vào máy → mở khoá → bấm **Trust** nếu điện thoại hỏi "Trust This Computer?"
2. Mở Sideloadly (đã cài sẵn tại `C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`,
   hoặc tìm "Sideloadly" trong Start Menu)
3. Kéo file này vào giữa cửa sổ Sideloadly (bản mới nhất, có cả giao diện tím + bản vá
   crash Keychain, build thành công lúc 19:11):
   `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely-ios-build\notely-voice-unsigned-ipa-fixed\notely-voice-unsigned-ipa\NotelyVoice-unsigned.ipa`
   (File cũ tại `notely-ios-build\notely-voice-unsigned-ipa\NotelyVoice-unsigned.ipa` **đã
   lỗi thời** — không có giao diện tím mới, không có bản vá crash Keychain — đừng dùng nữa.)
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
8. **Báo lại nếu có gì bất thường**, đặc biệt: crash bất kỳ ở đâu trên iPhone thật (đây là
   lần build đầu tiên có bản vá Keychain, cần xác nhận thật trên thiết bị) và bất kỳ chỗ
   nào nội dung AI trả về nhìn "sượng" (là do cần chỉnh lại prompt, không phải lỗi kỹ
   thuật).

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
đã sửa" bên dưới). Repo gốc trong `OneDrive\Desktop\CLAUDE CODE\notely repo` vẫn còn nhưng
**đã cũ hơn** bản ở `C:\dev\notely-repo` — nếu sửa code, sửa ở `C:\dev\notely-repo`. (Có
một lần 2 bản bị lệch nhau và một bản sửa lỗi thật bị kẹt lại ở bản OneDrive suốt nhiều
giờ — xem mục "Lỗi thật đã tìm và sửa" — nên từ giờ **luôn kiểm tra `git log` cả 2 nơi**
nếu nghi ngờ có gì đó "biến mất".)

**iOS (chạy trên GitHub Actions, cần ~20-30 phút, tốn phút Actions miễn phí):**
```
cd /c/dev/notely-repo
git add -A && git commit -m "mô tả thay đổi"
git push fork feature/vietnamese-ai-notes
gh workflow run build-ios-unsigned.yml --repo hoangvi1810-jpg/NotelyVoice --ref feature/vietnamese-ai-notes
```
Xem tiến trình: `gh run watch <run-id> --repo hoangvi1810-jpg/NotelyVoice --exit-status`
Tải kết quả: `gh run download <run-id> --repo hoangvi1810-jpg/NotelyVoice --dir <thư mục>`

---

## Tóm tắt những gì đã làm (để tham khảo, không cần đọc kỹ)

### Code
1. **Tiếng Việt nghe tốt hơn**: thêm model `ggml-large-v3-turbo-q5_0` (547MB), tự động
   chọn cho ngôn ngữ "vi" thay vì model base/small yếu.
2. **Lớp AI qua OpenRouter**: package `ai/` mới — gọi API, 5 prompt template tiếng Việt,
   cache kết quả vào SQLDelight, lưu API key mã hoá bằng Android Keystore/iOS Keychain
   (không dùng thư viện `security-crypto` vì đã bị Google khai tử).
3. **Giao diện 4 tab**: AI Note / Highlights / Summary / Transcript + bottom sheet chọn
   Note Template, giống app mẫu bạn gửi ảnh.
4. **Thiết kế lại toàn bộ giao diện sang tím** (thay cho bản be/caramel ban đầu): dựng hệ
   thống theme mới từ đầu (bảng màu, chữ, bo góc, bóng đổ, khoảng cách) + hiệu ứng bấm
   "giọt nước" (nhún mềm có nảy nhẹ) áp dụng toàn app + sửa luôn nhiều lỗi hiển thị thật
   (nút tàng hình, viền lệch, màu hardcode, thanh tiến trình giật, góc trắng lệch trong
   dark mode...).
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
- **Crash thật trên iOS khi mở Settings** (xác nhận bằng video bạn gửi): `SettingsScreen`
  render `AiSettingsSection`, phần này có `LaunchedEffect(Unit)` tự đọc Keychain
  (`aiRepository.getApiKey()`) ngay khi vừa xuất hiện trên màn hình — không cần bấm gì.
  Code Keychain trên iOS (`SecureKeyStore.ios.kt`) xây câu truy vấn bằng
  `NSMutableDictionary` và bridge các hằng số `kSec*` không nhất quán (có chỗ coi là đã sở
  hữu reference qua `CFBridgingRelease`, có chỗ nhét thẳng con trỏ CFStringRef chưa sở
  hữu) → lệch retain/release → crash khi đọc lẫn khi lưu. Đã viết lại toàn bộ bằng
  `CFMutableDictionary` thuần (`CFDictionaryCreateMutable`/`CFDictionarySetValue`): hằng số
  `kSec*` (không sở hữu) nhét thẳng vào, chỉ những giá trị tự tạo (chuỗi service/account,
  dữ liệu key) mới `CFBridgingRetain` để khớp đúng +1 mà dictionary cần. **Rủi ro:** bản
  sửa này ban đầu bị làm ở một bản copy code khác trên máy (nhánh Git tách rời, chưa từng
  push) — mất một lúc mới phát hiện ra và ghép lại đúng chỗ đang chạy thật. Bài học: khi
  nghi ngờ "mình đã sửa cái này rồi mà" — kiểm tra `git log` ở **cả hai** thư mục
  (`C:\dev\notely-repo` và bản OneDrive) trước khi kết luận.

### Đã test trực tiếp trên Android emulator (ảnh chụp màn hình thật, không phải suy đoán)
- Giao diện tím mới lên đúng toàn bộ app, cả sáng lẫn tối, hiệu ứng bấm "giọt nước" hoạt
  động
- Onboarding, Home, Settings, Model Selection: không crash (test lại y hệt bước trong
  video bạn gửi, trên Android — không tái hiện được crash ở đây, vì bug nằm riêng ở code
  Keychain của iOS)
- Giao diện 4 tab AI Note/Highlights/Summary/Transcript: hoạt động đúng
- Banner lỗi "Chưa cấu hình OpenRouter API key" hiện đúng khi bấm "Tạo với AI" mà chưa có
  key
- Note Template bottom sheet: cả 5 template hiện đúng, tiếng Việt có dấu render sạch
- **Chưa test được trên iPhone thật** — đây vẫn là rủi ro lớn nhất, vì bản vá Keychain vừa
  nói ở trên chưa từng compile bằng Xcode thật (máy này không có Mac)

### Thông tin kỹ thuật (để tham khảo khi cần)
- Repo fork của bạn: https://github.com/hoangvi1810-jpg/NotelyVoice (nhánh
  `feature/vietnamese-ai-notes`)
- Commit mới nhất: `7b62bc8` — logo mới + đổi tên note + sửa lỗi khung soạn thảo
- Build iOS mới nhất (thành công): run `34295120422` (logo mới, đổi tên note, sửa lỗi
  khung soạn thảo, + mọi thứ trước đó: giao diện tím, bản vá Keychain, AI Note tiếng Anh,
  nút Dán) → https://github.com/hoangvi1810-jpg/NotelyVoice/actions/runs/34295120422 —
  file `.ipa` đã tải về sẵn tại
  `notely-ios-build\notely-voice-unsigned-ipa-fixed\notely-voice-unsigned-ipa\`
- Build iOS trước đó: run `34248208233` (giao diện tím + bản vá Keychain +
  **toàn bộ tab AI Note luôn ra tiếng Anh** (cả 5 template: Classic/Brainstorm/Meeting/
  Lecture/Journaling) trong khi Highlights/Summary/Transcript vẫn tiếng Việt + nút "Dán"
  cho ô API key) → https://github.com/hoangvi1810-jpg/NotelyVoice/actions/runs/34248208233
  — file `.ipa` đã tải về sẵn tại
  `notely-ios-build\notely-voice-unsigned-ipa-fixed\notely-voice-unsigned-ipa\` (xem mục
  "Việc cần làm ngay" ở trên, **đừng dùng file cũ trong
  `notely-ios-build\notely-voice-unsigned-ipa\` nữa**)
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
- **Chưa test trên iPhone thật** — bản vá Keychain (`SecureKeyStore.ios.kt`) chỉ chạy thật
  lần đầu khi mở app trên thiết bị iOS thật, đây là rủi ro lớn nhất còn lại. Nếu sideload
  xong vẫn crash ở Settings, đó là manh mối cực kỳ quý — báo lại ngay kèm mô tả/video.
- Nút "tóm tắt nhanh" trên màn hình ghi âm (khác tab Summary) vẫn dùng công nghệ cũ,
  không hỗ trợ tiếng Việt — cố ý giữ vì đổi sẽ tốn thêm tiền gọi API cho một tính năng
  phụ, đã ghi rõ lý do trong code.
- Icon/logo app mới (ảnh con khỉ xanh bạn gửi) — cố ý để sau, chưa làm trong đợt này.
