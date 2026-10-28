package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class ReverseGstCalculatorActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reverse_gst_calculator);
        setupToolbar("Reverse GST", true);
        View btn = findViewById(R.id.btnCalculate);
        if (btn != null) btn.setOnClickListener(v -> calculate());
    }

    private void calculate() {
        hideKeyboard();
        try {
            EditText etTotal = findViewById(R.id.etTotal);
            EditText etRate = findViewById(R.id.etRate);
            double total = Double.parseDouble(etTotal.getText().toString().trim());
            double rate = Double.parseDouble(etRate.getText().toString().trim());
            double original = total * 100.0 / (100.0 + rate);
            double tax = total - original;
            TextView tvOriginal = findViewById(R.id.tvOriginal);
            TextView tvTax = findViewById(R.id.tvTax);
            if (tvOriginal != null) tvOriginal.setText(MoneyFormat.formatDecimal(this, original));
            if (tvTax != null) tvTax.setText(MoneyFormat.formatDecimal(this, tax));
            View card = findViewById(R.id.cardResult);
            if (card != null) card.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
