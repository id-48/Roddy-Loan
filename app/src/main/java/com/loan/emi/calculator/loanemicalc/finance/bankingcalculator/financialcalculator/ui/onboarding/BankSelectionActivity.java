package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.onboarding;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.data.LoanSession;

public class BankSelectionActivity extends BaseActivity {
    private static final String[] BANKS = {
            "HDFC Bank", "ICICI Bank", "SBI", "Axis Bank", "Kotak Mahindra",
            "Bank of Baroda", "Punjab National Bank", "Yes Bank"
    };
    private int selected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_bank_selection);
        setupToolbar("Select Bank", true);
        setupProgressHeader();
        TextView h = findViewById(R.id.tvHeadline);
        if (h != null) h.setText("Choose Preferred Bank");
        TextView s = findViewById(R.id.tvSubtext);
        if (s != null) s.setText("Select a bank for your loan application");
        RecyclerView rv = findViewById(R.id.rvBanks);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new Adapter());
        findViewById(R.id.btnNext).setOnClickListener(v -> {
            LoanSession.selectedBank = BANKS[selected];
            LoanSession.save(this);
            goNext(true);
        });
    }

    class Adapter extends RecyclerView.Adapter<Adapter.VH> {
        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pl_bank, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.name.setText(BANKS[position]);
            holder.check.setVisibility(position == selected ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> {
                selected = holder.getAdapterPosition();
                notifyDataSetChanged();
            });
        }

        @Override
        public int getItemCount() {
            return BANKS.length;
        }

        class VH extends RecyclerView.ViewHolder {
            TextView name;
            ImageView check;

            VH(View v) {
                super(v);
                name = v.findViewById(R.id.tvBankName);
                check = v.findViewById(R.id.ivCheck);
            }
        }
    }
}
