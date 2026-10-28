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

public class LoanPurposeActivity extends BaseActivity {
    private List<ImageView> checks;
    private String selected = "Personal Use";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_loan_purpose);
        setupToolbar("Loan Purpose", true);
        setupProgressHeader();
        setText(R.id.tvHeadline, "Purpose of Loan");
        setText(R.id.tvSubtext, "Tell us why you need this loan");

        setText(R.id.tvOpt1, "Personal Use");
        ImageView c0 = findViewById(R.id.ivCheckPersonal);
        findViewById(R.id.btnPersonal).setOnClickListener(v -> pick("Personal Use", c0));
        setText(R.id.tvOpt2, "Education");
        ImageView c1 = findViewById(R.id.ivCheckEducation);
        findViewById(R.id.btnEducation).setOnClickListener(v -> pick("Education", c1));
        setText(R.id.tvOpt3, "Medical");
        ImageView c2 = findViewById(R.id.ivCheckMedical);
        findViewById(R.id.btnMedical).setOnClickListener(v -> pick("Medical", c2));
        setText(R.id.tvOpt4, "Home Renovation");
        ImageView c3 = findViewById(R.id.ivCheckHomeRenovation);
        findViewById(R.id.btnHomeRenovation).setOnClickListener(v -> pick("Home Renovation", c3));
        setText(R.id.tvOpt5, "Debt Consolidation");
        ImageView c4 = findViewById(R.id.ivCheckDebt);
        findViewById(R.id.btnDebt).setOnClickListener(v -> pick("Debt Consolidation", c4));
        checks = Arrays.asList(c0, c1, c2, c3, c4);
        pick(selected, c0);
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            LoanSession.loanPurpose = selected;
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
