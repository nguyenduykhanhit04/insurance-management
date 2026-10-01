package com.training.insurance;

import com.training.insurance.util.NameFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameFormatterTest {

    @Test
    void testSpecExample() {
        String input = "Tr加加加ầN  2加  vi12Ệt hÙ&*@nG   ";
        String expected = "Tran Viet Hung";
        String actual = NameFormatter.formatName(input);
        assertEquals(expected, actual);
    }

    @Test
    void testVietnameseNames() {
        assertEquals("Nguyen Van An", NameFormatter.formatName("nguyễn   văn   an"));
        assertEquals("Tran Thi Mai", NameFormatter.formatName("TRẦN THỊ MAI"));
        assertEquals("Doan Dinh Dat", NameFormatter.formatName("ĐOÀN ĐÌNH ĐẠT"));
        assertEquals("Vu Duc", NameFormatter.formatName("  vũ   đức  "));
    }
}
