package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.button.MaterialButton;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Wraps only real button clicks for click_interval ads.
 * List / option / data-row taps are ignored (e.g. language &amp; currency selection).
 */
public final class ClickAdWrapper {
    private ClickAdWrapper() {
    }

    public static void wrap(Activity activity) {
        if (activity == null) {
            return;
        }
        View root = activity.findViewById(android.R.id.content);
        if (root != null) {
            walk(activity, root);
        }
    }

    private static void walk(Activity activity, View view) {
        if (view == null) {
            return;
        }
        if (isCountableButton(view)) {
            maybeWrap(activity, view);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                walk(activity, group.getChildAt(i));
            }
        }
    }

    private static boolean isCountableButton(View view) {
        return view instanceof MaterialButton
                || view instanceof Button
                || view instanceof ImageButton;
    }

    private static void maybeWrap(Activity activity, View view) {
        if (view.getTag(R.id.tag_ad_click_wrapped) != null) {
            return;
        }
        if (view.getTag(R.id.tag_ad_bypass_click) != null) {
            return;
        }
        View.OnClickListener original = peekClickListener(view);
        if (original == null) {
            return;
        }
        view.setTag(R.id.tag_ad_click_wrapped, Boolean.TRUE);
        view.setOnClickListener(v -> PlayAdSequence.show(activity, () -> original.onClick(v)));
    }

    private static View.OnClickListener peekClickListener(View view) {
        try {
            Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
            getListenerInfo.setAccessible(true);
            Object listenerInfo = getListenerInfo.invoke(view);
            if (listenerInfo == null) {
                return null;
            }
            Field field = listenerInfo.getClass().getDeclaredField("mOnClickListener");
            field.setAccessible(true);
            Object listener = field.get(listenerInfo);
            if (listener instanceof View.OnClickListener) {
                return (View.OnClickListener) listener;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
