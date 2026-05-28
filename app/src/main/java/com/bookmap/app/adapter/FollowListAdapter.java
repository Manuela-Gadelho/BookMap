package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import com.bookmap.app.model.User;
import com.bookmap.app.util.PhotoHelper;
import java.util.List;

public class FollowListAdapter extends RecyclerView.Adapter<FollowListAdapter.ViewHolder> {
    private final List<User> users;
    private final String listType; 
    private final FollowActionListener listener;

    public interface FollowActionListener {
        void onActionClick(User user, String listType);
        void onUserClick(User user);
    }

    public FollowListAdapter(List<User> users, String listType, FollowActionListener listener) {
        this.users = users;
        this.listType = listType;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_follow, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());

        if (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty()) {
            PhotoHelper.loadImageIntoView(holder.imgUserAvatar, user.getPhotoPath());
        } else {
            holder.imgUserAvatar.setImageResource(R.drawable.ic_default_user);
        }

        if ("followers".equals(listType)) {
            holder.btnAction.setText("REMOVER");
        } else {
            holder.btnAction.setText("DEIXAR DE SEGUIR");
        }

        holder.btnAction.setOnClickListener(v -> {
            if (listener != null) listener.onActionClick(user, listType);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onUserClick(user);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgUserAvatar;
        TextView tvUserName, tvUserEmail;
        Button btnAction;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUserAvatar = itemView.findViewById(R.id.imgUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            btnAction = itemView.findViewById(R.id.btnAction);
        }
    }
}
