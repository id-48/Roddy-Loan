package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;

import java.util.ArrayList;
import java.util.List;

public class CurrencyActivity extends BaseActivity {

    private static final String[][] CURRENCIES = {
            {"INR", "₹"},
            {"USD", "$"},
            {"EUR", "€"},
            {"GBP", "£"},
            {"AED", "د.إ"},
            {"SGD", "S$"},
            {"AUD", "A$"},
            {"CAD", "C$"}
    };

    private int selected;
    private final List<ImageView> checks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_currency);
        boolean fromSettings = getIntent().getBooleanExtra("from_settings", false);
        if (fromSettings) {
            View header = findViewById(R.id.plProgressHeaderRoot);
            if (header != null) header.setVisibility(View.GONE);
            setupToolbar(getString(R.string.currency_screen_title), true);
        } else {
            setupProgressHeader(getString(R.string.currency_milestone));
        }
        setText(R.id.tvHeadline, getString(R.string.currency_headline));
        setText(R.id.tvSubtext, getString(R.string.currency_subtext));

        String saved = AppPrefs.getCurrency(this);
        LinearLayout list = findViewById(R.id.llCurrencyList);
        LayoutInflater inflater = LayoutInflater.from(this);
        for (int i = 0; i < CURRENCIES.length; i++) {
            View row = inflater.inflate(R.layout.item_pl_currency, list, false);
            TextView symbolBig = row.findViewById(R.id.tvFlag);
            TextView codeLabel = row.findViewById(R.id.tvCode);
            ImageView check = row.findViewById(R.id.ivCheck);
            View divider = row.findViewById(R.id.vDivider);
            String code = CURRENCIES[i][0];
            String symbol = CURRENCIES[i][1];
            if (symbolBig != null) symbolBig.setText(symbol);
            if (codeLabel != null) codeLabel.setText(code);
            if (check != null) {
                checks.add(check);
                check.setVisibility(View.GONE);
            }
            if (divider != null && i == CURRENCIES.length - 1) {
                divider.setVisibility(View.GONE);
            }
            final int index = i;
            row.setOnClickListener(v -> select(index));
            list.addView(row);
            if (code.equalsIgnoreCase(saved)) {
                selected = i;
            }
        }
        select(selected);

        TextView next = findViewById(R.id.btnNext);
        if (next != null) {
            next.setText(R.string.btn_next);
            next.setOnClickListener(v -> {
                AppPrefs.setCurrency(this, CURRENCIES[selected][0], CURRENCIES[selected][1]);
                if (getIntent().getBooleanExtra("from_settings", false)) {
                    finish();
                } else {
                    goNext(true);
                }
            });
        }
    }

    private void select(int index) {
        selected = index;
        for (int i = 0; i < checks.size(); i++) {
            checks.get(i).setVisibility(i == index ? View.VISIBLE : View.GONE);
        }
    }
}
