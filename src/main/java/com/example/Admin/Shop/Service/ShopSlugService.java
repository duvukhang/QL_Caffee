package com.example.Admin.Shop.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public final class ShopSlugService {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{M}+");
    private static final Pattern NON_SLUG = Pattern.compile("[^a-z0-9]+");

    private ShopSlugService() {
    }

    public static String slugify(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD);
        String ascii = DIACRITICS.matcher(normalized)
                .replaceAll("")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
        String slug = NON_SLUG.matcher(ascii).replaceAll("-");
        slug = slug.replaceAll("^-+|-+$", "");
        return slug.isBlank() ? "san-pham" : slug;
    }
}
