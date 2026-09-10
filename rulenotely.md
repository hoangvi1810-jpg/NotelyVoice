# Quy tắc kỹ thuật — Notely Voice

Cập nhật 10/9/2026 (phiên rich-text editor + auto-paste ảnh). Đọc file này trước khi sửa code, tránh lặp lại lỗi đã tốn nhiều giờ mới tìm ra.

---

## 1. Chỉ có 1 repo code thật

**`C:\dev\notely-repo`** — đây là nơi duy nhất được sửa code. Chấm hết.

Có một bản copy cũ ở `C:\Users\hoang\OneDrive\Desktop\CLAUDE CODE\notely repo` — **đã lỗi thời hàng chục commit**, không dùng nữa. Nó còn tồn tại vì lúc đầu chưa biết phải copy ra ngoài OneDrive (đường dẫn có dấu cách + nằm trong OneDrive làm vỡ build C++ của thư viện Whisper — cờ `-ffile-prefix-map` bị Clang tách sai tham số). Không xoá (đề phòng), nhưng **không bao giờ sửa code ở đó**.

**Bẫy đã gặp nhiều lần**: terminal (tool Bash) có xu hướng tự reset thư mục làm việc về bản OneDrive giữa các lệnh, dù đã `cd /c/dev/notely-repo` ở lệnh trước. **Luôn gõ full path hoặc `cd` lại ở đầu mỗi lệnh quan trọng**, đừng tin `cd` từ lệnh trước còn hiệu lực. Nếu nghi ngờ "mình sửa cái này rồi mà sao không thấy" — chạy `git log --oneline -5` ở cả hai thư mục để so sánh.

---

## 2. Quy tắc Kotlin/Native cinterop: category member cần import tường minh

**Đây là quy tắc quan trọng nhất rút ra từ 4 lần build iOS liên tiếp thất bại trong 1 phiên (10/9/2026).**

Khi một hàm/thuộc tính Objective-C được khai báo trong một **category** (extension của class, ví dụ `@interface NSData (NSDataCreation)` hay `@interface UIPasteboard (UIPasteboardDataProvider)`) thay vì trong `@interface` chính của class, Kotlin/Native **luôn** expose nó thành **top-level extension function/property**, không phải thành viên của class. Gọi `ClassName.memberName(...)` sẽ báo `error: Unresolved reference 'memberName'` **trừ khi** bạn `import platform.<Framework>.<memberName>` tường minh.

Đã gặp thực tế (đều là category members trong Foundation/UIKit):
- `NSData.dataWithContentsOfFile(...)` — không tồn tại luôn, phải đổi hướng dùng biến thể URL.
- `NSData.dataWithContentsOfURL(...)` — cần `import platform.Foundation.dataWithContentsOfURL`.
- `NSData.writeToURL(url, atomically = true)` — cần `import platform.Foundation.writeToURL` (và dùng named param `atomically =`, không phải positional).
- `UIPasteboard.generalPasteboard.image` — chủ động thêm `import platform.UIKit.image` trước khi build (chưa xác nhận có cần hay không, nhưng theo đúng quy tắc này nên thêm phòng ngừa).

**Cách áp dụng khi viết code iOS mới:**
1. Trước khi dùng bất kỳ hàm Foundation/UIKit nào có dạng "xWithY:", "writeToZ:", hay bất kỳ thứ gì "nghe như category" — tìm xem đã có file `.ios.kt` nào trong repo dùng đúng hàm đó chưa (`grep -rn "tenHam" shared/src/iosMain`), copy chính xác cách import.
2. Nếu chưa từng dùng trong repo, cứ giả định nó cần import riêng và thêm `import platform.<Framework>.<tenHam>` ngay từ đầu — rẻ hơn nhiều so với chờ 5-6 phút CI fail rồi mới biết.
3. **Không có cách nào build/verify iOS tại chỗ** (máy này không có Mac/Xcode) — mọi lỗi loại này chỉ lộ ra sau khi CI chạy xong bước `compileKotlinIosArm64`, mất ~5 phút mỗi lần thử. Cẩn thận từ đầu tiết kiệm rất nhiều thời gian.

---

## 3. Kiến trúc dữ liệu: sổ tay (notebook) vs ghi âm

- `notebookEntity(id, name, created_at)` — danh sách sổ tay, seed sẵn 3 sổ: Nhật ký, Ý tưởng, Công việc.
- `noteNotebookEntity(note_id PK, notebook_id)` — bảng nối, **note_id là khoá chính** nên mỗi note chỉ thuộc đúng 1 sổ. Không có dòng = "Chưa phân loại".
- **Không có cột phân biệt loại note** trong `notesEntity`. Phân biệt ghi âm vs gõ tay hoàn toàn dựa vào `recordingPath` rỗng hay không (`isVoiceNote()` helper — tìm và dùng lại hàm này, đừng viết lại).
- Migration dùng file `.sqm` thật (`1.sqm`, `2.sqm` trong `shared/src/commonMain/sqldelight/database/`) — **không** dùng hướng "tự vá DDL runtime" đã từng cân nhắc rồi bỏ.

### Bẫy đã gặp: `NoteListViewModel` dùng chung giữa Home và NotebookNotesScreen

`NoteListScreen` (Home) và `NotebookNotesScreen` (màn 1 sổ tay) đều gọi `koinViewModel<NoteListViewModel>()` — Koin trả về **cùng 1 instance** trong cùng nav graph. Nếu lọc dữ liệu (ví dụ "chỉ hiện note ghi âm") ngay trong `NoteListViewModel.handleNotesUpdate()` (tầng state dùng chung), nó sẽ làm rỗng luôn dữ liệu mà `NotebookNotesScreen` cần (note gõ tay) — **đã tự gây bug này 1 lần**, màn sổ tay hiện "No Notes Yet" dù DB có dữ liệu.

**Quy tắc**: `NoteListViewModel`'s state (`originalNotes`, `filteredNotes`, `allNotesSizeStr`, `showEmptyContent`) phải luôn giữ **toàn bộ** note (voice + typed). Lọc riêng cho từng màn hình (voice-only cho Home, notebook-only cho NotebookNotesScreen) làm ở **UI call site**, không làm trong ViewModel.

### Tách biệt UI ghi âm vs note gõ tay (yêu cầu đã chốt, dễ tái phạm)

`NoteDetailScreen.kt` dùng chung cho cả 2 loại note. Cờ `isNotebookNote = pendingNotebook != null || currentNotebookId != null` quyết định:
- **Chỉ hiện khi `isNotebookNote == true`**: `NotebookRow` (badge sổ tay), `NotebookRichEditor` (thay cho `NoteEditor`), `AttachmentStrip` (ảnh đã dán, xem mục 4).
- **Chỉ hiện khi `isNotebookNote == false`**: nút mic FAB (ghi âm), `NoteEditor` (BasicTextField cũ), panel Title/Heading/Subheading cũ (`FormatBar`), 2 icon "Aa"/bullet-list ở `BottomNavigationBar` (đã gate bằng param `isNotebookNote` mới thêm vào `BottomNavigationBar`).

Nếu thêm tính năng mới vào `NoteDetailScreen`, luôn tự hỏi: tính năng này thuộc về note ghi âm hay note gõ tay? Đừng để nó hiện ở cả 2 — đó chính xác là thứ user đã bực mình nhiều lần trong phiên 9-10/9/2026.

---

## 4. Rich-text editor + dán ảnh tự động cho note gõ tay (thêm 10/9/2026)

### Kiến trúc
- Thư viện: `com.mohamedrejeb.richeditor:richeditor-compose` (`gradle/libs.versions.toml` key `richEditor = "1.0.0-rc13"`), thêm vào `shared/build.gradle.kts` khối `commonMain.dependencies`. Bản này build với Kotlin 2.1.21 + Compose 1.8.2 — khớp version app đang pin (Kotlin 2.2.0, CMP 1.8.2), verify qua CI thành công.
- `NotebookRichEditor` (`notebook/ui/NotebookRichEditor.kt`) — composable mới, chỉ dùng cho note gõ tay. **Không sửa `NoteEditor` cũ** (dòng ~805 `NoteDetailScreen.kt`) — nó vẫn phục vụ note ghi âm y nguyên.
- Lưu HTML ở bảng mới `richContentEntity(note_id PK, html)` (`sqldelight/database/richContent.sq`, migration `3.sqm` — nối tiếp `1.sqm`/`2.sqm`, chỉ `CREATE TABLE`, không đụng bảng cũ). Đồng thời mirror plain text vào `notesEntity.content` như cũ (qua `TextEditorViewModel.onUpdateContent`) — search/AI/export không cần sửa gì.
- `RichContentRepository`/`RichContentViewModel` (package `notebook/`) — theo đúng khuôn `AttachmentRepository`/`AttachmentViewModel`, đăng ký Koin trong `Modules.kt`. `DeleteNoteById` đã thêm bước dọn `richContentEntity` khi xoá note (giống pattern dọn `attachmentEntity`).

### Bẫy đã gặp và đã fix: mất nội dung khi mở lại note
`rememberRichTextState()` luôn khởi tạo rỗng. Composable mount → `LaunchedEffect(state.annotatedString)` bắn ngay lập tức với state rỗng, TRƯỚC KHI HTML đã lưu load xong từ DB (load là bất đồng bộ). Nếu không chặn, giá trị rỗng này bị lưu đè lên nội dung thật ~500ms sau (do debounce) — **note mất sạch nội dung khi mở lại, không cần gõ gì**.

Đã fix bằng cờ `readyToPersist` trong `NotebookRichEditor`: chỉ cho phép gọi `onHtmlChange`/`onPlainTextChange` khi (a) cha đã xác nhận load xong (`initialHtml != null`, kể cả khi là chuỗi rỗng `""` nghĩa là "đã load, xác nhận không có gì") HOẶC (b) người dùng đã thật sự gõ nội dung (`state.annotatedString.text.isNotEmpty()`) — case này cho note hoàn toàn mới, chưa từng gọi `load()`. **Nếu sửa lại luồng load/save của editor này, bắt buộc giữ nguyên logic chống-đè kiểu này**, không được bỏ qua "chờ load xong mới cho lưu".

Liên quan: `LaunchedEffect(currentNoteId, isNotebookNote)` trong `NoteDetailScreen.kt` (gọi `richContentViewModel.load(id)`) phải key theo **cả 2** biến, không chỉ `currentNoteId`. Lý do: mở 1 note gõ tay có sẵn, `currentNoteId` đã đúng ngay từ đầu, nhưng `isNotebookNote` (phụ thuộc `notebookState.assignments` load bất đồng bộ từ DB) có thể còn `false` ở khoảnh khắc effect chạy lần đầu — nếu chỉ key theo `currentNoteId`, effect không bao giờ chạy lại khi `isNotebookNote` chuyển `true`, khiến `richContentViewModel.load()` không bao giờ được gọi.

### Dán ảnh tự động — đã xoá nút "Dán ảnh"/"Đính kèm", xoá cả picker Video/PDF
User yêu cầu rõ: không cần nút, chạm vào ô văn bản là ảnh trong clipboard tự dán vào note. Đã xoá luôn `AttachmentPickerMenu`, `showAttachmentMenu`, panel Title/Heading/Subheading cũ theo yêu cầu (thấy thừa, chiếm chỗ).

- `AttachmentViewModel.autoPasteFromClipboardIfNew()` (thay thế `pasteImageFromClipboard()` cũ) — gọi từ `NotebookRichEditor`'s `onFocusChange` khi field nhận focus.
- Chống dán lặp: `ClipboardImageReader.fingerprint()` (method mới trên expect/actual class) — Android trả URI string, iOS trả `UIPasteboard.generalPasteboard.changeCount.toString()`. So với fingerprint lần dán tự động gần nhất, trùng thì bỏ qua.
- `AttachmentStrip` viết lại: ảnh full-width xếp dọc (không còn thẻ nhỏ nằm ngang + nút "+"), chỉ hiện `AttachmentKind.IMAGE` (video/PDF không còn đường vào từ note gõ tay vì picker đã xoá).

### Lưu ý cinterop mới trong phiên này
`UIPasteboard.generalPasteboard.changeCount` — theo đúng quy tắc mục 2, đã chủ động thêm `import platform.UIKit.changeCount` trước khi build (category member), build CI qua thành công lần đầu.

---

## 5. Quy trình build & test bắt buộc trước khi push iOS

1. Sửa code trong `C:\dev\notely-repo`.
2. Build + cài Android emulator (`NotelyTest`) trước — nhanh (~20-45s), bắt được phần lớn lỗi logic/UI chung giữa 2 nền tảng.
3. Verify bằng `adb shell uiautomator dump` + đọc `bounds` để tap chính xác — **đừng đoán toạ độ từ ảnh chụp màn hình**, tỷ lệ scale giữa ảnh hiển thị (thường 900×2000) và độ phân giải thật (1080×2400) gây sai số liên tục nếu nhân nhầm hệ số.
4. Chỉ khi Android verify xong mới commit/push/trigger CI iOS.
5. Với code chỉ có ở `iosMain` (không build được ở bước 2) — tự soát kỹ theo quy tắc mục 2 trước khi push, chấp nhận rủi ro CI fail nhưng giảm thiểu tối đa.
6. CI build mất ~25-36 phút nếu qua được bước biên dịch; nếu fail do lỗi biên dịch thường lộ ra rất nhanh (~5-6 phút).

```bash
# Android
cd /c/dev/notely-repo
export JAVA_HOME="/c/dev-tools/jdk-17.0.13+11"
export ANDROID_HOME="/c/dev-tools/android-sdk"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew.bat :shared:assembleDebug --console=plain

export MSYS_NO_PATHCONV=1
"$ANDROID_HOME/platform-tools/adb.exe" install -r "C:/dev/notely-repo/shared/build/outputs/apk/debug/shared-debug.apk"

# iOS (chỉ khi Android đã ổn)
git add -A && git commit -m "..."
git push fork feature/vietnamese-ai-notes
gh workflow run build-ios-unsigned.yml --repo hoangvi1810-jpg/NotelyVoice --ref feature/vietnamese-ai-notes
gh run view <run-id> --repo hoangvi1810-jpg/NotelyVoice --log-failed   # nếu fail
```

---

## 6. Việc khác cần nhớ

- **Emulator test data không đáng tin cậy tuyệt đối** — nhiều vòng cài đè/`pm clear` trong 1 phiên dài có thể để lại DB ở trạng thái version lệch (`Can't downgrade database from version X to Y`). Nếu gặp crash kiểu này trên emulator, `pm clear` là cách nhanh nhất để loại trừ nguyên nhân "dữ liệu test cũ" trước khi nghi ngờ code.
- **Xoá sổ tay không xoá note** — note bên trong chuyển về "Chưa phân loại". Không có cascade-delete, không có undo.
- Bug đã biết của Compose Multiplatform 1.8.2 trên iOS (JetBrains/compose-multiplatform#4502): `BasicTextField` có `VisualTransformation` khác null sẽ mất menu "Dán" native của iOS. Chỉ gắn `VisualTransformation` khi note thực sự có định dạng đang áp dụng.
