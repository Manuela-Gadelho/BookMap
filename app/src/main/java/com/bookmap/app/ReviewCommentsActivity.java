package com.bookmap.app;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.CommentAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.ReviewComment;
import com.bookmap.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class ReviewCommentsActivity extends AppCompatActivity {
    private RecyclerView recyclerComments;
    private CommentAdapter adapter;
    private EditText editCommentText;
    private ImageView btnSendComment;
    
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long reviewId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_comments);

        reviewId = getIntent().getLongExtra("REVIEW_ID", -1);
        if (reviewId == -1) {
            Toast.makeText(this, "Erro ao carregar resenha", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        recyclerComments = findViewById(R.id.recyclerComments);
        editCommentText = findViewById(R.id.editCommentText);
        btnSendComment = findViewById(R.id.btnSendComment);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CommentAdapter(new ArrayList<>());
        recyclerComments.setAdapter(adapter);

        btnSendComment.setOnClickListener(v -> submitComment());

        loadComments();
    }

    private void loadComments() {
        List<ReviewComment> comments = dbHelper.getReviewComments(reviewId);
        adapter.updateData(comments);
        if (!comments.isEmpty()) {
            recyclerComments.scrollToPosition(comments.size() - 1);
        }
    }

    private void submitComment() {
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Faça login para comentar", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = editCommentText.getText().toString().trim();
        if (text.isEmpty()) {
            return;
        }

        long userId = session.getUserId();
        long insertedId = dbHelper.addReviewComment(reviewId, userId, text);
        
        if (insertedId != -1) {
            editCommentText.setText("");
            
            
            loadComments();
            
            
            List<ReviewComment> all = dbHelper.getReviewComments(reviewId);
            for (ReviewComment rc : all) {
                if (rc.getId() == insertedId) {
                    FirebaseSyncHelper.getInstance(this).pushReviewComment(rc, null);
                    break;
                }
            }
        } else {
            Toast.makeText(this, "Erro ao salvar comentário", Toast.LENGTH_SHORT).show();
        }
    }
}
