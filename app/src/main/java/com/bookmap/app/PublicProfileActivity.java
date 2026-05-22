package com.bookmap.app;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bookmap.app.adapter.ReviewAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.Review;
import com.bookmap.app.model.User;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.SessionManager;
import java.util.List;
public class PublicProfileActivity extends AppCompatActivity {
    public static final String EXTRA_USER_ID = "user_id";
    private DatabaseHelper dbHelper;
    private FirebaseSyncHelper syncHelper;
    private SessionManager session;
    private boolean isFollowing = false;
    private long currentUserId;
    private long profileUserId;
    private TextView tvFollowersCount, tvFollowingCount;
    private Button btnFollow;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_profile);
        dbHelper = DatabaseHelper.getInstance(this);
        syncHelper = FirebaseSyncHelper.getInstance(this);
        session = new SessionManager(this);
        profileUserId = getIntent().getLongExtra(EXTRA_USER_ID, -1);
        if (profileUserId == -1) {
            finish();
            return;
        }
        User user = dbHelper.getUserById(profileUserId);
        currentUserId = session.getUserId();
        if (user == null) {
            finish();
            return;
        }
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        TextView tvName = findViewById(R.id.tvName);
        TextView tvBio = findViewById(R.id.tvBio);
        TextView tvGenres = findViewById(R.id.tvGenres);
        ImageView imgProfile = findViewById(R.id.imgProfile);
        tvName.setText(user.getName());
        tvBio.setText(user.getBio() != null && !user.getBio().isEmpty() ? user.getBio() : "Sem bio");
        tvGenres.setText(user.getFavoriteGenres() != null && !user.getFavoriteGenres().isEmpty()
                ? user.getFavoriteGenres()
                : "Nenhum gênero");
        if (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty()) {
            Glide.with(this).load(user.getPhotoPath()).circleCrop().into(imgProfile);
        }
                
        tvFollowersCount = findViewById(R.id.tvFollowersCount);
        tvFollowingCount = findViewById(R.id.tvFollowingCount);
        btnFollow = findViewById(R.id.btnFollow);
        View btnMessage = findViewById(R.id.btnMessage);
        
        updateFollowCounters();
        
        if (session.isLoggedIn() && currentUserId != profileUserId) {
            isFollowing = dbHelper.isFollowing(currentUserId, profileUserId);
            updateFollowButton();
            
            btnFollow.setOnClickListener(v -> toggleFollow());
            
            btnMessage.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, profileUserId);
                startActivity(intent);
            });
        } else {
            btnFollow.setVisibility(View.GONE);
            btnMessage.setVisibility(View.GONE);
        }
        
        LinearLayout layoutCurrentReading = findViewById(R.id.layoutCurrentReading);
        TextView tvCurrentBookTitle = findViewById(R.id.tvCurrentBookTitle);
        TextView tvCurrentBookAuthor = findViewById(R.id.tvCurrentBookAuthor);
        ProgressBar progressCurrentBook = findViewById(R.id.progressCurrentBook);
        TextView tvNoReading = findViewById(R.id.tvNoReading);
        UserBook currentReading = dbHelper.getCurrentReading(profileUserId);
        if (currentReading != null) {
            layoutCurrentReading.setVisibility(View.VISIBLE);
            tvNoReading.setVisibility(View.GONE);
            tvCurrentBookTitle.setText(currentReading.getBookTitle());
            tvCurrentBookAuthor.setText(currentReading.getBookAuthor());
            progressCurrentBook.setProgress(currentReading.getProgress());
        } else {
            layoutCurrentReading.setVisibility(View.GONE);
            tvNoReading.setVisibility(View.VISIBLE);
        }
        Button btnReport = findViewById(R.id.btnReport);
        if (session.isLoggedIn()) {
            btnReport.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, ReportActivity.class);
                    intent.putExtra(ReportActivity.EXTRA_REPORTED_USER_ID, profileUserId);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("PublicProfileActivity", "Error opening ReportActivity", e);
                }
            });
        } else {
            btnReport.setVisibility(View.GONE);
        }
        
        setupReviews();
    }
    
    private void updateFollowCounters() {
        tvFollowersCount.setText(String.valueOf(dbHelper.getFollowersCount(profileUserId)));
        tvFollowingCount.setText(String.valueOf(dbHelper.getFollowingCount(profileUserId)));
    }
    
    private void updateFollowButton() {
        if (isFollowing) {
            btnFollow.setText("SEGUINDO");
            btnFollow.setBackgroundResource(R.drawable.edit_text_bg);
            btnFollow.setTextColor(getResources().getColor(R.color.gray_text));
        } else {
            btnFollow.setText("SEGUIR");
            btnFollow.setBackgroundResource(R.drawable.btn_primary_bg);
            btnFollow.setTextColor(getResources().getColor(R.color.white));
        }
    }
    
    private void toggleFollow() {
        if (isFollowing) {
            if (dbHelper.unfollowUser(currentUserId, profileUserId)) {
                isFollowing = false;
                syncHelper.syncFollowToCloud(currentUserId, profileUserId, false);
            }
        } else {
            if (dbHelper.followUser(currentUserId, profileUserId)) {
                isFollowing = true;
                syncHelper.syncFollowToCloud(currentUserId, profileUserId, true);
            }
        }
        updateFollowButton();
        updateFollowCounters();
    }
    
    private void setupReviews() {
        RecyclerView recyclerReviews = findViewById(R.id.recyclerReviews);
        TextView tvNoReviews = findViewById(R.id.tvNoReviews);
        
        List<Review> reviews = dbHelper.getUserReviews(profileUserId);
        if (reviews != null && !reviews.isEmpty()) {
            recyclerReviews.setVisibility(View.VISIBLE);
            tvNoReviews.setVisibility(View.GONE);
            recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
            ReviewAdapter adapter = new ReviewAdapter(reviews);
            recyclerReviews.setAdapter(adapter);
        } else {
            recyclerReviews.setVisibility(View.GONE);
            tvNoReviews.setVisibility(View.VISIBLE);
        }
    }
}

