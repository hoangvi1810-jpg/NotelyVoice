# Thiết kế / UI — Notely Voice

Cập nhật 10/9/2026 (phiên rich-text editor + auto-paste ảnh). Ghi lại các quyết định thiết kế đã chốt, để không hỏi lại hay làm sai hướng đã thống nhất.

---

## Nguyên tắc cốt lõi: 2 thế giới tách biệt hoàn toàn

App có 2 luồng độc lập, **không được lẫn vào nhau ở bất kỳ đâu**:

1. **Ghi âm (voice-first)** — vào từ nút mic tròn (FAB) ở màn Home. Note tạo ra từ đây:
   - Có nút mic để ghi âm.
   - Có 4 tab AI Note / Highlights / Summary / Transcript (khi đã có bản ghi hoặc đã tạo AI).
   - **Không có** badge sổ tay, **không có** nút đính kèm/dán ảnh.
2. **Note gõ tay (notebook)** — vào từ hamburger drawer → chọn 1 sổ tay → nút "+" trong màn sổ tay đó. Note tạo ra từ đây:
   - Tự động gán vào đúng sổ tay đang mở, hiện badge tên sổ (VD "Nhật ký").
   - Dùng **rich-text editor thật** (`NotebookRichEditor`, xem mục riêng bên dưới) — không dùng `NoteEditor` (BasicTextField) của note ghi âm.
   - Dán ảnh **tự động khi chạm vào ô văn bản** — không có nút "Đính kèm" hay "Dán ảnh" nào cả (đã xoá hẳn, xem mục "Đính kèm ảnh/file" bên dưới).
   - **Không có** nút mic ghi âm, **không có** panel Title/Heading/Subheading cũ (đã xoá, xem bên dưới).

Màn Home chỉ liệt kê note ghi âm (`recordingPath` không rỗng). Note gõ tay **không bao giờ** xuất hiện ở Home — chỉ thấy trong đúng màn sổ tay của nó (hoặc "Tất cả sổ" trong drawer nếu muốn xem gộp — cân nhắc thêm sau, hiện tại mỗi sổ có màn riêng).

Lý do tách biệt: user từng báo lỗi tạo bản ghi âm mới ảnh hưởng tới sổ tay đang có — điều tra ra không phải bug xoá dữ liệu thật, mà là do UI 2 luồng quá gần nhau/lẫn lộn gây cảm giác nhầm lẫn. Giải pháp là tách biệt **cả về điều hướng lẫn UI**, không chỉ dữ liệu.

---

## Hamburger drawer

Thay cho hàng chip lọc sổ tay nằm ngang trên đầu màn Home (bị rối mắt khi có nhiều sổ). Bấm icon 3 gạch góc trên trái để mở.

Nội dung drawer (từ trên xuống):
1. Tiêu đề "Sổ tay"
2. "Tất cả sổ" (icon Menu)
3. Danh sách sổ tay, mỗi dòng: icon sách + `"${tên} (${số lượng note})"`
4. Ô nhập inline "Tạo sổ mới…" + nút "+"
5. "Quản lý sổ" → mở `NotebookManagerSheet` (đổi tên/xoá sổ)

Bấm vào 1 sổ → đóng drawer → điều hướng tới `NotebookNotesScreen(notebookId)`, màn hình **độc lập hoàn toàn**, không dính UI ghi âm.

---

## Màn chi tiết note (`NoteDetailScreen`)

Dùng chung code cho cả 2 loại note (không tách 2 file riêng), phân nhánh UI bằng cờ:
- `hasAiContext = recording.isRecordingExist || aiUiState.hasGenerated` → quyết định có hiện 4 tab AI hay không.
- `isNotebookNote = pendingNotebook != null || currentNotebookId != null` → quyết định:
  - Hiện badge sổ tay (`NotebookRow`), dùng `NotebookRichEditor` thay vì `NoteEditor`, hiện `AttachmentStrip` (ảnh đã dán) → khi `true`.
  - Hiện nút mic FAB, panel Title/Heading/Subheading cũ, 2 icon "Aa"/bullet-list ở bottom bar → khi `false`.

Tiêu đề tách biệt khỏi nội dung (gõ tiêu đề riêng, không tự lấy dòng đầu nội dung ghi đè — trừ khi tiêu đề đang trống thì mới tự động lấy dòng đầu làm gợi ý).

---

## Rich-text editor cho note gõ tay (mới, 10/9/2026)

Note gõ tay giờ dùng `NotebookRichEditor` (`notebook/ui/NotebookRichEditor.kt`), bọc thư viện `com.mohamedrejeb.richeditor:richeditor-compose` (bản `1.0.0-rc13`, build với Kotlin 2.1.21 + Compose 1.8.2 — khớp version app đang dùng). Note ghi âm **không đụng tới** — vẫn dùng `NoteEditor` (BasicTextField) cũ y nguyên.

- Toolbar riêng nổi lên trên khi focus vào ô văn bản: B / I / U / T (heading giả bằng cỡ chữ to+đậm) / bullet list — thay thế hoàn toàn panel "Title/Heading/Subheading/Body" cũ (đã xoá, xem lý do bên dưới).
- Lưu 2 bản song song mỗi lần đổi nội dung (debounce 500ms):
  - HTML (`richTextState.toHtml()`) → bảng mới `richContentEntity(note_id PK, html)`.
  - Plain text mirror → vẫn ghi vào `notesEntity.content` qua đúng `TextEditorViewModel.onUpdateContent` như trước — để tìm kiếm/AI/xuất PDF không cần sửa gì.
- **Bẫy đã gặp và đã fix**: `rememberRichTextState()` luôn khởi tạo rỗng, nên khi mở lại 1 note đã có nội dung, có một khoảnh khắc rất ngắn trạng thái rỗng này bị lưu đè lên nội dung thật (do load HTML từ DB là bất đồng bộ, chạy sau). Hậu quả thực tế: mở lại note là mất sạch nội dung đã lưu. Đã fix bằng cờ `readyToPersist` trong `NotebookRichEditor` — chỉ cho phép lưu khi đã nhận được tín hiệu load xong từ cha (`initialHtml != null`) HOẶC người dùng đã thật sự gõ gì đó (`text.isNotEmpty()`). Nếu sau này sửa lại cơ chế load/save của editor này, nhớ giữ nguyên logic chống-đè này.

## Đính kèm ảnh/file — đã đổi hoàn toàn hướng (10/9/2026)

### Đã xoá: nút "Đính kèm", nút "Dán ảnh", picker chọn Ảnh/Video/PDF, panel Title/Heading/Subheading cũ
User phản hồi rõ ràng các UI này **thừa và chiếm chỗ** sau khi có tính năng dán ảnh tự động (xem mục dưới) — đã xoá hẳn khỏi note gõ tay, không giữ lại dạng ẩn. `AttachmentPickerMenu` không còn được gọi từ đâu trong `NoteDetailScreen.kt` nữa (file component vẫn còn tồn tại trong repo, chỉ là không dùng — không xoá file vì risk thấp hơn giữ lại so với động vào cross-platform picker code).

### Dán ảnh tự động khi chạm vào ô văn bản — không cần bấm nút
User muốn trải nghiệm giống dán ảnh vào Word: copy ảnh ở đâu đó → chạm vào note → ảnh tự xuất hiện, không qua bất kỳ menu/nút nào.

Cơ chế đã cài (thay thế hoàn toàn nút "Dán ảnh" cũ): `NotebookRichEditor`'s `onFocusChange` khi field nhận focus (`isFocused == true`) sẽ gọi `AttachmentViewModel.autoPasteFromClipboardIfNew()`:
- Đọc `ClipboardImageReader.fingerprint()` trước (Android: URI dạng string; iOS: `UIPasteboard.generalPasteboard.changeCount`) — nếu trùng với lần dán tự động gần nhất thì **bỏ qua**, tránh dán lặp lại cùng 1 ảnh mỗi lần focus lại vào field.
- Fingerprint mới → đọc + copy ảnh thật (`ClipboardImageReader.read()`), gắn vào note.
- Không có ảnh trong clipboard → im lặng không làm gì (vẫn chưa có snackbar báo lỗi khi cần — biết là thiếu).

### Cập nhật: thêm lại nút "Dán" thủ công (thay cho chỉ auto-paste-on-focus)
Auto-paste-on-focus (mục trên) không đủ tin cậy trong thực tế test: nếu ô văn bản đã đang focus sẵn (bàn phím đang mở) khi user copy ảnh mới ở nơi khác, không có sự kiện focus-change nào để kích hoạt kiểm tra clipboard. Ngoài ra phát hiện `richeditor-compose` (bản rc13, chưa ra 1.0 chính thức) **không hỗ trợ gesture long-press chọn/copy/dán văn bản chuẩn của Android/iOS** — long-press vào chữ không hiện bong bóng Copy/Paste nào cả.

→ Thêm lại nút **"Dán"** (icon clipboard, luôn hiện phía trên ô văn bản note tay) xử lý cả 2 trong 1 lần bấm: đọc clipboard text (nếu có, ghép vào cuối nội dung hiện tại — **không chèn đúng vị trí con trỏ**, giới hạn đã chấp nhận) và đọc clipboard ảnh (nếu có, đính kèm). Đây là fallback chắc chắn hoạt động, không phụ thuộc gesture của thư viện rich-text.

**Vì sao không chèn ảnh thật vào giữa đoạn văn (đúng vị trí con trỏ) như Word?** Đã cân nhắc `Modifier.receiveContent` (Compose Foundation) và API insert-ảnh-inline của `richeditor-compose`, nhưng cả hai đều là API mới/chưa xác nhận hoạt động ổn định trên iOS target của CMP 1.8.2, và **không thể verify tại chỗ** (không có Mac). Chọn phương án "ảnh luôn xếp dưới văn bản" vì chắc chắn hoạt động (đã build+test qua CI thành công), không đánh cược thêm 1 vòng build-fail. Nếu sau này muốn nâng cấp lên chèn ảnh đúng vị trí con trỏ — phải test được trên thiết bị iOS thật trước khi commit vào luồng chính.

### Hiển thị ảnh: to, xếp dọc dưới văn bản (đã triển khai xong, không còn là "chưa làm")
`AttachmentStrip` viết lại hoàn toàn: không còn hàng thẻ nhỏ 88×96dp nằm ngang + nút "+", giờ là `Column` xếp dọc, mỗi ảnh full-width (`ContentScale.FillWidth`), bo góc 16dp. Chỉ hiện ảnh (`AttachmentKind.IMAGE`) — video/PDF không còn đường vào từ note gõ tay (picker đã xoá, xem mục trên). Chạm để mở full-screen (qua `AttachmentOpener`, dùng app xem ảnh hệ thống), chạm giữ để hiện nút xoá.

---

## Font, logo, splash (đã xong, ổn định)

- Font: Be Vietnam Pro toàn app (thay Poppins cũ), tải từ Google Fonts (SIL OFL), đủ 4 style (Regular/Medium/SemiBold/Bold).
- Logo: Shiba Inu màu cam, nền kem `#FAEBD3`-ish, phóng đầy khung, đủ mọi size Android (mipmap) + iOS (AppIcon.appiconset, logo-1024.imageset).
- Splash: xoá `UILaunchScreen` dict rỗng trong `Info.plist` (từng gây kẹt ảnh splash cũ do khai 2 key mâu thuẫn `UILaunchScreen` + `UILaunchStoryboardName`), chỉ giữ storyboard.

## Theme màu

Tông tím (không phải be/caramel ban đầu) — thẻ trắng nổi trên nền tím nhạt, bo góc lớn, bóng đổ mềm, hiệu ứng bấm "giọt nước" (nhún + nảy nhẹ) áp dụng toàn app.

---

## Việc còn thiếu / chưa làm (thành thật)

- Thông báo khi dán ảnh tự động không tìm thấy ảnh trong clipboard (hiện im lặng no-op).
- Ảnh chèn đúng vị trí con trỏ giữa đoạn văn (hiện luôn xếp dưới toàn bộ văn bản) — rủi ro cao trên iOS, cố tình hoãn (xem mục "Đính kèm ảnh/file" ở trên).
- Video/PDF không còn đường đính kèm vào note gõ tay nữa (đã xoá picker theo yêu cầu user) — nếu sau này cần lại, phải build UI mới, không phải bật lại `AttachmentPickerMenu` cũ vì `showAttachmentMenu`/`onAddAttachmentClick` đã bị xoá khỏi `NoteDetailScreen.kt`.
- Tag AI sinh ra chưa hiển thị ở danh sách note.
- Rich text: đã có bold/italic/underline/heading(giả)/bullet thật qua `richeditor-compose`. Chưa có: numbered list, đổi màu chữ, chèn ảnh inline (xem mục trên).
