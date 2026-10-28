package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.home.HomeActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.notify.AdminNotifyOpenActivity;

/**
 * Shows local notification from admin panel payload — once per notification id.
 */
public final class AdminNotifyHelper {
    private static final String TAG = "AdminNotify";
    private static final String CHANNEL = "admin_push";
    private static final String KEY_LAST_ID = "last_admin_notify_id";
    private static final int NOTIFY_ID = 7101;

    private AdminNotifyHelper() {
    }

    public static void showFromJson(Context context, String rawJson) {
        if (context == null || TextUtils.isEmpty(rawJson)) {
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(rawJson).getAsJsonObject();
            if (!root.has("notification") || root.get("notification").isJsonNull()
                    || !root.get("notification").isJsonObject()) {
                return;
            }
            JsonObject n = root.getAsJsonObject("notification");
            if (n == null || !n.has("title") || n.get("title").isJsonNull()) {
                return;
            }
            String title = n.get("title").getAsString();
            if (TextUtils.isEmpty(title)) {
                return;
            }
            String body = textOrEmpty(n, "body");
            String link = textOrEmpty(n, "main_link");
            String image = textOrEmpty(n, "image_url");
            String id = notifyKey(n, title);

            SharedPreferences prefs = AdsPrefs.prefs(context);
            String lastShown = prefs.getString(KEY_LAST_ID, "");
            if (!TextUtils.isEmpty(id) && id.equals(lastShown)) {
                Log.d(TAG, "skip already shown id=" + id);
                return;
            }
            // Persist before posting so a rapid re-poll cannot show again
            prefs.edit().putString(KEY_LAST_ID, id).commit();

            ensureChannel(context);
            Intent open = new Intent(context, AdminNotifyOpenActivity.class);
            open.putExtra(AdminNotifyOpenActivity.EXTRA_LINK, link);
            open.putExtra(AdminNotifyOpenActivity.EXTRA_IMAGE, image);
            open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pi = PendingIntent.getActivity(
                    context, NOTIFY_ID, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .setOnlyAlertOnce(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.notify(NOTIFY_ID, builder.build());
            }
        } catch (Exception e) {
            Log.w(TAG, "show failed: " + e.getMessage());
        }
    }

    private static String notifyKey(JsonObject n, String title) {
        String id = textOrEmpty(n, "id");
        if (!TextUtils.isEmpty(id)) {
            return id;
        }
        String created = textOrEmpty(n, "created_at");
        return title + "|" + created;
    }

    private static String textOrEmpty(JsonObject n, String key) {
        if (n == null || !n.has(key) || n.get(key).isJsonNull()) {
            return "";
        }
        JsonElement el = n.get(key);
        try {
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return String.valueOf(el.getAsLong());
            }
            return el.getAsString().trim();
        } catch (Exception e) {
            return String.valueOf(el).trim();
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
                CHANNEL, "Admin messages", NotificationManager.IMPORTANCE_HIGH);
        nm.createNotificationChannel(ch);
    }

    public static void openFallbackHome(Context context) {
        Intent home = new Intent(context, HomeActivity.class);
        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(home);
    }
}
