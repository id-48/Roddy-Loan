package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.GstCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.VatCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.ReverseGstCalculatorActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.MultiRateGstActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.SalesTaxActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.ServiceTaxActivity;

public class GstCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gst_category);
        setupToolbar("GST & VAT", true);
        click(R.id.cardGstCalc, GstCalculatorActivity.class);
        click(R.id.cardVatCalc, VatCalculatorActivity.class);
        click(R.id.cardReverseGstCalc, ReverseGstCalculatorActivity.class);
        click(R.id.cardMultiRateGstCalc, MultiRateGstActivity.class);
        click(R.id.cardSalesTaxCalc, SalesTaxActivity.class);
        click(R.id.cardServiceTaxCalc, ServiceTaxActivity.class);
    }
    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
    }
}
