package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.AgeCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.BmiCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.UnitConverterActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.CashCounterActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.NumberToWordActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.PetrolDieselPriceActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.SimpleCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.ProfitMarginCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.GratuityCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.BreakEventCalculatorActivity;

public class OtherCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_category);
        setupToolbar("Other Calculators", true);
        click(R.id.cardAgeCalc, AgeCalculatorActivity.class);
        click(R.id.cardBmiCalc, BmiCalculatorActivity.class);
        click(R.id.cardUnitCalc, UnitConverterActivity.class);
        click(R.id.cardCashCalc, CashCounterActivity.class);
        click(R.id.cardNumToWordCalc, NumberToWordActivity.class);
        click(R.id.cardPetrolCalc, PetrolDieselPriceActivity.class);
        click(R.id.cardSimpleCalc, SimpleCalculatorActivity.class);
        click(R.id.cardProfitMarginCalc, ProfitMarginCalculatorActivity.class);
        click(R.id.cardGratuityCalc, GratuityCalculatorActivity.class);
        click(R.id.cardBreakEventCalc, BreakEventCalculatorActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
