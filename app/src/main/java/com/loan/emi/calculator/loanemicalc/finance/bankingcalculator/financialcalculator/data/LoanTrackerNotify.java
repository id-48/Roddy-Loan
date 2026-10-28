package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan.HistoryActivity;

/**
 * Local notification when loan tracker advances to the next day-based step.
 * Marketing + loanTrackerON only; organic never notified.
 */
public final class LoanTrackerNotify {
    private static final String CHANNEL = "loan_tracker";
    private static final int NOTIFY_BASE = 7200;

    private LoanTrackerNotify() {
    }

    public static void checkAndNotify(Context context) {
        if (context == null) {
            return;
        }
        Context app = context.getApplicationContext();
        LoanSession.load(app);
        if (!LoanSession.applicationComplete || LoanSession.submissionTime <= 0L) {
            return;
        }
        if (!AdsPrefs.isMarketingUser(app) || !AdsPrefs.isLoanTrackerEnabled(app)) {
            return;
        }

        LoanTrackerProgress progress = LoanTrackerProgress.fromSubmission(LoanSession.submissionTime);
        int last = LoanSession.lastNotifiedTrackerStep;
        if (progress.step <= last) {
            return;
        }

        LoanSession.lastNotifiedTrackerStep = progress.step;
        LoanSession.save(app);

        ensureChannel(app);
        Intent open = new Intent(app, HistoryActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(
                app, NOTIFY_BASE + progress.step, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(app, CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(progress.notifyTitle)
                .setContentText(progress.notifyBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(progress.notifyBody))
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIFY_BASE + progress.step, builder.build());
        }
    }

    private static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        NotificationChannel ch = new NotificationChannel(
                CHANNEL, "Loan Tracker", NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(ch);
    }
}
