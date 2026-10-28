package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.ClickTracker;

import java.util.Date;

/**
 * After-call UI. Never shows or uses call number/name — always "Private Number".
 */
public class AfterCallActivity extends AppCompatActivity {
    public static final String EXTRA_CALL_TYPE = "extra_call_type";
    public static final String EXTRA_DURATION = "extra_duration";
    public static final String EXTRA_CALL_TIME = "extra_call_time";
    public static final String EXTRA_FROM_WHATSAPP = "extra_from_whatsapp";

    /** Kept for reminder receiver extras compatibility (always empty). */
    public static final String EXTRA_PHONE = "extra_phone";

    private static final String NOTES_PREFS = "after_call_notes";
    private static final String NOTES_KEY_PRIVATE = "private_number";

    private boolean fromWhatsApp;
    private final ClickTracker clickTracker = new ClickTracker(
            () -> fromWhatsApp ? "wp_call_end_screen" : "after_call");

    public static Intent createIntent(Context context, int callType,
                                      long durationSeconds, long callTimeMs, boolean fromWhatsApp) {
        Intent intent = new Intent(context, AfterCallActivity.class);
        intent.putExtra(EXTRA_CALL_TYPE, callType);
        intent.putExtra(EXTRA_DURATION, durationSeconds);
        intent.putExtra(EXTRA_CALL_TIME, callTimeMs);
        intent.putExtra(EXTRA_FROM_WHATSAPP, fromWhatsApp);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        fromWhatsApp = intent.getBooleanExtra(EXTRA_FROM_WHATSAPP, false);
        boolean allowed = fromWhatsApp
                ? AdsPrefs.isWhatsAppAfterCallEnabled(this)
                : AdsPrefs.isAfterCallEnabled(this);
        if (!allowed) {
            finish();
            return;
        }
        CallOverlayHelper.configureLockScreen(this);
        AdsManager.get().suppressResumeAppOpenBriefly();
        setContentView(R.layout.activity_after_call);
        trackOpen(intent);

        TextView title = findViewById(R.id.txtTitle);
        if (title != null) {
            title.setText(fromWhatsApp
                    ? "WhatsApp Call Ended"
                    : getString(R.string.after_call_title));
        }
        TextView nameView = findViewById(R.id.txtCallerName);
        if (nameView != null) {
            nameView.setText(R.string.after_call_private_number);
        }
        TextView meta = findViewById(R.id.txtCallMeta);
        if (meta != null) {
            long duration = intent.getLongExtra(EXTRA_DURATION, 0);
            long callTime = intent.getLongExtra(EXTRA_CALL_TIME, System.currentTimeMillis());
            meta.setText(formatMeta(
                    intent.getIntExtra(EXTRA_CALL_TYPE, CallOverlayHelper.TYPE_OUTGOING),
                    duration,
                    callTime));
        }

        EditText note = findViewById(R.id.inputNote);
        SharedPreferences notes = getSharedPreferences(NOTES_PREFS, MODE_PRIVATE);
        if (note != null) {
            note.setText(notes.getString(NOTES_KEY_PRIVATE, ""));
        }

        nameClick(R.id.btnClose, "close_button");
        nameClick(R.id.btnSaveNote, "save_note_button");
        nameClick(R.id.actionCallBack, "call_back_button");
        nameClick(R.id.actionWhatsApp, "whatsapp_button");
        nameClick(R.id.actionMessage, "message_button");
        nameClick(R.id.actionReminder, "reminder_button");

        View close = findViewById(R.id.btnClose);
        if (close != null) {
            close.setOnClickListener(v -> finish());
        }
        View save = findViewById(R.id.btnSaveNote);
        if (save != null) {
            save.setOnClickListener(v -> {
                if (note != null) {
                    notes.edit().putString(NOTES_KEY_PRIVATE, note.getText().toString()).apply();
                    Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                }
            });
        }
        bindAction(R.id.actionCallBack, () ->
                startActivity(new Intent(Intent.ACTION_DIAL)));
        bindAction(R.id.actionWhatsApp, () ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/"))));
        bindAction(R.id.actionMessage, () ->
                startActivity(new Intent(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_APP_MESSAGING)));
        bindAction(R.id.actionReminder, this::scheduleReminder);
        bindNativeAd();
    }

    private void nameClick(int id, String analyticsName) {
        View v = findViewById(id);
        if (v != null) {
            Analytics.setClickName(v, analyticsName);
        }
    }

    private void bindNativeAd() {
        ViewGroup ad = findViewById(R.id.adContainer);
        if (ad == null) {
            return;
        }
        ad.setVisibility(View.VISIBLE);
        ad.post(() -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            AdsManager.get().ensureReady(this, () -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                AdsManager.get().loadNativeInto(AfterCallActivity.this, ad);
                ad.postDelayed(() -> {
                    if (isFinishing() || isDestroyed()) {
                        return;
                    }
                    if (ad.getChildCount() == 0) {
                        AdsManager.get().loadNativeInto(AfterCallActivity.this, ad);
                    }
                }, 1200L);
            });
        });
    }

    @Override
    protected void onDestroy() {
        AdsManager.get().destroyNatives(this);
        CallOverlayHelper.releaseOverlayResources();
        super.onDestroy();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getWindow() != null && getWindow().getDecorView() != null) {
            clickTracker.onTouch(getWindow().getDecorView(), event);
        }
        return super.dispatchTouchEvent(event);
    }

    private void trackOpen(Intent intent) {
        String screen = fromWhatsApp ? "wp_call_end_screen" : "after_call";
        Analytics.setCurrentScreen(screen);
        SharedPreferences prefs = getSharedPreferences(NOTES_PREFS, MODE_PRIVATE);
        long total = prefs.getLong("open_count", 0) + 1;
        prefs.edit().putLong("open_count", total).apply();
        Bundle params = new Bundle();
        params.putString(Analytics.PARAM_SCREEN, screen);
        params.putLong("open_count", total);
        params.putString("call_type", callTypeName(
                intent.getIntExtra(EXTRA_CALL_TYPE, CallOverlayHelper.TYPE_OUTGOING)));
        params.putLong("call_duration_sec", intent.getLongExtra(EXTRA_DURATION, 0));
        params.putLong("has_number", 0);
        Analytics.logEvent("after_call_open", params);
        if (fromWhatsApp) {
            Analytics.logEvent("wp_call_end_screen", params);
        }
        Analytics.setUserProperty("after_call_opens", String.valueOf(total));
    }

    private static String callTypeName(int type) {
        if (type == CallOverlayHelper.TYPE_INCOMING) {
            return "incoming";
        }
        if (type == CallOverlayHelper.TYPE_MISSED) {
            return "missed";
        }
        return "outgoing";
    }

    private void bindAction(int id, Runnable action) {
        View view = findViewById(id);
        if (view != null) {
            view.setOnClickListener(v -> {
                try {
                    action.run();
                } catch (Exception e) {
                    Toast.makeText(this, "Unable to open", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private String formatMeta(int type, long durationSec, long callTimeMs) {
        String kind = type == CallOverlayHelper.TYPE_INCOMING ? "Incoming Call"
                : type == CallOverlayHelper.TYPE_MISSED ? "Missed Call" : "Outgoing Call";
        String dur = durationSec + "s";
        if (durationSec >= 60) {
            dur = (durationSec / 60) + "m " + (durationSec % 60) + "s";
        }
        long when = callTimeMs > 0 ? callTimeMs : System.currentTimeMillis();
        String time = android.text.format.DateFormat.format("h:mm a", new Date(when)).toString();
        return dur + " · " + time + " · " + kind;
    }

    private void scheduleReminder() {
        Intent intent = new Intent(this, AfterCallReminderReceiver.class);
        intent.putExtra(EXTRA_PHONE, "");
        PendingIntent pi = PendingIntent.getBroadcast(this, NOTES_KEY_PRIVATE.hashCode(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null) {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 60 * 1000L, pi);
            Analytics.logEvent("after_call_reminder_time_set");
            Toast.makeText(this, "Reminder in 1 hour", Toast.LENGTH_SHORT).show();
        }
    }
}
