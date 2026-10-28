package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class CashCounterActivity extends BaseActivity {
    private static final int[] DENS = {2000, 500, 200, 100, 50, 20, 10, 5, 2, 1};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash_counter);
        setupToolbar("Cash Counter", true);
        for (int den : DENS) {
            int rowId = getResources().getIdentifier("row" + den, "id", getPackageName());
            View row = findViewById(rowId);
            if (row == null) continue;
            TextView denLabel = row.findViewById(R.id.tvDenomination);
            if (denLabel != null) denLabel.setText(String.valueOf(den));
            EditText et = row.findViewById(R.id.etCount);
            TextView sub = row.findViewById(R.id.tvSubtotal);
            if (et != null) {
                et.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                    @Override
                    public void afterTextChanged(Editable s) {
                        long qty = 0;
                        try {
                            if (s != null && s.length() > 0) qty = Long.parseLong(s.toString().trim());
                        } catch (Exception ignored) {}
                        if (sub != null) sub.setText(MoneyFormat.formatDecimal(CashCounterActivity.this, den * qty));
                        updateTotal();
                    }
                });
            }
        }
        updateTotal();
    }

    private void updateTotal() {
        long total = 0;
        for (int den : DENS) {
            int rowId = getResources().getIdentifier("row" + den, "id", getPackageName());
            View row = findViewById(rowId);
            if (row == null) continue;
            EditText et = row.findViewById(R.id.etCount);
            long qty = 0;
            if (et != null) {
                try {
                    String s = et.getText().toString().trim();
                    if (!s.isEmpty()) qty = Long.parseLong(s);
                } catch (Exception ignored) {}
            }
            total += (long) den * qty;
        }
        TextView grand = findViewById(R.id.tvGrandTotal);
        if (grand != null) grand.setText(MoneyFormat.formatDecimal(this, total));
    }
}
