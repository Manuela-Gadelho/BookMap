package com.bookmap.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.R;
import com.bookmap.app.model.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.tvUser.setText(review.getUserName());
        holder.tvRating.setText(review.getStars());
        holder.tvText.setText(review.getText());
        holder.tvDate.setText(review.getCreatedAt() != null ? review.getCreatedAt() : "");
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void updateData(List<Review> newReviews) {
        reviews.clear();
        reviews.addAll(newReviews);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUser, tvRating, tvText, tvDate;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvReviewUser);
            tvRating = itemView.findViewById(R.id.tvReviewRating);
            tvText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
        }
    }
}
