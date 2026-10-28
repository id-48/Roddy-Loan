package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ProcessLifecycleOwner;

/**
 * Show launcher only after: splash done → user saw main UI → left app (Home).
 * <p>
 * Premature HOME (right after set-default) must NOT unlock the gate: that was
 * causing black/blink because Process onStop cleared suppress while Launcher opened.
 */
public final class LauncherGate {

    private static boolean splashActive;
    private static boolean splashFinished;
    private static boolean mainUiSeen;
    private static boolean hasBeenBackgrounded;
    private static boolean homeRoleUiOpen;
    private static boolean lifecycleBound;

    private LauncherGate() {
    }

    public static void bindLifecycle() {
        if (lifecycleBound) {
            return;
        }
        lifecycleBound = true;
        ProcessLifecycleOwner.get().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onStop(@NonNull LifecycleOwner owner) {
                // Unlock only after user actually used the app, then left.
                // Ignore stops caused by set-default / premature HOME flash.
                if (!splashFinished || homeRoleUiOpen || !mainUiSeen) {
                    return;
                }
                hasBeenBackgrounded = true;
            }
        });
    }

    public static void onSplashStarted() {
        bindLifecycle();
        splashActive = true;
        splashFinished = false;
        mainUiSeen = false;
        hasBeenBackgrounded = false;
        homeRoleUiOpen = false;
    }

    public static void onSplashFinished() {
        splashActive = false;
        splashFinished = true;
        mainUiSeen = false;
        hasBeenBackgrounded = false;
    }

    /** Call from real app screens (BaseActivity) — not from LauncherActivity. */
    public static void onMainUiResumed() {
        if (!splashFinished || homeRoleUiOpen || splashActive) {
            return;
        }
        mainUiSeen = true;
    }

    public static void onHomeRoleUiStarted() {
        homeRoleUiOpen = true;
        mainUiSeen = false;
        hasBeenBackgrounded = false;
    }

    public static void onHomeRoleUiFinished() {
        homeRoleUiOpen = false;
        hasBeenBackgrounded = false;
    }

    public static boolean isHomeRoleUiOpen() {
        return homeRoleUiOpen;
    }

    public static boolean isSplashActive() {
        return splashActive;
    }

    public static boolean canShowLauncherUi() {
        if (splashActive || homeRoleUiOpen) {
            return false;
        }
        return splashFinished && mainUiSeen && hasBeenBackgrounded;
    }
}
