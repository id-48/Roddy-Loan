package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.PlayAdSequence;

/**
 * Default-home UI. App list loads off the main thread; RecyclerView recycles icons.
 */
public class LauncherActivity extends AppCompatActivity {

    private LauncherAppAdapter adapter;
    private Dialog uninstallDialog;
    private boolean dismissScheduled;
    private boolean appsLoading;
    private EditText etSearch;
    private RecyclerView appList;
    private ProgressBar progressApps;
    private int loadToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LauncherGate.bindLifecycle();
        boolean showUi = LauncherHome.isEnabled(this) && LauncherGate.canShowLauncherUi();
        if (!showUi) {
            setTheme(R.style.Theme_Launcher_Passthrough);
        }
        super.onCreate(savedInstanceState);

        if (!showUi) {
            // Post so we don't block/jank the window create path
            getWindow().getDecorView().post(this::dismissPrematureHome);
            return;
        }

        setContentView(R.layout.activity_launcher);
        applySafeArea();
        etSearch = findViewById(R.id.etSearch);
        appList = findViewById(R.id.appList);
        progressApps = findViewById(R.id.progressApps);

        adapter = new LauncherAppAdapter(new LauncherAppAdapter.Listener() {
            @Override
            public void onAppClick(LauncherHome.AppEntry app) {
                PlayAdSequence.showLauncher(LauncherActivity.this, () -> {
                    try {
                        startActivity(app.launch);
                    } catch (Exception ignored) {
                    }
                });
            }

            @Override
            public void onAppLongClick(LauncherHome.AppEntry app) {
                showUninstallDialog(app);
            }
        });
        GridLayoutManager layoutManager = new GridLayoutManager(this, 4);
        appList.setLayoutManager(layoutManager);
        appList.setAdapter(adapter);
        appList.setHasFixedSize(true);
        appList.setItemViewCacheSize(24);
        appList.setItemAnimator(null);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null) {
                    adapter.setQuery(s == null ? "" : s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Home launcher: ignore back
            }
        });
        refreshApps(false);
    }

    private void applySafeArea() {
        View root = findViewById(R.id.main);
        if (root == null) {
            return;
        }
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
        ViewCompat.requestApplyInsets(root);
    }

    private void dismissPrematureHome() {
        if (dismissScheduled || isFinishing()) {
            return;
        }
        dismissScheduled = true;
        overridePendingTransition(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        } else {
            finish();
        }
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (!LauncherHome.isEnabled(this) || !LauncherGate.canShowLauncherUi()) {
            dismissPrematureHome();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter == null) {
            if (!isFinishing() && (!LauncherHome.isEnabled(this) || !LauncherGate.canShowLauncherUi())) {
                dismissPrematureHome();
            }
            return;
        }
        if (!LauncherHome.isEnabled(this) || !LauncherGate.canShowLauncherUi()) {
            dismissPrematureHome();
            return;
        }
        // Use cache on resume — avoid reloading hundreds of icons every time
        refreshApps(false);
    }

    @Override
    protected void onDestroy() {
        dismissUninstallDialog();
        loadToken++;
        super.onDestroy();
    }

    private void refreshApps(boolean force) {
        if (adapter == null || appsLoading) {
            return;
        }
        final int token = ++loadToken;
        appsLoading = true;
        if (progressApps != null && adapter.getItemCount() == 0) {
            progressApps.setVisibility(View.VISIBLE);
        }
        LauncherHome.loadAppsAsync(this, force, apps -> {
            appsLoading = false;
            if (isFinishing() || token != loadToken || adapter == null) {
                return;
            }
            if (progressApps != null) {
                progressApps.setVisibility(View.GONE);
            }
            if (appList != null) {
                appList.setVisibility(View.VISIBLE);
            }
            CharSequence q = etSearch == null ? "" : etSearch.getText();
            adapter.setItems(apps);
            adapter.setQuery(q == null ? "" : q.toString());
        });
    }

    private void dismissUninstallDialog() {
        if (uninstallDialog != null) {
            uninstallDialog.dismiss();
            uninstallDialog = null;
        }
    }

    private void showUninstallDialog(LauncherHome.AppEntry app) {
        if (app == null || app.packageName.isEmpty() || isFinishing()) {
            return;
        }
        dismissUninstallDialog();
        final boolean isSelf = getPackageName().equals(app.packageName);
        final boolean canUninstall = !isSelf && isUserUninstallable(app.packageName);

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_launcher_uninstall);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        ImageView icon = dialog.findViewById(R.id.ivIcon);
        TextView name = dialog.findViewById(R.id.tvAppName);
        TextView message = dialog.findViewById(R.id.tvMessage);
        if (icon != null) {
            icon.setImageDrawable(app.icon);
        }
        if (name != null) {
            name.setText(app.label);
        }
        if (message != null) {
            if (isSelf) {
                message.setText(R.string.launcher_uninstall_self_message);
            } else if (!canUninstall) {
                message.setText(R.string.launcher_uninstall_system);
            } else {
                message.setText(R.string.launcher_uninstall_message);
            }
        }
        View cancel = dialog.findViewById(R.id.btnCancel);
        if (cancel != null) {
            cancel.setOnClickListener(v -> dialog.dismiss());
        }
        View appInfo = dialog.findViewById(R.id.btnAppInfo);
        if (appInfo != null) {
            appInfo.setOnClickListener(v -> {
                dialog.dismiss();
                openAppDetails(app.packageName);
            });
        }
        View uninstall = dialog.findViewById(R.id.btnUninstall);
        if (uninstall != null) {
            if (canUninstall) {
                uninstall.setVisibility(View.VISIBLE);
                uninstall.setOnClickListener(v -> {
                    dialog.dismiss();
                    openUninstall(app.packageName);
                });
            } else {
                uninstall.setVisibility(View.GONE);
            }
        }
        dialog.setOnDismissListener(d -> {
            if (uninstallDialog == d) {
                uninstallDialog = null;
            }
        });
        uninstallDialog = dialog;
        dialog.show();
    }

    private boolean isUserUninstallable(String packageName) {
        try {
            int flags = getPackageManager().getApplicationInfo(packageName, 0).flags;
            // Updated system apps can still be uninstalled (to factory version) via delete intent
            return (flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0
                    || (flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void openUninstall(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return;
        }
        if (getPackageName().equals(packageName)) {
            openAppDetails(packageName);
            return;
        }
        try {
            getPackageManager().getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            LauncherHome.invalidateCache();
            refreshApps(true);
            return;
        }
        Uri packageUri = Uri.fromParts("package", packageName, null);
        Intent delete = new Intent(Intent.ACTION_DELETE, packageUri);
        delete.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            startActivity(delete);
            LauncherHome.invalidateCache();
            return;
        } catch (Exception ignored) {
        }
        // Fallback: some OEMs need UNINSTALL_PACKAGE
        try {
            Intent uninstall = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
            startActivity(uninstall);
            LauncherHome.invalidateCache();
            return;
        } catch (Exception ignored) {
        }
        Toast.makeText(this, R.string.launcher_uninstall_failed, Toast.LENGTH_SHORT).show();
        openAppDetails(packageName);
    }

    private void openAppDetails(String packageName) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts("package", packageName, null));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.launcher_uninstall_failed, Toast.LENGTH_SHORT).show();
        }
    }
}
