package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking.EmiLoanCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking.FixedDepositCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking.RecurringDepositCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking.PublicProvidentFundCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking.ReturnOnInvestmentActivity;

public class BankingCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banking_category);
        setupToolbar("Banking Calculators", true);
        click(R.id.cardEmiCalc, EmiLoanCalculatorActivity.class);
        click(R.id.cardFdCalc, FixedDepositCalculatorActivity.class);
        click(R.id.cardRdCalc, RecurringDepositCalculatorActivity.class);
        click(R.id.cardPpfCalc, PublicProvidentFundCalculatorActivity.class);
        click(R.id.cardRoiCalc, ReturnOnInvestmentActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
