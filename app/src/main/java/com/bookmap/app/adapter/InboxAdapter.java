package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Message;
import com.bookmap.app.model.User;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.util.List;

public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.InboxViewHolder> {
    private List<Message> messages;
    private long currentUserId;
    private DatabaseHelper dbHelper;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(long otherUserId);
    }

    public InboxAdapter(List<Message> messages, long currentUserId, DatabaseHelper dbHelper, OnChatClickListener listener) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.dbHelper = dbHelper;
        this.listener = listener;
    }

    public void updateData(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_preview, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        Message msg = messages.get(position);
        
        long otherUserId = (msg.getSenderId() == currentUserId) ? msg.getReceiverId() : msg.getSenderId();
        User otherUser = dbHelper.getUserById(otherUserId);
        
        if (otherUser != null) {
            holder.tvContactName.setText(otherUser.getName());
            if (otherUser.getPhotoPath() != null && !otherUser.getPhotoPath().isEmpty()) {
                Glide.with(holder.itemView.getContext())
                     .load(otherUser.getPhotoPath())
                     .transform(new CircleCrop())
                     .into(holder.imgContactPhoto);
            } else {
                holder.imgContactPhoto.setImageResource(R.mipmap.ic_launcher_round);
            }
        } else {
            holder.tvContactName.setText("Usuário Desconhecido");
        }

        holder.tvLastMessage.setText(msg.getContent());
        
        String time = msg.getTimestamp();
        if (time.length() >= 16) {
            time = time.substring(11, 16);
        }
        holder.tvTimestamp.setText(time);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(otherUserId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class InboxViewHolder extends RecyclerView.ViewHolder {
        ImageView imgContactPhoto;
        TextView tvContactName, tvLastMessage, tvTimestamp;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            imgContactPhoto = itemView.findViewById(R.id.imgContactPhoto);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
