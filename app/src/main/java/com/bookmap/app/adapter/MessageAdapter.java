package com.bookmap.app.adapter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import com.bookmap.app.model.Message;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messages;
    private long currentUserId;
    private OnMessageDeleteListener deleteListener;

    public interface OnMessageDeleteListener {
        void onDeleteClick(Message message, int position);
    }

    public MessageAdapter(List<Message> messages, long currentUserId, OnMessageDeleteListener listener) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.deleteListener = listener;
    }

    public void updateData(List<Message> newMessages) {
        this.messages = newMessages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messages.get(position);
        holder.tvMessageContent.setText(msg.getContent());
        
        // Format timestamp (just extracting time part for simplicity)
        String time = msg.getTimestamp();
        if (time.length() >= 16) {
            time = time.substring(11, 16); // Extract HH:mm from "YYYY-MM-DD HH:mm:ss"
        }
        holder.tvMessageTime.setText(time);

        GradientDrawable bgShape = new GradientDrawable();
        bgShape.setCornerRadius(16f);
        
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.layoutMessageBubble.getLayoutParams();

        if (msg.getSenderId() == currentUserId) {
            // Sent by me -> Blue bubble on the right
            bgShape.setColor(holder.itemView.getContext().getResources().getColor(R.color.blue_primary));
            holder.tvMessageContent.setTextColor(Color.WHITE);
            holder.tvMessageTime.setTextColor(Color.WHITE);
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            layoutParams.gravity = Gravity.END;
        } else {
            // Received -> Gray bubble on the left
            bgShape.setColor(holder.itemView.getContext().getResources().getColor(R.color.gray_bg));
            holder.tvMessageContent.setTextColor(Color.BLACK);
            holder.tvMessageTime.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.gray_text));
            holder.itemView.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
            layoutParams.gravity = Gravity.START;
        }

        holder.layoutMessageBubble.setBackground(bgShape);
        holder.layoutMessageBubble.setLayoutParams(layoutParams);

        holder.imgDeleteMessage.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onDeleteClick(msg, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutMessageBubble;
        TextView tvMessageContent, tvMessageTime;
        android.widget.ImageView imgDeleteMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutMessageBubble = itemView.findViewById(R.id.layoutMessageBubble);
            tvMessageContent = itemView.findViewById(R.id.tvMessageContent);
            tvMessageTime = itemView.findViewById(R.id.tvMessageTime);
            imgDeleteMessage = itemView.findViewById(R.id.imgDeleteMessage);
        }
    }
}
