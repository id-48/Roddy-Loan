package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.settings;

import android.os.Bundle;
import android.widget.TextView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class PrivacyPolicyActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_privacy);
        setupToolbar("Privacy Policy", true);
        TextView body = findViewById(R.id.tvPrivacyBody);
        if (body != null) {
            body.setText(
                    "Personal EMI Loan Calculator respects your privacy.\n\n"
                            + "1. Information we collect\n"
                            + "We store loan application details you enter (name, contact, income, loan amount) only on your device to show EMI estimates and application status.\n\n"
                            + "2. How we use data\n"
                            + "Data is used solely to calculate EMI, show loan status, and personalize calculator results. We do not sell your personal information.\n\n"
                            + "3. Storage\n"
                            + "Preferences such as language and currency are saved locally on your phone.\n\n"
                            + "4. Third parties\n"
                            + "This build does not send your loan data to external servers for underwriting.\n\n"
                            + "5. Contact\n"
                            + "For privacy questions, use Share App from Settings to reach support channels provided by the publisher.\n\n"
                            + "By using this app you agree to this policy."
            );
        }
    }
}
