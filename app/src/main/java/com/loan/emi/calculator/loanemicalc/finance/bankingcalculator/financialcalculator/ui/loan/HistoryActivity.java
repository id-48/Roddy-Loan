package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanTrackerNotify;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanTrackerProgress;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide.InsuranceActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide.SelectionActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class HistoryActivity extends BaseActivity {
    private boolean summaryNativeRequested;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_history);
        LoanSession.load(this);
        if (LoanSession.submissionTime <= 0L && LoanSession.applicationComplete) {
            LoanSession.submissionTime = System.currentTimeMillis();
            LoanSession.save(this);
        }

        ViewGroup lazyNative = findViewById(R.id.put_big_nativead2_container);
        if (lazyNative != null) {
            lazyNative.setTag(R.id.tag_native_lazy, Boolean.TRUE);
        }

        TextView title = findViewById(R.id.tvToolbarTitle);
        if (title != null) title.setText("Loan Status");
        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setOnClickListener(v -> finish());
        }

        bindTrackerSteps();

        fillRow(R.id.rowAmount, "Loan Amount", MoneyFormat.format(this, LoanSession.loanAmount));
        fillRow(R.id.rowRate, "Interest Rate",
                String.format("%.1f%% p.a.", LoanSession.interestRate > 0 ? LoanSession.interestRate : 10.5));
        fillRow(R.id.rowTenure, "Tenure", LoanSession.tenureMonths + " months");
        fillRow(R.id.rowEmi, "Monthly EMI", MoneyFormat.format(this,
                LoanSession.monthlyEmi > 0 ? LoanSession.monthlyEmi
                        : LoanSession.loanAmount / Math.max(1, LoanSession.tenureMonths)));

        String name = LoanSession.fullName == null || LoanSession.fullName.isEmpty() ? "—" : LoanSession.fullName;
        String mobile = LoanSession.mobile == null || LoanSession.mobile.isEmpty() ? "—" : "+91 " + LoanSession.mobile;
        String email = LoanSession.email == null || LoanSession.email.isEmpty() ? "—" : LoanSession.email;
        String emp = LoanSession.employmentType == null || LoanSession.employmentType.isEmpty()
                ? "—" : LoanSession.employmentType;
        fillRow(R.id.rowName, "Full Name", name);
        fillRow(R.id.rowMobile, "Mobile", mobile);
        fillRow(R.id.rowEmail, "Email", email);
        fillRow(R.id.rowEmployment, "Employment", emp);

        View explore = findViewById(R.id.btnExploreCalculators);
        if (explore != null) {
            explore.setOnClickListener(v -> {
                Intent i = new Intent(this, SelectionActivity.class);
                i.putExtra("type", "loans");
                startActivity(i);
            });
        }
        View insurance = findViewById(R.id.btnCheckInsurance);
        if (insurance != null) {
            insurance.setOnClickListener(v ->
                    startActivity(new Intent(this, InsuranceActivity.class)));
        }
        View close = findViewById(R.id.btnClose);
        if (close != null) close.setOnClickListener(v -> finish());
        View refresh = findViewById(R.id.btnRefresh);
        if (refresh != null) {
            refresh.setOnClickListener(v -> {
                LoanSession.load(this);
                recreate();
            });
        }

        setupLazySummaryNative();
        LoanTrackerNotify.checkAndNotify(this);
    }

    private void bindTrackerSteps() {
        LoanTrackerProgress progress = LoanTrackerProgress.fromSubmission(LoanSession.submissionTime);
        set(R.id.tvProgressTitle, "Application Progress");
        set(R.id.tvStep1Title, "Application Received");
        set(R.id.tvStep2Title, "Document Verification");
        set(R.id.tvStep3Title, "Underwriting");
        set(R.id.tvStep4Title, progress.rejected ? "Loan Rejected" : "Final Decision");

        for (int i = 1; i <= 4; i++) {
            set(getId("tvStep" + i + "Date"), progress.stepStatus(i));
            ImageView iv = findViewById(getId("ivStep" + i));
            if (iv != null) {
                boolean done = progress.isStepComplete(i) || (progress.rejected && i == 4);
                if (progress.rejected && i == 4) {
                    iv.setImageResource(R.drawable.ic_radio_button_unchecked);
                    iv.setColorFilter(ContextCompat.getColor(this, R.color.colorError));
                } else if (done || (i == progress.step && progress.step == 3)) {
                    iv.setImageResource(R.drawable.checked);
                    iv.clearColorFilter();
                } else if (i == progress.step) {
                    iv.setImageResource(R.drawable.ic_radio_button_unchecked);
                    iv.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary));
                } else {
                    iv.setImageResource(R.drawable.ic_radio_button_unchecked);
                    iv.clearColorFilter();
                }
            }
            TextView date = findViewById(getId("tvStep" + i + "Date"));
            if (date != null) {
                if (progress.rejected && i == 4) {
                    date.setTextColor(ContextCompat.getColor(this, R.color.colorError));
                } else if (progress.isStepComplete(i) || (i == progress.step && progress.step == 3)) {
                    date.setTextColor(ContextCompat.getColor(this, R.color.colorSuccess));
                } else if (i == progress.step) {
                    date.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
                } else {
                    date.setTextColor(ContextCompat.getColor(this, R.color.colorSubText));
                }
            }
        }

        View step4Row = findViewById(R.id.ivStep4);
        if (step4Row != null && step4Row.getParent() instanceof View) {
            View parent = (View) step4Row.getParent();
            parent.setAlpha(progress.step >= 4 || progress.rejected ? 1f : 0.5f);
        }

        for (int i = 1; i <= 4; i++) {
            final int step = i;
            View btn = findViewById(getId("btnTrackStep" + i));
            if (btn != null) {
                boolean unlocked = i <= progress.step || progress.rejected;
                btn.setVisibility(unlocked ? View.VISIBLE : View.GONE);
                btn.setOnClickListener(v -> {
                    Intent intent = new Intent(this, TrackingDetailActivity.class);
                    intent.putExtra("step_index", step);
                    intent.putExtra("rejected", progress.rejected && step == 4);
                    startActivity(intent);
                });
            }
        }
    }

    private int getId(String name) {
        return getResources().getIdentifier(name, "id", getPackageName());
    }

    private void setupLazySummaryNative() {
        ViewGroup container = findViewById(R.id.put_big_nativead2_container);
        NestedScrollView scroll = findScrollHost();
        if (container == null || scroll == null) {
            return;
        }
        ViewTreeObserver.OnScrollChangedListener listener = () -> maybeRequestSummaryNative(scroll, container);
        scroll.getViewTreeObserver().addOnScrollChangedListener(listener);
        scroll.post(() -> maybeRequestSummaryNative(scroll, container));
    }

    private NestedScrollView findScrollHost() {
        View root = findViewById(android.R.id.content);
        if (root instanceof ViewGroup) {
            return findNestedScroll((ViewGroup) root);
        }
        return null;
    }

    private NestedScrollView findNestedScroll(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof NestedScrollView) {
                return (NestedScrollView) child;
            }
            if (child instanceof ViewGroup) {
                NestedScrollView found = findNestedScroll((ViewGroup) child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private void maybeRequestSummaryNative(NestedScrollView scroll, ViewGroup container) {
        if (summaryNativeRequested || container == null || scroll == null) {
            return;
        }
        if (!container.getGlobalVisibleRect(new android.graphics.Rect())) {
            return;
        }
        int[] loc = new int[2];
        container.getLocationOnScreen(loc);
        int[] scrollLoc = new int[2];
        scroll.getLocationOnScreen(scrollLoc);
        int viewTop = loc[1] - scrollLoc[1];
        if (viewTop < scroll.getHeight()) {
            summaryNativeRequested = true;
            container.setTag(R.id.tag_native_lazy, null);
            AdsManager.get().loadNativeInto(this, container);
        }
    }

    private void fillRow(int includeId, String label, String value) {
        View row = findViewById(includeId);
        if (row == null) return;
        TextView tvLabel = row.findViewById(R.id.tvLabel);
        TextView tvValue = row.findViewById(R.id.tvValue);
        if (tvLabel != null) tvLabel.setText(label);
        if (tvValue != null) tvValue.setText(value);
    }

    private void set(int id, String t) {
        TextView tv = findViewById(id);
        if (tv != null) tv.setText(t);
    }
}
