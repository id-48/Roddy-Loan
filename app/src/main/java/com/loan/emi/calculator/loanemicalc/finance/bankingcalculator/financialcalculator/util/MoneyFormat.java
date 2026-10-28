package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util;

import android.content.Context;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyFormat {
    private MoneyFormat() {
    }

    public static String amountOnly(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(Math.round(amount));
    }

    public static String format(Context context, double amount) {
        return AppPrefs.getCurrencySymbol(context) + amountOnly(amount);
    }

    public static String formatDecimal(Context context, double amount) {
        String symbol = AppPrefs.getCurrencySymbol(context);
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return symbol + nf.format(amount);
    }
}
