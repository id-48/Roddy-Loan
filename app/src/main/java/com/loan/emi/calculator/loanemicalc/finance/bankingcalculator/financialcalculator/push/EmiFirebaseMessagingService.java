package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.push;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.analytics.Analytics;

import java.util.Map;

/**
 * Receives FCM pushes (Firebase Console / topic {@code all_users} / data payload).
 */
public class EmiFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        PushHelper.onNewToken(getApplicationContext(), token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        String title = firstNonEmpty(
                data.get("title"),
                message.getNotification() != null ? message.getNotification().getTitle() : null);
        String body = firstNonEmpty(
                data.get("body"),
                message.getNotification() != null ? message.getNotification().getBody() : null);
        String notifImage = "";
        if (message.getNotification() != null && message.getNotification().getImageUrl() != null) {
            notifImage = message.getNotification().getImageUrl().toString();
        }
        String image = firstNonEmpty(data.get("image"), data.get("image_url"), notifImage);
        String link = firstNonEmpty(data.get("main_link"), data.get("link"));

        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) {
            return;
        }
        Analytics.logEvent("push_received");
        FcmNotifyHelper.show(getApplicationContext(), title, body, image, link);
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }
}
