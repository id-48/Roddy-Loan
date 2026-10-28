package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.SipCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.AdvanceSipCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.SwpCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.LumpsumCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.ScssCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.CompoundCalculatorActivity;

public class MutualFundCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mutual_fund_category);
        setupToolbar("Mutual Fund & SIP", true);
        click(R.id.cardSipCalc, SipCalculatorActivity.class);
        click(R.id.cardAdvanceSipCalc, AdvanceSipCalculatorActivity.class);
        click(R.id.cardSwpCalc, SwpCalculatorActivity.class);
        click(R.id.cardLumpsumCalc, LumpsumCalculatorActivity.class);
        click(R.id.cardScssCalc, ScssCalculatorActivity.class);
        click(R.id.cardCompoundCalc, CompoundCalculatorActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
