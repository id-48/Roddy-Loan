package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking;

import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.CalculatorEngine;

public class PublicProvidentFundCalculatorActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_provident_fund_calculator);
        setupToolbar("PPF Calculator", true);
        View btn = findViewById(R.id.btnCalculate);
        if (btn != null) btn.setOnClickListener(v -> CalculatorEngine.run(this));
        View btn2 = findViewById(getResources().getIdentifier("btnConvert", "id", getPackageName()));
        if (btn2 != null) btn2.setOnClickListener(v -> CalculatorEngine.run(this));
        View btn3 = findViewById(getResources().getIdentifier("btnSave", "id", getPackageName()));
        if (btn3 != null) btn3.setOnClickListener(v -> CalculatorEngine.run(this));
        View btnCompare = findViewById(getResources().getIdentifier("btnCompare", "id", getPackageName()));
        if (btnCompare != null) btnCompare.setOnClickListener(v -> CalculatorEngine.run(this));
    }
}
