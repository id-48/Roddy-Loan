package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.appevents.AppEventsLogger;

/**
 * Initializes Facebook App Events + Audience Network from cohort ads config
 * (organic / marketing fb_app_id + fb_client_token).
 */
public final class FacebookSdkHelper {
    private static final String TAG = "FacebookSdkHelper";
    private static boolean sdkInitialized;
    private static boolean audienceInitStarted;

    private FacebookSdkHelper() {
    }

    public static void applyFromConfig(Context context, AdsConfig config) {
        if (context == null || config == null) {
            return;
        }
        String appId = config.fbAppId == null ? "" : config.fbAppId.trim();
        String token = config.fbClientToken == null ? "" : config.fbClientToken.trim();
        if (TextUtils.isEmpty(appId)) {
            Log.d(TAG, "skip_empty_fb_app_id");
            return;
        }
        FacebookPrefs.save(context, appId, token);
        initialize(context);
    }

    public static void initialize(Context context) {
        if (context == null) {
            return;
        }
        Context appContext = context.getApplicationContext();
        String appId = FacebookPrefs.getAppId(appContext);
        String clientToken = FacebookPrefs.getClientToken(appContext);
        if (TextUtils.isEmpty(appId)) {
            Log.d(TAG, "facebook_sdk_skip_empty_app_id");
            return;
        }
        if (sdkInitialized) {
            initAudienceNetwork(appContext);
            return;
        }

        try {
            FacebookSdk.setApplicationId(appId);
            FacebookSdk.setClientToken(clientToken == null ? "" : clientToken);
            FacebookSdk.sdkInitialize(appContext);
            FacebookSdk.setAutoInitEnabled(true);
            FacebookSdk.fullyInitialize();
            FacebookSdk.setAutoLogAppEventsEnabled(true);
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS);

            AppEventsLogger logger = AppEventsLogger.newLogger(appContext);
            logger.getApplicationId();

            if (appContext instanceof Application) {
                AppEventsLogger.activateApp((Application) appContext, appId);
            }

            sdkInitialized = true;
            Log.d(TAG, "facebook_sdk_initialized appId=" + appId);
            initAudienceNetwork(appContext);
        } catch (Exception e) {
            Log.w(TAG, "facebook_sdk_init_failed: " + e.getMessage());
        }
    }

    private static void initAudienceNetwork(Context appContext) {
        if (audienceInitStarted) {
            return;
        }
        audienceInitStarted = true;
        try {
            if (AudienceNetworkAds.isInitialized(appContext)) {
                return;
            }
            AudienceNetworkAds.initialize(appContext);
            Log.d(TAG, "audience_network_initialized");
        } catch (Exception e) {
            audienceInitStarted = false;
            Log.w(TAG, "audience_network_init_failed: " + e.getMessage());
        }
    }
}
