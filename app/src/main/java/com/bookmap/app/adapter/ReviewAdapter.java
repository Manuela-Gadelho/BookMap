package com.bookmap.app.adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.R;
import com.bookmap.app.model.Review;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.bookmap.app.ReviewCommentsActivity;
import com.bookmap.app.util.SessionManager;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
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
        holder.tvDate.setText(review.getCreatedAt() != null ? com.bookmap.app.util.DateUtil.formatToBrazilian(review.getCreatedAt()) : "");

        DatabaseHelper db = DatabaseHelper.getInstance(holder.itemView.getContext());
        SessionManager session = new SessionManager(holder.itemView.getContext());
        long myUserId = session.getUserId();
        long reviewId = review.getId();

        // Count logic
        int likeCount = db.getReviewLikesCount(reviewId);
        int commentCount = db.getReviewCommentsCount(reviewId);
        boolean isLiked = db.hasUserLikedReview(reviewId, myUserId);

        holder.tvLikeCount.setText(String.valueOf(likeCount));
        holder.tvCommentCount.setText(String.valueOf(commentCount));
        holder.ivLikeIcon.setImageResource(isLiked ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);

        holder.layoutLike.setOnClickListener(v -> {
            boolean likedNow = db.toggleReviewLike(reviewId, myUserId);
            int newCount = db.getReviewLikesCount(reviewId);
            holder.tvLikeCount.setText(String.valueOf(newCount));
            holder.ivLikeIcon.setImageResource(likedNow ? R.drawable.ic_like_filled : R.drawable.ic_like_outline);
            FirebaseSyncHelper.getInstance(holder.itemView.getContext()).pushReviewLike(reviewId, myUserId, likedNow);
        });

        holder.layoutComment.setOnClickListener(v -> {
            Intent intent = new Intent(holder.itemView.getContext(), ReviewCommentsActivity.class);
            intent.putExtra("REVIEW_ID", reviewId);
            holder.itemView.getContext().startActivity(intent);
        });
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
        LinearLayout layoutLike, layoutComment;
        ImageView ivLikeIcon;
        TextView tvLikeCount, tvCommentCount;
        
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUser = itemView.findViewById(R.id.tvReviewUser);
            tvRating = itemView.findViewById(R.id.tvReviewRating);
            tvText = itemView.findViewById(R.id.tvReviewText);
            tvDate = itemView.findViewById(R.id.tvReviewDate);
            layoutLike = itemView.findViewById(R.id.layoutLike);
            layoutComment = itemView.findViewById(R.id.layoutComment);
            ivLikeIcon = itemView.findViewById(R.id.ivLikeIcon);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
        }
    }
}
