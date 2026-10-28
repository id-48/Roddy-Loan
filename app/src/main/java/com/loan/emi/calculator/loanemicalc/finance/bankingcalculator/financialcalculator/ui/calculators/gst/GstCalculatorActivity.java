package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class GstCalculatorActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gst_calculator);
        setupToolbar("GST Calculator", true);
        View add = findViewById(R.id.btnAddGst);
        if (add != null) add.setOnClickListener(v -> calculate(true));
        View remove = findViewById(R.id.btnRemoveGst);
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
            double gst;
            double total;
            if (addTax) {
                net = amount;
                gst = amount * rate / 100.0;
                total = amount + gst;
            } else {
                total = amount;
                net = amount * 100.0 / (100.0 + rate);
                gst = total - net;
            }
            TextView tvNet = findViewById(R.id.tvNetAmount);
            TextView tvGst = findViewById(R.id.tvGstAmount);
            TextView tvTotal = findViewById(R.id.tvTotalAmount);
            if (tvNet != null) tvNet.setText(MoneyFormat.formatDecimal(this, net));
            if (tvGst != null) tvGst.setText(MoneyFormat.formatDecimal(this, gst));
            if (tvTotal != null) tvTotal.setText(MoneyFormat.formatDecimal(this, total));
            View card = findViewById(R.id.cardResult);
            if (card != null) card.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
