package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual;

import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.CalculatorEngine;

public class SipCalculatorActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sip_calculator);
        setupToolbar("SIP", true);
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
