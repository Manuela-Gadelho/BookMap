package com.bookmap.app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.UserAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;
import java.util.ArrayList;
import java.util.List;

public class CreateClubActivity extends AppCompatActivity {
    private EditText editClubName, editClubDescription;
    private CheckBox checkPublic;
    private EditText editSearchMembers;
    private RecyclerView recyclerMembers;
    private TextView tvSelectedCount;
    private UserAdapter userAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long clubId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_club);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        editClubName = findViewById(R.id.editClubName);
        editClubDescription = findViewById(R.id.editClubDescription);
        checkPublic = findViewById(R.id.checkPublic);
        editSearchMembers = findViewById(R.id.editSearchMember);
        recyclerMembers = findViewById(R.id.recyclerMembers);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        Button btnCreate = findViewById(R.id.btnCreateClub);
        TextView btnBack = findViewById(R.id.btnBack);

        clubId = getIntent().getLongExtra("club_id", -1);
        if (clubId != -1) {
            com.bookmap.app.model.Club club = dbHelper.getClubById(clubId);
            if (club != null) {
                editClubName.setText(club.getName());
                editClubDescription.setText(club.getDescription());
                checkPublic.setChecked(club.isPublic());
                android.view.View inviteSection = findViewById(R.id.layoutInviteSection);
                if (inviteSection != null) {
                    inviteSection.setVisibility(android.view.View.GONE);
                }
                TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
                if (tvHeaderTitle != null) {
                    tvHeaderTitle.setText("Editar Clube de Leitura");
                }
                btnCreate.setText("Salvar Alterações");
            }
        } else {
            recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
            loadUsers("");
            try {
                com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).pullUsersFromCloud(success -> {
                    if (success) {
                        runOnUiThread(() -> {
                            if (editSearchMembers != null) {
                                loadUsers(editSearchMembers.getText().toString());
                            }
                        });
                    }
                });
            } catch (Exception e) {
                android.util.Log.w("CreateClubActivity", "Could not start user cloud pull", e);
            }
            editSearchMembers.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    loadUsers(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        btnBack.setOnClickListener(v -> finish());
        btnCreate.setOnClickListener(v -> createClub());
    }

    private void loadUsers(String query) {
        List<User> users;
        if (query.isEmpty()) {
            users = dbHelper.getAllUsers();
        } else {
            users = dbHelper.searchUsers(query);
        }
        List<User> filtered = new ArrayList<>();
        for (User u : users) {
            if (u.getId() != session.getUserId()) {
                filtered.add(u);
            }
        }
        userAdapter = new UserAdapter(filtered, user -> {
            updateSelectedCount();
        }, true);
        recyclerMembers.setAdapter(userAdapter);
    }

    private void updateSelectedCount() {
        if (userAdapter != null) {
            int count = userAdapter.getSelectedCount();
            tvSelectedCount.setText(count + " membro(s) selecionado(s)");
        }
    }

    private void createClub() {
        String name = editClubName.getText().toString().trim();
        String description = editClubDescription.getText().toString().trim();
        boolean isPublic = checkPublic.isChecked();
        if (name.isEmpty()) {
            Toast.makeText(this, "O nome do clube é obrigatório.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (clubId != -1) {
            boolean success = dbHelper.updateClub(clubId, name, description, isPublic, null);
            if (success) {
                try {
                    com.bookmap.app.model.Club club = dbHelper.getClubById(clubId);
                    if (club != null) {
                        com.bookmap.app.database.FirebaseSyncHelper.getInstance(this).syncClubToCloud(club);
                    }
                } catch (Exception e) {
                    android.util.Log.w("CreateClubActivity", "Could not sync updated club to cloud", e);
                }
                Toast.makeText(this, "Clube atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao atualizar o clube.", Toast.LENGTH_SHORT).show();
            }
        } else {
            
            long newClubId = dbHelper.insertClub(name, description, isPublic, session.getUserId());
            if (newClubId > 0) {
                dbHelper.addClubMember(newClubId, session.getUserId(), "ORGANIZER", "APPROVED");
                List<Long> selectedIds = userAdapter != null ? userAdapter.getSelectedUserIds() : new ArrayList<>();
                for (Long userId : selectedIds) {
                    dbHelper.addClubMember(newClubId, userId, "MEMBER", "PENDING");
                }
                try {
                    com.bookmap.app.model.Club club = dbHelper.getClubById(newClubId);
                    if (club != null) {
                        com.bookmap.app.database.FirebaseSyncHelper syncHelper = com.bookmap.app.database.FirebaseSyncHelper
                                .getInstance(this);
                        syncHelper.syncClubToCloud(club);
                        syncHelper.syncClubMemberToCloud(newClubId, session.getUserId(), "ORGANIZER", "APPROVED");
                        for (Long userId : selectedIds) {
                            syncHelper.syncClubMemberToCloud(newClubId, userId, "MEMBER", "PENDING");
                        }
                    }
                } catch (Exception e) {
                    android.util.Log.w("CreateClubActivity", "Could not sync club and members to cloud", e);
                }
                Toast.makeText(this, "Clube criado com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao criar clube", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
