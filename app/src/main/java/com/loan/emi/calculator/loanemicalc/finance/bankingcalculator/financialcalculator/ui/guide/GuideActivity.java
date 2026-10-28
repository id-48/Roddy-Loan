package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class GuideActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_guide);
        String title = getIntent().getStringExtra("title");
        if (title == null) {
            title = "Guide";
        }
        setupToolbar(title, true);
        setText(R.id.tvGuideSubtitle, title);
        String content = getIntent().getStringExtra("content");
        if (content == null || content.trim().isEmpty()) {
            content = GuideContent.forTitle(title);
        }
        setText(R.id.tvGuideContent, content);
        TextView note = findViewById(R.id.tvNote);
        if (note != null) {
            note.setText("This information is for educational purposes and is not a loan offer.");
        }
        View next = findViewById(R.id.btnNextPart);
        if (next != null) {
            next.setOnClickListener(v -> finish());
        }
    }
}
