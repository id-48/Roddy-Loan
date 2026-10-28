package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;
import android.view.WindowManager;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

/**
 * Center card with "Ads Loading..." while fullscreen ads load (loan EMI / Contacts style).
 */
final class AdLoadingDialog {
    private Dialog dialog;

    void show(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        if (dialog != null && dialog.isShowing()) {
            return;
        }
        dismiss();
        Dialog created = new Dialog(activity);
        created.requestWindowFeature(Window.FEATURE_NO_TITLE);
        created.setContentView(R.layout.dialog_ad_loading);
        created.setCancelable(false);
        created.setCanceledOnTouchOutside(false);
        Window window = created.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.45f);
        }
        try {
            created.show();
            dialog = created;
        } catch (Exception ignored) {
            dialog = null;
        }
    }

    void dismiss() {
        if (dialog != null) {
            try {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception ignored) {
            }
        }
        dialog = null;
    }
}
