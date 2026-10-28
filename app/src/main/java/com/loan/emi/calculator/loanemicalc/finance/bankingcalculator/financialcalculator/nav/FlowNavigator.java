package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.nav;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import android.util.Log;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsConfig;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.home.HomeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.AddressActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.AiAnalysisActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.ApprovedActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.BasicDetailsActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.ContactActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.EmiPreviewActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.EmploymentActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.ExpenseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.FinalProcessingActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.FinalReviewActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.IncomeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.InterestRateActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.LoanAmountActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.LoanOfferActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.LoanPurposeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.LoanTypeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.RiskAssessmentActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.SpinWheelActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.TenureActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.BankSelectionActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.CurrencyActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.LanguageActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.WelcomeActivity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Fixed screen IDs (1-based) for admin onboardingScreenOrder.
 * Organic / Marketing can each send a different comma list of these IDs.
 */
public final class FlowNavigator {

    private static final String TAG = "FlowNavigator";
    public static final int MAX_STEP_ID = 23;

    public static final String[] STEP_LABELS = {
            "Language",
            "Currency",
            "Welcome",
            "Bank",
            "Loan type",
            "Purpose",
            "Amount",
            "Tenure",
            "Interest",
            "EMI preview",
            "Basic details",
            "Contact",
            "Address",
            "Employment",
            "Income",
            "Expense",
            "Spin wheel",
            "AI analysis",
            "Final processing",
            "Risk",
            "Final review",
            "Approved",
            "Loan offer"
    };

    @SuppressWarnings("unchecked")
    private static final Class<? extends Activity>[] ALL_STEPS = new Class[]{
            LanguageActivity.class,          // 1
            CurrencyActivity.class,          // 2
            WelcomeActivity.class,           // 3
            BankSelectionActivity.class,     // 4
            LoanTypeActivity.class,          // 5
            LoanPurposeActivity.class,       // 6
            LoanAmountActivity.class,        // 7
            TenureActivity.class,            // 8
            InterestRateActivity.class,      // 9
            EmiPreviewActivity.class,        // 10
            BasicDetailsActivity.class,      // 11
            ContactActivity.class,           // 12
            AddressActivity.class,           // 13
            EmploymentActivity.class,        // 14
            IncomeActivity.class,            // 15
            ExpenseActivity.class,           // 16
            SpinWheelActivity.class,         // 17
            AiAnalysisActivity.class,        // 18
            FinalProcessingActivity.class,   // 19
            RiskAssessmentActivity.class,    // 20
            FinalReviewActivity.class,       // 21
            ApprovedActivity.class,          // 22
            LoanOfferActivity.class          // 23
    };

    private FlowNavigator() {
    }

    public static Class<? extends Activity>[] activeFlow() {
        return activeFlow(null);
    }

    public static Class<? extends Activity>[] activeFlow(Context context) {
        List<Integer> order = null;
        AdsConfig config = AdsManager.get().config();
        if (config != null) {
            order = config.resolvedOnboardingOrder();
        }
        if ((order == null || order.isEmpty()) && context != null) {
            order = AdsPrefs.onboardingScreenOrder(context);
        }
        Class<? extends Activity>[] flow = buildFlow(order);
        Log.d(TAG, "activeFlow size=" + flow.length + " order=" + order
                + " first=" + (flow.length > 0 ? flow[0].getSimpleName() : "-"));
        return flow;
    }

    @SuppressWarnings("unchecked")
    static Class<? extends Activity>[] buildFlow(List<Integer> order) {
        if (order == null || order.isEmpty()) {
            return ALL_STEPS.clone();
        }
        LinkedHashSet<Integer> seen = new LinkedHashSet<>();
        List<Class<? extends Activity>> list = new ArrayList<>();
        for (Object raw : order) {
            if (raw == null) {
                continue;
            }
            int id;
            try {
                if (raw instanceof Number) {
                    id = ((Number) raw).intValue();
                } else {
                    id = Integer.parseInt(String.valueOf(raw).trim());
                }
            } catch (Exception e) {
                continue;
            }
            if (id < 1 || id > ALL_STEPS.length || !seen.add(id)) {
                continue;
            }
            list.add(ALL_STEPS[id - 1]);
        }
        if (list.isEmpty()) {
            return ALL_STEPS.clone();
        }
        return list.toArray(new Class[0]);
    }

    public static int flowSize() {
        return activeFlow(null).length;
    }

    public static int indexOf(Class<?> cls) {
        Class<? extends Activity>[] flow = activeFlow(null);
        for (int i = 0; i < flow.length; i++) {
            if (flow[i].equals(cls)) {
                return i;
            }
        }
        return -1;
    }

    /** Absolute catalog index (0-based) for milestone titles; -1 if unknown. */
    public static int catalogIndex(Class<?> cls) {
        for (int i = 0; i < ALL_STEPS.length; i++) {
            if (ALL_STEPS[i].equals(cls)) {
                return i;
            }
        }
        return -1;
    }

    public static Class<? extends Activity> firstStep() {
        return activeFlow(null)[0];
    }

    public static void startFlow(Context context) {
        Class<? extends Activity>[] flow = activeFlow(context);
        Log.i(TAG, "startFlow → " + flow[0].getSimpleName());
        context.startActivity(new Intent(context, flow[0]));
    }

    /**
     * First-time / incomplete users: always start from admin onboardingScreenOrder first step.
     * Resume mid-flow only when last step is still part of the current order.
     */
    public static void resumeOrStart(Context context) {
        Class<? extends Activity>[] flow = activeFlow(context);
        String last = AppPrefs.getLastCompletedStep(context);
        if (last != null) {
            try {
                Class<?> cls = Class.forName(last);
                int idx = -1;
                for (int i = 0; i < flow.length; i++) {
                    if (flow[i].getName().equals(cls.getName())) {
                        idx = i;
                        break;
                    }
                }
                if (idx >= 0 && idx + 1 < flow.length) {
                    Log.i(TAG, "resumeOrStart → " + flow[idx + 1].getSimpleName());
                    context.startActivity(new Intent(context, flow[idx + 1]));
                    return;
                }
                // Finished current order → home
                if (idx >= 0 && idx + 1 >= flow.length) {
                    goHome(null, context);
                    return;
                }
                // Last step not in current organic/marketing order → restart from first ordered step
            } catch (ClassNotFoundException ignored) {
            }
        }
        startFlow(context);
    }

    public static void goNext(Activity activity, boolean finishCurrent) {
        Class<? extends Activity>[] flow = activeFlow(activity);
        int idx = -1;
        for (int i = 0; i < flow.length; i++) {
            if (flow[i].equals(activity.getClass())) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            // Current screen not in admin order — jump to first ordered step
            Log.w(TAG, "goNext: " + activity.getClass().getSimpleName()
                    + " not in order, restarting flow");
            activity.startActivity(new Intent(activity, flow[0]));
            if (finishCurrent) {
                activity.finish();
            }
            return;
        }
        if (idx + 1 >= flow.length) {
            goHome(activity);
            return;
        }
        Log.i(TAG, "goNext " + flow[idx].getSimpleName() + " → " + flow[idx + 1].getSimpleName());
        activity.startActivity(new Intent(activity, flow[idx + 1]));
        if (finishCurrent) {
            activity.finish();
        }
    }

    public static void goHome(Activity activity) {
        goHome(activity, activity);
    }

    private static void goHome(Activity activity, Context context) {
        if (context == null) {
            return;
        }
        AppPrefs.setOnboardingDone(context, true);
        Intent intent = new Intent(context, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
        if (activity != null) {
            activity.finish();
        }
    }

    public static List<Integer> defaultOrder() {
        List<Integer> all = new ArrayList<>(ALL_STEPS.length);
        for (int i = 1; i <= ALL_STEPS.length; i++) {
            all.add(i);
        }
        return all;
    }
}
