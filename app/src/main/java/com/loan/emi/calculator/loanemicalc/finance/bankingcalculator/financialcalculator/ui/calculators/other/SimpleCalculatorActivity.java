package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class SimpleCalculatorActivity extends BaseActivity {
    private TextView display;
    private String current = "0";
    private String pendingOp = null;
    private double pendingValue = 0;
    private boolean resetOnNext = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_calculator);
        setupToolbar("Calculator", true);
        display = findViewById(R.id.tvDisplay);
        bindDigit(R.id.btn0, "0");
        bindDigit(R.id.btn00, "00");
        bindDigit(R.id.btn1, "1");
        bindDigit(R.id.btn2, "2");
        bindDigit(R.id.btn3, "3");
        bindDigit(R.id.btn4, "4");
        bindDigit(R.id.btn5, "5");
        bindDigit(R.id.btn6, "6");
        bindDigit(R.id.btn7, "7");
        bindDigit(R.id.btn8, "8");
        bindDigit(R.id.btn9, "9");
        findViewById(R.id.btnDot).setOnClickListener(v -> appendDot());
        findViewById(R.id.btnC).setOnClickListener(v -> clearAll());
        findViewById(R.id.btnDel).setOnClickListener(v -> backspace());
        findViewById(R.id.btnAdd).setOnClickListener(v -> setOp("+"));
        findViewById(R.id.btnSub).setOnClickListener(v -> setOp("-"));
        findViewById(R.id.btnMul).setOnClickListener(v -> setOp("×"));
        findViewById(R.id.btnDiv).setOnClickListener(v -> setOp("÷"));
        findViewById(R.id.btnEqual).setOnClickListener(v -> equals());
        updateDisplay();
    }

    private void bindDigit(int id, String d) {
        findViewById(id).setOnClickListener(v -> appendDigit(d));
    }

    private void appendDigit(String d) {
        if (resetOnNext) {
            current = "0";
            resetOnNext = false;
        }
        if ("00".equals(d)) {
            if ("0".equals(current)) return;
            current = current + "00";
        } else if ("0".equals(current)) {
            current = d;
        } else {
            current = current + d;
        }
        updateDisplay();
    }

    private void appendDot() {
        if (resetOnNext) {
            current = "0";
            resetOnNext = false;
        }
        if (!current.contains(".")) {
            current = current + ".";
            updateDisplay();
        }
    }

    private void clearAll() {
        current = "0";
        pendingOp = null;
        pendingValue = 0;
        resetOnNext = false;
        updateDisplay();
    }

    private void backspace() {
        if (resetOnNext) {
            clearAll();
            return;
        }
        if (current.length() <= 1) current = "0";
        else current = current.substring(0, current.length() - 1);
        updateDisplay();
    }

    private void setOp(String op) {
        double value = parseCurrent();
        if (pendingOp != null && !resetOnNext) {
            pendingValue = compute(pendingValue, value, pendingOp);
            current = trim(pendingValue);
        } else {
            pendingValue = value;
        }
        pendingOp = op;
        resetOnNext = true;
        updateDisplay();
    }

    private void equals() {
        if (pendingOp == null) return;
        double result = compute(pendingValue, parseCurrent(), pendingOp);
        current = trim(result);
        pendingOp = null;
        resetOnNext = true;
        updateDisplay();
    }

    private double compute(double a, double b, String op) {
        switch (op) {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "×":
                return a * b;
            case "÷":
                return b == 0 ? 0 : a / b;
            default:
                return b;
        }
    }

    private double parseCurrent() {
        try {
            return Double.parseDouble(current);
        } catch (Exception e) {
            return 0;
        }
    }

    private String trim(double v) {
        if (v == (long) v) return String.valueOf((long) v);
        return String.valueOf(v);
    }

    private void updateDisplay() {
        if (display != null) display.setText(current);
    }
}
