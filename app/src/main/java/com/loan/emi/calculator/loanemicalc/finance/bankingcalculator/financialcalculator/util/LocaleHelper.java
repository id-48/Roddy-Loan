package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util;

import android.content.Context;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LocaleHelper {

    public static final String DEFAULT_TAG = "en";

    private static final Map<String, String> NAME_TO_TAG = new HashMap<>();

    static {
        NAME_TO_TAG.put("english", "en");
        NAME_TO_TAG.put("hindi", "hi");
        NAME_TO_TAG.put("vietnamese", "vi");
        NAME_TO_TAG.put("urdu", "ur");
        NAME_TO_TAG.put("spanish", "es");
        NAME_TO_TAG.put("indonesian", "id");
        NAME_TO_TAG.put("filipino", "fil");
        NAME_TO_TAG.put("swahili", "sw");
        NAME_TO_TAG.put("portuguese", "pt");
        NAME_TO_TAG.put("bengali", "bn");
        // Already-stored tags
        NAME_TO_TAG.put("en", "en");
        NAME_TO_TAG.put("hi", "hi");
        NAME_TO_TAG.put("vi", "vi");
        NAME_TO_TAG.put("ur", "ur");
        NAME_TO_TAG.put("es", "es");
        NAME_TO_TAG.put("id", "id");
        NAME_TO_TAG.put("in", "id");
        NAME_TO_TAG.put("fil", "fil");
        NAME_TO_TAG.put("tl", "fil");
        NAME_TO_TAG.put("sw", "sw");
        NAME_TO_TAG.put("pt", "pt");
        NAME_TO_TAG.put("bn", "bn");
    }

    private LocaleHelper() {
    }

    public static String toLanguageTag(String stored) {
        if (TextUtils.isEmpty(stored)) {
            return DEFAULT_TAG;
        }
        String key = stored.trim().toLowerCase(Locale.US);
        String tag = NAME_TO_TAG.get(key);
        return tag != null ? tag : DEFAULT_TAG;
    }

    public static void applyStored(Context context) {
        apply(context, AppPrefs.getLanguage(context));
    }

    public static void apply(Context context, String language) {
        String tag = toLanguageTag(language);
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
    }

    public static String displayName(String language) {
        String tag = toLanguageTag(language);
        switch (tag) {
            case "hi":
                return "हिंदी";
            case "vi":
                return "Tiếng Việt";
            case "ur":
                return "اردو";
            case "es":
                return "Español";
            case "id":
                return "Bahasa Indonesia";
            case "fil":
                return "Filipino";
            case "sw":
                return "Kiswahili";
            case "pt":
                return "Português";
            case "bn":
                return "বাংলা";
            default:
                return "English";
        }
    }
}
