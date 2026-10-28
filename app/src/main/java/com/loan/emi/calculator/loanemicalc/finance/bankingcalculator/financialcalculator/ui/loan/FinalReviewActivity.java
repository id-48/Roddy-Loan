package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class FinalReviewActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_final_review);
        setupToolbar("Review", true);
        setupProgressHeader();
        set(R.id.tvHeadline, "Review Application");
        set(R.id.resAmount, MoneyFormat.format(this, LoanSession.loanAmount));
        set(R.id.resEmi, MoneyFormat.format(this, LoanSession.monthlyEmi));
        set(R.id.resName, LoanSession.fullName);
        set(R.id.resMobile, LoanSession.mobile);
        set(R.id.resEmail, LoanSession.email);
        set(R.id.resDob, LoanSession.dob);
        set(R.id.resGender, LoanSession.gender);
        set(R.id.resJob, LoanSession.employmentType);
        set(R.id.resAddress, LoanSession.address);
        CheckBox cb = findViewById(R.id.cbTerms);
        findViewById(R.id.btnSubmit).setOnClickListener(v -> {
            if (cb != null && !cb.isChecked()) {
                Toast.makeText(this, "Please accept terms", Toast.LENGTH_SHORT).show();
                return;
            }
            goNext(true);
        });
    }
    private void set(int id, String t) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(t);
    }
}
