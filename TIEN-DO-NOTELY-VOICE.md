# Tiến độ dự án Notely Voice — cập nhật 9/9/2026 (bản mới nhất, đọc file này trước)

Ghi lại toàn bộ để xem lại sau vài ngày mà không cần hỏi lại từ đầu.

---

## ⚠️ ĐỌC TRƯỚC KHI LÀM BẤT CỨ GÌ: chỉ có 1 repo code thật

**Code thật, duy nhất, là `C:\dev\notely-repo`.** Đừng sửa code ở bất kỳ đâu khác.

Có một bản copy cũ ở `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely repo` —
**bản này đã lỗi thời, đang kẹt ở commit `af5c5e8`, chậm hơn `C:\dev\notely-repo` tới 8+
commit.** Nó vẫn còn tồn tại vì lúc đầu chưa biết phải copy ra ngoài OneDrive (đường dẫn có
dấu cách + nằm trong OneDrive làm vỡ build C++ của Whisper). **Không xoá** (đề phòng có gì
đó chưa kịp copy sang), nhưng **không bao giờ sửa code ở đó nữa**.

### Bẫy dễ mắc lại: terminal tự nhảy về thư mục OneDrive

Ngay trong phiên làm việc hôm nay (9/9), lệnh `cd /c/dev/notely-repo` chạy xong, nhưng lệnh
**tiếp theo** trong tool khác lại tự động reset về `OneDrive\Desktop\CLAUDE CODE\notely
repo`. Đây chính là lý do rất dễ vô tình sửa nhầm code ở bản cũ mà không nhận ra — y hệt lỗi
đã xảy ra ở phiên làm việc trước đó (mất hàng giờ mới phát hiện một bản sửa lỗi bị kẹt ở
repo sai, chưa từng được đẩy lên GitHub).

**Quy tắc bắt buộc từ giờ:** trước khi sửa file hoặc chạy lệnh git/gradle/gh, luôn kiểm tra
đường dẫn tuyệt đối đang đứng ở đâu (`pwd`), và **luôn gõ full path `C:\dev\notely-repo`**
trong mọi lệnh quan trọng thay vì tin vào `cd` từ lệnh trước còn hiệu lực. Nếu nghi ngờ "mình
đã sửa cái này rồi mà sao không thấy" — chạy `git log --oneline -5` ở **cả hai** thư mục để
so sánh trước khi kết luận bất cứ điều gì.

---

## Trạng thái hiện tại: đang build bản iOS mới nhất, đủ cả 4 đợt sửa lỗi bạn báo

Bạn test bản IPA trước trên iPhone thật qua Sideloadly, báo lại 4 vấn đề — cả 4 đã sửa xong,
đã verify trên Android emulator, **đang chờ CI build iOS xong** (thường mất 25-35 phút).

**Run đang chạy: `34357955925`**, commit `dc5118e`, nhánh `feature/vietnamese-ai-notes`.
Xem tiến trình: `gh run view 34357955925 --repo hoangvi1810-jpg/NotelyVoice`
Khi xong: `gh run download 34357955925 --repo hoangvi1810-jpg/NotelyVoice --dir <thư mục>`

**⚠️ Bản IPA đã gửi bạn trước đó (run `34349732927`, commit `32dbeb2`) đã LỖI THỜI** — chưa
có 4 chỗ sửa dưới đây. Bản mới từ run `34357955925` sẽ thay thế hoàn toàn.

### 4 vấn đề bạn báo & đã sửa (verify trên Android emulator, thật — không suy đoán)

1. **Hàng chip sổ tay (Nhật ký/Ý tưởng/Công việc) nằm ngang gây rối mắt** → chuyển hẳn vào
   **hamburger drawer** bên trái (bấm icon 3 gạch góc trên trái). Màn Home không còn hàng
   chip nữa, hiện tất cả note như cũ. File mới: `notebook/ui/NotebookDrawer.kt`.
2. **Thẻ note không có nội dung vẫn hiện dòng "No additional text"** → bỏ hẳn, giờ thẻ trống
   nội dung chỉ hiện tiêu đề, không có dòng thừa bên dưới.
3. **Copy rồi dán (paste) không được trong note mới tự gõ trên iPhone** → nguyên nhân là bug
   đã biết của Compose Multiplatform 1.8.2 trên iOS (JetBrains#4502): bất kỳ `BasicTextField`
   nào có `VisualTransformation` khác null đều làm mất menu "Dán" của iOS. Editor note vẫn
   luôn gắn `VisualTransformation` (để tô đậm/nghiêng/gạch chân) dù note không định dạng gì.
   Sửa: chỉ gắn `VisualTransformation` khi note **có** định dạng đang áp dụng; note trơn
   không định dạng thì không gắn gì cả → menu Dán của iOS hoạt động bình thường trở lại.
4. **Note ghi âm và sổ tay (notebook) lẫn vào nhau, tạo bản ghi âm mới lại ảnh hưởng tới sổ
   tay đang có** → tách hẳn thành 2 luồng độc lập:
   - Hamburger → bấm vào 1 sổ tay (VD "Nhật ký") → mở **màn hình riêng** chỉ chứa note tự gõ
     thuộc sổ đó, có nút "+" riêng để tạo note mới **tự động gán vào đúng sổ đang mở**.
     Không có gì liên quan tới ghi âm/AI ở màn này.
   - Nút tròn (FAB) ở màn Home **chỉ dùng để ghi âm**, không đổi.
   - Cũng nhân tiện sửa 1 lỗi thật không liên quan: đính kèm file (ảnh/PDF) vào note **mới
     tạo, chưa gõ chữ gì** trước đây bấm nút đính kèm không có tác dụng gì (vì note chưa được
     lưu vào DB nên chưa có ID) — giờ bấm đính kèm sẽ tự lưu note trước rồi mới mở bảng chọn
     file.

### Việc cần làm khi có IPA mới (run `34357955925`)

Y hệt quy trình cũ (xem mục "Cách sideload" bên dưới) — kéo file `.ipa` mới vào Sideloadly,
ký lại bằng Apple ID, cài đè lên bản cũ trên iPhone (không cần gỡ app trước, cài đè là được).

---

## KẾ HOẠCH 4 ĐỢT (biến app thành sổ ghi chú thật sự) — tiến độ

Kế hoạch chi tiết đầy đủ nằm trong file plan riêng (`~/.claude/plans/`), đây là tóm tắt tiến
độ thật:

- **Đợt 1 (sửa lỗi + thương hiệu) — XONG**: copy/share/PDF theo đúng tab đang xem, sửa khung
  soạn thảo bị "nở" khoảng trắng, logo Shiba mới (Android + iOS đủ mọi kích thước), splash
  không còn nhấp nháy logo cũ, đổi font toàn app sang Be Vietnam Pro.
- **Đợt 2 (notebook + drawer) — XONG**: 2 bảng phụ `notebookEntity` +
  `noteNotebookEntity` (note thuộc đúng 1 sổ qua khoá chính), migration bằng file `.sqm` thật
  (`1.sqm`, `2.sqm` — **không phải** hướng "tự vá DDL" từng cân nhắc trước đó), drawer bên
  trái thay cho hàng chip ngang, màn hình riêng cho từng sổ tay (tách khỏi luồng ghi âm).
- **Đợt 3 (đính kèm ảnh/PDF) — XONG**: đã có sẵn code đính kèm (`attachment/` package) từ
  trước, đã review + sửa lỗi "đính kèm vào note chưa lưu không có tác dụng" + build + test.
- **Đợt 4 (rich text thật: heading, bullet thật...)** — **chưa làm**, chưa lên kế hoạch chi
  tiết. Hiện tại note tự gõ vẫn dùng in đậm/nghiêng/gạch chân qua `VisualTransformation` (đủ
  dùng, không có heading/bullet thật). Nếu muốn làm tiếp, cân nhắc thư viện
  `com.mohamedrejeb.richeditor:richeditor-compose` — cần spike ~1 ngày để xác nhận build được
  cho iOS trước khi cam kết (xem chi tiết trong file plan gốc).

---

## Cách sideload lên iPhone thật (Sideloadly)

1. Cắm iPhone vào máy → mở khoá → bấm **Trust** nếu hỏi "Trust This Computer?"
2. Mở Sideloadly (`C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`)
3. Kéo file `.ipa` mới nhất vào giữa cửa sổ Sideloadly
4. Gõ Apple ID + mật khẩu (nên dùng Apple ID phụ, không phải ID chính)
5. Bấm **Start**, chờ 1-3 phút
6. Trên iPhone: **Cài đặt → Cài đặt chung → VPN & Quản lý thiết bị** → bấm vào Apple ID vừa
   dùng → **Trust**
7. Mở app "Notely Voice"

**Lưu ý:** Apple ID miễn phí → app hết hạn sau **7 ngày**, phải mở lại Sideloadly ký lại (từ
lần 2 trở đi có thể ký qua Wi-Fi, không cần cáp — bật "Sync over Wi-Fi" trong Sideloadly).

## Test nhanh trên máy tính (Android emulator, không cần iPhone/cáp)

Emulator tên `NotelyTest` đã cài sẵn:
```
export ANDROID_HOME="/c/dev-tools/android-sdk"
"$ANDROID_HOME/emulator/emulator.exe" -avd NotelyTest &
```
Đợi 1-2 phút, app đã có sẵn trong máy ảo, bấm mở như điện thoại thường.

## Build lại app (khi có sửa code mới)

**Android (nhanh, ~15-30s nếu chỉ sửa Kotlin, không sửa C++):**
```
cd /c/dev/notely-repo
export JAVA_HOME="/c/dev-tools/jdk-17.0.13+11"
export ANDROID_HOME="/c/dev-tools/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew.bat :shared:assembleDebug --console=plain
```
APK ra ở `shared/build/outputs/apk/debug/shared-debug.apk`, cài vào emulator:
```
export MSYS_NO_PATHCONV=1
"$ANDROID_HOME/platform-tools/adb.exe" install -r "C:/dev/notely-repo/shared/build/outputs/apk/debug/shared-debug.apk"
```

**iOS (chạy trên GitHub Actions, ~25-35 phút):**
```
cd /c/dev/notely-repo
git add -A && git commit -m "mô tả thay đổi"
git push fork feature/vietnamese-ai-notes
gh workflow run build-ios-unsigned.yml --repo hoangvi1810-jpg/NotelyVoice --ref feature/vietnamese-ai-notes
```
Xem tiến trình: `gh run watch <run-id> --repo hoangvi1810-jpg/NotelyVoice --exit-status`
Tải kết quả: `gh run download <run-id> --repo hoangvi1810-jpg/NotelyVoice --dir <thư mục>`

**Kiểm tra dữ liệu trong DB của emulator (khi cần debug):**
```
"$ANDROID_HOME/platform-tools/adb.exe" shell run-as com.module.notelycompose.android \
  sqlite3 databases/notes.db "SELECT * FROM notebookEntity;"
```

---

## Mô hình dữ liệu sổ tay (để nhớ khi sửa code liên quan)

- `notebookEntity(id, name, created_at)` — danh sách sổ tay.
- `noteNotebookEntity(note_id PK, notebook_id)` — bảng nối, **note_id là khoá chính** nên
  mỗi note chỉ thuộc **đúng 1 sổ**. Không có note_id trong bảng này = "Chưa phân loại".
- **Không có cột phân biệt loại note** (voice vs tự gõ). Phân biệt bằng
  `recordingPath` rỗng hay không (`isVoice`) — note ghi âm luôn có đường dẫn file ghi âm,
  note tự gõ thì không.
- `NoteDetailScreen.kt` dùng chung cho cả 2 loại note — phân nhánh UI theo cờ
  `hasAiContext = recording.isRecordingExist || aiUiState.hasGenerated`, **không phải** 2
  màn hình riêng biệt như đã tưởng lúc đầu.

---

## Lỗi thật đã tìm và sửa trong dự án (lịch sử, để tham khảo)

- Pod `ffmpegkit-kmp-ios` bị CocoaPods gỡ bỏ (FFmpegKit khai tử 4/2025) — xoá khỏi Podfile.
- Ktor 3.5.2 không tương thích Kotlin 2.2.0 của project — chốt dùng bản 3.2.3.
- Model OpenRouter alias đúng là `~google/gemini-flash-latest` (có dấu ngã đầu).
- Build Android trên Windows vỡ vì đường dẫn có dấu cách (`CLAUDE CODE`) — cờ
  `-ffile-prefix-map` của Clang tách sai tham số. Đã copy code sang `C:\dev\notely-repo`
  (không dấu cách, ngoài OneDrive).
- `ModelSelectionScreen.kt` hiện sai dung lượng model tiếng Việt (465MB thay vì 547MB thật).
- **Crash thật trên iOS khi mở Settings**: `SecureKeyStore.ios.kt` lệch retain/release khi
  build `NSMutableDictionary` cho Keychain. Viết lại bằng `CFMutableDictionary` thuần
  (`CFDictionaryCreateMutable`/`CFDictionarySetValue`), chỉ `CFBridgingRetain` những giá trị
  tự tạo (không phải hằng số `kSec*`).
- **Bug dán (paste) không hoạt động trên iOS** (JetBrains/compose-multiplatform#4502): CMP
  1.8.2 làm mất menu dán của iOS trên bất kỳ `BasicTextField` nào có `VisualTransformation`
  khác null. Sửa bằng cách chỉ gắn transformation khi note thật sự có định dạng đang áp dụng.
- **Đính kèm file vào note mới tạo không có tác dụng**: note chưa gõ gì thì chưa được lưu
  vào DB (`currentNoteId` vẫn là 0), nên `AttachmentViewModel.pick()` bị chặn sớm vì thiếu
  noteId. Sửa bằng hàm `ensureNoteSaved()` — ép lưu note trước khi mở bảng chọn file.
- Dữ liệu test trên emulator từng mất 1 sổ tay ("Nhật ký") ở một phiên làm việc trước — đã
  xác nhận **không phải do code gây ra** (mọi đường xoá sổ đều yêu cầu 2 lần xác nhận), khôi
  phục lại bằng lệnh SQL trực tiếp trên emulator, chỉ để tiếp tục test — không phải bản vá
  code.

## Việc còn thiếu / chưa làm (thành thật, không giấu)

- Tag AI sinh ra có lưu vào máy nhưng **chưa hiển thị** ở màn hình danh sách note.
- Đợt 4 (rich text thật: heading, bullet thật, không chỉ in đậm/nghiêng/gạch chân) — chưa
  làm, chưa lên kế hoạch chi tiết.
- Nút "tóm tắt nhanh" trên màn ghi âm (khác tab Summary) vẫn dùng công nghệ cũ, không hỗ trợ
  tiếng Việt — cố ý giữ nguyên để tiết kiệm chi phí gọi API cho một tính năng phụ.

## Thông tin kỹ thuật

- Repo fork: https://github.com/hoangvi1810-jpg/NotelyVoice (nhánh
  `feature/vietnamese-ai-notes`)
- Code làm việc thật, duy nhất: **`C:\dev\notely-repo`** (xem cảnh báo đầu file)
- Sideloadly: `C:\Users\hoang\AppData\Local\Sideloadly\sideloadly.exe`
- Android SDK/JDK/emulator: `C:\dev-tools\` (JDK `jdk-17.0.13+11`, SDK `android-sdk`), AVD
  tên `NotelyTest` (Pixel 6, Android 14)
