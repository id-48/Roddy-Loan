package com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.launcher;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LauncherAppAdapter extends RecyclerView.Adapter<LauncherAppAdapter.Holder> {

    public interface Listener {
        void onAppClick(LauncherHome.AppEntry app);

        void onAppLongClick(LauncherHome.AppEntry app);
    }

    private final Listener listener;
    private final List<LauncherHome.AppEntry> all = new ArrayList<>();
    private final List<LauncherHome.AppEntry> visible = new ArrayList<>();
    private String query = "";

    public LauncherAppAdapter(Listener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void setItems(List<LauncherHome.AppEntry> items) {
        all.clear();
        if (items != null) {
            all.addAll(items);
        }
        applyFilter();
    }

    public void setQuery(String value) {
        String next = value == null ? "" : value.trim().toLowerCase(Locale.US);
        if (next.equals(query)) {
            return;
        }
        query = next;
        applyFilter();
    }

    private void applyFilter() {
        visible.clear();
        if (query.isEmpty()) {
            visible.addAll(all);
        } else {
            for (LauncherHome.AppEntry app : all) {
                if (app.label.toLowerCase(Locale.US).contains(query)
                        || app.packageName.toLowerCase(Locale.US).contains(query)) {
                    visible.add(app);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return visible.get(position).packageName.hashCode();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_launcher_app, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        LauncherHome.AppEntry app = visible.get(position);
        holder.label.setText(app.label);
        if (app.icon != null) {
            holder.icon.setImageDrawable(app.icon);
        } else {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon);
        }
        holder.itemView.setClickable(true);
        holder.itemView.setLongClickable(true);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAppClick(app);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            v.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);
            if (listener != null) {
                listener.onAppLongClick(app);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return visible.size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView label;

        Holder(android.view.View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.ivIcon);
            label = itemView.findViewById(R.id.tvLabel);
        }
    }
}
