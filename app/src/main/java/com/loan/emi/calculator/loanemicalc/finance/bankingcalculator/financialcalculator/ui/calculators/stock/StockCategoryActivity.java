package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.stock;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.stock.IntradayCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.stock.DeliveryCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.stock.OptionsCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.stock.FuturesCalculatorActivity;

public class StockCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_category);
        setupToolbar("Stock Calculators", true);
        click(R.id.cardIntradayCalc, IntradayCalculatorActivity.class);
        click(R.id.cardDeliveryCalc, DeliveryCalculatorActivity.class);
        click(R.id.cardOptionsCalc, OptionsCalculatorActivity.class);
        click(R.id.cardFuturesCalc, FuturesCalculatorActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
