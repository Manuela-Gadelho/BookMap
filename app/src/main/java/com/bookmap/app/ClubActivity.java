package com.bookmap.app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.EventAdapter;
import com.bookmap.app.adapter.UserAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Club;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.model.Event;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.util.ArrayList;
import java.util.List;

public class ClubActivity extends AppCompatActivity {
    public static final String EXTRA_CLUB_ID = "club_id";
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long clubId;

    private TextView tvClubName, tvClubDescription, tvClubType;
    private RecyclerView recyclerMembers, recyclerEvents;
    private UserAdapter memberAdapter;
    private EventAdapter eventAdapter;
    private TextView tvNoEvents;
    private TextView btnJoinClub;
    private TextView btnCreateEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        clubId = getIntent().getLongExtra(EXTRA_CLUB_ID, -1);
        if (clubId == -1) {
            finish();
            return;
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        tvClubName = findViewById(R.id.tvClubName);
        tvClubDescription = findViewById(R.id.tvClubDescription);
        tvClubType = findViewById(R.id.tvClubType);
        recyclerMembers = findViewById(R.id.recyclerMembers);
        recyclerEvents = findViewById(R.id.recyclerEvents);
        tvNoEvents = findViewById(R.id.tvNoEvents);
        btnJoinClub = findViewById(R.id.btnJoinClub);
        btnCreateEvent = findViewById(R.id.btnCreateEvent);

        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new UserAdapter(new ArrayList<>(), user -> {
            try {
                Intent intent = new Intent(this, PublicProfileActivity.class);
                intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, user.getId());
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("ClubActivity", "Error opening PublicProfile", e);
            }
        }, false);
        recyclerMembers.setAdapter(memberAdapter);

        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        eventAdapter = new EventAdapter(new ArrayList<>(), event -> {
            try {
                Intent intent = new Intent(this, EventActivity.class);
                intent.putExtra(EventActivity.EXTRA_EVENT_ID, event.getId());
                startActivity(intent);
            } catch (Exception e) {
                android.util.Log.e("ClubActivity", "Error opening EventActivity", e);
            }
        });
        recyclerEvents.setAdapter(eventAdapter);

        loadClubDetails();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadClubDetails();
    }

    private void loadClubDetails() {
        Club club = dbHelper.getClubById(clubId);
        if (club == null) {
            finish();
            return;
        }
        tvClubName.setText(club.getName());
        tvClubDescription.setText(club.getDescription());
        tvClubType.setText(club.isPublic() ? "Público" : "Privado");

        // Load Creator
        View layoutClubCreator = findViewById(R.id.layoutClubCreator);
        android.widget.ImageView imgCreatorPhoto = findViewById(R.id.imgCreatorPhoto);
        TextView tvCreatorName = findViewById(R.id.tvCreatorName);
        
        User creator = dbHelper.getUserById(club.getCreatorId());
        if (creator != null) {
            tvCreatorName.setText(creator.getName());
            if (creator.getPhotoPath() != null && !creator.getPhotoPath().isEmpty()) {
                Glide.with(this).load(creator.getPhotoPath()).transform(new CircleCrop()).into(imgCreatorPhoto);
            }
            layoutClubCreator.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, PublicProfileActivity.class);
                    intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, creator.getId());
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("ClubActivity", "Error opening creator profile", e);
                }
            });
        } else {
            layoutClubCreator.setVisibility(View.GONE);
        }

        // Load members
        List<ClubMember> clubMembers = dbHelper.getClubMembers(clubId);
        List<User> memberUsers = new ArrayList<>();
        for (ClubMember cm : clubMembers) {
            User u = dbHelper.getUserById(cm.getUserId());
            if (u != null)
                memberUsers.add(u);
        }
        memberAdapter.updateData(memberUsers);

        // Sync cloud members
        try {
            com.bookmap.app.database.FirebaseSyncHelper syncHelper = com.bookmap.app.database.FirebaseSyncHelper
                    .getInstance(this);
            syncHelper.pullUsersFromCloud(usersSuccess -> {
                syncHelper.pullClubMembersFromCloud(clubId, membersSuccess -> {
                    if (membersSuccess) {
                        runOnUiThread(() -> {
                            List<ClubMember> updatedMembers = dbHelper.getClubMembers(clubId);
                            List<User> updatedUsers = new ArrayList<>();
                            for (ClubMember cm : updatedMembers) {
                                User u = dbHelper.getUserById(cm.getUserId());
                                if (u != null)
                                    updatedUsers.add(u);
                            }
                            memberAdapter.updateData(updatedUsers);
                        });
                    }
                });
            });
        } catch (Exception e) {
            android.util.Log.w("ClubActivity", "Could not sync club members", e);
        }

        // Load events
        List<Event> events = dbHelper.getClubEvents(clubId);
        eventAdapter.updateData(events);
        if (events.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }

        // Action buttons
        if (session.isLoggedIn()) {
            boolean isCreator = club.getCreatorId() == session.getUserId();
            boolean isMember = dbHelper.getClubMember(clubId, session.getUserId()) != null;

            if (isCreator) {
                btnJoinClub.setText("Organizador");
                btnJoinClub.setEnabled(false);
                btnCreateEvent.setVisibility(View.VISIBLE);
            } else if (isMember) {
                btnJoinClub.setText("Membro");
                btnJoinClub.setEnabled(false);
                btnCreateEvent.setVisibility(View.GONE);
            } else {
                btnJoinClub.setVisibility(View.VISIBLE);
                btnJoinClub.setEnabled(true);
                btnJoinClub.setText("SOLICITAR ENTRADA NO CLUBE");
                btnJoinClub.setOnClickListener(v -> {
                    long result = dbHelper.addClubMember(clubId, session.getUserId(), "MEMBER", "PENDING");
                    if (result > 0) {
                        Toast.makeText(this, "Solicitação enviada!", Toast.LENGTH_SHORT).show();
                        btnJoinClub.setText("Pendente");
                        btnJoinClub.setEnabled(false);
                        try {
                            com.bookmap.app.database.FirebaseSyncHelper.getInstance(this)
                                    .syncClubMemberToCloud(clubId, session.getUserId(), "MEMBER", "PENDING");
                        } catch (Exception e) {
                            android.util.Log.e("ClubActivity", "Error syncing join request", e);
                        }
                    }
                });
                btnCreateEvent.setVisibility(View.GONE);
            }

            btnCreateEvent.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(this, EventActivity.class);
                    intent.putExtra(EventActivity.EXTRA_CLUB_ID, clubId);
                    startActivity(intent);
                } catch (Exception e) {
                    android.util.Log.e("ClubActivity", "Error opening EventActivity", e);
                }
            });

            // Edit / Delete actions
            View layoutClubCreatorActions = findViewById(R.id.layoutClubCreatorActions);
            Button btnEditClub = findViewById(R.id.btnEditClub);
            Button btnDeleteClub = findViewById(R.id.btnDeleteClub);

            if (isCreator || session.isAdmin()) {
                layoutClubCreatorActions.setVisibility(View.VISIBLE);
                btnEditClub.setOnClickListener(v -> {
                    Intent intent = new Intent(this, CreateClubActivity.class);
                    intent.putExtra("club_id", clubId);
                    startActivity(intent);
                });
                btnDeleteClub.setOnClickListener(v -> {
                    new AlertDialog.Builder(this)
                            .setTitle("Excluir Clube")
                            .setMessage("Tem certeza de que deseja excluir este clube?")
                            .setPositiveButton("Sim", (dialog, which) -> {
                                if (dbHelper.deleteClub(clubId)) {
                                    com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).deleteClubFromCloud(clubId);
                                    Toast.makeText(this, "Clube excluído com sucesso!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(this, "Erro ao excluir o clube.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Não", null)
                            .show();
                });
            } else {
                layoutClubCreatorActions.setVisibility(View.GONE);
            }
        } else {
            btnJoinClub.setVisibility(View.GONE);
            btnCreateEvent.setVisibility(View.GONE);
            findViewById(R.id.layoutClubCreatorActions).setVisibility(View.GONE);
        }
    }
}
