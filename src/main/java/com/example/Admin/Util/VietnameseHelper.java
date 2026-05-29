package com.example.Admin.Util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class VietnameseHelper {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private VietnameseHelper() {
    }

    public static String removeSign(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return DIACRITICS.matcher(normalized)
                .replaceAll("")
                .replace('\u0111', 'd')
                .replace('\u0110', 'D');
    }
}
