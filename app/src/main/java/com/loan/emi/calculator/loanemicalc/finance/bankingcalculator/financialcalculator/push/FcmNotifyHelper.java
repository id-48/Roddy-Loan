package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.push;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.notify.AdminNotifyOpenActivity;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public final class FcmNotifyHelper {
    private FcmNotifyHelper() {
    }

    public static void ensureChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) {
            return;
        }
        NotificationChannel ch = new NotificationChannel(
                PushHelper.CHANNEL_ID,
                "App updates",
                NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("Firebase push notifications");
        nm.createNotificationChannel(ch);
    }

    public static void show(Context context, String title, String body, String imageUrl, String link) {
        if (context == null) {
            return;
        }
        ensureChannel(context);
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            return;
        }
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(body)) {
            return;
        }
        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.app_name);
        }
        if (body == null) {
            body = "";
        }

        Intent open = new Intent(context, AdminNotifyOpenActivity.class);
        open.putExtra(AdminNotifyOpenActivity.EXTRA_LINK, link == null ? "" : link);
        open.putExtra(AdminNotifyOpenActivity.EXTRA_IMAGE, imageUrl == null ? "" : imageUrl);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        int req = Math.abs((title + body).hashCode());
        PendingIntent pi = PendingIntent.getActivity(
                context, req, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, PushHelper.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        Bitmap image = loadImage(imageUrl);
        if (image != null) {
            builder.setLargeIcon(image);
            builder.setStyle(new NotificationCompat.BigPictureStyle()
                    .bigPicture(image)
                    .bigLargeIcon((Bitmap) null));
        }

        NotificationManagerCompat.from(context).notify(req, builder.build());
    }

    private static Bitmap loadImage(String imageUrl) {
        if (TextUtils.isEmpty(imageUrl)) {
            return null;
        }
        HttpURLConnection connection = null;
        InputStream input = null;
        try {
            URL url = new URL(imageUrl.trim());
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.setDoInput(true);
            connection.connect();
            input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (Exception ignored) {
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
