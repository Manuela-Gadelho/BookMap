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
import com.bookmap.app.model.User;
import java.util.List;
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    private final List<Object> notifications;
    private final NotificationActionListener listener;
    public interface NotificationActionListener {
        void onApproveClubMember(ClubMember member);
        void onRejectClubMember(ClubMember member);
        void onApproveFollower(User follower);
        void onRejectFollower(User follower);
    }
    public NotificationAdapter(List<Object> notifications, NotificationActionListener listener) {
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
        Object item = notifications.get(position);
        if (item instanceof ClubMember) {
            ClubMember member = (ClubMember) item;
            holder.tvUserName.setText(member.getUserName() != null ? member.getUserName() : "Usuário");
            holder.tvClubName.setText("Clube: " + (member.getClubName() != null ? member.getClubName() : ""));
            holder.tvStatus.setText("Solicitação pendente (Clube)");
            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApproveClubMember(member);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onRejectClubMember(member);
            });
        } else if (item instanceof User) {
            User follower = (User) item;
            holder.tvUserName.setText(follower.getName());
            holder.tvClubName.setText("Quer seguir seu perfil");
            holder.tvStatus.setText("Solicitação pendente (Perfil)");
            holder.btnApprove.setOnClickListener(v -> {
                if (listener != null) listener.onApproveFollower(follower);
            });
            holder.btnReject.setOnClickListener(v -> {
                if (listener != null) listener.onRejectFollower(follower);
            });
        }
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

