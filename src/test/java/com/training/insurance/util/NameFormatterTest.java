package com.training.insurance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * UNIT TEST cho NameFormatter
 *
 * @ParameterizedTest : Chạy cùng 1 test với nhiều bộ dữ liệu khác nhau
 * @CsvSource         : Cung cấp dữ liệu dạng "input, expected"
 * @NullAndEmptySource: Test cả null lẫn empty string trong 1 test
 * @ValueSource       : Danh sách giá trị đơn lẻ
 */
@DisplayName("NameFormatter - Unit Tests")
class NameFormatterTest {

    @ParameterizedTest(name = "input={0} → rỗng")
    @NullAndEmptySource
    @DisplayName("Input null hoặc empty → trả về chuỗi rỗng")
    void formatName_nullOrEmpty_returnsEmpty(String input) {
        assertEquals("", NameFormatter.formatName(input));
    }

    @ParameterizedTest(name = "input=\"{0}\" → rỗng")
    @ValueSource(strings = {"   ", "\t", "123", "@#$%"})
    @DisplayName("Input chỉ có whitespace/số/ký tự đặc biệt → trả về rỗng")
    void formatName_noLetters_returnsEmpty(String input) {
        assertEquals("", NameFormatter.formatName(input));
    }

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @CsvSource({
            "nguyen van an,         Nguyen Van An",
            "TRAN THI MAI,          Tran Thi Mai",
            "DOAN DINH DAT,         Doan Dinh Dat",
            "le thi hoa,            Le Thi Hoa",
            "HO CHI MINH,           Ho Chi Minh",
    })
    @DisplayName("Tên ASCII thuần → format đúng Title Case")
    void formatName_asciiNames_returnsFormatted(String input, String expected) {
        assertEquals(expected.trim(), NameFormatter.formatName(input.trim()));
    }

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @CsvSource({
            "nguyen van an,         Nguyen Van An",
            "NGUYEN VAN AN,         Nguyen Van An",
    })
    @DisplayName("Tên tiếng Việt đã bỏ dấu → format đúng")
    void formatName_vietnameseNames_returnsFormatted(String input, String expected) {
        assertEquals(expected.trim(), NameFormatter.formatName(input.trim()));
    }

    @ParameterizedTest(name = "\"{0}\" → \"{1}\"")
    @CsvSource({
            // Ký tự lạ xen giữa - spec example
            "'Tr加加加\u1EA7N  2加  vi12\u1EC6t h\u00D9&*@nG   ', Tran Viet Hung",
            // Số xen vào trong từ: split theo space trước, rồi strip số trong từ
            // "nguy3n" → strip "3" → "nguyn" (không phải "nguyen")
            "'nguy3n v4n a', Nguyn Vn A",
            // Nhiều khoảng trắng
            "'  vu   duc  ', Vu Duc",
            // Chỉ 1 từ
            "nguyen, Nguyen",
    })
    @DisplayName("Ký tự đặc biệt và số bị loại bỏ, khoảng trắng thừa bị trim")
    void formatName_specialCharsAndNumbers_strippedAndFormatted(String input, String expected) {
        assertEquals(expected.trim(), NameFormatter.formatName(input.trim()));
    }

    @Test
    @DisplayName("Tên 1 chữ cái → viết hoa đúng")
    void formatName_singleLetter_returnCapitalized() {
        assertEquals("A", NameFormatter.formatName("a"));
        assertEquals("A", NameFormatter.formatName("A"));
    }

    @Test
    @DisplayName("Spec example từ đề bài → Tran Viet Hung")
    void formatName_specExample_returnsCorrectResult() {
        String input = "Tr\u52A0\u52A0\u52A0\u1EA7N  2\u52A0  vi12\u1EC6t h\u00D9&*@nG   ";
        assertEquals("Tran Viet Hung", NameFormatter.formatName(input));
    }
}
