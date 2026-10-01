package com.training.insurance.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MD5Util - Unit Tests")
class MD5UtilTest {

    @Test
    @DisplayName("md5 của chuỗi cố định → hash đúng 32 ký tự hex")
    void md5_knownInput_returnsExpectedHash() {
        String result = MD5Util.md5("admin");
        assertNotNull(result);
        assertEquals(32, result.length());
        assertEquals("21232f297a57a5a743894a0e4a801fc3", result);
    }

    @Test
    @DisplayName("md5 của cùng input → luôn trả về cùng hash (deterministic)")
    void md5_sameInput_alwaysReturnsSameHash() {
        assertEquals(MD5Util.md5("password123"), MD5Util.md5("password123"));
    }

    @Test
    @DisplayName("md5 của hai input khác nhau → hash khác nhau")
    void md5_differentInputs_returnDifferentHashes() {
        assertNotEquals(MD5Util.md5("abc"), MD5Util.md5("ABC"));
    }

    @Test
    @DisplayName("md5 null → trả về null")
    void md5_nullInput_returnsNull() {
        assertNull(MD5Util.md5(null));
    }

    @Test
    @DisplayName("md5 chuỗi rỗng → trả về hash hợp lệ")
    void md5_emptyString_returnsValidHash() {
        String result = MD5Util.md5("");
        assertNotNull(result);
        assertEquals(32, result.length());
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", result);
    }

    @Test
    @DisplayName("md5 output chỉ chứa ký tự hex lowercase")
    void md5_output_isLowercaseHex() {
        String result = MD5Util.md5("test");
        assertNotNull(result);
        assertTrue(result.matches("[0-9a-f]{32}"), "MD5 phai la 32 ky tu hex lowercase");
    }
}
