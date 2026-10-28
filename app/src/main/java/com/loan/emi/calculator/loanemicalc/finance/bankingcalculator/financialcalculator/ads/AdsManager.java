package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.ViewGroup;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AdsManager {
    private static final String TAG = "EmiAds";
    private static final AdsManager INSTANCE = new AdsManager();
    /** Max wait on "Ads Loading..." before dismiss + continue without ad. */
    private static final long AD_LOAD_WAIT_MS = 6_000L;

    public enum Format {
        APP_OPEN("app_open"),
        INTERSTITIAL("interstitial"),
        REWARDED("rewarded");

        final String value;

        Format(String value) {
            this.value = value;
        }
    }

    public interface AdCallback {
        void onClosed();

        void onUnavailable();
    }

    public interface RewardCallback extends AdCallback {
        void onRewarded();
    }

    private final Map<Format, Slot> slots = new EnumMap<>(Format.class);
    private Context application;
    private AdsConfig config;
    private final AdLoadingDialog loadingDialog = new AdLoadingDialog();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private int loaderGeneration;
    private Runnable loaderTimeout;
    private WeakReference<Activity> host = new WeakReference<>(null);
    private boolean lifecycleBound;
    private boolean sawBackground;
    private boolean resumeQueued;
    private boolean sdkReady;
    /** After-call dialog open — skip resume App Open briefly so it does not steal focus. */
    private volatile long suppressResumeAppOpenUntilMs;
    private final Map<Activity, List<NativeAdLoader>> nativesByActivity =
            new java.util.WeakHashMap<>();
    /** Prevents double native requests when locale recreate restarts the same screen quickly. */
    private final Map<String, Long> nativeBindAtByScreen = new java.util.HashMap<>();
    /**
     * Network rotate index per format.
     * priority {@code 1,2} → 1st show AdMob, 2nd show AdX, then repeat.
     * On fail within one show, remaining priority (then fallback) still waterfall.
     */
    private final Map<Format, Integer> networkRotateIndex = new EnumMap<>(Format.class);

    private AdsManager() {
        for (Format format : Format.values()) {
            slots.put(format, new Slot());
            networkRotateIndex.put(format, 0);
        }
    }

    public static AdsManager get() {
        return INSTANCE;
    }

    public void warmUp(Context context) {
        application = context.getApplicationContext();
        MobileAds.initialize(application, status -> {
            sdkReady = true;
            Log.d(TAG, "google sdk initialized");
        });
        bindLifecycle();
    }

    public void initialize(Context context, AdsConfig adsConfig) {
        if (application == null) {
            warmUp(context);
        }
        if (context instanceof Activity) {
            host = new WeakReference<>((Activity) context);
        }
        config = adsConfig == null ? AdsConfig.defaults() : adsConfig;
        Log.d(TAG, "configuration loaded");
    }

    /**
     * After-call / WhatsApp: process may wake with no in-memory config.
     * Hydrate from prefs cache sync, then run {@code whenReady}.
     */
    public void ensureReady(Context context, Runnable whenReady) {
        if (context == null) {
            return;
        }
        if (application == null) {
            warmUp(context);
        }
        if (config == null) {
            hydrateConfigFromCache(context);
        }
        if (config != null) {
            if (whenReady != null) {
                whenReady.run();
            }
            return;
        }
        AdsConfigLoader.load(context, cfg -> {
            if (whenReady != null) {
                whenReady.run();
            }
        });
    }

    private void hydrateConfigFromCache(Context context) {
        try {
            android.content.SharedPreferences prefs =
                    context.getApplicationContext()
                            .getSharedPreferences("emi_ads_prefs", Context.MODE_PRIVATE);
            String cached = prefs.getString("ads_config_json", null);
            if (cached == null || cached.trim().isEmpty()) {
                Log.w(TAG, "no cached ads config for after-call native");
                return;
            }
            boolean marketing = AdsPrefs.isMarketingUser(context);
            config = AdsConfig.parse(cached, marketing);
            Log.d(TAG, "hydrated ads config from cache for after-call");
        } catch (Exception e) {
            Log.e(TAG, "hydrateConfigFromCache failed", e);
        }
    }

    public AdsConfig config() {
        return config;
    }

    public boolean areAdsEnabled() {
        return config != null && config.areAdsEnabled();
    }

    public boolean areClickAdsEnabled() {
        return config != null && config.areClickAdsEnabled();
    }

    public void bindNatives(Activity activity) {
        if (activity == null || config == null || !config.areNativeEnabled()) {
            return;
        }
        String screenKey = activity.getClass().getName();
        long now = SystemClock.uptimeMillis();
        Long last = nativeBindAtByScreen.get(screenKey);
        if (last != null && now - last < 1500L) {
            Log.d(TAG, "skip duplicate bindNatives screen=" + activity.getClass().getSimpleName());
            return;
        }
        nativeBindAtByScreen.put(screenKey, now);

        destroyNatives(activity);
        List<NativeAdLoader> loaders = new ArrayList<>();
        int[] ids = new int[]{
                com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R.id.put_big_nativead_container,
                com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R.id.put_big_nativead2_container,
                com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R.id.put_big_nativead3_container
        };
        for (int id : ids) {
            ViewGroup container = activity.findViewById(id);
            if (container != null) {
                if (Boolean.TRUE.equals(container.getTag(
                        com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R.id.tag_native_lazy))) {
                    continue;
                }
                NativeAdLoader loader = new NativeAdLoader(config);
                loaders.add(loader);
                loader.load(activity, container, emptyCallback());
            }
        }
        nativesByActivity.put(activity, loaders);
    }

    public void loadNativeInto(Activity activity, ViewGroup container) {
        if (activity == null || container == null) {
            return;
        }
        if (config == null) {
            Log.w(TAG, "loadNativeInto skipped: config null — ensureReady first");
            return;
        }
        if (!config.areNativeEnabled()) {
            Log.w(TAG, "loadNativeInto skipped: natives disabled");
            container.setVisibility(android.view.View.GONE);
            return;
        }
        NativeAdLoader loader = new NativeAdLoader(config);
        List<NativeAdLoader> existing = nativesByActivity.get(activity);
        if (existing == null) {
            existing = new ArrayList<>();
            nativesByActivity.put(activity, existing);
        }
        existing.add(loader);
        loader.load(activity, container, emptyCallback());
    }

    /** Splash fullscreen by admin splashfullpriority: 0 skip, 1 inter, 2 reward, 3 app open, 4 rewarded inter, 5 custom. */
    public void showSplashFullscreen(Activity activity, AdCallback callback) {
        if (callback == null) {
            callback = emptyCallback();
        }
        if (config == null || !config.isSplashFullscreen || !areAdsEnabled()) {
            callback.onUnavailable();
            return;
        }
        final AdCallback original = callback;
        AdCallback wrapped = new AdCallback() {
            @Override
            public void onClosed() {
                hideLoader();
                original.onClosed();
            }

            @Override
            public void onUnavailable() {
                hideLoader();
                original.onUnavailable();
            }
        };
        // Center "Ads Loading..." card until fullscreen is ready (max 6s)
        showLoader(activity);
        java.util.List<Integer> order = config.splashFullPriority;
        if (order == null || order.isEmpty()) {
            present(Format.APP_OPEN, activity, wrapped, null, true);
            return;
        }
        playSplashSlot(activity, order, 0, wrapped);
    }

    private void playSplashSlot(Activity activity, java.util.List<Integer> order, int index,
                                AdCallback callback) {
        if (index >= order.size()) {
            callback.onUnavailable();
            return;
        }
        int slot = order.get(index) == null ? 0 : order.get(index);
        AdCallback next = new AdCallback() {
            @Override
            public void onClosed() {
                callback.onClosed();
            }

            @Override
            public void onUnavailable() {
                playSplashSlot(activity, order, index + 1, callback);
            }
        };
        if (slot == 0) {
            next.onUnavailable();
            return;
        }
        if (slot == 1) {
            present(Format.INTERSTITIAL, activity, next, null, true);
            return;
        }
        if (slot == 2) {
            RewardCallback reward = new RewardCallback() {
                @Override
                public void onRewarded() {
                    callback.onClosed();
                }

                @Override
                public void onClosed() {
                    next.onUnavailable();
                }

                @Override
                public void onUnavailable() {
                    next.onUnavailable();
                }
            };
            present(Format.REWARDED, activity, reward, reward, true);
            return;
        }
        if (slot == 3) {
            present(Format.APP_OPEN, activity, next, null, true);
            return;
        }
        if (slot == 4) {
            RewardCallback reward = new RewardCallback() {
                @Override
                public void onRewarded() {
                    callback.onClosed();
                }

                @Override
                public void onClosed() {
                    present(Format.INTERSTITIAL, activity, next, null, true);
                }

                @Override
                public void onUnavailable() {
                    present(Format.INTERSTITIAL, activity, next, null, true);
                }
            };
            present(Format.REWARDED, activity, reward, reward, true);
            return;
        }
        if (slot == 5) {
            String url = config.resolveAdId("custom", "interstitial");
            if (CustomChromeHelper.open(activity, url)) {
                AdAnalytics.logAdRequest("splash", "custom", url);
                AdAnalytics.logAdLoad("splash", "custom", url);
                AdAnalytics.logAdImpression("splash", "custom", url);
                callback.onClosed();
            } else {
                next.onUnavailable();
            }
            return;
        }
        next.onUnavailable();
    }

    public void destroyNatives(Activity activity) {
        List<NativeAdLoader> loaders = nativesByActivity.remove(activity);
        if (loaders == null) {
            return;
        }
        for (NativeAdLoader loader : loaders) {
            loader.destroy();
        }
    }

    public void showAppOpen(Activity activity, AdCallback callback) {
        present(Format.APP_OPEN, activity, callback, null, true);
    }

    /** Custom link fullscreen (Chrome Custom Tab) for click / splash priority slot 4. */
    public void showCustomFullscreen(Activity activity, AdCallback callback) {
        if (callback == null) {
            callback = emptyCallback();
        }
        if (!areAdsEnabled() || config == null) {
            callback.onUnavailable();
            return;
        }
        String url = config.resolveAdId("custom", "interstitial");
        if (url == null || url.trim().isEmpty()) {
            callback.onUnavailable();
            return;
        }
        showLoader(activity);
        if (CustomChromeHelper.open(activity, url.trim())) {
            AdAnalytics.logAdRequest("click_fs", "custom", url);
            AdAnalytics.logAdLoad("click_fs", "custom", url);
            AdAnalytics.logAdImpression("click_fs", "custom", url);
            hideLoader();
            callback.onClosed();
        } else {
            hideLoader();
            callback.onUnavailable();
        }
    }

    public void suppressResumeAppOpenBriefly() {
        suppressResumeAppOpenUntilMs = SystemClock.elapsedRealtime() + 12_000L;
        resumeQueued = false;
    }

    public void maybeShowAppOpenOnResume(Activity activity) {
        if (!resumeQueued) {
            return;
        }
        resumeQueued = false;
        if (activity == null || !areAdsEnabled()) {
            return;
        }
        if (SystemClock.elapsedRealtime() < suppressResumeAppOpenUntilMs) {
            return;
        }
        String name = activity.getClass().getSimpleName();
        if (name.contains("Splash") || name.contains("AfterCall")) {
            return;
        }
        showAppOpen(activity, emptyCallback());
    }

    public void showInterstitial(Activity activity, AdCallback callback) {
        if (callback == null) {
            callback = emptyCallback();
        }
        if (!areAdsEnabled()) {
            callback.onUnavailable();
            return;
        }
        present(Format.INTERSTITIAL, activity, callback, null, true);
    }

    public void showRewarded(Activity activity, RewardCallback callback) {
        if (callback == null) {
            return;
        }
        if (!areAdsEnabled()) {
            callback.onUnavailable();
            return;
        }
        present(Format.REWARDED, activity, callback, callback, true);
    }

    private void present(Format format, Activity activity, AdCallback callback,
                         RewardCallback rewardCallback, boolean withLoader) {
        Slot slot = slots.get(format);
        if (slot == null || !ready(activity) || slot.showing) {
            if (callback != null) {
                callback.onUnavailable();
            }
            return;
        }
        host = new WeakReference<>(activity);
        slot.callback = callback;
        slot.rewardCallback = rewardCallback;
        slot.rewardGranted = false;
        slot.usingFallback = false;
        slot.orderIndex = 0;
        slot.rotateStart = networkRotateIndex.getOrDefault(format, 0);
        if (hasAd(slot)) {
            hideLoader();
            show(format, slot, activity);
            return;
        }
        if (withLoader) {
            showLoader(activity);
        }
        if (!slot.loading) {
            request(format, slot);
        }
    }

    private void request(Format format, Slot slot) {
        // Timed out / cancelled — do not continue waterfall
        if (slot.callback == null && slot.rewardCallback == null) {
            slot.loading = false;
            return;
        }
        Activity activity = host.get();
        Candidate candidate = nextCandidate(format, slot);
        if (candidate == null) {
            if (!slot.usingFallback) {
                slot.usingFallback = true;
                slot.orderIndex = 0;
                candidate = nextCandidate(format, slot);
            }
            if (candidate == null) {
                slot.loading = false;
                deliverUnavailable(slot);
                return;
            }
        }
        slot.loading = true;
        Log.d(TAG, "request network=" + candidate.network + " format=" + format.value
                + " priority=" + candidate.priority + " fallback=" + slot.usingFallback);
        AdAnalytics.logAdRequest(format.value, candidate.network, candidate.adId);
        AtomicBoolean settled = new AtomicBoolean();
        if ("custom".equals(candidate.network) || CustomChromeHelper.isUrl(candidate.adId)) {
            loadCustom(format, slot, activity, candidate, settled);
            return;
        }
        if ("tradplus".equals(candidate.network) && !isGoogleSdkUnit(candidate.adId)) {
            // TradPlus SDK optional — skip to next when unit is not Google-style
            fail(format, slot, candidate, settled, "tradplus sdk not linked");
            return;
        }
        if (!isGoogleSdkUnit(candidate.adId) && !"custom".equals(candidate.network)) {
            fail(format, slot, candidate, settled, "unsupported ad unit");
            return;
        }
        loadGoogle(format, slot, candidate, settled);
    }

    private void loadCustom(Format format, Slot slot, Activity activity,
                            Candidate candidate, AtomicBoolean settled) {
        String url = candidate.adId;
        AdsConfig.NetworkConfig network = config.networks == null ? null : config.networks.get("custom");
        if ((url == null || url.isEmpty()) && network != null) {
            url = network.customUrl;
        }
        if (activity == null || !CustomChromeHelper.open(activity, url)) {
            fail(format, slot, candidate, settled, "custom url failed");
            return;
        }
        if (!settled.compareAndSet(false, true)) {
            return;
        }
        slot.loading = false;
        slot.loaded = candidate;
        AdAnalytics.logAdLoad(format.value, candidate.network, url);
        AdAnalytics.logAdImpression(format.value, candidate.network, url);
        hideLoader();
        if (format == Format.REWARDED) {
            slot.rewardGranted = true;
            if (slot.rewardCallback != null) {
                slot.rewardCallback.onRewarded();
            }
        }
        finish(format, slot, true);
    }

    private void loadGoogle(Format format, Slot slot, Candidate candidate, AtomicBoolean settled) {
        AdRequest request = new AdRequest.Builder().build();
        if (format == Format.APP_OPEN) {
            AppOpenAd.load(application, candidate.adId, request, new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(AppOpenAd ad) {
                    slot.appOpenAd = ad;
                    loaded(format, slot, candidate, settled);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    fail(format, slot, candidate, settled, error.getMessage());
                }
            });
            return;
        }
        if (format == Format.INTERSTITIAL) {
            InterstitialAd.load(application, candidate.adId, request, new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(InterstitialAd ad) {
                    slot.interstitialAd = ad;
                    loaded(format, slot, candidate, settled);
                }

                @Override
                public void onAdFailedToLoad(LoadAdError error) {
                    fail(format, slot, candidate, settled, error.getMessage());
                }
            });
            return;
        }
        RewardedAd.load(application, candidate.adId, request, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(RewardedAd ad) {
                slot.rewardedAd = ad;
                loaded(format, slot, candidate, settled);
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                fail(format, slot, candidate, settled, error.getMessage());
            }
        });
    }

    private void loaded(Format format, Slot slot, Candidate candidate, AtomicBoolean settled) {
        if (!settled.compareAndSet(false, true)) {
            return;
        }
        slot.loading = false;
        slot.loaded = candidate;
        Log.i(TAG, "loaded network=" + candidate.network + " format=" + format.value);
        AdAnalytics.logAdLoad(format.value, candidate.network, candidate.adId);
        deliverReady(format, slot);
    }

    private void fail(Format format, Slot slot, Candidate candidate, AtomicBoolean settled, String reason) {
        if (!settled.compareAndSet(false, true)) {
            return;
        }
        Log.w(TAG, "failed network=" + (candidate == null ? "?" : candidate.network)
                + " format=" + format.value + " reason=" + reason);
        clearAds(slot);
        // Waterfall: try next network in current order, then fallback list
        request(format, slot);
    }

    private void deliverReady(Format format, Slot slot) {
        // Wait timed out — caller already continued; never show late ad
        if (slot.callback == null && slot.rewardCallback == null) {
            clearAds(slot);
            slot.loading = false;
            return;
        }
        Activity activity = host.get();
        if (!active(activity)) {
            deliverUnavailable(slot);
            return;
        }
        show(format, slot, activity);
    }

    private void show(Format format, Slot slot, Activity activity) {
        if (slot.showing || !hasAd(slot)) {
            hideLoader();
            deliverUnavailable(slot);
            return;
        }
        slot.showing = true;
        hideLoader();
        FullScreenContentCallback callback = new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                clearAds(slot);
                finish(format, slot, true);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(com.google.android.gms.ads.AdError adError) {
                clearAds(slot);
                // show failed → try fallback waterfall
                slot.showing = false;
                if (!slot.usingFallback) {
                    slot.usingFallback = true;
                    slot.orderIndex = 0;
                    request(format, slot);
                } else {
                    finish(format, slot, false);
                }
            }

            @Override
            public void onAdShowedFullScreenContent() {
                String network = slot.loaded == null ? "?" : slot.loaded.network;
                String unit = slot.loaded == null ? "-" : slot.loaded.adId;
                Log.i(TAG, "impression format=" + format.value + " network=" + network);
                AdAnalytics.logAdImpression(format.value, network, unit);
            }
        };
        try {
            if (slot.appOpenAd != null) {
                slot.appOpenAd.setFullScreenContentCallback(callback);
                slot.appOpenAd.show(activity);
                return;
            }
            if (slot.interstitialAd != null) {
                slot.interstitialAd.setFullScreenContentCallback(callback);
                slot.interstitialAd.show(activity);
                return;
            }
            if (slot.rewardedAd != null) {
                slot.rewardedAd.setFullScreenContentCallback(callback);
                slot.rewardedAd.show(activity, rewardItem -> {
                    slot.rewardGranted = true;
                    if (slot.rewardCallback != null) {
                        slot.rewardCallback.onRewarded();
                    }
                });
                return;
            }
        } catch (Exception e) {
            Log.e(TAG, "show error", e);
        }
        slot.showing = false;
        clearAds(slot);
        finish(format, slot, false);
    }

    private void finish(Format format, Slot slot, boolean closed) {
        slot.showing = false;
        slot.loading = false;
        hideLoader();
        // Next fullscreen of this format starts at next network in priority (1,2 → admob then adx)
        bumpNetworkRotate(format);
        AdCallback callback = slot.callback;
        RewardCallback rewardCallback = slot.rewardCallback;
        slot.callback = null;
        slot.rewardCallback = null;
        slot.loaded = null;
        if (format == Format.REWARDED && rewardCallback != null) {
            if (closed) {
                // Always fire close so callers redirect only after dismiss
                // (onRewarded may have already fired while ad was open)
                rewardCallback.onClosed();
            } else {
                rewardCallback.onUnavailable();
            }
            return;
        }
        if (callback != null) {
            if (closed) {
                callback.onClosed();
            } else {
                callback.onUnavailable();
            }
        }
    }

    private void deliverUnavailable(Slot slot) {
        hideLoader();
        slot.loading = false;
        slot.showing = false;
        Format format = formatOf(slot);
        if (format != null) {
            bumpNetworkRotate(format);
        }
        AdCallback callback = slot.callback;
        slot.callback = null;
        slot.rewardCallback = null;
        clearAds(slot);
        if (callback != null) {
            callback.onUnavailable();
        }
    }

    private Format formatOf(Slot slot) {
        for (Map.Entry<Format, Slot> e : slots.entrySet()) {
            if (e.getValue() == slot) {
                return e.getKey();
            }
        }
        return null;
    }

    private void bumpNetworkRotate(Format format) {
        if (config == null || format == null) {
            return;
        }
        List<String> order = config.orderFor(format.value, false);
        if (order == null || order.isEmpty()) {
            return;
        }
        int cur = networkRotateIndex.getOrDefault(format, 0);
        networkRotateIndex.put(format, (cur + 1) % order.size());
        Log.d(TAG, "network rotate format=" + format.value
                + " nextIndex=" + networkRotateIndex.get(format)
                + " order=" + order);
    }

    private Candidate nextCandidate(Format format, Slot slot) {
        if (config == null || config.networks == null) {
            return null;
        }
        List<String> order = config.orderFor(format.value, slot.usingFallback);
        if (order == null || order.isEmpty()) {
            return null;
        }
        // priority 1,2 → rotate start each show (1st AdMob, 2nd AdX). Fallback list unchanged.
        if (!slot.usingFallback && slot.rotateStart > 0) {
            order = rotateList(order, slot.rotateStart);
        }
        while (slot.orderIndex < order.size()) {
            String name = order.get(slot.orderIndex);
            slot.orderIndex++;
            if (name == null || name.isEmpty()) {
                continue;
            }
            String adId = config.resolveAdId(name, format.value);
            if (adId == null || adId.trim().isEmpty()) {
                Log.d(TAG, "skip network=" + name + " format=" + format.value + " reason=empty ad id");
                continue;
            }
            return new Candidate(name, adId.trim(), slot.orderIndex);
        }
        return null;
    }

    private static List<String> rotateList(List<String> source, int start) {
        if (source == null || source.isEmpty()) {
            return source;
        }
        int n = source.size();
        int s = Math.floorMod(start, n);
        if (s == 0) {
            return source;
        }
        List<String> out = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            out.add(source.get((s + i) % n));
        }
        return out;
    }

    private boolean hasAd(Slot slot) {
        return slot.appOpenAd != null || slot.interstitialAd != null || slot.rewardedAd != null;
    }

    private void clearAds(Slot slot) {
        slot.appOpenAd = null;
        slot.interstitialAd = null;
        slot.rewardedAd = null;
    }

    private void bindLifecycle() {
        if (lifecycleBound) {
            return;
        }
        lifecycleBound = true;
        ProcessLifecycleOwner.get().getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_STOP) {
                sawBackground = true;
            } else if (event == Lifecycle.Event.ON_START && sawBackground) {
                resumeQueued = true;
            }
        });
    }

    private boolean ready(Activity activity) {
        return application != null && config != null && active(activity);
    }

    private boolean active(Activity activity) {
        return activity != null && !activity.isFinishing() && !activity.isDestroyed();
    }

    private void showLoader(Activity activity) {
        loadingDialog.show(activity);
        scheduleLoaderTimeout();
    }

    private void scheduleLoaderTimeout() {
        cancelLoaderTimeout();
        final int gen = ++loaderGeneration;
        loaderTimeout = () -> {
            if (gen != loaderGeneration) {
                return;
            }
            Log.w(TAG, "ad load wait timeout " + AD_LOAD_WAIT_MS + "ms — dismiss loader, continue");
            loadingDialog.dismiss();
            for (Format format : Format.values()) {
                Slot slot = slots.get(format);
                if (slot == null || slot.showing) {
                    continue;
                }
                if (slot.callback != null || slot.rewardCallback != null) {
                    clearAds(slot);
                    deliverUnavailable(slot);
                }
            }
        };
        mainHandler.postDelayed(loaderTimeout, AD_LOAD_WAIT_MS);
    }

    private void cancelLoaderTimeout() {
        if (loaderTimeout != null) {
            mainHandler.removeCallbacks(loaderTimeout);
            loaderTimeout = null;
        }
    }

    private void hideLoader() {
        cancelLoaderTimeout();
        loaderGeneration++;
        loadingDialog.dismiss();
    }

    private static AdCallback emptyCallback() {
        return new AdCallback() {
            @Override
            public void onClosed() {
            }

            @Override
            public void onUnavailable() {
            }
        };
    }

    /** AdMob ca-app-pub-… or Ad Manager / AdX /network/…unit */
    private static boolean isGoogleSdkUnit(String adId) {
        if (adId == null) {
            return false;
        }
        String t = adId.trim();
        return t.startsWith("ca-app-pub-") || t.startsWith("/");
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static final class Slot {
        private boolean loading;
        private boolean showing;
        private boolean usingFallback;
        private int orderIndex;
        private int rotateStart;
        private boolean rewardGranted;
        private Candidate loaded;
        private AdCallback callback;
        private RewardCallback rewardCallback;
        private AppOpenAd appOpenAd;
        private InterstitialAd interstitialAd;
        private RewardedAd rewardedAd;
    }

    private static final class Candidate {
        private final String network;
        private final String adId;
        private final int priority;

        private Candidate(String network, String adId, int priority) {
            this.network = network;
            this.adId = adId;
            this.priority = priority;
        }
    }
}
