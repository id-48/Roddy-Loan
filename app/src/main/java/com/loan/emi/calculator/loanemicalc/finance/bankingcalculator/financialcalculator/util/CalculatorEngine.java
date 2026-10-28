package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared calculator runner for all banking / loan / GST / mutual / stock / other screens.
 * Reads whatever EditTexts exist on the layout and fills the result card.
 */
public final class CalculatorEngine {
    private CalculatorEngine() {
    }

    public static void run(Activity activity) {
        run(activity, true);
    }

    /** @param addTax true = add GST/VAT on amount; false = extract tax from inclusive amount */
    public static void run(Activity activity, boolean addTax) {
        hideKeyboard(activity);
        try {
            if (runStockGstKnown(activity, addTax)) {
                return;
            }
            if (runLoanKnown(activity)) {
                return;
            }
            if (runBankingKnown(activity)) {
                return;
            }
            if (runSpecial(activity)) {
                return;
            }
            runGeneric(activity);
        } catch (Exception e) {
            Toast.makeText(activity, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private static boolean runStockGstKnown(Activity activity, boolean addTax) {
        // Options: premium × lot
        EditText etPremium = find(activity, "etPremium");
        if (etPremium != null && find(activity, "etLotSize") != null) {
            double premium = require(etPremium);
            double lot = require(find(activity, "etLotSize"));
            double strike = find(activity, "etStrikePrice") != null ? require(find(activity, "etStrikePrice")) : 0;
            double total = premium * lot;
            setText(activity, "tvTotalPremium", MoneyFormat.formatDecimal(activity, total));
            setText(activity, "tvBreakeven", MoneyFormat.formatDecimal(activity, strike + premium));
            setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, total));
            showResult(activity);
            return true;
        }

        // Intraday / Delivery / Futures P&L
        EditText buy = find(activity, "etBuyPrice");
        EditText sell = find(activity, "etSellPrice");
        if (buy != null && sell != null) {
            double buyP = require(buy);
            double sellP = require(sell);
            EditText qtyEt = first(activity, "etQuantity", "etLotSize");
            double qty = qtyEt != null ? require(qtyEt) : 1;
            double turnover = (buyP + sellP) * qty;
            boolean intraday = hasView(activity, "tvBreakeven") && find(activity, "etLotSize") == null;
            boolean futures = find(activity, "etLotSize") != null;
            double chargePct = futures ? 0.0003 : (intraday ? 0.0005 : 0.001);
            double charges = Math.max(20, turnover * chargePct);
            double gross = (sellP - buyP) * qty;
            double net = gross - charges;
            setText(activity, "tvNetPl", MoneyFormat.formatDecimal(activity, net));
            setText(activity, "tvCharges", MoneyFormat.formatDecimal(activity, charges));
            setText(activity, "tvProfit", MoneyFormat.formatDecimal(activity, net));
            setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, net));
            if (hasView(activity, "tvBreakeven")) {
                double be = buyP + (charges / Math.max(1, qty));
                setText(activity, "tvBreakeven", MoneyFormat.formatDecimal(activity, be));
            }
            showResult(activity);
            return true;
        }

        // Reverse GST: tax-inclusive total → original + tax
        EditText etTotal = find(activity, "etTotal");
        if (etTotal != null && find(activity, "etRate") != null && hasView(activity, "tvOriginal")) {
            double total = require(etTotal);
            double rate = require(find(activity, "etRate"));
            double original = total * 100.0 / (100.0 + rate);
            double tax = total - original;
            setText(activity, "tvOriginal", MoneyFormat.formatDecimal(activity, original));
            setText(activity, "tvTax", MoneyFormat.formatDecimal(activity, tax));
            setText(activity, "tvGstAmount", MoneyFormat.formatDecimal(activity, tax));
            setText(activity, "tvNetAmount", MoneyFormat.formatDecimal(activity, original));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, total));
            showResult(activity);
            return true;
        }

        // Multi-rate GST → CGST + SGST
        if (hasView(activity, "tvCgst") && find(activity, "etPrice") != null
                && find(activity, "etRate") != null) {
            double price = require(find(activity, "etPrice"));
            double rate = require(find(activity, "etRate"));
            double totalGst = price * rate / 100.0;
            double half = totalGst / 2.0;
            setText(activity, "tvCgst", MoneyFormat.formatDecimal(activity, half));
            setText(activity, "tvSgst", MoneyFormat.formatDecimal(activity, half));
            setText(activity, "tvTotalGst", MoneyFormat.formatDecimal(activity, totalGst));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, price + totalGst));
            showResult(activity);
            return true;
        }

        // Sales / Service tax (etPrice + etRate → tvTaxAmount + tvGrossAmount)
        if (hasView(activity, "tvGrossAmount") && find(activity, "etPrice") != null
                && find(activity, "etRate") != null) {
            double price = require(find(activity, "etPrice"));
            double rate = require(find(activity, "etRate"));
            double tax = price * rate / 100.0;
            setText(activity, "tvTaxAmount", MoneyFormat.formatDecimal(activity, tax));
            setText(activity, "tvGrossAmount", MoneyFormat.formatDecimal(activity, price + tax));
            showResult(activity);
            return true;
        }

        // Financial Sales Tax calculator (etAmount + etTaxRate)
        if (find(activity, "etTaxRate") != null && find(activity, "etAmount") != null
                && hasView(activity, "tvTaxAmount")) {
            double amount = require(find(activity, "etAmount"));
            double rate = require(find(activity, "etTaxRate"));
            double tax = amount * rate / 100.0;
            setText(activity, "tvTaxAmount", MoneyFormat.formatDecimal(activity, tax));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, amount + tax));
            showResult(activity);
            return true;
        }

        // GST calculator (Add / Remove)
        if (hasView(activity, "tvGstAmount") && find(activity, "etAmount") != null
                && find(activity, "etRate") != null) {
            double amount = require(find(activity, "etAmount"));
            double rate = require(find(activity, "etRate"));
            double net;
            double gst;
            double total;
            if (addTax) {
                net = amount;
                gst = amount * rate / 100.0;
                total = amount + gst;
            } else {
                total = amount;
                net = amount * 100.0 / (100.0 + rate);
                gst = total - net;
            }
            setText(activity, "tvNetAmount", MoneyFormat.formatDecimal(activity, net));
            setText(activity, "tvGstAmount", MoneyFormat.formatDecimal(activity, gst));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, total));
            showResult(activity);
            return true;
        }

        // VAT calculator (Add / Remove)
        if (hasView(activity, "tvVatAmount") && find(activity, "etAmount") != null
                && find(activity, "etRate") != null) {
            double amount = require(find(activity, "etAmount"));
            double rate = require(find(activity, "etRate"));
            double net;
            double vat;
            double total;
            if (addTax) {
                net = amount;
                vat = amount * rate / 100.0;
                total = amount + vat;
            } else {
                total = amount;
                net = amount * 100.0 / (100.0 + rate);
                vat = total - net;
            }
            setText(activity, "tvNetAmount", MoneyFormat.formatDecimal(activity, net));
            setText(activity, "tvVatAmount", MoneyFormat.formatDecimal(activity, vat));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, total));
            showResult(activity);
            return true;
        }

        return false;
    }

    /** All Loan-category calculators with correct result field IDs. */
    private static boolean runLoanKnown(Activity activity) {
        // Compare two loans (amount1/2)
        EditText a1 = find(activity, "etAmount1");
        EditText a2 = find(activity, "etAmount2");
        if (a1 != null && a2 != null) {
            double amt1 = require(a1);
            double amt2 = require(a2);
            double rate1 = require(find(activity, "etRate1"));
            double rate2 = require(find(activity, "etRate2"));
            int m1 = (int) Math.max(1, require(find(activity, "etMonths1")));
            int m2 = (int) Math.max(1, require(find(activity, "etMonths2")));
            double emi1 = EmiMath.monthlyEmi(amt1, rate1, m1);
            double emi2 = EmiMath.monthlyEmi(amt2, rate2, m2);
            double pay1 = EmiMath.totalPayable(emi1, m1);
            double pay2 = EmiMath.totalPayable(emi2, m2);
            double int1 = EmiMath.totalInterest(amt1, emi1, m1);
            double int2 = EmiMath.totalInterest(amt2, emi2, m2);
            setText(activity, "tvEmi1", MoneyFormat.formatDecimal(activity, emi1));
            setText(activity, "tvEmi2", MoneyFormat.formatDecimal(activity, emi2));
            setText(activity, "tvInt1", MoneyFormat.formatDecimal(activity, int1));
            setText(activity, "tvInt2", MoneyFormat.formatDecimal(activity, int2));
            setText(activity, "tvPay1", MoneyFormat.formatDecimal(activity, pay1));
            setText(activity, "tvPay2", MoneyFormat.formatDecimal(activity, pay2));
            setText(activity, "tvDiff", MoneyFormat.formatDecimal(activity, Math.abs(pay1 - pay2)));
            showResult(activity);
            return true;
        }

        // Loan comparison (same amount, two offers)
        EditText etCmpAmount = find(activity, "etAmount");
        EditText etRate1 = find(activity, "etRate1");
        EditText etRate2 = find(activity, "etRate2");
        EditText etMonths1 = find(activity, "etMonths1");
        EditText etMonths2 = find(activity, "etMonths2");
        if (etCmpAmount != null && etRate1 != null && etRate2 != null && etMonths1 != null && etMonths2 != null
                && find(activity, "etExtra") == null && hasView(activity, "tvDecision")) {
            double amount = require(etCmpAmount);
            int m1 = (int) Math.max(1, require(etMonths1));
            int m2 = (int) Math.max(1, require(etMonths2));
            double emi1 = EmiMath.monthlyEmi(amount, require(etRate1), m1);
            double emi2 = EmiMath.monthlyEmi(amount, require(etRate2), m2);
            double int1 = EmiMath.totalInterest(amount, emi1, m1);
            double int2 = EmiMath.totalInterest(amount, emi2, m2);
            setText(activity, "tvEmi1", MoneyFormat.formatDecimal(activity, emi1));
            setText(activity, "tvEmi2", MoneyFormat.formatDecimal(activity, emi2));
            setText(activity, "tvInt1", MoneyFormat.formatDecimal(activity, int1));
            setText(activity, "tvInt2", MoneyFormat.formatDecimal(activity, int2));
            String better = int1 <= int2 ? "Offer 1 saves more interest" : "Offer 2 saves more interest";
            setText(activity, "tvDecision", better + " · Diff "
                    + MoneyFormat.formatDecimal(activity, Math.abs(int1 - int2)));
            showResult(activity);
            return true;
        }

        // Interest-only monthly payment
        if (hasView(activity, "tvInterestPayment") && find(activity, "etAmount") != null
                && find(activity, "etRate") != null && find(activity, "etMonths") == null
                && find(activity, "etExtra") == null) {
            double amount = require(find(activity, "etAmount"));
            double rate = require(find(activity, "etRate"));
            double monthly = amount * (rate / 100.0) / 12.0;
            setText(activity, "tvInterestPayment", MoneyFormat.formatDecimal(activity, monthly));
            setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, monthly));
            showResult(activity);
            return true;
        }

        // Value Add / prepayment savings
        EditText etExtra = find(activity, "etExtra");
        EditText etEmiInput = find(activity, "etEmi");
        if (etExtra != null && etEmiInput != null && find(activity, "etAmount") != null) {
            double principal = require(find(activity, "etAmount"));
            double rate = require(find(activity, "etRate"));
            double extra = require(etExtra);
            double emi = require(etEmiInput);
            int normalMonths = monthsForEmi(principal, rate, emi);
            double normalInterest = EmiMath.totalInterest(principal, emi, normalMonths);
            // with extra monthly payment
            double boosted = emi + extra;
            int newMonths = monthsForEmi(principal, rate, boosted);
            double newInterest = EmiMath.totalInterest(principal, boosted, newMonths);
            setText(activity, "tvInterestSaved", MoneyFormat.formatDecimal(activity,
                    Math.max(0, normalInterest - newInterest)));
            setText(activity, "tvTenureSaved", Math.max(0, normalMonths - newMonths) + " months");
            showResult(activity);
            return true;
        }

        // Mortgage
        EditText prop = find(activity, "etPropertyPrice");
        if (prop != null && find(activity, "etDownPayment") != null && find(activity, "etTermYears") != null) {
            double price = require(prop);
            double down = require(find(activity, "etDownPayment"));
            double loan = Math.max(0, price - down);
            double rate = require(find(activity, "etInterestRate"));
            int months = (int) Math.max(1, require(find(activity, "etTermYears")) * 12);
            double emi = EmiMath.monthlyEmi(loan, rate, months);
            double total = EmiMath.totalPayable(emi, months);
            double interest = EmiMath.totalInterest(loan, emi, months);
            setText(activity, "tvMonthlyPayment", MoneyFormat.formatDecimal(activity, emi));
            setText(activity, "tvMonthlyEmi", MoneyFormat.formatDecimal(activity, emi));
            setText(activity, "tvLoanAmount", MoneyFormat.formatDecimal(activity, loan));
            setText(activity, "tvTotalInterest", MoneyFormat.formatDecimal(activity, interest));
            setText(activity, "tvTotalPay", MoneyFormat.formatDecimal(activity, total));
            showResult(activity);
            return true;
        }

        // Auto loan
        EditText vehicle = find(activity, "etVehiclePrice");
        if (vehicle != null && find(activity, "etDownPayment") != null) {
            double price = require(vehicle);
            double down = require(find(activity, "etDownPayment"));
            double loan = Math.max(0, price - down);
            double rate = require(find(activity, "etInterestRate"));
            int months = (int) Math.max(1, require(find(activity, "etTenureMonths")));
            double emi = EmiMath.monthlyEmi(loan, rate, months);
            double total = EmiMath.totalPayable(emi, months);
            setText(activity, "tvMonthlyEmi", MoneyFormat.formatDecimal(activity, emi));
            setText(activity, "tvTotalPay", MoneyFormat.formatDecimal(activity, total));
            setText(activity, "tvTotalInterest", MoneyFormat.formatDecimal(activity, total - loan));
            showResult(activity);
            return true;
        }

        // Loan amount from affordable EMI
        EditText affEmi = find(activity, "etAffordableEmi");
        if (affEmi != null && find(activity, "etTenureMonths") != null) {
            double emi = require(affEmi);
            double rate = require(find(activity, "etInterestRate"));
            int months = (int) Math.max(1, require(find(activity, "etTenureMonths")));
            double principal = principalFromEmi(emi, rate, months);
            setText(activity, "tvMaxLoanAmount", MoneyFormat.formatDecimal(activity, principal));
            setText(activity, "tvSummary", "You can borrow up to "
                    + MoneyFormat.formatDecimal(activity, principal)
                    + " at EMI " + MoneyFormat.formatDecimal(activity, emi));
            setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, principal));
            showResult(activity);
            return true;
        }

        // Affordability from income
        EditText income = find(activity, "etIncome");
        if (income != null && find(activity, "etExistingEmi") != null
                && hasView(activity, "tvAffordableAmount")) {
            double maxEmi = Math.max(0, require(income) * 0.4 - require(find(activity, "etExistingEmi")));
            double rate = require(find(activity, "etRate"));
            int months = (int) Math.max(1, require(find(activity, "etMonths")));
            double loan = principalFromEmi(maxEmi, rate, months);
            setText(activity, "tvAffordableAmount", MoneyFormat.formatDecimal(activity, loan));
            setText(activity, "tvAffordableEmi", MoneyFormat.formatDecimal(activity, maxEmi));
            setText(activity, "tvSummary", "Based on 40% of income after existing EMIs");
            showResult(activity);
            return true;
        }

        // Interest rate from EMI (binary search)
        EditText etMonthlyEmi = find(activity, "etMonthlyEmi");
        if (etMonthlyEmi != null && find(activity, "etLoanAmount") != null
                && find(activity, "etTenureMonths") != null && hasView(activity, "tvRateResult")
                && find(activity, "etInterestRate") == null) {
            double principal = require(find(activity, "etLoanAmount"));
            double emi = require(etMonthlyEmi);
            int months = (int) Math.max(1, require(find(activity, "etTenureMonths")));
            double rate = rateFromEmi(principal, emi, months);
            setText(activity, "tvRateResult", String.format(Locale.US, "%.2f%% p.a.", rate));
            setText(activity, "tvResult", String.format(Locale.US, "%.2f%%", rate));
            showResult(activity);
            return true;
        }

        // Tenure from EMI
        if (etMonthlyEmi != null && find(activity, "etLoanAmount") != null
                && find(activity, "etInterestRate") != null && hasView(activity, "tvTenureResult")) {
            double principal = require(find(activity, "etLoanAmount"));
            double emi = require(etMonthlyEmi);
            double rate = require(find(activity, "etInterestRate"));
            int months = monthsForEmi(principal, rate, emi);
            int years = months / 12;
            int rem = months % 12;
            setText(activity, "tvTenureResult", years + " yrs " + rem + " mo (" + months + " months)");
            setText(activity, "tvSummary", "Total payable "
                    + MoneyFormat.formatDecimal(activity, emi * months));
            showResult(activity);
            return true;
        }

        // Moratorium
        if (find(activity, "etMoratoriumMonths") != null && find(activity, "etLoanAmount") != null) {
            double principal = require(find(activity, "etLoanAmount"));
            double rate = require(find(activity, "etInterestRate"));
            int mor = (int) Math.max(0, require(find(activity, "etMoratoriumMonths")));
            int rem = (int) Math.max(1, require(find(activity, "etRemainingTenure")));
            double oldEmi = EmiMath.monthlyEmi(principal, rate, rem);
            double newPrincipal = principal * Math.pow(1 + rate / 12.0 / 100.0, mor);
            double newEmi = EmiMath.monthlyEmi(newPrincipal, rate, rem);
            setText(activity, "tvNewEmi", MoneyFormat.formatDecimal(activity, newEmi));
            setText(activity, "tvExtraInterest", MoneyFormat.formatDecimal(activity,
                    Math.max(0, (newEmi - oldEmi) * rem + (newPrincipal - principal))));
            setText(activity, "tvMorSummary", "Principal after moratorium: "
                    + MoneyFormat.formatDecimal(activity, newPrincipal));
            showResult(activity);
            return true;
        }

        // Amortization first-month breakdown
        if (hasView(activity, "tvSummary") && find(activity, "etAmount") != null
                && find(activity, "etMonths") != null && find(activity, "etRate") != null
                && find(activity, "etExtra") == null && hasView(activity, "tvEmi")
                && !hasView(activity, "tvDecision") && !hasView(activity, "tvInterestPayment")) {
            double principal = require(find(activity, "etAmount"));
            double rate = require(find(activity, "etRate"));
            int months = (int) Math.max(1, require(find(activity, "etMonths")));
            double emi = EmiMath.monthlyEmi(principal, rate, months);
            double firstInterest = principal * (rate / 12.0 / 100.0);
            double firstPrincipal = emi - firstInterest;
            setText(activity, "tvEmi", MoneyFormat.formatDecimal(activity, emi));
            setText(activity, "tvPrincipal", MoneyFormat.formatDecimal(activity, firstPrincipal));
            setText(activity, "tvInterest", MoneyFormat.formatDecimal(activity, firstInterest));
            setText(activity, "tvSummary", "Total interest "
                    + MoneyFormat.formatDecimal(activity, EmiMath.totalInterest(principal, emi, months))
                    + " · Total payable "
                    + MoneyFormat.formatDecimal(activity, EmiMath.totalPayable(emi, months)));
            showResult(activity);
            return true;
        }

        // Standard Loan EMI (loan category + banking EMI share same pattern)
        EditText etLoanAmt = find(activity, "etLoanAmount");
        if (etLoanAmt != null && find(activity, "etInterestRate") != null
                && (find(activity, "etTenure") != null || find(activity, "rgTenureType") != null)
                && find(activity, "etMonthlyEmi") == null && find(activity, "etMoratoriumMonths") == null) {
            double principal = require(etLoanAmt);
            double rate = require(find(activity, "etInterestRate"));
            int months = periodToMonths(activity, (int) require(first(activity, "etTenure", "etTenureMonths")));
            double emi = EmiMath.monthlyEmi(principal, rate, months);
            double total = EmiMath.totalPayable(emi, months);
            double interest = EmiMath.totalInterest(principal, emi, months);
            setAllResults(activity, emi, interest, total);
            showResult(activity);
            return true;
        }

        // Simple / compound interest calculator (Interest)
        EditText etPrincipal = find(activity, "etPrincipal");
        if (etPrincipal != null && find(activity, "etDuration") != null && find(activity, "etRate") != null
                && hasView(activity, "tvInterest")) {
            double p = require(etPrincipal);
            double rate = require(find(activity, "etRate"));
            double years = require(find(activity, "etDuration"));
            String mode = "yearly";
            int toggleId = id(activity, "toggleCompounding");
            if (toggleId != 0) {
                View toggle = activity.findViewById(toggleId);
                if (toggle instanceof com.google.android.material.button.MaterialButtonToggleGroup) {
                    int checked = ((com.google.android.material.button.MaterialButtonToggleGroup) toggle)
                            .getCheckedButtonId();
                    if (checked == id(activity, "btnSimple")) mode = "simple";
                    else if (checked == id(activity, "btnMonthly")) mode = "monthly";
                }
            }
            double maturity;
            if ("simple".equals(mode)) {
                maturity = p + p * rate / 100.0 * years;
            } else if ("monthly".equals(mode)) {
                maturity = EmiMath.compoundAmount(p, rate, (int) Math.max(1, Math.round(years)), 12);
            } else {
                maturity = EmiMath.compoundAmount(p, rate, (int) Math.max(1, Math.round(years)), 1);
            }
            setText(activity, "tvInterest", MoneyFormat.formatDecimal(activity, maturity - p));
            setText(activity, "tvMaturity", MoneyFormat.formatDecimal(activity, maturity));
            showResult(activity);
            return true;
        }

        return false;
    }

    private static double principalFromEmi(double emi, double annualRate, int months) {
        if (months <= 0) return 0;
        if (annualRate <= 0) return emi * months;
        double r = annualRate / 12.0 / 100.0;
        double pow = Math.pow(1 + r, months);
        return emi * (pow - 1) / (r * pow);
    }

    private static int monthsForEmi(double principal, double annualRate, double emi) {
        if (emi <= 0) return 0;
        if (annualRate <= 0) return (int) Math.ceil(principal / emi);
        double r = annualRate / 12.0 / 100.0;
        if (emi <= principal * r) return 600; // never pays down
        double months = Math.log(emi / (emi - principal * r)) / Math.log(1 + r);
        return (int) Math.max(1, Math.ceil(months));
    }

    private static double rateFromEmi(double principal, double emi, int months) {
        if (principal <= 0 || emi <= 0 || months <= 0) return 0;
        double low = 0.01;
        double high = 100;
        for (int i = 0; i < 60; i++) {
            double mid = (low + high) / 2.0;
            double calc = EmiMath.monthlyEmi(principal, mid, months);
            if (calc > emi) high = mid;
            else low = mid;
        }
        return (low + high) / 2.0;
    }

    private static void hideKeyboard(Activity activity) {
        View focus = activity.getCurrentFocus();
        if (focus == null) {
            focus = activity.findViewById(android.R.id.content);
        }
        if (focus != null) {
            focus.clearFocus();
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        }
    }

    /** Explicit handlers so PPF / FD / RD / Investment / ROI always fill the right labels. */
    private static boolean runBankingKnown(Activity activity) {
        // ROI: initial vs current
        EditText etInitInv = find(activity, "etInitialInvestment");
        EditText etCurrent = find(activity, "etCurrentValue");
        if (etInitInv != null && etCurrent != null) {
            double initial = require(etInitInv);
            double current = require(etCurrent);
            double profit = current - initial;
            double roiPct = initial == 0 ? 0 : (profit / initial) * 100.0;
            setText(activity, "tvProfitLoss", MoneyFormat.formatDecimal(activity, profit));
            setText(activity, "tvRoiPercentage", String.format(Locale.US, "%.2f%%", roiPct));
            setText(activity, "tvResult", String.format(Locale.US, "%.2f%%", roiPct));
            showResult(activity);
            return true;
        }

        // PPF: yearly investment
        EditText etYearly = find(activity, "etYearlyInvestment");
        if (etYearly != null) {
            double yearly = require(etYearly);
            double rate = require(first(activity, "etInterestRate", "etRate"));
            int years = (int) Math.max(1, require(first(activity, "etDuration", "etYears", "etTenure")));
            double maturity = ppfMaturity(yearly, rate, years);
            double invested = yearly * years;
            fillMaturityCard(activity, invested, maturity - invested, maturity);
            showResult(activity);
            return true;
        }

        // FD: deposit + period (+ rgPeriodType)
        EditText etDeposit = find(activity, "etDepositAmount");
        if (etDeposit != null && find(activity, "etPeriod") != null) {
            double principal = require(etDeposit);
            double rate = require(first(activity, "etInterestRate", "etRate"));
            int months = periodToMonths(activity, (int) require(find(activity, "etPeriod")));
            double years = months / 12.0;
            double maturity = EmiMath.compoundAmount(principal, rate, Math.max(1, (int) Math.ceil(years)), 4);
            // more accurate for fractional years
            maturity = principal * Math.pow(1 + rate / 100.0 / 4.0, 4.0 * (months / 12.0));
            fillMaturityCard(activity, principal, maturity - principal, maturity);
            showResult(activity);
            return true;
        }

        // RD: monthly deposit
        EditText etMonthlyDep = find(activity, "etMonthlyDeposit");
        if (etMonthlyDep != null) {
            double monthly = require(etMonthlyDep);
            double rate = require(first(activity, "etInterestRate", "etRate", "etReturnRate"));
            int months = periodToMonths(activity, (int) require(first(activity, "etPeriod", "etYears", "etTenure")));
            if (find(activity, "etYears") != null && find(activity, "etPeriod") == null) {
                months = (int) require(find(activity, "etYears")) * 12;
            }
            double maturity = EmiMath.sipFutureValue(monthly, rate, months);
            double invested = monthly * months;
            fillMaturityCard(activity, invested, maturity - invested, maturity);
            showResult(activity);
            return true;
        }

        // Investment calculator: initial + monthly + return + years
        EditText etInitial = find(activity, "etInitial");
        EditText etMonthly = find(activity, "etMonthly");
        EditText etReturn = find(activity, "etReturn");
        EditText etYears = find(activity, "etYears");
        if (etInitial != null && etMonthly != null && etReturn != null && etYears != null) {
            double initial = require(etInitial);
            double monthly = require(etMonthly);
            double rate = require(etReturn);
            int years = (int) Math.max(1, require(etYears));
            int months = years * 12;
            double fvLump = EmiMath.lumpsumFutureValue(initial, rate, years);
            double fvSip = EmiMath.sipFutureValue(monthly, rate, months);
            double maturity = fvLump + fvSip;
            double invested = initial + monthly * months;
            fillMaturityCard(activity, invested, maturity - invested, maturity);
            setText(activity, "tvWealthGained", MoneyFormat.formatDecimal(activity, maturity - invested));
            showResult(activity);
            return true;
        }

        // SIP style
        EditText etSip = first(activity, "etMonthlyInvestment", "etInitialSip");
        if (etSip != null) {
            double monthly = require(etSip);
            double rate = require(first(activity, "etReturnRate", "etInterestRate", "etRate", "etReturn"));
            int years = (int) Math.max(1, require(first(activity, "etYears", "etTenure", "etPeriod")));
            int months = years * 12;
            if (find(activity, "etPeriod") != null && find(activity, "etYears") == null) {
                months = periodToMonths(activity, years);
            }
            EditText stepUp = find(activity, "etStepUp");
            double maturity;
            if (stepUp != null) {
                maturity = sipWithStepUp(monthly, rate, years, require(stepUp));
            } else {
                maturity = EmiMath.sipFutureValue(monthly, rate, months);
            }
            double invested = monthly * months;
            fillMaturityCard(activity, invested, maturity - invested, maturity);
            setText(activity, "tvInvestedAmount", MoneyFormat.formatDecimal(activity, invested));
            setText(activity, "tvEstimatedReturns", MoneyFormat.formatDecimal(activity, maturity - invested));
            showResult(activity);
            return true;
        }

        // Lumpsum / compound
        EditText etInvest = first(activity, "etInvestment", "etPrincipal");
        if (etInvest != null && (find(activity, "etYears") != null || find(activity, "etN") != null)
                && find(activity, "etLoanAmount") == null) {
            double principal = require(etInvest);
            double rate = require(first(activity, "etRate", "etInterestRate", "etReturnRate"));
            int years = (int) Math.max(1, require(first(activity, "etYears", "etN", "etDuration")));
            double maturity = EmiMath.lumpsumFutureValue(principal, rate, years);
            if (find(activity, "etPrincipal") != null && find(activity, "etN") == null) {
                // compound calculator often compounds yearly
                maturity = EmiMath.compoundAmount(principal, rate, years, 1);
            }
            fillMaturityCard(activity, principal, maturity - principal, maturity);
            setText(activity, "tvMaturityValue", MoneyFormat.formatDecimal(activity, maturity));
            setText(activity, "tvInterestGained", MoneyFormat.formatDecimal(activity, maturity - principal));
            setText(activity, "tvProfit", MoneyFormat.formatDecimal(activity, maturity - principal));
            showResult(activity);
            return true;
        }

        return false;
    }

    private static double ppfMaturity(double yearly, double ratePercent, int years) {
        double balance = 0;
        double r = ratePercent / 100.0;
        for (int y = 0; y < years; y++) {
            balance = (balance + yearly) * (1 + r);
        }
        return balance;
    }

    private static double sipWithStepUp(double monthly, double ratePercent, int years, double stepUpPct) {
        double total = 0;
        double r = ratePercent / 12.0 / 100.0;
        double installment = monthly;
        int months = years * 12;
        for (int m = 1; m <= months; m++) {
            total = (total + installment) * (1 + r);
            if (m % 12 == 0) {
                installment *= (1 + stepUpPct / 100.0);
            }
        }
        return total;
    }

    private static int periodToMonths(Activity activity, int raw) {
        RadioGroup rg = activity.findViewById(id(activity, "rgPeriodType"));
        if (rg == null) rg = activity.findViewById(id(activity, "rgTenureType"));
        if (rg != null) {
            int checked = rg.getCheckedRadioButtonId();
            if (checked != -1) {
                View rb = activity.findViewById(checked);
                if (rb instanceof RadioButton) {
                    CharSequence t = ((RadioButton) rb).getText();
                    if (t != null) {
                        String s = t.toString().toLowerCase(Locale.US);
                        if (s.contains("month") || s.equals("mo")) {
                            return Math.max(1, raw);
                        }
                        if (s.contains("year") || s.equals("yr")) {
                            return Math.max(1, raw * 12);
                        }
                    }
                }
            }
            // Prefer Years radio if present and checked by default in XML
            View rbYears = activity.findViewById(id(activity, "rbYears"));
            if (rbYears instanceof RadioButton && ((RadioButton) rbYears).isChecked()) {
                return Math.max(1, raw * 12);
            }
            return Math.max(1, raw);
        }
        return Math.max(1, raw);
    }

    private static void fillMaturityCard(Activity a, double invested, double interest, double maturity) {
        String inv = MoneyFormat.formatDecimal(a, invested);
        String ints = MoneyFormat.formatDecimal(a, interest);
        String mat = MoneyFormat.formatDecimal(a, maturity);
        setText(a, "tvDepositAmount", inv);
        setText(a, "tvTotalDeposit", inv);
        setText(a, "tvTotalInvestment", inv);
        setText(a, "tvInvestedAmount", inv);
        setText(a, "tvPrincipal", inv);
        setText(a, "tvTotalInterest", ints);
        setText(a, "tvInterest", ints);
        setText(a, "tvInterestEarned", ints);
        setText(a, "tvInterestGained", ints);
        setText(a, "tvEstimatedReturns", ints);
        setText(a, "tvWealthGained", ints);
        setText(a, "tvProfit", ints);
        setText(a, "tvMaturityAmount", mat);
        setText(a, "tvMaturity", mat);
        setText(a, "tvMaturityValue", mat);
        setText(a, "tvFutureValue", mat);
        setText(a, "tvTotalAmount", mat);
        setText(a, "tvResult", mat);
        setText(a, "tvRoiPercentage", String.format(Locale.US, "%.2f%%",
                invested == 0 ? 0 : (interest / invested) * 100.0));
    }

    private static double require(EditText et) {
        if (et == null) throw new NumberFormatException("missing");
        return parse(et);
    }

    private static boolean runSpecial(Activity activity) {
        EditText etWeight = find(activity, "etWeight");
        EditText etHeight = find(activity, "etHeight");
        if (etWeight != null && etHeight != null) {
            double w = parse(etWeight);
            double hCm = parse(etHeight);
            if (w <= 0 || hCm <= 0) {
                toast(activity, "Enter weight and height");
                return true;
            }
            double h = hCm / 100.0;
            double bmi = w / (h * h);
            String cat;
            if (bmi < 18.5) cat = "Underweight";
            else if (bmi < 25) cat = "Normal";
            else if (bmi < 30) cat = "Overweight";
            else cat = "Obese";
            setText(activity, "tvBmiScore", String.format(Locale.US, "%.1f", bmi));
            setText(activity, "tvBmiCategory", cat);
            setText(activity, "tvResult", String.format(Locale.US, "BMI %.1f · %s", bmi, cat));
            showResult(activity);
            return true;
        }

        EditText etMeters = find(activity, "etMeters");
        if (etMeters != null && find(activity, "etWeight") == null) {
            double m = parse(etMeters);
            setText(activity, "tvCm", String.format(Locale.US, "%.2f cm", m * 100));
            setText(activity, "tvKm", String.format(Locale.US, "%.4f km", m / 1000.0));
            setText(activity, "tvFeet", String.format(Locale.US, "%.2f ft", m * 3.28084));
            setText(activity, "tvInches", String.format(Locale.US, "%.2f in", m * 39.3701));
            showResult(activity);
            return true;
        }

        EditText etNumber = find(activity, "etNumber");
        if (etNumber != null && find(activity, "etRate") == null && find(activity, "etCost") == null) {
            long n = (long) parse(etNumber);
            setText(activity, "tvWords", NumberToWords.toWords(n));
            setText(activity, "tvResult", NumberToWords.toWords(n));
            showResult(activity);
            return true;
        }

        EditText etPetrol = find(activity, "etPetrol");
        EditText etDiesel = find(activity, "etDiesel");
        if (etPetrol != null && etDiesel != null) {
            double p = parse(etPetrol);
            double d = parse(etDiesel);
            setText(activity, "tvDisplay",
                    String.format(Locale.US, "Petrol: %s\nDiesel: %s",
                            MoneyFormat.formatDecimal(activity, p),
                            MoneyFormat.formatDecimal(activity, d)));
            showView(activity, "cardDisplay");
            showResult(activity);
            return true;
        }

        EditText etCost = find(activity, "etCost");
        EditText etRevenue = find(activity, "etRevenue");
        if (etCost != null && etRevenue != null) {
            double cost = parse(etCost);
            double rev = parse(etRevenue);
            double profit = rev - cost;
            double margin = rev == 0 ? 0 : (profit / rev) * 100.0;
            setText(activity, "tvProfit", MoneyFormat.formatDecimal(activity, profit));
            setText(activity, "tvMargin", String.format(Locale.US, "%.2f%%", margin));
            setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, profit));
            showResult(activity);
            return true;
        }

        EditText etSalary = find(activity, "etSalary");
        if (etSalary != null && find(activity, "etTenure") != null && find(activity, "etLoanAmount") == null) {
            double salary = parse(etSalary);
            double years = parse(find(activity, "etTenure"));
            double gratuity = (salary * 15.0 * years) / 26.0;
            setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, gratuity));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, gratuity));
            setText(activity, "tvMaturity", MoneyFormat.formatDecimal(activity, gratuity));
            showResult(activity);
            return true;
        }

        EditText etFixed = find(activity, "etFixedCost");
        EditText etVar = find(activity, "etVariableCost");
        EditText etSell = find(activity, "etSellingPrice");
        if (etFixed != null && etVar != null && etSell != null) {
            double fixed = parse(etFixed);
            double variable = parse(etVar);
            double selling = parse(etSell);
            double contrib = selling - variable;
            double units = contrib <= 0 ? 0 : fixed / contrib;
            setText(activity, "tvResult", String.format(Locale.US, "%.0f units", Math.ceil(units)));
            setText(activity, "tvBreakEven", String.format(Locale.US, "%.0f units", Math.ceil(units)));
            setText(activity, "tvTotalAmount", MoneyFormat.formatDecimal(activity, fixed));
            showResult(activity);
            return true;
        }

        if (activity.findViewById(id(activity, "row2000")) != null
                || activity.findViewById(id(activity, "tvGrandTotal")) != null) {
            runCashCounter(activity);
            return true;
        }

        return false;
    }

    private static void runCashCounter(Activity activity) {
        int[] dens = {2000, 500, 200, 100, 50, 20, 10, 5, 2, 1};
        long total = 0;
        for (int den : dens) {
            View row = activity.findViewById(id(activity, "row" + den));
            if (row == null) continue;
            EditText et = findEditIn(row);
            long count = 0;
            if (et != null) {
                String s = et.getText().toString().trim();
                if (!s.isEmpty()) count = Long.parseLong(s);
            }
            total += (long) den * count;
            TextView tv = row.findViewById(id(activity, "tvSubtotal"));
            if (tv != null) {
                tv.setText(MoneyFormat.formatDecimal(activity, den * count));
            }
        }
        setText(activity, "tvGrandTotal", MoneyFormat.formatDecimal(activity, total));
        setText(activity, "tvResult", MoneyFormat.formatDecimal(activity, total));
        showResult(activity);
    }

    private static void runGeneric(Activity activity) {
        List<EditText> edits = collectEdits(activity);
        if (edits.isEmpty()) {
            toast(activity, "Nothing to calculate");
            return;
        }
        for (EditText et : edits) {
            if (et.getText() == null || et.getText().toString().trim().isEmpty()) {
                toast(activity, "Please fill all fields");
                return;
            }
        }

        double[] v = new double[edits.size()];
        for (int i = 0; i < edits.size(); i++) {
            v[i] = parse(edits.get(i));
        }

        // Prefer known ID combinations
        EditText etLoan = first(activity, "etLoanAmount", "etPrincipal", "etAmount", "etDepositAmount",
                "etInvestment", "etMonthlyInvestment", "etMonthlyDeposit", "etInitial", "etPv",
                "etPropertyPrice", "etVehiclePrice", "etBase", "etPrice", "etCostPrice", "etBill",
                "etIncome", "etAffordableEmi");
        EditText etRate = first(activity, "etInterestRate", "etRate", "etReturnRate", "etReturn",
                "etRoi", "etGstRate", "etTaxRate", "etTipPercent", "etDiscount", "etMargin");
        EditText etTenure = first(activity, "etTenure", "etYears", "etMonths", "etPeriod",
                "etTenureMonths", "etTermYears", "etDuration", "etN", "etMoratoriumMonths",
                "etRemainingTenure", "etPeople", "etQuantity", "etLotSize", "etExtra", "etEmi",
                "etTax", "etAddition", "etStepUp", "etMonthly", "etDownPayment", "etExistingEmi",
                "etMonthlyEmi", "etVariableCost", "etSellingPrice", "etSellPrice", "etBuyPrice",
                "etCost", "etRevenue");

        double amount = etLoan != null ? parse(etLoan) : v[0];
        double rate = etRate != null ? parse(etRate) : (v.length > 1 ? v[1] : 10);
        double tenureRaw = etTenure != null ? parse(etTenure) : (v.length > 2 ? v[2] : 12);

        // Stock P&L style
        EditText buy = first(activity, "etBuyPrice");
        EditText sell = first(activity, "etSellPrice", "etSellingPrice");
        EditText qty = first(activity, "etQuantity", "etLotSize");
        if (buy != null && sell != null) {
            double q = qty != null ? parse(qty) : 1;
            double pnl = (parse(sell) - parse(buy)) * q;
            setAllResults(activity, Math.abs(pnl), pnl, pnl);
            setText(activity, "tvProfit", MoneyFormat.formatDecimal(activity, pnl));
            setText(activity, "tvLoss", MoneyFormat.formatDecimal(activity, Math.min(0, pnl)));
            showResult(activity);
            return;
        }

        // Discount / tip / tax
        EditText price = first(activity, "etPrice", "etBill", "etCostPrice");
        EditText disc = first(activity, "etDiscount", "etTipPercent", "etTaxRate", "etGstRate");
        if (price != null && disc != null && etLoan == null) {
            double p = parse(price);
            double d = parse(disc);
            double taxOrTip = p * d / 100.0;
            double finalAmt = p + taxOrTip;
            if (find(activity, "etDiscount") != null) {
                finalAmt = p - taxOrTip;
                EditText tax = find(activity, "etTax");
                if (tax != null) finalAmt += finalAmt * parse(tax) / 100.0;
            }
            if (find(activity, "etPeople") != null) {
                double people = Math.max(1, parse(find(activity, "etPeople")));
                setText(activity, "tvPerPerson", MoneyFormat.formatDecimal(activity, finalAmt / people));
            }
            setAllResults(activity, finalAmt, taxOrTip, p);
            showResult(activity);
            return;
        }

        // Margin on cost
        EditText cost = find(activity, "etCost");
        EditText marginEt = find(activity, "etMargin");
        if (cost != null && marginEt != null) {
            double c = parse(cost);
            double m = parse(marginEt);
            double sellPrice = c * (1 + m / 100.0);
            setAllResults(activity, sellPrice, sellPrice - c, c);
            showResult(activity);
            return;
        }

        // Price calculator cost vs selling
        EditText cp = find(activity, "etCostPrice");
        EditText sp = find(activity, "etSellingPrice");
        if (cp != null && sp != null) {
            double profit = parse(sp) - parse(cp);
            double pct = parse(cp) == 0 ? 0 : profit / parse(cp) * 100;
            setAllResults(activity, profit, pct, parse(sp));
            setText(activity, "tvProfit", MoneyFormat.formatDecimal(activity, profit));
            setText(activity, "tvMargin", String.format(Locale.US, "%.2f%%", pct));
            showResult(activity);
            return;
        }

        // Loan amount from EMI
        EditText affEmi = find(activity, "etAffordableEmi");
        if (affEmi != null) {
            double emi = parse(affEmi);
            double r = rate / 12.0 / 100.0;
            int months = (int) Math.max(1, tenureRaw);
            double principal;
            if (r <= 0) principal = emi * months;
            else {
                double pow = Math.pow(1 + r, months);
                principal = emi * (pow - 1) / (r * pow);
            }
            setAllResults(activity, principal, emi * months - principal, emi * months);
            showResult(activity);
            return;
        }

        // Affordability
        EditText income = find(activity, "etIncome");
        if (income != null) {
            double existing = 0;
            EditText ex = find(activity, "etExistingEmi");
            if (ex != null) existing = parse(ex);
            double maxEmi = Math.max(0, parse(income) * 0.4 - existing);
            int months = (int) Math.max(1, tenureRaw);
            double loan = maxEmi * months; // approx without rate compound first
            if (rate > 0) {
                double r = rate / 12.0 / 100.0;
                double pow = Math.pow(1 + r, months);
                loan = maxEmi * (pow - 1) / (r * pow);
            }
            setAllResults(activity, loan, maxEmi, loan);
            setText(activity, "tvMonthlyEmi", MoneyFormat.formatDecimal(activity, maxEmi));
            showResult(activity);
            return;
        }

        // Mortgage / auto: price - down payment
        EditText prop = first(activity, "etPropertyPrice", "etVehiclePrice");
        EditText down = find(activity, "etDownPayment");
        if (prop != null && down != null) {
            amount = Math.max(0, parse(prop) - parse(down));
        }

        // SIP / RD / monthly investment
        EditText monthlyInv = first(activity, "etMonthlyInvestment", "etMonthlyDeposit", "etInitialSip", "etMonthly");
        if (monthlyInv != null && (find(activity, "etReturnRate") != null || find(activity, "etInterestRate") != null
                || find(activity, "etRate") != null || find(activity, "etReturn") != null)) {
            int years = (int) Math.max(1, tenureRaw);
            // if tenure field looks like months (etPeriod with radio)
            int months = years;
            if (find(activity, "etYears") != null || find(activity, "etTermYears") != null) {
                months = years * 12;
            } else if (find(activity, "etPeriod") != null) {
                months = tenureMonths(activity, (int) tenureRaw);
            } else if (find(activity, "etMonths") != null || find(activity, "etTenureMonths") != null) {
                months = (int) tenureRaw;
            } else {
                months = years * 12;
            }
            double fv = EmiMath.sipFutureValue(parse(monthlyInv), rate, months);
            double invested = parse(monthlyInv) * months;
            setAllResults(activity, fv, fv - invested, invested);
            setText(activity, "tvFutureValue", MoneyFormat.formatDecimal(activity, fv));
            setText(activity, "tvMaturity", MoneyFormat.formatDecimal(activity, fv));
            showResult(activity);
            return;
        }

        // Lumpsum / FD / PPF / compound
        EditText deposit = first(activity, "etDepositAmount", "etInvestment", "etPrincipal", "etInitial", "etPv", "etLoanAmount", "etAmount");
        if (deposit != null) {
            int months = tenureMonths(activity, (int) Math.max(1, tenureRaw));
            boolean looksLikeYears = find(activity, "etYears") != null || find(activity, "etTermYears") != null
                    || find(activity, "etN") != null;
            if (looksLikeYears && find(activity, "etTenure") == null && find(activity, "etTenureMonths") == null
                    && find(activity, "etMonths") == null && find(activity, "etPeriod") == null) {
                months = (int) tenureRaw * 12;
            }

            // EMI style if tenure is months/years radio present OR etLoanAmount
            if (find(activity, "etLoanAmount") != null || find(activity, "rgTenureType") != null
                    || find(activity, "etTenure") != null || find(activity, "etTenureMonths") != null
                    || find(activity, "etMonths") != null || find(activity, "etRemainingTenure") != null) {
                if (find(activity, "etMoratoriumMonths") != null) {
                    int mor = (int) parse(find(activity, "etMoratoriumMonths"));
                    int rem = find(activity, "etRemainingTenure") != null
                            ? (int) parse(find(activity, "etRemainingTenure")) : months;
                    double newPrincipal = amount * Math.pow(1 + rate / 12.0 / 100.0, mor);
                    double emi = EmiMath.monthlyEmi(newPrincipal, rate, Math.max(1, rem));
                    setAllResults(activity, emi, EmiMath.totalInterest(newPrincipal, emi, rem),
                            EmiMath.totalPayable(emi, rem));
                    showResult(activity);
                    return;
                }
                double emi = EmiMath.monthlyEmi(amount, rate, Math.max(1, months));
                double total = EmiMath.totalPayable(emi, Math.max(1, months));
                double interest = EmiMath.totalInterest(amount, emi, Math.max(1, months));
                setAllResults(activity, emi, interest, total);
                showResult(activity);
                return;
            }

            // Compound / FD maturity
            int years = Math.max(1, months / 12);
            if (looksLikeYears) years = (int) Math.max(1, tenureRaw);
            double maturity = EmiMath.compoundAmount(amount, rate, years, 4);
            if (find(activity, "etInvestment") != null || find(activity, "etPv") != null) {
                maturity = EmiMath.lumpsumFutureValue(amount, rate, years);
            }
            setAllResults(activity, maturity, maturity - amount, amount);
            setText(activity, "tvMaturity", MoneyFormat.formatDecimal(activity, maturity));
            setText(activity, "tvFutureValue", MoneyFormat.formatDecimal(activity, maturity));
            showResult(activity);
            return;
        }

        // Fallback: use first values as EMI
        int months = (int) Math.max(1, tenureRaw);
        double emi = EmiMath.monthlyEmi(amount, rate, months);
        setAllResults(activity, emi, EmiMath.totalInterest(amount, emi, months),
                EmiMath.totalPayable(emi, months));
        showResult(activity);
    }

    private static int tenureMonths(Activity activity, int raw) {
        return periodToMonths(activity, raw);
    }

    private static void setAllResults(Activity a, double primary, double interestOrMid, double total) {
        String p = MoneyFormat.formatDecimal(a, primary);
        String i = MoneyFormat.formatDecimal(a, interestOrMid);
        String t = MoneyFormat.formatDecimal(a, total);
        setText(a, "tvMonthlyEmi", p);
        setText(a, "tvEmi", p);
        setText(a, "tvResult", p);
        setText(a, "tvTotalInterest", i);
        setText(a, "tvInterest", i);
        setText(a, "tvInterestEarned", i);
        setText(a, "tvInterestGained", i);
        setText(a, "tvEstimatedReturns", i);
        setText(a, "tvWealthGained", i);
        setText(a, "tvProfit", i);
        setText(a, "tvProfitLoss", i);
        setText(a, "tvTotalAmount", t);
        setText(a, "tvMaturity", t);
        setText(a, "tvMaturityAmount", t);
        setText(a, "tvMaturityValue", t);
        setText(a, "tvFutureValue", t);
        setText(a, "tvDepositAmount", MoneyFormat.formatDecimal(a, Math.max(0, total - interestOrMid)));
        setText(a, "tvTotalDeposit", MoneyFormat.formatDecimal(a, Math.max(0, total - interestOrMid)));
        setText(a, "tvTotalInvestment", MoneyFormat.formatDecimal(a, Math.max(0, total - interestOrMid)));
        setText(a, "tvInvestedAmount", MoneyFormat.formatDecimal(a, Math.max(0, total - interestOrMid)));
        setText(a, "tvPrincipal", MoneyFormat.formatDecimal(a, Math.max(0, total - interestOrMid)));
        setText(a, "tvGstAmount", i);
        setText(a, "tvNetAmount", t);
        setText(a, "tvTaxAmount", i);
        setText(a, "tvFinalPrice", t);
        setText(a, "tvTipAmount", i);
        setText(a, "tvGrandTotal", t);
        double invested = Math.max(0, total - interestOrMid);
        setText(a, "tvRoiPercentage", String.format(Locale.US, "%.2f%%",
                invested == 0 ? 0 : (interestOrMid / invested) * 100.0));
    }

    private static void showResult(Activity a) {
        showView(a, "cardResult");
        showView(a, "resultLayout");
        showView(a, "cardDisplay");
        showView(a, "layoutResult");
    }

    private static List<EditText> collectEdits(Activity activity) {
        List<EditText> list = new ArrayList<>();
        View root = activity.findViewById(android.R.id.content);
        if (root instanceof ViewGroup) {
            collectEdits((ViewGroup) root, list);
        }
        return list;
    }

    private static void collectEdits(ViewGroup group, List<EditText> out) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof EditText && child.getVisibility() == View.VISIBLE && child.isEnabled()) {
                out.add((EditText) child);
            } else if (child instanceof ViewGroup) {
                collectEdits((ViewGroup) child, out);
            }
        }
    }

    private static EditText findEditIn(View root) {
        if (root instanceof EditText) return (EditText) root;
        if (root instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) root;
            for (int i = 0; i < g.getChildCount(); i++) {
                EditText et = findEditIn(g.getChildAt(i));
                if (et != null) return et;
            }
        }
        return null;
    }

    private static TextView findTextIn(View root, String... names) {
        for (String name : names) {
            int res = id(root.getContext() instanceof Activity
                    ? (Activity) root.getContext()
                    : null, name);
            if (res == 0 && root.getContext() != null) {
                res = root.getContext().getResources().getIdentifier(name, "id", root.getContext().getPackageName());
            }
            if (res != 0) {
                View v = root.findViewById(res);
                if (v instanceof TextView) return (TextView) v;
            }
        }
        return null;
    }

    private static EditText first(Activity a, String... names) {
        for (String n : names) {
            EditText et = find(a, n);
            if (et != null) return et;
        }
        return null;
    }


    private static boolean hasView(Activity a, String name) {
        int res = id(a, name);
        if (res == 0) return false;
        return a.findViewById(res) != null;
    }

    private static EditText find(Activity a, String name) {
        int res = id(a, name);
        if (res == 0) return null;
        View v = a.findViewById(res);
        return v instanceof EditText ? (EditText) v : null;
    }

    private static int id(Activity a, String name) {
        if (a == null) return 0;
        return a.getResources().getIdentifier(name, "id", a.getPackageName());
    }

    private static void setText(Activity a, String name, String text) {
        int res = id(a, name);
        if (res == 0) return;
        View v = a.findViewById(res);
        if (v instanceof TextView) ((TextView) v).setText(text);
    }

    private static void showView(Activity a, String name) {
        int res = id(a, name);
        if (res == 0) return;
        View v = a.findViewById(res);
        if (v != null) v.setVisibility(View.VISIBLE);
    }

    private static double parse(EditText et) {
        String s = et.getText().toString().trim().replace(",", "");
        if (s.isEmpty()) throw new NumberFormatException("empty");
        return Double.parseDouble(s);
    }

    private static void toast(Activity a, String msg) {
        Toast.makeText(a, msg, Toast.LENGTH_SHORT).show();
    }

    /** Indian-style number to words for NumberToWord calculator. */
    public static final class NumberToWords {
        private static final String[] ONES = {
                "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
                "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
                "Seventeen", "Eighteen", "Nineteen"
        };
        private static final String[] TENS = {
                "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };

        private NumberToWords() {
        }

        public static String toWords(long n) {
            if (n == 0) return "Zero";
            if (n < 0) return "Minus " + toWords(-n);
            StringBuilder sb = new StringBuilder();
            n = append(sb, n, 10000000, "Crore ");
            n = append(sb, n, 100000, "Lakh ");
            n = append(sb, n, 1000, "Thousand ");
            n = append(sb, n, 100, "Hundred ");
            if (n > 0) {
                if (sb.length() > 0) sb.append("and ");
                if (n < 20) sb.append(ONES[(int) n]);
                else {
                    sb.append(TENS[(int) n / 10]);
                    if (n % 10 != 0) sb.append(" ").append(ONES[(int) n % 10]);
                }
            }
            return sb.toString().trim() + " Only";
        }

        private static long append(StringBuilder sb, long n, long div, String label) {
            if (n >= div) {
                long q = n / div;
                sb.append(toWordsUnderThousand(q)).append(" ").append(label);
                return n % div;
            }
            return n;
        }

        private static String toWordsUnderThousand(long n) {
            if (n < 20) return ONES[(int) n];
            if (n < 100) {
                return TENS[(int) n / 10] + (n % 10 == 0 ? "" : " " + ONES[(int) n % 10]);
            }
            return ONES[(int) n / 100] + " Hundred" + (n % 100 == 0 ? "" : " " + toWordsUnderThousand(n % 100));
        }
    }
}
