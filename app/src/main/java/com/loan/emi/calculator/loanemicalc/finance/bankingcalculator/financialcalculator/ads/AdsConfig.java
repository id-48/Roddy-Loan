package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdsConfig {
    private static final Gson GSON = new Gson();
    private static final Type WEIGHTED_LIST = new TypeToken<List<WeightedUnit>>() {
    }.getType();
    private static final Map<String, String> NETWORK_BY_SLOT = new LinkedHashMap<>();
    private static final Random RANDOM = new Random();

    static {
        NETWORK_BY_SLOT.put("1", "admob");
        NETWORK_BY_SLOT.put("2", "adx");
        NETWORK_BY_SLOT.put("3", "tradplus");
        NETWORK_BY_SLOT.put("4", "custom");
        NETWORK_BY_SLOT.put("admob", "admob");
        NETWORK_BY_SLOT.put("adx", "adx");
        NETWORK_BY_SLOT.put("tradplus", "tradplus");
        NETWORK_BY_SLOT.put("custom", "custom");
    }

    /** Resolved from native C++ (XOR obfuscated). Not stored as plaintext in Java. */
    public static String DEFAULT_API_BASE() {
        return SecureApi.baseUrl();
    }

    @SerializedName("ads_enabled")
    public Boolean adsEnabled;

    @SerializedName("splash_ad_enabled")
    public boolean splashAdEnabled = true;

    @SerializedName("ads_config_url")
    public String adsConfigUrl;

    @SerializedName("versionCode")
    public int versionCode;

    @SerializedName("applink")
    public String applink;

    @SerializedName("updateInprogress")
    public boolean updateInprogress;

    @SerializedName("isSplashFullscreen")
    public boolean isSplashFullscreen = true;

    @SerializedName("splashfullpriority")
    public List<Integer> splashFullPriority;

    @SerializedName(value = "afterCallON", alternate = {"afterCall"})
    public Boolean afterCallON;

    @SerializedName("whatsappAfterCall")
    public Boolean whatsappAfterCall;

    /**
     * How often to show Call Ended screen (phone + WhatsApp shared), per audience.
     * 100 = every call, 50 = every 2nd, 25 = every 4th. 0 = never.
     */
    @SerializedName(value = "afterCallShowPercent", alternate = {"after_call_show_percent"})
    public Integer afterCallShowPercent;

    /** Marketing only: show Loan Tracker card + step notifications. Organic always hidden. */
    @SerializedName(value = "loanTrackerON", alternate = {"loanTracker"})
    public Boolean loanTrackerON;

    @SerializedName("onboardingScreenOrder")
    public List<Integer> onboardingScreenOrder;

    /** Facebook App ID (per organic / marketing cohort). */
    @SerializedName(value = "fb_app_id", alternate = {"facebook_app_id", "fbAppId"})
    public String fbAppId;

    /** Facebook Client Token / secret (per organic / marketing cohort). */
    @SerializedName(value = "fb_client_token", alternate = {"facebook_client_token", "fbClientToken", "fb_secret"})
    public String fbClientToken;

    /** Show fullscreen ad every N clicks (0 = off). Organic / marketing per cohort. */
    @SerializedName(value = "click_interval", alternate = {"clickInterval"})
    public Integer clickInterval;

    /** Home launcher enabled for this cohort. */
    @SerializedName(value = "launcherON", alternate = {"launcher", "isLauncher"})
    public Boolean launcherON;

    /** Launcher app-icon clicks before fullscreen ad (0 = off). */
    @SerializedName(value = "launcher_interval", alternate = {"launcherInterval", "launcherClick"})
    public Integer launcherInterval;

    /**
     * Click fullscreen format rotate: 1=interstitial, 2=reward, 3=app_open, 4=custom.
     * Example 1,3,4 → inter → app open → custom → repeat.
     */
    @SerializedName(value = "fullscreen_priority", alternate = {"fullscreenPriority", "click_fullscreen_priority"})
    public List<Integer> fullscreenPriority;

    @SerializedName("mainTest")
    public boolean mainTest;

    @SerializedName("test_mode")
    public Boolean testMode;

    @SerializedName("priority")
    public Map<String, List<Object>> priority;

    @SerializedName("fallback")
    public Map<String, List<Object>> fallback;

    @SerializedName("networks")
    public Map<String, NetworkConfig> networks;

    public static class WeightedUnit {
        @SerializedName("id")
        public String id;

        @SerializedName("percent")
        public int percent;
    }

    public static class NetworkConfig {
        @SerializedName("app_open_id")
        public String appOpenId;

        @SerializedName("interstitial_id")
        public String interstitialId;

        @SerializedName("rewarded_id")
        public String rewardedId;

        @SerializedName("rewarded_interstitial_id")
        public String rewardedInterstitialId;

        @SerializedName("native_id")
        public String nativeId;

        @SerializedName("banner_id")
        public String bannerId;

        @SerializedName("app_id")
        public String appId;

        @SerializedName("custom_url")
        public String customUrl;

        /** AdX percentage units (CashPop-style network with weighted IDs). */
        @SerializedName("app_open")
        public List<WeightedUnit> appOpenUnits;

        @SerializedName("interstitial")
        public List<WeightedUnit> interstitialUnits;

        @SerializedName("rewarded")
        public List<WeightedUnit> rewardedUnits;

        @SerializedName("native")
        public List<WeightedUnit> nativeUnits;

        @SerializedName("banner")
        public List<WeightedUnit> bannerUnits;
    }

    public static AdsConfig parse(String raw) {
        return parse(raw, false);
    }

    public static AdsConfig parse(String raw, boolean marketing) {
        try {
            JsonObject root = JsonParser.parseString(raw == null || raw.isEmpty() ? "{}" : raw)
                    .getAsJsonObject();
            AdsConfig shared = mergeDefaults(fromJson(root));
            if (root.has("organic") || root.has("marketing")) {
                JsonObject picked = object(root, marketing ? "marketing" : "organic");
                if (picked == null) {
                    picked = object(root, "organic");
                }
                if (picked == null) {
                    picked = object(root, "marketing");
                }
                if (picked != null) {
                    AdsConfig cohort = mergeDefaults(fromJson(picked));
                    // Keep root-level platform flags from shared
                    cohort.adsConfigUrl = DEFAULT_API_BASE();
                    cohort.versionCode = shared.versionCode;
                    cohort.applink = shared.applink;
                    cohort.updateInprogress = shared.updateInprogress;
                    cohort.isSplashFullscreen = shared.isSplashFullscreen;
                    cohort.splashFullPriority = shared.splashFullPriority;
                    cohort.afterCallON = shared.afterCallON;
                    cohort.whatsappAfterCall = shared.whatsappAfterCall;
                    // Prefer cohort percent; fall back to root payload from ads_config.php
                    if (cohort.afterCallShowPercent == null) {
                        cohort.afterCallShowPercent = shared.afterCallShowPercent;
                    }
                    cohort.loanTrackerON = shared.loanTrackerON;
                    // Prefer cohort onboarding order (organic/marketing); fall back to root
                    if (cohort.onboardingScreenOrder == null || cohort.onboardingScreenOrder.isEmpty()) {
                        cohort.onboardingScreenOrder = shared.onboardingScreenOrder;
                    }
                    cohort.mainTest = shared.mainTest || (cohort.testMode != null && cohort.testMode);
                    return cohort;
                }
            }
            return shared;
        } catch (Exception e) {
            return defaults();
        }
    }

    private static AdsConfig fromJson(JsonObject root) {
        AdsConfig config = GSON.fromJson(root, AdsConfig.class);
        if (config == null) {
            return new AdsConfig();
        }
        // Gson Class-based parse can leave List<Integer> as Doubles — read explicitly
        config.onboardingScreenOrder = readIntList(root, "onboardingScreenOrder");
        List<Integer> fs = readIntList(root, "fullscreen_priority");
        if (fs.isEmpty()) {
            fs = readIntList(root, "fullscreenPriority");
        }
        if (!fs.isEmpty()) {
            config.fullscreenPriority = fs;
        }
        if (root.has("click_interval") && root.get("click_interval").isJsonPrimitive()) {
            try {
                config.clickInterval = root.get("click_interval").getAsInt();
            } catch (Exception ignored) {
            }
        }
        if (root.has("networks") && root.get("networks").isJsonObject()) {
            JsonObject networks = root.getAsJsonObject("networks");
            if (networks.has("adx") && networks.get("adx").isJsonObject()) {
                NetworkConfig adx = config.networks == null ? new NetworkConfig()
                        : config.networks.get("adx");
                if (adx == null) {
                    adx = new NetworkConfig();
                }
                JsonObject adxObj = networks.getAsJsonObject("adx");
                adx.appOpenUnits = readWeighted(adxObj, "app_open");
                adx.interstitialUnits = readWeighted(adxObj, "interstitial");
                adx.rewardedUnits = readWeighted(adxObj, "rewarded");
                adx.nativeUnits = readWeighted(adxObj, "native");
                adx.bannerUnits = readWeighted(adxObj, "banner");
                if (config.networks == null) {
                    config.networks = new LinkedHashMap<>();
                }
                config.networks.put("adx", adx);
            }
        }
        return config;
    }

    private static List<WeightedUnit> readWeighted(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonArray()) {
            return new ArrayList<>();
        }
        JsonArray array = obj.getAsJsonArray(key);
        List<WeightedUnit> list = GSON.fromJson(array, WEIGHTED_LIST);
        return list == null ? new ArrayList<>() : list;
    }

    /** Prefer cohort order; never leave null Doubles from Gson. */
    public static List<Integer> readIntList(JsonObject root, String key) {
        List<Integer> out = new ArrayList<>();
        if (root == null || !root.has(key) || root.get(key).isJsonNull()) {
            return out;
        }
        JsonElement el = root.get(key);
        if (el.isJsonArray()) {
            for (JsonElement item : el.getAsJsonArray()) {
                if (item == null || item.isJsonNull()) {
                    continue;
                }
                try {
                    if (item.isJsonPrimitive() && item.getAsJsonPrimitive().isNumber()) {
                        out.add(item.getAsInt());
                    } else if (item.isJsonPrimitive()) {
                        String raw = item.getAsString().trim();
                        if (!raw.isEmpty()) {
                            out.add(Integer.parseInt(raw));
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return out;
        }
        if (el.isJsonPrimitive()) {
            // Comma string "1,3,8"
            String raw = el.getAsString();
            for (String part : raw.split(",")) {
                String p = part.trim();
                if (p.isEmpty()) {
                    continue;
                }
                try {
                    out.add(Integer.parseInt(p));
                } catch (Exception ignored) {
                }
            }
        }
        return out;
    }

    public List<Integer> resolvedOnboardingOrder() {
        if (onboardingScreenOrder == null || onboardingScreenOrder.isEmpty()) {
            return new ArrayList<>(Arrays.asList(
                    1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23));
        }
        List<Integer> clean = new ArrayList<>();
        for (Object raw : onboardingScreenOrder) {
            if (raw == null) {
                continue;
            }
            try {
                if (raw instanceof Number) {
                    clean.add(((Number) raw).intValue());
                } else {
                    clean.add(Integer.parseInt(String.valueOf(raw).trim()));
                }
            } catch (Exception ignored) {
            }
        }
        return clean.isEmpty()
                ? new ArrayList<>(Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23))
                : clean;
    }

    public static AdsConfig defaults() {
        AdsConfig config = new AdsConfig();
        config.adsEnabled = true;
        config.splashAdEnabled = true;
        config.adsConfigUrl = DEFAULT_API_BASE();
        config.versionCode = 1;
        config.applink = "";
        config.updateInprogress = false;
        config.isSplashFullscreen = true;
        config.splashFullPriority = Arrays.asList(1);
        config.afterCallON = false;
        config.whatsappAfterCall = false;
        config.afterCallShowPercent = 100;
        config.loanTrackerON = false;
        config.fbAppId = "";
        config.fbClientToken = "";
        config.launcherON = false;
        config.launcherInterval = 1;
        config.onboardingScreenOrder = Arrays.asList(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23);
        config.clickInterval = 1;
        config.fullscreenPriority = Arrays.asList(1, 2, 3, 4);
        config.mainTest = true;
        config.priority = new LinkedHashMap<>();
        config.priority.put("interstitial", Arrays.asList(1, 2, 3, 4));
        config.priority.put("rewarded", Arrays.asList(1, 2, 3, 4));
        config.priority.put("app_open", Arrays.asList(1, 2, 3, 4));
        config.priority.put("native", Arrays.asList(1, 2, 3, 4));
        config.fallback = new LinkedHashMap<>();
        config.fallback.put("interstitial", Arrays.asList(2, 1));
        config.fallback.put("rewarded", Arrays.asList(3, 2));
        config.fallback.put("app_open", Arrays.asList(1, 2, 3));
        config.networks = new LinkedHashMap<>();
        NetworkConfig admob = new NetworkConfig();
        admob.appOpenId = "ca-app-pub-3940256099942544/9257395921";
        admob.interstitialId = "ca-app-pub-3940256099942544/1033173712";
        admob.rewardedId = "ca-app-pub-3940256099942544/5224354917";
        admob.nativeId = "ca-app-pub-3940256099942544/2247696110";
        admob.bannerId = "ca-app-pub-3940256099942544/6300978111";
        config.networks.put("admob", admob);
        NetworkConfig adx = new NetworkConfig();
        adx.appOpenUnits = new ArrayList<>();
        adx.interstitialUnits = new ArrayList<>();
        adx.rewardedUnits = new ArrayList<>();
        adx.nativeUnits = new ArrayList<>();
        adx.bannerUnits = new ArrayList<>();
        config.networks.put("adx", adx);
        config.networks.put("tradplus", new NetworkConfig());
        NetworkConfig custom = new NetworkConfig();
        custom.customUrl = "https://play.google.com/store";
        config.networks.put("custom", custom);
        return config;
    }

    private static AdsConfig mergeDefaults(AdsConfig parsed) {
        AdsConfig base = defaults();
        if (parsed == null) {
            return base;
        }
        if (parsed.adsEnabled != null) {
            base.adsEnabled = parsed.adsEnabled;
        }
        base.splashAdEnabled = parsed.splashAdEnabled;
        // Always use native obfuscated base — ignore plaintext from JSON/APK assets
        base.adsConfigUrl = DEFAULT_API_BASE();
        base.versionCode = parsed.versionCode;
        if (parsed.applink != null) {
            base.applink = parsed.applink;
        }
        base.updateInprogress = parsed.updateInprogress;
        base.isSplashFullscreen = parsed.isSplashFullscreen;
        if (parsed.splashFullPriority != null) {
            base.splashFullPriority = parsed.splashFullPriority;
        }
        if (parsed.afterCallON != null) {
            base.afterCallON = parsed.afterCallON;
        }
        if (parsed.whatsappAfterCall != null) {
            base.whatsappAfterCall = parsed.whatsappAfterCall;
        }
        if (parsed.afterCallShowPercent != null) {
            base.afterCallShowPercent = Math.max(0, Math.min(100, parsed.afterCallShowPercent));
        }
        if (parsed.loanTrackerON != null) {
            base.loanTrackerON = parsed.loanTrackerON;
        }
        if (parsed.fbAppId != null) {
            base.fbAppId = parsed.fbAppId;
        }
        if (parsed.fbClientToken != null) {
            base.fbClientToken = parsed.fbClientToken;
        }
        if (parsed.onboardingScreenOrder != null) {
            base.onboardingScreenOrder = parsed.onboardingScreenOrder;
        }
        if (parsed.clickInterval != null) {
            base.clickInterval = parsed.clickInterval;
        }
        if (parsed.launcherON != null) {
            base.launcherON = parsed.launcherON;
        }
        if (parsed.launcherInterval != null) {
            base.launcherInterval = parsed.launcherInterval;
        }
        if (parsed.fullscreenPriority != null && !parsed.fullscreenPriority.isEmpty()) {
            base.fullscreenPriority = parsed.fullscreenPriority;
        }
        base.mainTest = parsed.mainTest;
        if (parsed.testMode != null) {
            base.testMode = parsed.testMode;
            if (parsed.testMode) {
                base.mainTest = true;
            }
        }
        if (parsed.priority != null && !parsed.priority.isEmpty()) {
            base.priority = parsed.priority;
        }
        if (parsed.fallback != null && !parsed.fallback.isEmpty()) {
            base.fallback = parsed.fallback;
        }
        if (parsed.networks != null && !parsed.networks.isEmpty()) {
            for (Map.Entry<String, NetworkConfig> entry : parsed.networks.entrySet()) {
                base.networks.put(entry.getKey(), entry.getValue());
            }
        }
        return base;
    }

    public boolean areAdsEnabled() {
        return adsEnabled == null || adsEnabled;
    }

    public boolean areClickAdsEnabled() {
        return areAdsEnabled() && resolvedClickInterval() > 0;
    }

    public int resolvedClickInterval() {
        if (clickInterval == null) {
            return 1;
        }
        return Math.max(0, clickInterval);
    }

    public boolean isLauncherEnabled() {
        return launcherON != null && launcherON;
    }

    public int resolvedLauncherInterval() {
        if (launcherInterval == null) {
            return 1;
        }
        return Math.max(0, launcherInterval);
    }

    public List<Integer> resolvedFullscreenPriority() {
        if (fullscreenPriority == null || fullscreenPriority.isEmpty()) {
            return Arrays.asList(1, 2, 3, 4);
        }
        List<Integer> clean = new ArrayList<>();
        for (Object raw : fullscreenPriority) {
            if (raw == null) {
                continue;
            }
            try {
                int v = raw instanceof Number ? ((Number) raw).intValue()
                        : Integer.parseInt(String.valueOf(raw).trim());
                if (v >= 1 && v <= 4) {
                    clean.add(v);
                }
            } catch (Exception ignored) {
            }
        }
        return clean.isEmpty() ? Arrays.asList(1, 2, 3, 4) : clean;
    }

    public boolean areNativeEnabled() {
        return areAdsEnabled();
    }

    public List<String> orderFor(String format, boolean useFallback) {
        Map<String, List<Object>> source = useFallback ? fallback : priority;
        if (source == null || source.get(format) == null) {
            return Collections.emptyList();
        }
        List<String> order = new ArrayList<>();
        for (Object raw : source.get(format)) {
            String key = String.valueOf(raw).trim();
            if (key.endsWith(".0")) {
                key = key.substring(0, key.length() - 2);
            }
            String network = NETWORK_BY_SLOT.get(key.toLowerCase());
            if (network != null) {
                order.add(network);
            }
        }
        return order;
    }

    public String resolveAdId(String networkName, String format) {
        if (networks == null || networkName == null) {
            return null;
        }
        if (mainTest && ("admob".equals(networkName) || "adx".equals(networkName))) {
            return testUnit(format);
        }
        NetworkConfig network = networks.get(networkName);
        if (network == null) {
            return null;
        }
        if ("custom".equals(networkName)) {
            return trimToNull(network.customUrl);
        }
        if ("adx".equals(networkName)) {
            String weighted = pickWeighted(unitsFor(network, format));
            if (weighted != null) {
                return weighted;
            }
        }
        if ("app_open".equals(format)) {
            return trimToNull(network.appOpenId);
        }
        if ("interstitial".equals(format)) {
            return trimToNull(network.interstitialId);
        }
        if ("rewarded".equals(format) || "rewarded_interstitial".equals(format)) {
            String rewardedInterstitial = trimToNull(network.rewardedInterstitialId);
            if ("rewarded_interstitial".equals(format) && rewardedInterstitial != null) {
                return rewardedInterstitial;
            }
            return trimToNull(network.rewardedId);
        }
        if ("native".equals(format)) {
            return trimToNull(network.nativeId);
        }
        if ("banner".equals(format)) {
            return trimToNull(network.bannerId);
        }
        return null;
    }

    private static String testUnit(String format) {
        if ("app_open".equals(format)) {
            return "ca-app-pub-3940256099942544/9257395921";
        }
        if ("rewarded".equals(format) || "rewarded_interstitial".equals(format)) {
            return "ca-app-pub-3940256099942544/5224354917";
        }
        if ("native".equals(format)) {
            return "ca-app-pub-3940256099942544/2247696110";
        }
        if ("banner".equals(format)) {
            return "ca-app-pub-3940256099942544/6300978111";
        }
        return "ca-app-pub-3940256099942544/1033173712";
    }

    private static List<WeightedUnit> unitsFor(NetworkConfig network, String format) {
        if ("app_open".equals(format)) {
            return network.appOpenUnits;
        }
        if ("interstitial".equals(format)) {
            return network.interstitialUnits;
        }
        if ("rewarded".equals(format)) {
            return network.rewardedUnits;
        }
        if ("native".equals(format)) {
            return network.nativeUnits;
        }
        if ("banner".equals(format)) {
            return network.bannerUnits;
        }
        return null;
    }

    static String pickWeighted(List<WeightedUnit> units) {
        if (units == null || units.isEmpty()) {
            return null;
        }
        List<WeightedUnit> valid = new ArrayList<>();
        int total = 0;
        for (WeightedUnit unit : units) {
            if (unit == null || unit.id == null || unit.id.trim().isEmpty() || unit.percent <= 0) {
                continue;
            }
            valid.add(unit);
            total += unit.percent;
        }
        if (valid.isEmpty()) {
            return null;
        }
        if (total <= 0) {
            return valid.get(0).id.trim();
        }
        int roll = RANDOM.nextInt(total);
        int cursor = 0;
        for (WeightedUnit unit : valid) {
            cursor += unit.percent;
            if (roll < cursor) {
                return unit.id.trim();
            }
        }
        return valid.get(valid.size() - 1).id.trim();
    }

    /** Build fetch URL from API base (CashPop-style remote config). */
    public static String resolveConfigEndpoint(String baseOrFull) {
        return resolveApiPath(baseOrFull, "ads_config.php");
    }

    public static String resolveApiPath(String baseOrFull, String path) {
        if (baseOrFull == null || baseOrFull.trim().isEmpty()) {
            return DEFAULT_API_BASE() + path;
        }
        String url = baseOrFull.trim();
        if (url.endsWith(path) || url.contains("/" + path)) {
            return url;
        }
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url + path;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static JsonObject object(JsonObject root, String key) {
        JsonElement element = root.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }
}
