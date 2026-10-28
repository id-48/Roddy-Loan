package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ui.guide;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.ads.AdsManager;
import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.base.BaseActivity;

public class SelectionActivity extends BaseActivity {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_AD = 1;
    private static final int AD_AFTER_INDEX = 3; // after 3 data rows

    private String type;
    private String[] items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pl_selection);
        setupToolbar("Guides", true);
        type = getIntent().getStringExtra("type");
        if (type == null) {
            type = "loans";
        }
        items = itemsFor(type);
        TextView title = findViewById(R.id.tvSelectionTitle);
        if (title != null) {
            title.setText(titleFor(type));
        }
        RecyclerView rv = findViewById(R.id.rvSelection);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new Adapter());
    }

    private String titleFor(String t) {
        switch (t) {
            case "epf":
                return "EPF Services";
            case "questions":
                return "Loan Guide";
            case "life_insurance":
                return "Life Insurance";
            case "general_insurance":
                return "General Insurance";
            default:
                return "All Loans";
        }
    }

    private String[] itemsFor(String t) {
        switch (t) {
            case "epf":
                return new String[]{"What is EPF", "How to withdraw", "EPF balance check", "Transfer EPF", "EPF interest rate"};
            case "questions":
                return new String[]{"How EMI works", "Tips to get loan", "Documents required", "CIBIL score tips", "Prepayment benefits"};
            case "life_insurance":
                return new String[]{"Term Insurance", "Endowment", "ULIP", "Whole Life"};
            case "general_insurance":
                return new String[]{"Health Insurance", "Motor Insurance", "Home Insurance", "Travel Insurance"};
            default:
                return new String[]{"Personal Loan", "Home Loan", "Car Loan", "Education Loan", "Business Loan", "Gold Loan"};
        }
    }

    private boolean hasMidAd() {
        return items != null && items.length > AD_AFTER_INDEX;
    }

    /** Returns item index, or -1 for the mid-list native ad row. */
    private int dataIndexFor(int adapterPos) {
        if (!hasMidAd()) {
            return adapterPos;
        }
        if (adapterPos < AD_AFTER_INDEX) {
            return adapterPos;
        }
        if (adapterPos == AD_AFTER_INDEX) {
            return -1;
        }
        return adapterPos - 1;
    }

    class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public int getItemViewType(int position) {
            return dataIndexFor(position) < 0 ? TYPE_AD : TYPE_ITEM;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_AD) {
                View ad = inflater.inflate(R.layout.put_big_nativead, parent, false);
                return new AdVH(ad);
            }
            View view = inflater.inflate(R.layout.item_guide_row, parent, false);
            return new ItemVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AdVH) {
                AdVH adHolder = (AdVH) holder;
                if (!adHolder.loaded) {
                    adHolder.loaded = true;
                    ViewGroup container = adHolder.itemView.findViewById(R.id.put_big_nativead_container);
                    if (container == null && adHolder.itemView instanceof ViewGroup) {
                        container = (ViewGroup) adHolder.itemView;
                    }
                    if (container != null) {
                        AdsManager.get().loadNativeInto(SelectionActivity.this, container);
                    }
                }
                return;
            }
            int dataIndex = dataIndexFor(position);
            if (dataIndex < 0 || dataIndex >= items.length) {
                return;
            }
            ItemVH itemHolder = (ItemVH) holder;
            String title = items[dataIndex];
            itemHolder.title.setText(title);
            itemHolder.subtitle.setText(GuideContent.subtitleFor(title));
            itemHolder.icon.setText(String.valueOf(dataIndex + 1));
            itemHolder.itemView.setOnClickListener(v -> {
                Intent i = new Intent(SelectionActivity.this, GuideActivity.class);
                i.putExtra("title", title);
                i.putExtra("content", GuideContent.forTitle(title));
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            if (items == null) {
                return 0;
            }
            return items.length + (hasMidAd() ? 1 : 0);
        }

        class ItemVH extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView subtitle;
            final TextView icon;

            ItemVH(View v) {
                super(v);
                title = v.findViewById(R.id.tvGuideTitle);
                subtitle = v.findViewById(R.id.tvGuideSubtitle);
                icon = v.findViewById(R.id.tvGuideIcon);
            }
        }

        class AdVH extends RecyclerView.ViewHolder {
            boolean loaded;

            AdVH(View v) {
                super(v);
            }
        }
    }
}
