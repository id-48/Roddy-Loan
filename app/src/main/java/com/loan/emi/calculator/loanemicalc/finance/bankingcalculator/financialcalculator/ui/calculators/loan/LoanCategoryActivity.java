package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.LoanEmiCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.ValueAddCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.InvestmentCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.CompareCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.InterestCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.MortgageCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.AutoCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.AmortizationCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.LoanComparisonCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.InterestOnlyCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.LoanAffordabilityCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.LoanAmountCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.LoanTenureCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.InterestRateCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.MoratoriumCalculatorActivity;

public class LoanCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loan_category);
        setupToolbar("Loan Calculators", true);
        click(R.id.cardLoanEmiCalc, LoanEmiCalculatorActivity.class);
        click(R.id.cardValueAddCalc, ValueAddCalculatorActivity.class);
        click(R.id.cardInvestmentCalc, InvestmentCalculatorActivity.class);
        click(R.id.cardCompareCalc, CompareCalculatorActivity.class);
        click(R.id.cardInterestCalc, InterestCalculatorActivity.class);
        click(R.id.cardMortgageCalc, MortgageCalculatorActivity.class);
        click(R.id.cardAutoCalc, AutoCalculatorActivity.class);
        click(R.id.cardAmortizationCalc, AmortizationCalculatorActivity.class);
        click(R.id.cardLoanComparisonCalc, LoanComparisonCalculatorActivity.class);
        click(R.id.cardInterestOnlyCalc, InterestOnlyCalculatorActivity.class);
        click(R.id.cardLoanAffordabilityCalc, LoanAffordabilityCalculatorActivity.class);
        click(R.id.cardLoanAmountCalc, LoanAmountCalculatorActivity.class);
        click(R.id.cardLoanTenureCalc, LoanTenureCalculatorActivity.class);
        click(R.id.cardInterestRateCalc, InterestRateCalculatorActivity.class);
        click(R.id.cardMoratoriumCalc, MoratoriumCalculatorActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
