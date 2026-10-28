package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.browser.customtabs.CustomTabsIntent;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.CurrencyActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.LanguageActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.LocaleHelper;

public class SettingActivity extends BaseActivity {

    private static final String PRIVACY_POLICY_URL =
            "https://www.termsfeed.com/live/86da0c96-b9f9-4866-939f-1db1b10a98b2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_setting);
        setupToolbar(getString(R.string.settings_title), true);

        TextView share = findViewById(R.id.tvSettingShare);
        if (share != null) share.setText(R.string.settings_share);
        TextView privacy = findViewById(R.id.tvSettingPrivacy);
        if (privacy != null) privacy.setText(R.string.settings_privacy);

        findViewById(R.id.cardLanguage).setOnClickListener(v -> {
            Intent i = new Intent(this, LanguageActivity.class);
            i.putExtra("from_settings", true);
            startActivity(i);
        });
        findViewById(R.id.cardCurrency).setOnClickListener(v -> {
            Intent i = new Intent(this, CurrencyActivity.class);
            i.putExtra("from_settings", true);
            startActivity(i);
        });
        findViewById(R.id.cardShare).setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Check out Personal EMI Loan Calculator — calculate EMI & manage loans easily.");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.settings_share)));
        });
        findViewById(R.id.cardPrivacy).setOnClickListener(v -> openPrivacyPolicy());
    }

    private void openPrivacyPolicy() {
        Uri uri = Uri.parse(PRIVACY_POLICY_URL);
        try {
            new CustomTabsIntent.Builder().build().launchUrl(this, uri);
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextView lang = findViewById(R.id.tvSettingLanguage);
        if (lang != null) {
            lang.setText(getString(R.string.settings_language_fmt,
                    LocaleHelper.displayName(AppPrefs.getLanguage(this))));
        }
        TextView cur = findViewById(R.id.tvSettingCurrency);
        if (cur != null) {
            cur.setText(getString(R.string.settings_currency_fmt, AppPrefs.getCurrency(this)));
        }
    }
}
