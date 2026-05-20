package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.util.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private RecyclerView recyclerNotifications;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            finish();
            return;
        }
        recyclerNotifications = findViewById(R.id.recyclerNotifications);
        tvEmpty = findViewById(R.id.tvEmpty);
        TextView btnBack = findViewById(R.id.btnBack);
        recyclerNotifications.setLayoutManager(new LinearLayoutManager(this));
        btnBack.setOnClickListener(v -> finish());
        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        List<ClubMember> pendingRequests = dbHelper.getPendingMemberRequests(session.getUserId());
        if (pendingRequests.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerNotifications.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerNotifications.setVisibility(View.VISIBLE);
        }
        com.bookmap.app.adapter.NotificationAdapter adapter = new com.bookmap.app.adapter.NotificationAdapter(
                pendingRequests,
                new com.bookmap.app.adapter.NotificationAdapter.NotificationActionListener() {
                    @Override
                    public void onApprove(ClubMember member) {
                        dbHelper.updateMemberStatus(member.getClubId(), member.getUserId(), "APPROVED");
                        Toast.makeText(NotificationsActivity.this,
                                "Membro aprovado!", Toast.LENGTH_SHORT).show();
                        loadNotifications();
                    }

                    @Override
                    public void onReject(ClubMember member) {
                        dbHelper.updateMemberStatus(member.getClubId(), member.getUserId(), "REJECTED");
                        Toast.makeText(NotificationsActivity.this,
                                "Solicitação rejeitada", Toast.LENGTH_SHORT).show();
                        loadNotifications();
                    }
                });
        recyclerNotifications.setAdapter(adapter);
    }
}
