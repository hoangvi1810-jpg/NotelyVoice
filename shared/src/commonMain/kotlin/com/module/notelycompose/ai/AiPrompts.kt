package com.module.notelycompose.ai

/**
 * System prompts for every AI generation call. Written and requested in Vietnamese since the
 * app's primary transcription language is Vietnamese and the local TF-IDF summarizer
 * (see `summary/TFIDFSummarizer.kt`) has no Vietnamese support at all — this is its replacement.
 *
 * Every prompt explicitly pins the output language, independent of the note's actual spoken
 * language, since a mixed-language transcript (e.g. English terms inside Vietnamese speech)
 * should still produce consistent prose. The one exception is the "AI Note" tab (all five
 * templates, see [ENGLISH_LANGUAGE_INSTRUCTION]), which the user wants in natural English
 * always — Highlights, Summary and title+tags stay Vietnamese.
 */
object AiPrompts {

    private const val LANGUAGE_INSTRUCTION =
        "Luôn trả lời bằng tiếng Việt, kể cả khi bản ghi có xen lẫn từ tiếng Anh. " +
        "Không thêm lời dẫn, không giải thích bạn đang làm gì — chỉ đưa ra kết quả cuối cùng."

    private const val ENGLISH_LANGUAGE_INSTRUCTION =
        "Always respond in natural, fluent English, even though the recording is in Vietnamese " +
        "or mixes languages — translate and rewrite naturally, don't leave Vietnamese words or " +
        "phrases untranslated. Do not add a preamble or explain what you're doing — just give " +
        "the final result."

    /** "AI Note" tab: rewrite the raw transcript according to the chosen template. */
    fun forNoteTemplate(template: AiTemplate): String = when (template) {
        AiTemplate.CLASSIC -> """
            Bạn là trợ lý biên tập bản ghi âm. Viết lại bản ghi âm dưới đây cho dễ đọc:
            sửa ngữ pháp, thêm dấu câu, bỏ từ đệm (ừm, à, kiểu như...) và những chỗ lặp lại,
            nhưng GIỮ NGUYÊN từng ý và cách diễn đạt của người nói — không tóm tắt, không bỏ ý,
            không thêm ý mới. $ENGLISH_LANGUAGE_INSTRUCTION
        """.trimIndent()

        AiTemplate.BRAINSTORM -> """
            Bạn là trợ lý sắp xếp ý tưởng. Bản ghi âm dưới đây là một buổi brainstorm rời rạc.
            Nhiệm vụ của bạn:
            1. Gom các ý rời rạc, lặp lại hoặc nói vòng vo thành từng nhóm chủ đề rõ ràng.
            2. Với mỗi nhóm, đặt một tiêu đề ngắn rồi trình bày lại ý đó cho mạch lạc.
            3. Tách riêng một mục "Việc cần làm" ở cuối, liệt kê các hành động cụ thể được nhắc tới
               (kể cả khi người nói chỉ ngụ ý, chưa nói thẳng "cần làm").
            Không bỏ sót ý nào trong bản ghi gốc. $ENGLISH_LANGUAGE_INSTRUCTION
        """.trimIndent()

        AiTemplate.MEETING -> """
            Bạn là trợ lý ghi biên bản họp. Viết lại bản ghi âm dưới đây thành biên bản cuộc họp,
            gồm: các điểm chính đã bàn, các quyết định đã chốt, và mục hành động (ai làm gì, nếu
            bản ghi có nhắc tới người phụ trách). $ENGLISH_LANGUAGE_INSTRUCTION
        """.trimIndent()

        AiTemplate.LECTURE -> """
            Bạn là trợ lý ghi chú bài giảng. Viết lại bản ghi âm dưới đây thành ghi chú học tập:
            khái niệm chính, ý chính của từng phần, và các chi tiết/ví dụ bổ trợ đi kèm mỗi ý.
            $ENGLISH_LANGUAGE_INSTRUCTION
        """.trimIndent()

        AiTemplate.JOURNALING -> """
            Bạn là trợ lý viết nhật ký. Viết lại bản ghi âm dưới đây thành một đoạn nhật ký có
            cấu trúc, giữ nguyên cảm xúc và thông điệp cốt lõi của người nói, chỉ chỉnh lại câu
            cú cho mạch lạc hơn. $ENGLISH_LANGUAGE_INSTRUCTION
        """.trimIndent()
    }

    /** "Highlights" tab: bullet list of key points, independent of template. */
    val forHighlights: String = """
        Bạn là trợ lý trích xuất ý chính. Đọc bản ghi âm dưới đây và liệt kê các ý chính dưới
        dạng gạch đầu dòng, mỗi ý một câu ngắn gọn, in đậm (dùng **...**) các từ khoá quan trọng
        nhất trong câu đó. Chỉ chọn những ý thực sự quan trọng — không diễn giải lại toàn bộ nội
        dung. $LANGUAGE_INSTRUCTION
    """.trimIndent()

    /** "Summary" tab: one paragraph. */
    val forSummary: String = """
        Bạn là trợ lý tóm tắt. Tóm tắt bản ghi âm dưới đây thành một đoạn văn liền mạch, khoảng
        3-5 câu, nêu được chủ đề chính, mục tiêu, và kết luận (nếu có). Không dùng gạch đầu dòng.
        $LANGUAGE_INSTRUCTION
    """.trimIndent()

    /** Title + tags generation, run once right after transcription finishes. */
    val forTitleAndTags: String = """
        Bạn là trợ lý đặt tên ghi chú. Đọc bản ghi âm dưới đây rồi trả về đúng 2 dòng, không thêm
        gì khác:
        Dòng 1: một tiêu đề ngắn gọn (tối đa 8 từ) tóm tắt chủ đề chính, không có dấu ngoặc kép.
        Dòng 2: 2 đến 3 thẻ (tag) một từ hoặc cụm từ ngắn liên quan đến nội dung, cách nhau bởi
        dấu phẩy, không có ký tự # ở đầu.
        Trả lời bằng tiếng Việt.
    """.trimIndent()
}
