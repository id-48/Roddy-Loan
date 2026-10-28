package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.ClickAdWrapper;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.PlayAdSequence;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.ClickTracker;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher.LauncherGate;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.nav.FlowNavigator;

public abstract class BaseActivity extends AppCompatActivity {
    private boolean bypassNextStartAd;
    private final ClickTracker clickTracker = new ClickTracker(this::screenName);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoanSession.load(this);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        fillDefaultButtonLabels();
        getWindow().getDecorView().post(() -> {
            AdsManager.get().bindNatives(this);
            ClickAdWrapper.wrap(this);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mark real app UI so premature HOME after set-default does not unlock launcher
        LauncherGate.onMainUiResumed();
        // Screen open also logged globally via ScreenTracker
        AdsManager.get().maybeShowAppOpenOnResume(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getWindow() != null && getWindow().getDecorView() != null) {
            clickTracker.onTouch(getWindow().getDecorView(), event);
        }
        return super.dispatchTouchEvent(event);
    }

    protected String screenName() {
        return Analytics.screenName(getClass());
    }

    @Override
    protected void onDestroy() {
        AdsManager.get().destroyNatives(this);
        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        if (bypassNextStartAd || intent == null || !AdsManager.get().areClickAdsEnabled()) {
            bypassNextStartAd = false;
            super.startActivity(intent);
            return;
        }
        PlayAdSequence.show(this, () -> {
            bypassNextStartAd = true;
            BaseActivity.super.startActivity(intent);
            bypassNextStartAd = false;
        });
    }

    protected void hideKeyboard() {
        View focus = getCurrentFocus();
        if (focus == null) {
            focus = findViewById(android.R.id.content);
        }
        if (focus != null) {
            focus.clearFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        }
    }

    protected void fillDefaultButtonLabels() {
        setButtonLabel(R.id.btnNext, R.string.btn_next);
        setButtonLabel(R.id.btnContinue, R.string.btn_continue);
        setButtonLabel(R.id.btnCalculate, R.string.btn_calculate);
        setButtonLabel(R.id.btnApply, R.string.btn_continue);
        setButtonLabel(R.id.btnSubmit, R.string.btn_submit);
        setButtonLabel(R.id.btnStart, R.string.btn_get_started);
        setButtonLabel(R.id.btnAccept, R.string.btn_go_home);
        setButtonLabel(R.id.btnSpin, R.string.btn_spin_now);
        setButtonLabel(R.id.btnRetryNow, R.string.btn_retry_now);
        setButtonLabel(R.id.btnApplyNow, R.string.btn_check_eligibility);
        setButtonLabel(R.id.btnGetInsurance, R.string.btn_get_insurance);
    }

    private void setButtonLabel(int id, int stringRes) {
        View view = findViewById(id);
        if (view == null) {
            return;
        }
        CharSequence label = getText(stringRes);
        if (view instanceof MaterialButton) {
            ((MaterialButton) view).setText(label);
        } else if (view instanceof Button) {
            ((Button) view).setText(label);
        } else if (view instanceof TextView) {
            ((TextView) view).setText(label);
        }
    }

    protected void setupToolbar(String title, boolean showBack) {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        TextView titleView = findViewById(R.id.toolbarTitle);
        if (titleView != null) {
            titleView.setText(title);
        }
        if (toolbar != null) {
            if (showBack) {
                toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
            } else {
                toolbar.setNavigationIcon(null);
            }
        }
        ImageView settings = findViewById(R.id.ivSettings);
        if (settings != null) {
            settings.setVisibility(View.GONE);
        }
    }

    protected void setupProgressHeader() {
        setupProgressHeader(null);
    }

    protected void setupProgressHeader(@Nullable String milestone) {
        TextView stepText = findViewById(R.id.tvStepCount);
        TextView milestoneView = findViewById(R.id.tvMilestone);
        LinearProgressIndicator progress = findViewById(R.id.linearProgress);
        int index = FlowNavigator.indexOf(getClass());
        int total = FlowNavigator.flowSize();
        if (stepText != null && index >= 0) {
            stepText.setText(getString(R.string.step_progress, index + 1, total));
        }
        if (milestoneView != null) {
            if (!TextUtils.isEmpty(milestone)) {
                milestoneView.setText(milestone);
            } else if (index >= 0) {
                milestoneView.setText(defaultMilestone(index));
            }
        }
        if (progress != null && index >= 0 && total > 0) {
            progress.setMax(100);
            progress.setProgress((int) ((index + 1f) * 100f / total));
        }
    }

    private String defaultMilestone(int index) {
        // Prefer catalog label for current screen (stable across reordered flows)
        int catalog = FlowNavigator.catalogIndex(getClass());
        String[] labels = getResources().getStringArray(R.array.onboarding_step_labels);
        if (catalog >= 0 && catalog < labels.length) {
            return labels[catalog];
        }
        if (catalog >= 0 && catalog < FlowNavigator.STEP_LABELS.length) {
            return FlowNavigator.STEP_LABELS[catalog];
        }
        return getString(R.string.continue_application);
    }

    protected void setText(int id, CharSequence text) {
        TextView tv = findViewById(id);
        if (tv != null && text != null) {
            tv.setText(text);
        }
    }

    protected void showAndEnable(int id) {
        View view = findViewById(id);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.setEnabled(true);
        }
    }

    protected void goNext(boolean finishCurrent) {
        AppPrefs.setLastCompletedStep(this, getClass().getName());
        LoanSession.save(this);
        Analytics.logEvent("step_next");
        PlayAdSequence.show(this, () -> {
            bypassNextStartAd = true;
            FlowNavigator.goNext(this, finishCurrent);
            bypassNextStartAd = false;
        });
    }

    protected void markStepAndFinishToHome() {
        AppPrefs.setLastCompletedStep(this, getClass().getName());
        LoanSession.save(this);
        PlayAdSequence.show(this, () -> {
            bypassNextStartAd = true;
            FlowNavigator.goHome(this);
            bypassNextStartAd = false;
        });
    }

    /** Run action after fullscreen ad sequence (interstitial / reward waterfall). */
    protected void runAfterAd(Runnable action) {
        PlayAdSequence.show(this, action);
    }
}
