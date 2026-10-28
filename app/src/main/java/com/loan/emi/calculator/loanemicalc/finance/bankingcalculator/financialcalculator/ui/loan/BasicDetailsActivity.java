package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

import java.util.Calendar;

public class BasicDetailsActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_basic_details);
        setupToolbar("Basic Details", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Personal Details");
        EditText etName = findViewById(R.id.etName);
        EditText etDob = findViewById(R.id.etDob);
        etName.setText(LoanSession.fullName);
        etDob.setText(LoanSession.dob);
        etDob.setFocusable(false);
        etDob.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view, y, m, d) -> {
                String dob = String.format("%02d/%02d/%04d", d, m + 1, y);
                etDob.setText(dob);
            }, 1995, 0, 1).show();
        });
        RadioGroup rg = findViewById(R.id.rgGender);
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            if (name.isEmpty() || dob.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            LoanSession.fullName = name;
            LoanSession.dob = dob;
            int checked = rg.getCheckedRadioButtonId();
            if (checked != -1) {
                RadioButton rb = findViewById(checked);
                LoanSession.gender = rb.getText().toString();
            }
            LoanSession.save(this);
            goNext(true);
        });
    }
}
