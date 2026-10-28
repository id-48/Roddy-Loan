package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

import java.util.Arrays;
import java.util.List;

public class LoanTypeActivity extends BaseActivity {
    private List<ImageView> checks;
    private String selected = "Personal Loan";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_loan_type);
        setupToolbar("Loan Type", true);
        setupProgressHeader();
        setText(R.id.tvHeadline, "Select Loan Type");
        setText(R.id.tvSubtext, "Choose the loan that fits your need");

        setText(R.id.tvOpt1, "Personal Loan");
        ImageView c0 = findViewById(R.id.ivCheckPersonal);
        findViewById(R.id.btnPersonalLoan).setOnClickListener(v -> pick("Personal Loan", c0));
        setText(R.id.tvOpt2, "Business Loan");
        ImageView c1 = findViewById(R.id.ivCheckBusiness);
        findViewById(R.id.btnBusinessLoan).setOnClickListener(v -> pick("Business Loan", c1));
        setText(R.id.tvOpt3, "Home Loan");
        ImageView c2 = findViewById(R.id.ivCheckHome);
        findViewById(R.id.btnHomeLoan).setOnClickListener(v -> pick("Home Loan", c2));
        setText(R.id.tvOpt4, "Vehicle Loan");
        ImageView c3 = findViewById(R.id.ivCheckVehicle);
        findViewById(R.id.btnVehicleLoan).setOnClickListener(v -> pick("Vehicle Loan", c3));
        checks = Arrays.asList(c0, c1, c2, c3);
        pick(selected, c0);
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            LoanSession.loanType = selected;
            LoanSession.save(this);
            goNext(true);
        });
    }

    private void pick(String value, ImageView check) {
        selected = value;
        for (ImageView iv : checks) {
            if (iv != null) iv.setVisibility(iv == check ? View.VISIBLE : View.GONE);
        }
    }

    private void setText(int id, String text) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(text);
    }
}
