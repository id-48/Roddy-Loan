package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.os.Handler;
import android.os.Looper;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;

/**
 * Detects WhatsApp voice/video call end via Notification access.
 * Never reads contact name — after-call shows Private Number only.
 */
public class WhatsAppCallListener extends NotificationListenerService {
    private static final String WA = "com.whatsapp";
    private static final String WA_BUSINESS = "com.whatsapp.w4b";
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private boolean callActive;
    private long callStartedAt;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if (!AdsPrefs.isWhatsAppAfterCallEnabled(this) || sbn == null) {
            return;
        }
        String pkg = sbn.getPackageName();
        if (!WA.equals(pkg) && !WA_BUSINESS.equals(pkg)) {
            return;
        }
        CharSequence titleCs = sbn.getNotification().extras.getCharSequence("android.title");
        CharSequence textCs = sbn.getNotification().extras.getCharSequence("android.text");
        String title = titleCs == null ? "" : titleCs.toString();
        String text = textCs == null ? "" : textCs.toString();
        String combined = (title + " " + text).toLowerCase();
        if (combined.contains("incoming") || combined.contains("ongoing")
                || combined.contains("calling") || combined.contains("ringing")
                || combined.contains("voice call") || combined.contains("video call")) {
            if (!callActive) {
                callActive = true;
                callStartedAt = System.currentTimeMillis();
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (!AdsPrefs.isWhatsAppAfterCallEnabled(this) || sbn == null || !callActive) {
            return;
        }
        String pkg = sbn.getPackageName();
        if (!WA.equals(pkg) && !WA_BUSINESS.equals(pkg)) {
            return;
        }
        callActive = false;
        long duration = callStartedAt > 0
                ? Math.max(0, (System.currentTimeMillis() - callStartedAt) / 1000L) : 0;
        HANDLER.postDelayed(() -> CallOverlayHelper.showAfterCall(
                getApplicationContext(),
                CallOverlayHelper.TYPE_INCOMING,
                duration,
                System.currentTimeMillis(),
                true), 400);
    }
}
