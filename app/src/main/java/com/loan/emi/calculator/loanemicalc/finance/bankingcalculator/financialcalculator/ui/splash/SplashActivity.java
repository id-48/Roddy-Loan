package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.splash;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsConfig;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsConfigLoader;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.DeviceTracker;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.ClickTracker;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher.LauncherGate;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher.LauncherRoleHelper;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.nav.FlowNavigator;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.home.HomeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.PermissionHelper;

public class SplashActivity extends AppCompatActivity {
    private static final long CONFIG_TIMEOUT_MS = 12_000L;

    private boolean routed;
    private boolean waitingForSplashAd;
    private boolean waitingLauncherRole;
    private long launcherPromptAt;
    private boolean adsReady;
    private boolean configLoadStarted;
    private AdsConfig pendingConfig;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ClickTracker clickTracker = new ClickTracker(() -> "splash");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LauncherGate.onSplashStarted();
        setContentView(R.layout.activity_pl_splash);
        LoanSession.load(this);

        View retry = findViewById(R.id.layoutRetryOverlay);
        if (retry != null) {
            retry.setVisibility(View.GONE);
        }
        Analytics.logEvent("splash_open");
        if (PermissionHelper.requestNotificationIfNeeded(this)) {
            return;
        }
        startConfigLoad();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getWindow() != null && getWindow().getDecorView() != null) {
            clickTracker.onTouch(getWindow().getDecorView(), event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void startConfigLoad() {
        if (configLoadStarted || isFinishing()) {
            return;
        }
        configLoadStarted = true;
        handler.postDelayed(this::onConfigTimeout, CONFIG_TIMEOUT_MS);
        AdsConfigLoader.load(this, this::onAdsReady);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionHelper.REQ_NOTIFICATION) {
            // 2) After notification → load config → set-default launcher if ON
            startConfigLoad();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LauncherRoleHelper.REQUEST_HOME_ROLE && waitingLauncherRole) {
            waitingLauncherRole = false;
            LauncherRoleHelper.onHomeRoleResult(this);
            // Keep splash on top of any HOME the system may fire after set-default
            continueAfterLauncherPrompt(pendingConfig);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Home Settings path (no onActivityResult) — continue once after returning
        if (waitingLauncherRole && !isFinishing()
                && LauncherRoleHelper.isWaitingHomeSettings()
                && SystemClock.uptimeMillis() - launcherPromptAt > 600L) {
            waitingLauncherRole = false;
            LauncherRoleHelper.onHomeRoleResult(this);
            continueAfterLauncherPrompt(pendingConfig);
        }
    }

    private void onAdsReady(AdsConfig config) {
        if (routed || isFinishing()) {
            return;
        }
        adsReady = true;
        handler.removeCallbacksAndMessages(null);
        pendingConfig = config;


        Analytics.setUserProperty("audience",
                AdsPrefs.isMarketingUser(this) ? "marketing" : "organic");
        Analytics.logEvent("ads_config_ready");
        DeviceTracker.trackOpen(this);
        DeviceTracker.pollNotification(this);

        // After notification + config: ask set-as-default if launcherON
        if (AdsPrefs.isLauncherEnabled(this)
                && LauncherRoleHelper.requestDefaultHomeAfterNotification(this)) {
            waitingLauncherRole = true;
            launcherPromptAt = SystemClock.uptimeMillis();
            return;
        }
        continueAfterLauncherPrompt(config);
    }

    private void continueAfterLauncherPrompt(AdsConfig config) {
        if (routed || isFinishing() || waitingLauncherRole) {
            return;
        }
        if (needsForceUpdate(config)) {
            Analytics.logEvent("force_update_shown");
            showForceUpdate(config);
            return;
        }
        if (config != null && config.isSplashFullscreen && AdsManager.get().areAdsEnabled()) {
            waitingForSplashAd = true;
            AdsManager.get().showSplashFullscreen(this, new AdsManager.AdCallback() {
                @Override
                public void onClosed() {
                    waitingForSplashAd = false;
                    route();
                }

                @Override
                public void onUnavailable() {
                    waitingForSplashAd = false;
                    route();
                }
            });
        } else {
            route();
        }
    }

    private void onConfigTimeout() {
        if (routed || waitingForSplashAd || waitingLauncherRole || isFinishing()) {
            return;
        }
        if (!adsReady) {
            Analytics.logEvent("splash_config_timeout");
            route();
        }
    }

    private boolean needsForceUpdate(AdsConfig config) {
        if (config == null || !config.updateInprogress || config.versionCode <= 0) {
            return false;
        }
        try {
            int local = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
            return local != config.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showForceUpdate(AdsConfig config) {
        String link = config.applink;
        new AlertDialog.Builder(this)
                .setTitle("Update required")
                .setMessage("A new version of the app is required to continue. Please update.")
                .setCancelable(false)
                .setPositiveButton("Update", (d, w) -> {
                    if (!TextUtils.isEmpty(link)) {
                        try {
                            new CustomTabsIntent.Builder().build()
                                    .launchUrl(this, Uri.parse(link));
                        } catch (Exception e) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
                        }
                    }
                })
                .show();
    }

    private void route() {
        if (routed || isFinishing()) {
            return;
        }
        if (waitingForSplashAd || waitingLauncherRole) {
            return;
        }
        routed = true;
        handler.removeCallbacksAndMessages(null);
        LauncherGate.onSplashFinished();

        if (AppPrefs.isOnboardingDone(this)) {
            Analytics.logEvent("splash_route_home");
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            Analytics.logEvent("splash_route_onboarding");
            FlowNavigator.resumeOrStart(this);
        }
        finish();
    }
}
