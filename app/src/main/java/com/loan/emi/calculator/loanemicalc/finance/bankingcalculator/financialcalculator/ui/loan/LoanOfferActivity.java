package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.loan;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.util.MoneyFormat;

public class LoanOfferActivity extends BaseActivity {
    private int selectedIndex = 0;
    private MaterialCardView[] cards;
    private String[] banks;
    private double[] rates;
    private double[] emis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_loan_offer);
        setupToolbar("Choose Offer", true);

        View fetching = findViewById(R.id.layoutFetching);
        View main = findViewById(R.id.layoutMainContent);
        if (main != null) main.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (fetching != null) fetching.setVisibility(View.GONE);
            if (main != null) main.setVisibility(View.VISIBLE);
            TextView h = findViewById(R.id.tvHeadline);
            if (h != null) h.setText("Choose an Offer");
            buildOffers();
        }, 1200);

        View accept = findViewById(R.id.btnAccept);
        if (accept != null) {
            accept.setOnClickListener(v -> {
                if (banks == null || banks.length == 0) {
                    Toast.makeText(this, "Please wait for offers", Toast.LENGTH_SHORT).show();
                    return;
                }
                LoanSession.selectedBank = banks[selectedIndex];
                LoanSession.interestRate = rates[selectedIndex];
                LoanSession.monthlyEmi = emis[selectedIndex];
                LoanSession.applicationComplete = true;
                if (LoanSession.submissionTime <= 0L) {
                    LoanSession.submissionTime = System.currentTimeMillis();
                }
                LoanSession.lastNotifiedTrackerStep = 0;
                LoanSession.save(this);
                markStepAndFinishToHome();
            });
        }
    }

    private void buildOffers() {
        LinearLayout container = findViewById(R.id.containerOffers);
        if (container == null) return;
        container.removeAllViews();

        banks = new String[]{
                LoanSession.selectedBank.isEmpty() ? "HDFC Bank" : LoanSession.selectedBank,
                "ICICI Bank",
                "State Bank of India"
        };
        rates = new double[]{
                LoanSession.interestRate > 0 ? LoanSession.interestRate : 10.5,
                (LoanSession.interestRate > 0 ? LoanSession.interestRate : 10.5) + 0.4,
                (LoanSession.interestRate > 0 ? LoanSession.interestRate : 10.5) + 0.9
        };
        emis = new double[3];
        for (int i = 0; i < 3; i++) {
            emis[i] = LoanSession.monthlyEmi > 0
                    ? LoanSession.monthlyEmi * (1 + i * 0.015)
                    : LoanSession.loanAmount / Math.max(1, LoanSession.tenureMonths);
        }

        cards = new MaterialCardView[3];
        for (int i = 0; i < 3; i++) {
            final int index = i;
            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
            card.setLayoutParams(lp);
            card.setRadius(16 * getResources().getDisplayMetrics().density);
            card.setCardElevation(0);
            card.setStrokeWidth((int) (1.5f * getResources().getDisplayMetrics().density));
            card.setStrokeColor(Color.parseColor("#E2E8F0"));
            card.setCardBackgroundColor(Color.WHITE);

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            int pad = (int) (16 * getResources().getDisplayMetrics().density);
            inner.setPadding(pad, pad, pad, pad);

            TextView bank = new TextView(this);
            bank.setText(banks[i]);
            bank.setTextSize(17);
            bank.setTypeface(bank.getTypeface(), android.graphics.Typeface.BOLD);
            bank.setTextColor(Color.parseColor("#0F172A"));

            TextView meta = new TextView(this);
            meta.setText(String.format("%.1f%% p.a.  ·  EMI %s", rates[i], MoneyFormat.format(this, emis[i])));
            meta.setTextSize(14);
            meta.setTextColor(Color.parseColor("#64748B"));
            meta.setPadding(0, (int) (6 * getResources().getDisplayMetrics().density), 0, 0);

            TextView amount = new TextView(this);
            amount.setText("Loan " + MoneyFormat.format(this, LoanSession.loanAmount));
            amount.setTextSize(13);
            amount.setTextColor(getResources().getColor(R.color.colorPrimary));
            amount.setPadding(0, (int) (8 * getResources().getDisplayMetrics().density), 0, 0);

            if (i == 0) {
                TextView badge = new TextView(this);
                badge.setText("Recommended");
                badge.setTextSize(11);
                badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);
                badge.setTextColor(Color.WHITE);
                badge.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                int bp = (int) (6 * getResources().getDisplayMetrics().density);
                badge.setPadding(bp * 2, bp, bp * 2, bp);
                badge.setGravity(Gravity.CENTER);
                LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                blp.bottomMargin = (int) (8 * getResources().getDisplayMetrics().density);
                badge.setLayoutParams(blp);
                inner.addView(badge);
            }

            inner.addView(bank);
            inner.addView(meta);
            inner.addView(amount);
            card.addView(inner);
            card.setOnClickListener(v -> selectOffer(index));
            container.addView(card);
            cards[i] = card;

            // Native after 1st offer card (index 0)
            if (i == 0) {
                View adInclude = LayoutInflater.from(this)
                        .inflate(R.layout.put_big_nativead, container, false);
                LinearLayout.LayoutParams adLp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                adLp.topMargin = (int) (4 * getResources().getDisplayMetrics().density);
                adLp.bottomMargin = (int) (12 * getResources().getDisplayMetrics().density);
                adInclude.setLayoutParams(adLp);
                container.addView(adInclude);
                ViewGroup adContainer = adInclude.findViewById(R.id.put_big_nativead_container);
                if (adContainer == null && adInclude instanceof ViewGroup) {
                    adContainer = (ViewGroup) adInclude;
                }
                if (adContainer != null) {
                    AdsManager.get().loadNativeInto(this, adContainer);
                }
            }
        }
        selectOffer(0);
    }

    private void selectOffer(int index) {
        selectedIndex = index;
        if (cards == null) return;
        for (int i = 0; i < cards.length; i++) {
            if (cards[i] == null) continue;
            boolean sel = i == index;
            cards[i].setStrokeColor(sel
                    ? getResources().getColor(R.color.colorPrimary)
                    : Color.parseColor("#E2E8F0"));
            cards[i].setStrokeWidth((int) ((sel ? 2.5f : 1.5f) * getResources().getDisplayMetrics().density));
            cards[i].setCardBackgroundColor(sel ? Color.parseColor("#F0FDFA") : Color.WHITE);
        }
    }
}
