package com.bookmap.app.adapter;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.R;
import com.bookmap.app.util.SessionManager;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.ReviewComment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private final List<ReviewComment> comments;

    public CommentAdapter(List<ReviewComment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReviewComment comment = comments.get(position);
        holder.tvUserName.setText(comment.getUserName());
        holder.tvText.setText(comment.getText());
        holder.tvDate.setText(comment.getTimestamp() != null ? comment.getTimestamp() : "");

        SessionManager session = new SessionManager(holder.itemView.getContext());
        
        // Regra de exclusão: se o comentário for da pessoa logada, ela pode excluir pressionando e segurando.
        holder.itemView.setOnLongClickListener(v -> {
            if (comment.getUserId() == session.getUserId()) {
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Excluir comentário")
                        .setMessage("Deseja apagar este comentário?")
                        .setPositiveButton("Apagar", (dialog, which) -> {
                            DatabaseHelper db = DatabaseHelper.getInstance(holder.itemView.getContext());
                            if (db.deleteReviewComment(comment.getId())) {
                                FirebaseSyncHelper.getInstance(holder.itemView.getContext()).softDeleteReviewCommentFromCloud(comment.getId());
                                comments.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, comments.size());
                                Toast.makeText(holder.itemView.getContext(), "Comentário apagado", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateData(List<ReviewComment> newComments) {
        comments.clear();
        comments.addAll(newComments);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserName, tvText, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tvCommentUserName);
            tvText = itemView.findViewById(R.id.tvCommentText);
            tvDate = itemView.findViewById(R.id.tvCommentDate);
        }
    }
}
