package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

/**
 * ProfileActivity - manages user profile editing.
 */
public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText editName, editBio, editGenres;
    private Spinner spinnerLanguage;
    private TextView tvUserName, tvUserEmail, tvUserRole;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        editName = findViewById(R.id.editName);
        editBio = findViewById(R.id.editBio);
        editGenres = findViewById(R.id.editGenres);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserRole = findViewById(R.id.tvUserRole);

        Button btnSave = findViewById(R.id.btnSave);
        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnLogout = findViewById(R.id.btnLogout);

        // Language spinner
        String[] languages = {"Portugues", "English", "Espanol"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, languages);
        spinnerLanguage.setAdapter(adapter);

        // Load user data
        loadUserData();

        btnSave.setOnClickListener(v -> saveProfile());
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> {
            session.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadUserData() {
        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        User user = dbHelper.getUserById(session.getUserId());
        if (user == null) {
            finish();
            return;
        }

        tvUserName.setText(user.getName());
        tvUserEmail.setText(user.getEmail());

        String roleLabel;
        switch (user.getRole()) {
            case "ORGANIZER": roleLabel = "Organizador"; break;
            case "READER": roleLabel = "Leitor"; break;
            default: roleLabel = "Convidado"; break;
        }
        tvUserRole.setText(roleLabel);

        editName.setText(user.getName());
        editBio.setText(user.getBio());
        editGenres.setText(user.getFavoriteGenres());

        // Set language spinner
        String lang = user.getLanguage();
        if (lang != null) {
            String[] languages = {"Portugues", "English", "Espanol"};
            for (int i = 0; i < languages.length; i++) {
                if (languages[i].equals(lang)) {
                    spinnerLanguage.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        String genres = editGenres.getText().toString().trim();
        String language = spinnerLanguage.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "Nome nao pode estar vazio", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.getUserById(session.getUserId());
        if (user == null) return;

        user.setName(name);
        user.setBio(bio);
        user.setFavoriteGenres(genres);
        user.setLanguage(language);

        if (dbHelper.updateUser(user)) {
            session.createLoginSession(user.getId(), user.getName(), user.getEmail(), user.getRole());
            tvUserName.setText(name);
            Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao salvar perfil", Toast.LENGTH_SHORT).show();
        }
    }
}
