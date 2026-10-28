package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class VatCalculatorActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vat_calculator);
        setupToolbar("VAT Calculator", true);
        View add = findViewById(R.id.btnAddVat);
        if (add != null) add.setOnClickListener(v -> calculate(true));
        View remove = findViewById(R.id.btnRemoveVat);
        if (remove != null) remove.setOnClickListener(v -> calculate(false));
    }

    private void calculate(boolean addTax) {
        hideKeyboard();
        try {
            EditText etAmount = findViewById(R.id.etAmount);
            EditText etRate = findViewById(R.id.etRate);
            double amount = Double.parseDouble(etAmount.getText().toString().trim());
            double rate = Double.parseDouble(etRate.getText().toString().trim());
            double net;
            double vat;
            double total;
            if (addTax) {
                net = amount;
                vat = amount * rate / 100.0;
                total = amount + vat;
            } else {
                total = amount;
                net = amount * 100.0 / (100.0 + rate);
                vat = total - net;
            }
            TextView tvNet = findViewById(R.id.tvNetAmount);
            TextView tvVat = findViewById(R.id.tvVatAmount);
            TextView tvTotal = findViewById(R.id.tvTotalAmount);
            if (tvNet != null) tvNet.setText(MoneyFormat.formatDecimal(this, net));
            if (tvVat != null) tvVat.setText(MoneyFormat.formatDecimal(this, vat));
            if (tvTotal != null) tvTotal.setText(MoneyFormat.formatDecimal(this, total));
            View card = findViewById(R.id.cardResult);
            if (card != null) card.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
