package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data;

import android.content.Context;
import android.content.SharedPreferences;

public final class LoanSession {
    private static final String PREFS = "loan_prefs";

    public static String loanType = "Personal Loan";
    public static String loanPurpose = "";
    public static double loanAmount = 100000;
    public static int tenureMonths = 24;
    public static double interestRate = 10.5;
    public static double monthlyEmi;
    public static double totalPayable;
    public static String fullName = "";
    public static String dob = "";
    public static String gender = "Male";
    public static String mobile = "";
    public static String email = "";
    public static String address = "";
    public static String employmentType = "Salaried";
    public static double monthlyIncome = 50000;
    public static double monthlyExpense = 20000;
    public static String selectedBank = "";
    public static int creditScore = 750;
    public static double spinDiscountPercent;
    public static boolean spinApplied;
    public static boolean applicationComplete;
    public static long submissionTime;
    /** Last loan-tracker step that already fired a local notification (0 = none). */
    public static int lastNotifiedTrackerStep;

    private LoanSession() {
    }

    private static SharedPreferences sp(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static void load(Context context) {
        SharedPreferences p = sp(context);
        loanType = p.getString("loanType", loanType);
        loanPurpose = p.getString("loanPurpose", loanPurpose);
        loanAmount = Double.longBitsToDouble(p.getLong("loanAmount", Double.doubleToLongBits(loanAmount)));
        tenureMonths = p.getInt("tenureMonths", tenureMonths);
        interestRate = Double.longBitsToDouble(p.getLong("interestRate", Double.doubleToLongBits(interestRate)));
        monthlyEmi = Double.longBitsToDouble(p.getLong("monthlyEmi", Double.doubleToLongBits(monthlyEmi)));
        totalPayable = Double.longBitsToDouble(p.getLong("totalPayable", Double.doubleToLongBits(totalPayable)));
        fullName = p.getString("fullName", fullName);
        dob = p.getString("dob", dob);
        gender = p.getString("gender", gender);
        mobile = p.getString("mobile", mobile);
        email = p.getString("email", email);
        address = p.getString("address", address);
        employmentType = p.getString("employmentType", employmentType);
        monthlyIncome = Double.longBitsToDouble(p.getLong("monthlyIncome", Double.doubleToLongBits(monthlyIncome)));
        monthlyExpense = Double.longBitsToDouble(p.getLong("monthlyExpense", Double.doubleToLongBits(monthlyExpense)));
        selectedBank = p.getString("selectedBank", selectedBank);
        creditScore = p.getInt("creditScore", creditScore);
        spinDiscountPercent = Double.longBitsToDouble(p.getLong("spinDiscountPercent", Double.doubleToLongBits(spinDiscountPercent)));
        spinApplied = p.getBoolean("spinApplied", spinApplied);
        applicationComplete = p.getBoolean("applicationComplete", applicationComplete);
        submissionTime = p.getLong("submissionTime", submissionTime);
        lastNotifiedTrackerStep = p.getInt("lastNotifiedTrackerStep", lastNotifiedTrackerStep);
    }

    public static void save(Context context) {
        sp(context).edit()
                .putString("loanType", loanType)
                .putString("loanPurpose", loanPurpose)
                .putLong("loanAmount", Double.doubleToLongBits(loanAmount))
                .putInt("tenureMonths", tenureMonths)
                .putLong("interestRate", Double.doubleToLongBits(interestRate))
                .putLong("monthlyEmi", Double.doubleToLongBits(monthlyEmi))
                .putLong("totalPayable", Double.doubleToLongBits(totalPayable))
                .putString("fullName", fullName)
                .putString("dob", dob)
                .putString("gender", gender)
                .putString("mobile", mobile)
                .putString("email", email)
                .putString("address", address)
                .putString("employmentType", employmentType)
                .putLong("monthlyIncome", Double.doubleToLongBits(monthlyIncome))
                .putLong("monthlyExpense", Double.doubleToLongBits(monthlyExpense))
                .putString("selectedBank", selectedBank)
                .putInt("creditScore", creditScore)
                .putLong("spinDiscountPercent", Double.doubleToLongBits(spinDiscountPercent))
                .putBoolean("spinApplied", spinApplied)
                .putBoolean("applicationComplete", applicationComplete)
                .putLong("submissionTime", submissionTime)
                .putInt("lastNotifiedTrackerStep", lastNotifiedTrackerStep)
                .apply();
    }

    public static void clear(Context context) {
        loanType = "Personal Loan";
        loanPurpose = "";
        loanAmount = 100000;
        tenureMonths = 24;
        interestRate = 10.5;
        monthlyEmi = 0;
        totalPayable = 0;
        fullName = "";
        dob = "";
        gender = "Male";
        mobile = "";
        email = "";
        address = "";
        employmentType = "Salaried";
        monthlyIncome = 50000;
        monthlyExpense = 20000;
        selectedBank = "";
        creditScore = 750;
        spinDiscountPercent = 0;
        spinApplied = false;
        applicationComplete = false;
        submissionTime = 0;
        lastNotifiedTrackerStep = 0;
        sp(context).edit().clear().apply();
        AppPrefs.clearLastCompletedStep(context);
    }
}
