package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

public class EmploymentActivity extends BaseActivity {
    private String selected = "Salaried";
    private TextView c1;
    private TextView c2;
    private TextView c3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_employment);
        setupProgressHeader("Employment details");
        setText(R.id.tvHeadline, "Employment Type");
        setText(R.id.tvSubtext, "Choose how you earn income");

        c1 = findViewById(R.id.tvChipSalaried);
        c2 = findViewById(R.id.tvChipSelfEmployed);
        c3 = findViewById(R.id.tvChipBusiness);
        if (c1 != null) {
            c1.setText("Salaried");
            c1.setOnClickListener(v -> pick("Salaried", c1));
        }
        if (c2 != null) {
            c2.setText("Self Employed");
            c2.setOnClickListener(v -> pick("Self Employed", c2));
        }
        if (c3 != null) {
            c3.setText("Business");
            c3.setOnClickListener(v -> pick("Business", c3));
        }
        pick(selected, c1);

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            LoanSession.employmentType = selected;
            LoanSession.save(this);
            goNext(true);
        });
    }

    private void pick(String value, TextView selectedChip) {
        selected = value;
        updateChip(c1, c1 == selectedChip);
        updateChip(c2, c2 == selectedChip);
        updateChip(c3, c3 == selectedChip);
    }

    private void updateChip(TextView chip, boolean selectedState) {
        if (chip == null) {
            return;
        }
        chip.setSelected(selectedState);
        chip.setTextColor(getColor(selectedState ? R.color.colorOnPrimary : R.color.colorText));
    }
}
