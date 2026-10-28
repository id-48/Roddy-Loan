package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

public class ContactActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_contact);
        setupToolbar("Contact", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Contact Details");
        EditText etMobile = findViewById(R.id.etMobile);
        EditText etEmail = findViewById(R.id.etEmail);
        etMobile.setText(LoanSession.mobile);
        etEmail.setText(LoanSession.email);
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String mobile = etMobile.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            if (mobile.length() < 10 || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid mobile and email", Toast.LENGTH_SHORT).show();
                return;
            }
            LoanSession.mobile = mobile;
            LoanSession.email = email;
            LoanSession.save(this);
            goNext(true);
        });
    }
}
