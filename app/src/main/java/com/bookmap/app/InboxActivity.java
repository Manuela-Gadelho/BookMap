package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.InboxAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.Message;
import com.bookmap.app.util.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerInbox;
    private TextView tvEmptyInbox;
    private InboxAdapter inboxAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerInbox = findViewById(R.id.recyclerInbox);
        tvEmptyInbox = findViewById(R.id.tvEmptyInbox);

        recyclerInbox.setLayoutManager(new LinearLayoutManager(this));
        inboxAdapter = new InboxAdapter(new ArrayList<>(), session.getUserId(), dbHelper, otherUserId -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_OTHER_USER_ID, otherUserId);
            startActivity(intent);
        });
        recyclerInbox.setAdapter(inboxAdapter);

        loadInbox();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadInbox();
        try {
            FirebaseSyncHelper.getInstance(this).pullMessagesFromCloud(session.getUserId(), success -> {
                if (success) {
                    runOnUiThread(this::loadInbox);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("InboxActivity", "Error syncing messages", e);
        }
    }

    private void loadInbox() {
        List<Message> messages = dbHelper.getInbox(session.getUserId());
        if (messages.isEmpty()) {
            tvEmptyInbox.setVisibility(View.VISIBLE);
            recyclerInbox.setVisibility(View.GONE);
        } else {
            tvEmptyInbox.setVisibility(View.GONE);
            recyclerInbox.setVisibility(View.VISIBLE);
            inboxAdapter.updateData(messages);
        }
    }
}
