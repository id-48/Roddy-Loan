package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.DiscountCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.InflationCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.PriceCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.TvmCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.CurrencyConverterActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.TipsCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.MarginCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.CumulativeCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.SalesTaxCalculatorActivity;

public class FinancialCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_financial_category);
        setupToolbar("Financial Calculators", true);
        click(R.id.cardDiscountCalc, DiscountCalculatorActivity.class);
        click(R.id.cardInflationCalc, InflationCalculatorActivity.class);
        click(R.id.cardPriceCalc, PriceCalculatorActivity.class);
        click(R.id.cardTvmCalc, TvmCalculatorActivity.class);
        click(R.id.cardCurrencyCalc, CurrencyConverterActivity.class);
        click(R.id.cardTipsCalc, TipsCalculatorActivity.class);
        click(R.id.cardMarginCalc, MarginCalculatorActivity.class);
        click(R.id.cardCumulativeCalc, CumulativeCalculatorActivity.class);
        click(R.id.cardSalesTaxCalc, SalesTaxCalculatorActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
