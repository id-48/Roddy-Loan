package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.home;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call.CallStateMonitor;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanTrackerNotify;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanTrackerProgress;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.banking.BankingCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.financial.FinancialCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.gst.GstCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.loan.LoanCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.mutual.MutualFundCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.other.OtherCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.calculators.stock.StockCategoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide.InsuranceActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide.SelectionActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.HistoryActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding.WelcomeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.settings.SettingActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class HomeActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_home);
        CallStateMonitor.ensureStarted(this);
        setupToolbar(getString(R.string.home_toolbar_title), false);
        ImageView settings = findViewById(R.id.ivSettings);
        if (settings != null) {
            settings.setVisibility(View.VISIBLE);
            settings.setOnClickListener(v -> startActivity(new Intent(this, SettingActivity.class)));
        }
        refreshLoanCards();
        findViewById(R.id.btnApplyNow).setOnClickListener(v -> {
            if (LoanSession.applicationComplete) {
                LoanSession.clear(this);
            }
            startActivity(new Intent(this, WelcomeActivity.class));
        });
        findViewById(R.id.cardLoanStatus).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));
        open(R.id.cardAllLoans, SelectionActivity.class, "loans");
        open(R.id.cardEpfServices, SelectionActivity.class, "epf");
        open(R.id.cardLoanGuide, SelectionActivity.class, "questions");
        findViewById(R.id.cardInsurance).setOnClickListener(v ->
                startActivity(new Intent(this, InsuranceActivity.class)));
        click(R.id.cardBankingCat, BankingCategoryActivity.class);
        click(R.id.cardLoanCat, LoanCategoryActivity.class);
        click(R.id.cardFinancialCat, FinancialCategoryActivity.class);
        click(R.id.cardMutualCat, MutualFundCategoryActivity.class);
        click(R.id.cardStockCat, StockCategoryActivity.class);
        click(R.id.cardGstCat, GstCategoryActivity.class);
        click(R.id.cardOtherCat, OtherCategoryActivity.class);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoanSession.load(this);
        refreshLoanCards();
        LoanTrackerNotify.checkAndNotify(this);
        CallStateMonitor.ensureStarted(this);
    }

    private boolean shouldShowLoanTracker() {
        return LoanSession.applicationComplete
                && AdsPrefs.isMarketingUser(this)
                && AdsPrefs.isLoanTrackerEnabled(this);
    }

    private void refreshLoanCards() {
        View offer = findViewById(R.id.cardLoanOffer);
        View status = findViewById(R.id.cardLoanStatus);
        boolean showTracker = shouldShowLoanTracker();
        if (showTracker) {
            if (LoanSession.submissionTime <= 0L) {
                LoanSession.submissionTime = System.currentTimeMillis();
                LoanSession.save(this);
            }
            if (offer != null) offer.setVisibility(View.GONE);
            if (status != null) status.setVisibility(View.VISIBLE);

            LoanTrackerProgress progress = LoanTrackerProgress.fromSubmission(LoanSession.submissionTime);
            TextView amount = findViewById(R.id.tvStatusAmount);
            if (amount != null) amount.setText(MoneyFormat.format(this, LoanSession.loanAmount));
            TextView title = findViewById(R.id.tvStatusTitle);
            if (title != null) title.setText(R.string.loan_tracker_title);
            TextView subtitle = findViewById(R.id.tvStatusSubtitle);
            if (subtitle != null) subtitle.setText(progress.homeSubtitle);
            TextView badge = findViewById(R.id.tvStatusBadge);
            if (badge != null) {
                badge.setText(progress.badge);
                int color = ContextCompat.getColor(this,
                        progress.rejected ? R.color.colorError : R.color.colorPrimary);
                badge.setBackgroundTintList(ColorStateList.valueOf(color));
            }
            LinearProgressIndicator pb = findViewById(R.id.pbStatus);
            if (pb != null) pb.setProgress(progress.progress);
        } else {
            if (offer != null) {
                offer.setVisibility(LoanSession.applicationComplete ? View.GONE : View.VISIBLE);
            }
            if (status != null) status.setVisibility(View.GONE);
        }
    }

    private void click(int id, Class<?> cls) {
        View v = findViewById(id);
        if (v != null) {
            v.setClickable(true);
            v.setFocusable(true);
            v.setOnClickListener(x -> startActivity(new Intent(this, cls)));
        }
    }

    private void open(int id, Class<?> cls, String type) {
        View v = findViewById(id);
        if (v != null) {
            v.setClickable(true);
            v.setFocusable(true);
            v.setOnClickListener(x -> {
                Intent i = new Intent(this, cls);
                i.putExtra("type", type);
                startActivity(i);
            });
        }
    }
}
