package com.training.insurance.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("User entity - Unit Tests")
class UserTest {

    private User userWithSex(String sex) {
        return User.builder()
                .userInternalId(1).username("test")
                .userFullName("Test User").userSexDivision(sex)
                .build();
    }

    @ParameterizedTest(name = "sexDivision=\"{0}\" → \"Nam\"")
    @ValueSource(strings = {"01", "1"})
    @DisplayName("sexDivision 01 hoặc 1 → Nam")
    void getGenderText_maleCodes_returnsNam(String sex) {
        assertEquals("Nam", userWithSex(sex).getGenderText());
    }

    @ParameterizedTest(name = "sexDivision=\"{0}\" → \"Nữ\"")
    @ValueSource(strings = {"02", "2"})
    @DisplayName("sexDivision 02 hoặc 2 → Nữ")
    void getGenderText_femaleCodes_returnsNu(String sex) {
        assertEquals("Nữ", userWithSex(sex).getGenderText());
    }

    @ParameterizedTest(name = "sexDivision=\"{0}\" → \"\"")
    @ValueSource(strings = {"03", "99", "abc"})
    @DisplayName("sexDivision không hợp lệ → chuỗi rỗng")
    void getGenderText_invalidCode_returnsEmpty(String sex) {
        assertEquals("", userWithSex(sex).getGenderText());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("sexDivision null hoặc empty → chuỗi rỗng")
    void getGenderText_nullOrEmpty_returnsEmpty(String sex) {
        assertEquals("", userWithSex(sex).getGenderText());
    }
}
