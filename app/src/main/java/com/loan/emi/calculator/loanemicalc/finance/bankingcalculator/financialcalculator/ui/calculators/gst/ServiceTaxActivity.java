package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class ServiceTaxActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_tax);
        setupToolbar("Service Tax", true);
        View btn = findViewById(R.id.btnCalculate);
        if (btn != null) btn.setOnClickListener(v -> calculate());
    }

    private void calculate() {
        hideKeyboard();
        try {
            EditText etPrice = findViewById(R.id.etPrice);
            EditText etRate = findViewById(R.id.etRate);
            double price = Double.parseDouble(etPrice.getText().toString().trim());
            double rate = Double.parseDouble(etRate.getText().toString().trim());
            double tax = price * rate / 100.0;
            TextView tvTax = findViewById(R.id.tvTaxAmount);
            TextView tvGross = findViewById(R.id.tvGrossAmount);
            if (tvTax != null) tvTax.setText(MoneyFormat.formatDecimal(this, tax));
            if (tvGross != null) tvGross.setText(MoneyFormat.formatDecimal(this, price + tax));
            View card = findViewById(R.id.cardResult);
            if (card != null) card.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
