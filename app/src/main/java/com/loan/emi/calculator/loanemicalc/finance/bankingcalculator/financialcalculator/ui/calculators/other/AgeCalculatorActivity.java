package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

import java.util.Calendar;
import java.util.Locale;

public class AgeCalculatorActivity extends BaseActivity {
    private Calendar dob = Calendar.getInstance();
    private boolean dobSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_age_calculator);
        setupToolbar("Age Calculator", true);
        dob.add(Calendar.YEAR, -25);
        View pick = findViewById(R.id.btnPickDate);
        if (pick != null) {
            pick.setOnClickListener(v -> {
                Calendar now = Calendar.getInstance();
                new DatePickerDialog(this, (view, y, m, d) -> {
                    dob.set(y, m, d);
                    dobSet = true;
                    if (pick instanceof TextView) {
                        ((TextView) pick).setText(String.format(Locale.US, "%02d/%02d/%04d", d, m + 1, y));
                    }
                }, dob.get(Calendar.YEAR), dob.get(Calendar.MONTH), dob.get(Calendar.DAY_OF_MONTH)).show();
            });
        }
        View btn = findViewById(R.id.btnCalculate);
        if (btn != null) btn.setOnClickListener(v -> calculate());
    }

    private void calculate() {
        if (!dobSet) {
            Toast.makeText(this, "Please pick date of birth", Toast.LENGTH_SHORT).show();
            return;
        }
        Calendar today = Calendar.getInstance();
        int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
        int days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH);
        if (days < 0) {
            months--;
            days += today.getActualMaximum(Calendar.DAY_OF_MONTH);
        }
        if (months < 0) {
            years--;
            months += 12;
        }
        TextView age = findViewById(R.id.tvAge);
        TextView detail = findViewById(R.id.tvDetail);
        if (age != null) age.setText(years + " Years");
        if (detail != null) {
            detail.setText(String.format(Locale.US, "%d years, %d months, %d days", years, months, days));
        }
        View card = findViewById(R.id.cardResult);
        if (card != null) card.setVisibility(View.VISIBLE);
    }
}
