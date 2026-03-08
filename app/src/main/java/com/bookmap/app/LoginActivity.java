package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;

/**
 * LoginActivity - handles user login with email and password.
 * Guests can skip login to browse with limited access.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText editEmail, editPassword;
    private DatabaseHelper dbHelper;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvGuest = findViewById(R.id.tvGuest);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvGuest.setOnClickListener(v -> {
            // Guest mode: go to Home with limited access
            session.logout(); // Ensure clean state
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = dbHelper.getUserByEmail(email);
        if (user == null) {
            Toast.makeText(this, "Usuario nao encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!PasswordUtil.verifyPassword(password, user.getPasswordHash())) {
            Toast.makeText(this, "Senha incorreta", Toast.LENGTH_SHORT).show();
            return;
        }

        session.createLoginSession(user.getId(), user.getName(), user.getEmail(), user.getRole());
        Toast.makeText(this, "Bem-vindo, " + user.getName() + "!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
