# Thiết kế / UI — Notely Voice

Cập nhật 10/9/2026. Ghi lại các quyết định thiết kế đã chốt, để không hỏi lại hay làm sai hướng đã thống nhất.

---

## Nguyên tắc cốt lõi: 2 thế giới tách biệt hoàn toàn

App có 2 luồng độc lập, **không được lẫn vào nhau ở bất kỳ đâu**:

1. **Ghi âm (voice-first)** — vào từ nút mic tròn (FAB) ở màn Home. Note tạo ra từ đây:
   - Có nút mic để ghi âm.
   - Có 4 tab AI Note / Highlights / Summary / Transcript (khi đã có bản ghi hoặc đã tạo AI).
   - **Không có** badge sổ tay, **không có** nút đính kèm/dán ảnh.
2. **Note gõ tay (notebook)** — vào từ hamburger drawer → chọn 1 sổ tay → nút "+" trong màn sổ tay đó. Note tạo ra từ đây:
   - Tự động gán vào đúng sổ tay đang mở, hiện badge tên sổ (VD "Nhật ký").
   - Có nút "Đính kèm" (chọn Ảnh/Video/PDF qua picker) và nút "Dán ảnh" (đọc thẳng từ clipboard).
   - **Không có** nút mic ghi âm.

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
- `isNotebookNote = pendingNotebook != null || currentNotebookId != null` → quyết định hiện badge sổ tay + đính kèm (true) hay nút mic (false).

Tiêu đề tách biệt khỏi nội dung (gõ tiêu đề riêng, không tự lấy dòng đầu nội dung ghi đè — trừ khi tiêu đề đang trống thì mới tự động lấy dòng đầu làm gợi ý).

---

## Đính kèm ảnh/file

### Thẻ đính kèm — ảnh hiện thumbnail thật
Ban đầu chỉ hiện icon chung chung (Image/PlayArrow/Description) + tên file. Đã nâng cấp: ảnh (`AttachmentKind.IMAGE`) decode byte thật thành `ImageBitmap` (qua `org.jetbrains.compose.resources.decodeToImageBitmap`) và hiện đúng ảnh, `ContentScale.Crop` lấp đầy thẻ 88×96dp. Video/PDF vẫn giữ icon (không làm thumbnail — không đáng công sức thêm thư viện).

**Quyết định UI đã hỏi & chốt** (nhưng chưa triển khai đầy đủ — vẫn đang ở dạng thẻ nhỏ nằm ngang): về lâu dài user muốn ảnh **to, xếp dọc dưới văn bản** (kiểu Evernote/Notion) thay vì thẻ vuông nhỏ nằm ngang. Việc này **chưa làm** — ghi chú lại để làm tiếp khi có thời gian, không phải quên.

### "Dán ảnh" — tính năng mới quan trọng nhất trong đợt sửa 9-10/9/2026
User muốn trải nghiệm giống dán ảnh vào Word: copy ảnh ở đâu đó → vào note → dán → ảnh hiện ra ngay, **không phải** qua menu "Đính kèm > Ảnh > chọn từ thư viện" (3 bước).

Giải pháp đã chọn (cân nhắc kỹ, không đánh cược vào API chưa chắc chắn hoạt động trên iOS — xem lý do bên dưới): thêm nút **"Dán ảnh"** riêng, đứng cạnh "Đính kèm", **1 chạm**:
- Đọc thẳng ảnh đang có trong clipboard hệ thống (Android: `ClipboardManager` + kiểm tra `ClipData` có URI ảnh; iOS: `UIPasteboard.generalPasteboard.image`).
- Lưu vào storage app (giống hệt luồng đính kèm bình thường), gắn vào note ngay lập tức.
- Không tìm thấy ảnh trong clipboard → im lặng không làm gì (chưa có thông báo lỗi dạng snackbar — biết là thiếu, chấp nhận do thời gian gấp).

**Vì sao không hook thẳng vào gesture "Dán" (paste) gốc của hệ điều hành thay vì thêm nút riêng?** Đã cân nhắc dùng `Modifier.receiveContent` (Compose Foundation ReceiveContent API) để bắt sự kiện dán ảnh ngay trong `BasicTextField`, đúng chuẩn "như Word" 100%. Nhưng API này khá mới, chưa chắc chắn đã có mặt đầy đủ trên target iOS của Compose Multiplatform 1.8.2, và **không thể verify tại chỗ** (không có Mac). Đánh cược vào 1 API chưa chắc + không test được = rủi ro cao, đặc biệt sau khi đã tốn 4 lần build fail vì lỗi cinterop khác trong đúng phiên này. Chọn phương án "nút Dán ảnh" vì: chắc chắn hoạt động (dùng đúng API clipboard đơn giản, đã có precedent), triển khai nhanh, chỉ tốn 1 lần chạm thêm so với "Ctrl+V" thật.

Nếu sau này muốn nâng cấp lên dán thật (không cần bấm nút), thử `Modifier.receiveContent` — nhưng phải test được trên thiết bị iOS thật trước khi commit vào luồng chính, tránh lặp lại vòng build-fail như phiên này.

---

## Font, logo, splash (đã xong, ổn định)

- Font: Be Vietnam Pro toàn app (thay Poppins cũ), tải từ Google Fonts (SIL OFL), đủ 4 style (Regular/Medium/SemiBold/Bold).
- Logo: Shiba Inu màu cam, nền kem `#FAEBD3`-ish, phóng đầy khung, đủ mọi size Android (mipmap) + iOS (AppIcon.appiconset, logo-1024.imageset).
- Splash: xoá `UILaunchScreen` dict rỗng trong `Info.plist` (từng gây kẹt ảnh splash cũ do khai 2 key mâu thuẫn `UILaunchScreen` + `UILaunchStoryboardName`), chỉ giữ storyboard.

## Theme màu

Tông tím (không phải be/caramel ban đầu) — thẻ trắng nổi trên nền tím nhạt, bo góc lớn, bóng đổ mềm, hiệu ứng bấm "giọt nước" (nhún + nảy nhẹ) áp dụng toàn app.

---

## Việc còn thiếu / chưa làm (thành thật)

- Ảnh đính kèm to + xếp dọc dưới văn bản (đã chốt hướng, chưa code).
- Thông báo khi "Dán ảnh" không tìm thấy ảnh trong clipboard (hiện im lặng no-op).
- Rich text thật (heading, bullet thật) — hiện vẫn dùng in đậm/nghiêng/gạch chân qua `VisualTransformation` dạng span, chưa có cấu trúc đoạn thật.
- Tag AI sinh ra chưa hiển thị ở danh sách note.
