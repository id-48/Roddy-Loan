package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

/**
 * Native-backed API base. Plaintext URL is not stored in Java/assets.
 */
public final class SecureApi {
    private static volatile String cached;
    private static final boolean LOADED;

    static {
        boolean ok = false;
        try {
            System.loadLibrary("emi_secure");
            ok = true;
        } catch (UnsatisfiedLinkError ignored) {
            ok = false;
        }
        LOADED = ok;
    }

    private SecureApi() {
    }

    public static String baseUrl() {
        if (cached != null) {
            return cached;
        }
        synchronized (SecureApi.class) {
            if (cached != null) {
                return cached;
            }
            String url = null;
            if (LOADED) {
                try {
                    url = nativeBaseUrl();
                } catch (Throwable ignored) {
                    url = null;
                }
            }
            if (url == null || url.trim().isEmpty()) {
                // Last-resort empty — callers should treat empty as misconfig
                url = "";
            }
            if (!url.isEmpty() && !url.endsWith("/")) {
                url = url + "/";
            }
            cached = url;
            return cached;
        }
    }

    private static native String nativeBaseUrl();
}
