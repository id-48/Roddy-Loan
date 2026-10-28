package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.notify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.home.HomeActivity;

/** Handles tap on admin-sent notification: Custom Tab if link, else Home. */
public class AdminNotifyOpenActivity extends AppCompatActivity {
    public static final String EXTRA_LINK = "link";
    public static final String EXTRA_IMAGE = "image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String link = getIntent() != null ? getIntent().getStringExtra(EXTRA_LINK) : null;
        if (!TextUtils.isEmpty(link)) {
            try {
                new CustomTabsIntent.Builder().build().launchUrl(this, Uri.parse(link));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            }
        } else {
            Intent home = new Intent(this, HomeActivity.class);
            home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(home);
        }
        finish();
    }
}
