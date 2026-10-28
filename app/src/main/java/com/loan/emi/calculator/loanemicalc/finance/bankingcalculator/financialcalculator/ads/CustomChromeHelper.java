package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.browser.customtabs.CustomTabColorSchemeParams;
import androidx.browser.customtabs.CustomTabsIntent;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

final class CustomChromeHelper {
    private CustomChromeHelper() {
    }

    static boolean open(Activity activity, String url) {
        if (activity == null || url == null || url.trim().isEmpty()) {
            return false;
        }
        String trimmed = url.trim();
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return false;
        }
        try {
            CustomTabColorSchemeParams colors = new CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(activity.getColor(R.color.colorPrimary))
                    .build();
            CustomTabsIntent intent = new CustomTabsIntent.Builder()
                    .setDefaultColorSchemeParams(colors)
                    .setShowTitle(true)
                    .build();
            intent.launchUrl(activity, Uri.parse(trimmed));
            return true;
        } catch (Exception e) {
            try {
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(trimmed)));
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    static boolean isUrl(String value) {
        if (value == null) {
            return false;
        }
        String trimmed = value.trim();
        return trimmed.startsWith("http://") || trimmed.startsWith("https://");
    }
}
