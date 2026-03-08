package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.R;
import com.bookmap.app.model.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private final List<User> users;
    private OnUserClickListener listener;
    private boolean showCheckbox;
    private final Set<Long> selectedUserIds = new HashSet<>();

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(List<User> users, OnUserClickListener listener, boolean showCheckbox) {
        this.users = users;
        this.listener = listener;
        this.showCheckbox = showCheckbox;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getName());
        holder.tvGenres.setText(user.getFavoriteGenres() != null ? user.getFavoriteGenres() : "");

        if (showCheckbox) {
            holder.checkSelect.setVisibility(View.VISIBLE);
            holder.checkSelect.setChecked(selectedUserIds.contains(user.getId()));
            holder.checkSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedUserIds.add(user.getId());
                } else {
                    selectedUserIds.remove(user.getId());
                }
            });
        } else {
            holder.checkSelect.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (showCheckbox) {
                holder.checkSelect.setChecked(!holder.checkSelect.isChecked());
            } else if (listener != null) {
                listener.onUserClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void updateData(List<User> newUsers) {
        users.clear();
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    public List<Long> getSelectedUserIds() {
        return new ArrayList<>(selectedUserIds);
    }

    public int getSelectedCount() {
        return selectedUserIds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvGenres;
        CheckBox checkSelect;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvGenres = itemView.findViewById(R.id.tvUserGenres);
            checkSelect = itemView.findViewById(R.id.checkSelect);
        }
    }
}
