package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.R;
import com.bookmap.app.model.ClubMember;

import java.util.List;

/**
 * Adapter for displaying pending club membership notifications.
 * Shows member name, club name, and approve/reject buttons.
 */
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final List<ClubMember> notifications;
    private final NotificationActionListener listener;

    public interface NotificationActionListener {
        void onApprove(ClubMember member);
        void onReject(ClubMember member);
    }

    public NotificationAdapter(List<ClubMember> notifications, NotificationActionListener listener) {
        this.notifications = notifications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClubMember member = notifications.get(position);
        holder.tvUserName.setText(member.getUserName() != null ? member.getUserName() : "Usuario");
        holder.tvClubName.setText("Clube: " + (member.getClubName() != null ? member.getClubName() : ""));
        holder.tvStatus.setText("Solicitacao pendente");

        holder.btnApprove.setOnClickListener(v -> {
            if (listener != null) listener.onApprove(member);
        });
        holder.btnReject.setOnClickListener(v -> {
            if (listener != null) listener.onReject(member);
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvClubName, tvStatus;
        Button btnApprove, btnReject;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvClubName = itemView.findViewById(R.id.tvClubName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}
