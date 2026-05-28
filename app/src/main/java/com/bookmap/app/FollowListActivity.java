package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.FollowListAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;
import java.util.List;

public class FollowListActivity extends AppCompatActivity implements FollowListAdapter.FollowActionListener {
    public static final String EXTRA_LIST_TYPE = "list_type";
    
    private String listType; // "followers" or "following"
    private DatabaseHelper dbHelper;
    private FirebaseSyncHelper syncHelper;
    private SessionManager session;
    private RecyclerView recyclerFollowList;
    private TextView tvEmpty, tvTitle;
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_list);

        dbHelper = DatabaseHelper.getInstance(this);
        syncHelper = FirebaseSyncHelper.getInstance(this);
        session = new SessionManager(this);
        
        if (!session.isLoggedIn()) {
            finish();
            return;
        }
        
        currentUserId = session.getUserId();
        listType = getIntent().getStringExtra(EXTRA_LIST_TYPE);
        if (listType == null) listType = "followers";

        tvTitle = findViewById(R.id.tvTitle);
        recyclerFollowList = findViewById(R.id.recyclerFollowList);
        tvEmpty = findViewById(R.id.tvEmpty);
        TextView btnBack = findViewById(R.id.btnBack);

        if ("followers".equals(listType)) {
            tvTitle.setText("Seguidores");
        } else {
            tvTitle.setText("Seguindo");
        }

        btnBack.setOnClickListener(v -> finish());
        recyclerFollowList.setLayoutManager(new LinearLayoutManager(this));

        loadList();
    }

    private void loadList() {
        List<User> users;
        if ("followers".equals(listType)) {
            users = dbHelper.getFollowersList(currentUserId);
            tvEmpty.setText("Você não possui seguidores.");
        } else {
            users = dbHelper.getFollowingList(currentUserId);
            tvEmpty.setText("Você não está seguindo ninguém.");
        }

        if (users.isEmpty()) {
            recyclerFollowList.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerFollowList.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
            FollowListAdapter adapter = new FollowListAdapter(users, listType, this);
            recyclerFollowList.setAdapter(adapter);
        }
    }

    @Override
    public void onActionClick(User targetUser, String type) {
        String title = "followers".equals(type) ? "Remover Seguidor" : "Deixar de Seguir";
        String message = "followers".equals(type) ? 
                "Deseja remover " + targetUser.getName() + " da sua lista de seguidores?" :
                "Deseja parar de seguir " + targetUser.getName() + "?";

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Sim", (dialog, which) -> {
                    if ("followers".equals(type)) {
                        // The current user removes a follower (targetUser is following currentUser)
                        if (dbHelper.unfollowUser(targetUser.getId(), currentUserId)) {
                            syncHelper.syncFollowToCloud(targetUser.getId(), currentUserId, false, null);
                            Toast.makeText(this, "Seguidor removido.", Toast.LENGTH_SHORT).show();
                            loadList();
                        } else {
                            Toast.makeText(this, "Erro ao remover seguidor.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // The current user unfollows someone (currentUser is following targetUser)
                        if (dbHelper.unfollowUser(currentUserId, targetUser.getId())) {
                            syncHelper.syncFollowToCloud(currentUserId, targetUser.getId(), false, null);
                            Toast.makeText(this, "Você deixou de seguir " + targetUser.getName() + ".", Toast.LENGTH_SHORT).show();
                            loadList();
                        } else {
                            Toast.makeText(this, "Erro ao deixar de seguir.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    public void onUserClick(User user) {
        Intent intent = new Intent(this, PublicProfileActivity.class);
        intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, user.getId());
        startActivity(intent);
    }
}
