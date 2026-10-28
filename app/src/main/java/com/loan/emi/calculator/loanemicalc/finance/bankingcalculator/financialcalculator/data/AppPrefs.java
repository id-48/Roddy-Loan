package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class AppPrefs {
    private static final String PREFS = "mysession";

    private AppPrefs() {
    }

    private static SharedPreferences sp(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static boolean isOnboardingDone(Context context) {
        return sp(context).getBoolean("onboarding_done", false);
    }

    public static void setOnboardingDone(Context context, boolean done) {
        sp(context).edit().putBoolean("onboarding_done", done).apply();
    }

    public static String getLanguage(Context context) {
        return sp(context).getString("app_language", "en");
    }

    public static void setLanguage(Context context, String language) {
        sp(context).edit().putString("app_language", language == null ? "en" : language).apply();
    }

    public static String getCurrency(Context context) {
        return sp(context).getString("app_currency", "INR");
    }

    public static String getCurrencySymbol(Context context) {
        return sp(context).getString("app_currency_symbol", "₹");
    }

    public static void setCurrency(Context context, String code, String symbol) {
        sp(context).edit()
                .putString("app_currency", code)
                .putString("app_currency_symbol", symbol)
                .apply();
    }

    public static String getLastCompletedStep(Context context) {
        return sp(context).getString("last_completed_step_class", null);
    }

    public static void setLastCompletedStep(Context context, String className) {
        sp(context).edit().putString("last_completed_step_class", className).apply();
    }

    public static void clearLastCompletedStep(Context context) {
        sp(context).edit().remove("last_completed_step_class").apply();
    }

    public static boolean showLoanOfferCard(Context context) {
        return sp(context).getBoolean("show_loan_offer_card", true);
    }
}
