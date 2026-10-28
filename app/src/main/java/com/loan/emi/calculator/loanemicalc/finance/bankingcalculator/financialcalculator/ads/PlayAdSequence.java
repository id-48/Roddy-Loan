package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Activity;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;

/**
 * Click → fullscreen ads using admin {@code click_interval} + {@code fullscreen_priority}
 * (organic / marketing cohort).
 * <p>
 * Priority: 1=interstitial, 2=reward, 3=app_open, 4=custom.
 * Example {@code 1,3,4}: 1st ad show interstitial, 2nd app open, 3rd custom, then rotate.
 */
public final class PlayAdSequence {
    private static final String TAG = "YLoanads";

    private static int tapCount;
    private static int launcherTapCount;
    private static int adShowIndex;
    private static long lastFinishedAt;
    private static long lastCallAt;
    private static boolean showing;

    private PlayAdSequence() {
    }

    public static void show(Activity activity, Runnable onContinue) {
        show(activity, onContinue, onContinue);
    }

    /** Launcher app-icon clicks use admin {@code launcher_interval}. */
    public static void showLauncher(Activity activity, Runnable onContinue) {
        if (activity == null) {
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        AdsConfig config = AdsManager.get().config();
        if (config == null || !config.areAdsEnabled()) {
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        long now = SystemClock.uptimeMillis();
        if (showing || now - lastFinishedAt < 900L || now - lastCallAt < 900L) {
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        lastCallAt = now;
        launcherTapCount++;
        int interval = config.resolvedLauncherInterval();
        if (interval <= 0 || launcherTapCount % interval != 0) {
            Log.d(TAG, "launcher click skip tap=" + launcherTapCount + " interval=" + interval);
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        showing = true;
        List<Integer> order = config.resolvedFullscreenPriority();
        if (order == null || order.isEmpty()) {
            done(onContinue);
            return;
        }
        int start = Math.floorMod(adShowIndex, order.size());
        adShowIndex++;
        Log.i(TAG, "launcher fullscreen tap=" + launcherTapCount + " interval=" + interval);
        playFormatChain(activity, order, start, 0, onContinue, onContinue);
    }

    public static void show(Activity activity, Runnable onContinue, Runnable onCancel) {
        if (activity == null) {
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        AdsConfig config = AdsManager.get().config();
        if (config == null || !config.areClickAdsEnabled()) {
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        // Debounce double-fire (ClickAdWrapper + goNext) — count once per user tap
        long now = SystemClock.uptimeMillis();
        if (showing || now - lastFinishedAt < 900L || now - lastCallAt < 900L) {
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }
        lastCallAt = now;

        tapCount++;
        int interval = config.resolvedClickInterval();
        if (interval <= 0 || tapCount % interval != 0) {
            Log.d(TAG, "click skip tap=" + tapCount + " interval=" + interval);
            if (onContinue != null) {
                onContinue.run();
            }
            return;
        }

        showing = true;
        List<Integer> order = config.resolvedFullscreenPriority();
        int start = Math.floorMod(adShowIndex, order.size());
        adShowIndex++;
        Log.i(TAG, "click fullscreen tap=" + tapCount + " interval=" + interval
                + " slotIndex=" + start + " order=" + order);
        playFormatChain(activity, order, start, 0, onContinue, onCancel);
    }

    /** Try formats from order starting at startIndex; wrap once through the list. */
    private static void playFormatChain(Activity activity, List<Integer> order, int startIndex,
                                        int tried, Runnable onContinue, Runnable onCancel) {
        if (tried >= order.size()) {
            done(onContinue);
            return;
        }
        int slot = order.get((startIndex + tried) % order.size());
        AdsManager.AdCallback next = new AdsManager.AdCallback() {
            @Override
            public void onClosed() {
                done(onContinue);
            }

            @Override
            public void onUnavailable() {
                playFormatChain(activity, order, startIndex, tried + 1, onContinue, onCancel);
            }
        };
        if (slot == 1) {
            AdsManager.get().showInterstitial(activity, next);
            return;
        }
        if (slot == 2) {
            showRewarded(activity, onContinue, () ->
                    playFormatChain(activity, order, startIndex, tried + 1, onContinue, onCancel));
            return;
        }
        if (slot == 3) {
            AdsManager.get().showAppOpen(activity, next);
            return;
        }
        if (slot == 4) {
            AdsManager.get().showCustomFullscreen(activity, next);
            return;
        }
        playFormatChain(activity, order, startIndex, tried + 1, onContinue, onCancel);
    }

    private static void showRewarded(Activity activity, Runnable onContinue, Runnable onUnavailable) {
        AdsManager.get().showRewarded(activity, new AdsManager.RewardCallback() {
            @Override
            public void onRewarded() {
                // Wait for close — do not redirect while reward ad is still open
            }

            @Override
            public void onClosed() {
                done(onContinue);
            }

            @Override
            public void onUnavailable() {
                if (onUnavailable != null) {
                    onUnavailable.run();
                } else {
                    done(onContinue);
                }
            }
        });
    }

    private static void done(Runnable onContinue) {
        showing = false;
        lastFinishedAt = SystemClock.uptimeMillis();
        if (onContinue != null) {
            onContinue.run();
        }
    }
}
