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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * CreateClubActivity - create a new reading club.
 * Must include member invitations at creation time.
 */
public class CreateClubActivity extends AppCompatActivity {

    private TextInputEditText editClubName, editClubDescription;
    private CheckBox checkPublic;
    private EditText editSearchMembers;
    private RecyclerView recyclerMembers;
    private TextView tvSelectedCount;
    private UserAdapter userAdapter;
    private DatabaseHelper dbHelper;
    private SessionManager session;

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

        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));

        // Load all users for invitation (with checkboxes)
        loadUsers("");

        // Search members
        editSearchMembers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                loadUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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

        // Remove current user from list
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
            Toast.makeText(this, "Nome do clube e obrigatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userAdapter == null || userAdapter.getSelectedCount() == 0) {
            Toast.makeText(this, "Convide pelo menos um membro", Toast.LENGTH_SHORT).show();
            return;
        }

        long clubId = dbHelper.insertClub(name, description, isPublic, session.getUserId());
        if (clubId > 0) {
            // Add creator as ORGANIZER
            dbHelper.addClubMember(clubId, session.getUserId(), "ORGANIZER", "APPROVED");

            // Add invited members
            List<Long> selectedIds = userAdapter.getSelectedUserIds();
            for (Long userId : selectedIds) {
                dbHelper.addClubMember(clubId, userId, "MEMBER", "PENDING");
            }

            Toast.makeText(this, "Clube criado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao criar clube", Toast.LENGTH_SHORT).show();
        }
    }
}
