package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util;

public final class EmiMath {
    private EmiMath() {
    }

    public static double monthlyEmi(double principal, double annualRatePercent, int months) {
        if (months <= 0) {
            return 0;
        }
        if (annualRatePercent <= 0) {
            return principal / months;
        }
        double r = annualRatePercent / 12.0 / 100.0;
        double pow = Math.pow(1 + r, months);
        return principal * r * pow / (pow - 1);
    }

    public static double totalPayable(double emi, int months) {
        return emi * months;
    }

    public static double totalInterest(double principal, double emi, int months) {
        return Math.max(0, (emi * months) - principal);
    }

    public static double compoundAmount(double principal, double annualRatePercent, int years, int compoundsPerYear) {
        if (compoundsPerYear <= 0 || years < 0) {
            return principal;
        }
        double r = annualRatePercent / 100.0 / compoundsPerYear;
        return principal * Math.pow(1 + r, compoundsPerYear * years);
    }

    public static double sipFutureValue(double monthlyInvestment, double annualRatePercent, int months) {
        if (months <= 0) {
            return 0;
        }
        if (annualRatePercent <= 0) {
            return monthlyInvestment * months;
        }
        double r = annualRatePercent / 12.0 / 100.0;
        return monthlyInvestment * (Math.pow(1 + r, months) - 1) / r * (1 + r);
    }

    public static double lumpsumFutureValue(double principal, double annualRatePercent, int years) {
        return principal * Math.pow(1 + annualRatePercent / 100.0, years);
    }
}
