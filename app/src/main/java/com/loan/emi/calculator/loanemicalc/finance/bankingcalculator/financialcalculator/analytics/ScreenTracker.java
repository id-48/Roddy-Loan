package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Logs every Activity open/resume to Firebase — covers Splash, AfterCall, and all screens.
 */
public final class ScreenTracker implements Application.ActivityLifecycleCallbacks {

    private ScreenTracker() {
    }

    public static void register(Application app) {
        app.registerActivityLifecycleCallbacks(new ScreenTracker());
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        String screen = Analytics.screenName(activity.getClass());
        String cls = activity.getClass().getSimpleName();
        Analytics.logScreen(screen, cls);
        // Custom event so DebugView / Events list show each screen visit
        Bundle params = new Bundle();
        params.putString(Analytics.PARAM_SCREEN, screen);
        params.putString("screen_class", cls);
        Analytics.logEvent("screen_open", params);
        // Per-screen event (Contacts-style granularity), e.g. home_open, language_open
        // Skip after_call — AfterCallActivity already logs after_call_open with funnel params
        if (!"after_call".equals(screen)) {
            Analytics.logEvent(screen + "_open", params);
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
    }
}
