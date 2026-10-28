package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class MultiRateGstActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_rate_gst);
        setupToolbar("Multi Rate GST", true);
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
            double totalGst = price * rate / 100.0;
            double half = totalGst / 2.0;
            TextView tvCgst = findViewById(R.id.tvCgst);
            TextView tvSgst = findViewById(R.id.tvSgst);
            TextView tvTotal = findViewById(R.id.tvTotalGst);
            if (tvCgst != null) tvCgst.setText(MoneyFormat.formatDecimal(this, half));
            if (tvSgst != null) tvSgst.setText(MoneyFormat.formatDecimal(this, half));
            if (tvTotal != null) tvTotal.setText(MoneyFormat.formatDecimal(this, totalGst));
            View card = findViewById(R.id.cardResult);
            if (card != null) card.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }
}
