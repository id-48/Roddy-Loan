package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

public class AddressActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_address);
        setupToolbar("Address", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Address Details");
        TextView country = findViewById(R.id.actvCountry);
        if (country != null) country.setText("India");
        EditText etAddress = findViewById(R.id.etAddress);
        etAddress.setText(LoanSession.address);
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();
            if (address.isEmpty()) {
                Toast.makeText(this, "Please enter address", Toast.LENGTH_SHORT).show();
                return;
            }
            LoanSession.address = address;
            LoanSession.save(this);
            goNext(true);
        });
    }
}
