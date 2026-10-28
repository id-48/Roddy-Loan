package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.call.CallStateMonitor;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.AppPrefs;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.LocaleHelper;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

public class LanguageActivity extends BaseActivity {
    private static final String EXTRA_PROCEED_AFTER_LOCALE = "proceed_after_locale";

    private final List<ImageView> checks = new ArrayList<>();
    private String selected = LocaleHelper.DEFAULT_TAG;
    private boolean waitingPhonePermission;
    private boolean waitingOverlayPermission;

    /** localeTag, btn, check, label, flag, emoji, displayName */
    private static final Object[][] LANGS = {
            {"en", R.id.btnEnglish, R.id.ivCheckEnglish, R.id.tvOpt1, R.id.tvFlagEnglish, "🇬🇧", "English"},
            {"hi", R.id.btnHindi, R.id.ivCheckHindi, R.id.tvOpt2, R.id.tvFlagHindi, "🇮🇳", "हिंदी (Hindi)"},
            {"vi", R.id.btnVietnamese, R.id.ivCheckVietnamese, R.id.tvOptVietnamese, R.id.tvFlagViet, "🇻🇳", "Tiếng Việt"},
            {"ur", R.id.btnUrdu, R.id.ivCheckUrdu, R.id.tvOptUrdu, R.id.tvFlagUrdu, "🇵🇰", "اردو (Urdu)"},
            {"es", R.id.btnSpanish, R.id.ivCheckSpanish, R.id.tvOptSpanish, R.id.tvFlagSpan, "🇪🇸", "Español"},
            {"id", R.id.btnIndonesian, R.id.ivCheckIndonesian, R.id.tvOptIndonesian, R.id.tvFlagIndo, "🇮🇩", "Bahasa Indonesia"},
            {"fil", R.id.btnFilipino, R.id.ivCheckFilipino, R.id.tvOptFilipino, R.id.tvFlagFili, "🇵🇭", "Filipino"},
            {"sw", R.id.btnSwahili, R.id.ivCheckSwahili, R.id.tvOptSwahili, R.id.tvFlagSwah, "🇰🇪", "Kiswahili"},
            {"pt", R.id.btnPortuguese, R.id.ivCheckPortuguese, R.id.tvOptPortuguese, R.id.tvFlagPort, "🇵🇹", "Português"},
            {"bn", R.id.btnBengali, R.id.ivCheckBengali, R.id.tvOptBengali, R.id.tvFlagBeng, "🇧🇩", "বাংলা (Bengali)"},
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Locale apply recreates this screen — continue once after recreate (avoids Currency double-start)
        if (getIntent().getBooleanExtra(EXTRA_PROCEED_AFTER_LOCALE, false)) {
            getIntent().removeExtra(EXTRA_PROCEED_AFTER_LOCALE);
            if (getIntent().getBooleanExtra("from_settings", false)) {
                finish();
                return;
            }
            goNext(true);
            return;
        }

        setContentView(R.layout.activity_pl_language);
        setupToolbar(getString(R.string.language_screen_title), true);
        boolean fromSettings = getIntent().getBooleanExtra("from_settings", false);
        if (fromSettings) {
            View header = findViewById(R.id.plProgressHeaderRoot);
            if (header != null) header.setVisibility(View.GONE);
        } else {
            setupProgressHeader(getString(R.string.language_milestone));
        }
        setText(R.id.tvHeadline, getString(R.string.language_headline));
        setText(R.id.tvSubtext, getString(R.string.language_subtext));

        selected = LocaleHelper.toLanguageTag(AppPrefs.getLanguage(this));

        ImageView selectedCheck = findViewById(R.id.ivCheckEnglish);
        for (Object[] row : LANGS) {
            String tag = (String) row[0];
            ImageView check = bind(tag, (Integer) row[1], (Integer) row[2], (Integer) row[3], (Integer) row[4],
                    (String) row[5], (String) row[6]);
            if (tag.equals(selected) && check != null) {
                selectedCheck = check;
            }
        }

        View next = findViewById(R.id.btnNext);
        if (next != null) {
            if (next instanceof TextView) {
                ((TextView) next).setText(R.string.btn_next);
            }
            next.setOnClickListener(v -> onNextClicked());
        }
        selectCheck(selectedCheck);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Back from Appear-on-top settings
        if (!waitingOverlayPermission) {
            return;
        }
        waitingOverlayPermission = false;
        if (PermissionHelper.hasAfterCallPermissions(this)) {
            confirmLanguage();
        } else if (!PermissionHelper.canDrawOverlays(this)) {
            Toast.makeText(this,
                    "Appear on top is required for Call Ended dialog. Turn it ON and tap Next again.",
                    Toast.LENGTH_LONG).show();
        } else {
            continueAfterCallPermissions();
        }
    }

    /**
     * Step 1 Next: always ask Phone + Appear on top (even if admin afterCall /
     * whatsappAfterCall is OFF) so permissions are ready when features turn ON.
     * No Call Log / Contacts.
     */
    private void onNextClicked() {
        if (getIntent().getBooleanExtra("from_settings", false)) {
            confirmLanguage();
            return;
        }
        continueAfterCallPermissions();
    }

    private void continueAfterCallPermissions() {
        if (!PermissionHelper.hasPhoneState(this)) {
            waitingPhonePermission = true;
            PermissionHelper.requestPhoneStateIfNeeded(this);
            return;
        }
        if (!PermissionHelper.canDrawOverlays(this)) {
            waitingOverlayPermission = true;
            Toast.makeText(this,
                    "Enable Appear on top so Call Ended dialog can show after calls.",
                    Toast.LENGTH_LONG).show();
            PermissionHelper.requestOverlayIfNeeded(this);
            return;
        }
        confirmLanguage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PermissionHelper.REQ_PHONE_STATE || !waitingPhonePermission) {
            return;
        }
        waitingPhonePermission = false;
        if (PermissionHelper.hasPhoneState(this)) {
            continueAfterCallPermissions();
            return;
        }
        boolean anyDeniedForever = false;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED
                    && !shouldShowRequestPermissionRationale(permissions[i])) {
                anyDeniedForever = true;
                break;
            }
        }
        Toast.makeText(this,
                anyDeniedForever
                        ? "Phone permission required for after-call. Enable in Settings."
                        : "Allow Phone permission to continue (needed for after-call).",
                Toast.LENGTH_LONG).show();
    }

    private void confirmLanguage() {
        // Start monitor whenever phone permission exists — feature flags gate the UI later
        CallStateMonitor.ensureStarted(this);
        String previous = LocaleHelper.toLanguageTag(AppPrefs.getLanguage(this));
        AppPrefs.setLanguage(this, selected);
        boolean fromSettings = getIntent().getBooleanExtra("from_settings", false);

        if (!selected.equals(previous)) {
            // Apply locale first; navigate only after recreate so Currency binds once
            getIntent().putExtra(EXTRA_PROCEED_AFTER_LOCALE, true);
            LocaleHelper.apply(this, selected);
            return;
        }

        if (fromSettings) {
            finish();
        } else {
            goNext(true);
        }
    }

    private ImageView bind(String langKey, int btn, int check, int label, int flag, String flagEmoji, String displayName) {
        View b = findViewById(btn);
        ImageView c = findViewById(check);
        TextView t = findViewById(label);
        TextView f = findViewById(flag);
        if (t != null) {
            t.setText(displayName);
            t.setVisibility(View.VISIBLE);
        }
        if (f != null) {
            f.setText(flagEmoji);
            f.setVisibility(View.VISIBLE);
            f.setTextSize(22f);
        }
        if (c != null) {
            checks.add(c);
            c.setVisibility(View.GONE);
        }
        if (b != null) {
            b.setOnClickListener(v -> {
                selected = langKey;
                selectCheck(c);
            });
        }
        return c;
    }

    private void selectCheck(ImageView selectedIv) {
        for (ImageView iv : checks) {
            if (iv != null) {
                iv.setVisibility(iv == selectedIv ? View.VISIBLE : View.GONE);
            }
        }
    }
}
