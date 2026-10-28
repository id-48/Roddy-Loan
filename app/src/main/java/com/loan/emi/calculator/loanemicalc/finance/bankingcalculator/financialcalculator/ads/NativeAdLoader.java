package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class NativeAdLoader {
    private static final String TAG = "EmiNativeAds";

    /** Shared rotate: priority 1,2 → 1st native AdMob, 2nd native AdX, then repeat. */
    private static int rotateIndex;

    private final AdsConfig config;
    private NativeAd googleNativeAd;
    private boolean loading;

    NativeAdLoader(AdsConfig config) {
        this.config = config;
    }

    void load(Activity activity, ViewGroup container, AdsManager.AdCallback callback) {
        if (activity == null || container == null || config == null || !config.areNativeEnabled()) {
            if (container != null) {
                container.removeAllViews();
                container.setVisibility(View.GONE);
            }
            if (callback != null) {
                callback.onUnavailable();
            }
            return;
        }
        if (loading) {
            return;
        }
        loading = true;
        showPlaceholder(activity, container);
        List<String> order = buildAttemptOrder();
        if (order.isEmpty()) {
            loading = false;
            container.removeAllViews();
            container.setVisibility(View.GONE);
            if (callback != null) {
                callback.onUnavailable();
            }
            return;
        }
        Log.d(TAG, "native attempt order=" + order + " rotateIndex=" + rotateIndex);
        tryLoad(activity, container, order, 0, callback);
    }

    void destroy() {
        if (googleNativeAd != null) {
            googleNativeAd.destroy();
            googleNativeAd = null;
        }
        loading = false;
    }

    /**
     * priority rotates per load; on fail within one load, try rest of priority then fallback.
     */
    private List<String> buildAttemptOrder() {
        List<String> priority = networksWithIds(config.orderFor("native", false));
        List<String> fallback = networksWithIds(config.orderFor("native", true));
        List<String> attempt = new ArrayList<>();

        if (!priority.isEmpty()) {
            int start = Math.floorMod(rotateIndex, priority.size());
            for (int i = 0; i < priority.size(); i++) {
                attempt.add(priority.get((start + i) % priority.size()));
            }
            rotateIndex++;
        }

        for (String network : fallback) {
            if (!attempt.contains(network)) {
                attempt.add(network);
            }
        }

        if (attempt.isEmpty()) {
            attempt.add("admob");
        }
        return attempt;
    }

    private List<String> networksWithIds(List<String> networks) {
        List<String> out = new ArrayList<>();
        if (networks == null) {
            return out;
        }
        LinkedHashSet<String> seenUnits = new LinkedHashSet<>();
        for (String name : networks) {
            if (name == null || name.isEmpty()) {
                continue;
            }
            String unit = config.resolveAdId(name, "native");
            if (unit == null || unit.trim().isEmpty()) {
                continue;
            }
            // Same unit twice in one attempt → skip duplicate request
            if (!seenUnits.add(unit.trim())) {
                continue;
            }
            out.add(name);
        }
        return out;
    }

    private void tryLoad(Activity activity, ViewGroup container, List<String> order, int index,
                         AdsManager.AdCallback callback) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            loading = false;
            return;
        }
        if (index >= order.size()) {
            loading = false;
            container.removeAllViews();
            container.setVisibility(View.GONE);
            if (callback != null) {
                callback.onUnavailable();
            }
            return;
        }
        String network = order.get(index);
        String adId = config.resolveAdId(network, "native");
        // AdMob (ca-app-pub-…) and Ad Manager / AdX (/network/…unit) both via Google SDK
        if (!isGoogleSdkUnit(adId)) {
            Log.d(TAG, "skip network=" + network + " reason=unsupported native id=" + adId);
            tryLoad(activity, container, order, index + 1, callback);
            return;
        }
        String unit = adId.trim();
        Log.d(TAG, "request network=" + network + " format=native slot=" + (index + 1)
                + "/" + order.size());
        AdAnalytics.logAdRequest("native", network, unit);
        AtomicBoolean settled = new AtomicBoolean(false);
        AdLoader loader = new AdLoader.Builder(activity, unit)
                .forNativeAd(nativeAd -> {
                    if (!settled.compareAndSet(false, true)) {
                        nativeAd.destroy();
                        return;
                    }
                    if (activity.isFinishing() || activity.isDestroyed()) {
                        nativeAd.destroy();
                        loading = false;
                        return;
                    }
                    if (googleNativeAd != null) {
                        googleNativeAd.destroy();
                    }
                    googleNativeAd = nativeAd;
                    bindGoogle(activity, container, nativeAd);
                    loading = false;
                    AdAnalytics.logAdLoad("native", network, unit);
                    AdAnalytics.logAdImpression("native", network, unit);
                    if (callback != null) {
                        callback.onClosed();
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        if (!settled.compareAndSet(false, true)) {
                            return;
                        }
                        Log.w(TAG, "failed network=" + network + " reason="
                                + (loadAdError == null ? "unknown" : loadAdError.getMessage()));
                        tryLoad(activity, container, order, index + 1, callback);
                    }
                })
                .withNativeAdOptions(new NativeAdOptions.Builder().build())
                .build();
        loader.loadAd(new AdRequest.Builder().build());
    }

    private void showPlaceholder(Activity activity, ViewGroup container) {
        container.setVisibility(View.VISIBLE);
        container.removeAllViews();
        LayoutInflater.from(activity).inflate(R.layout.item_native_ad_placeholder, container, true);
    }

    private void bindGoogle(Activity activity, ViewGroup container, NativeAd nativeAd) {
        container.removeAllViews();
        NativeAdView adView = (NativeAdView) LayoutInflater.from(activity)
                .inflate(R.layout.item_native_ad_google, container, false);
        MediaView mediaView = adView.findViewById(R.id.adMedia);
        ImageView iconView = adView.findViewById(R.id.adIcon);
        TextView headline = adView.findViewById(R.id.adHeadline);
        TextView advertiser = adView.findViewById(R.id.adAdvertiser);
        TextView body = adView.findViewById(R.id.adBody);
        TextView cta = adView.findViewById(R.id.adCallToAction);

        adView.setMediaView(mediaView);
        adView.setHeadlineView(headline);
        adView.setBodyView(body);
        adView.setCallToActionView(cta);
        adView.setIconView(iconView);
        adView.setAdvertiserView(advertiser);

        headline.setText(nativeAd.getHeadline());
        if (body != null) {
            body.setText(nativeAd.getBody());
        }
        if (cta != null) {
            cta.setText(nativeAd.getCallToAction() == null ? "Install" : nativeAd.getCallToAction());
        }
        if (advertiser != null) {
            if (nativeAd.getAdvertiser() == null) {
                advertiser.setVisibility(View.GONE);
            } else {
                advertiser.setVisibility(View.VISIBLE);
                advertiser.setText(nativeAd.getAdvertiser());
            }
        }
        if (iconView != null) {
            if (nativeAd.getIcon() == null || nativeAd.getIcon().getDrawable() == null) {
                iconView.setVisibility(View.GONE);
            } else {
                iconView.setVisibility(View.VISIBLE);
                iconView.setImageDrawable(nativeAd.getIcon().getDrawable());
            }
        }
        adView.setNativeAd(nativeAd);
        container.addView(adView);
        container.setVisibility(View.VISIBLE);
    }

    /** AdMob ca-app-pub-… or Ad Manager /123…/unit */
    private static boolean isGoogleSdkUnit(String adId) {
        if (adId == null) {
            return false;
        }
        String t = adId.trim();
        return t.startsWith("ca-app-pub-") || t.startsWith("/");
    }
}
