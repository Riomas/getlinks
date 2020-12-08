package com.riomas.app.getlinks;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class LanguageUtils {

    private static final Map<Locale, ResourceBundle> bundles = new HashMap<>();

    private LanguageUtils() {}

    private static ResourceBundle getBunble(Locale locale) {
        ResourceBundle bundle = bundles.get(locale);
        if (bundle==null) {
            bundle = ResourceBundle.getBundle("language", locale);
            bundles.put(locale, bundle);
        }
        return bundle;
    }

    private static ResourceBundle getBunble() {

        ResourceBundle bundle = bundles.get(null);
        if (bundle==null) {
            bundle = ResourceBundle.getBundle("language");
            bundles.put(null, bundle);
        }
        return bundle;
    }


    public static String getString(String key, Locale locale) {
        return getBunble(locale).getString(key);
    }

    public static String getString(String key) {
        return getBunble().getString(key);
    }
}
