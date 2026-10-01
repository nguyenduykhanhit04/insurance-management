package com.training.insurance.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class NameFormatter {

    private static final Pattern DIACRITICS_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private NameFormatter() {}

    public static String formatName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String normalized = input.replace("đ", "d").replace("Đ", "D");
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        normalized = DIACRITICS_PATTERN.matcher(normalized).replaceAll("");

        String[] words = normalized.split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            String cleanedWord = word.replaceAll("[^a-zA-Z]", "");
            if (!cleanedWord.isEmpty()) {
                String formattedWord = Character.toUpperCase(cleanedWord.charAt(0))
                        + (cleanedWord.length() > 1 ? cleanedWord.substring(1).toLowerCase() : "");
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(formattedWord);
            }
        }

        return result.toString();
    }
}
