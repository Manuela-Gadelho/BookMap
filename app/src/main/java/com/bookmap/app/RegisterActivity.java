package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import com.google.android.material.chip.ChipGroup;
import com.bookmap.app.util.GenreUIHelper;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private Spinner spinnerGenres;
    private ChipGroup chipGroupGenres;
    private List<String> selectedGenres = new ArrayList<>();
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = DatabaseHelper.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        spinnerGenres = findViewById(R.id.spinnerGenres);
        chipGroupGenres = findViewById(R.id.chipGroupGenres);
        GenreUIHelper.setupGenreSpinner(this, spinnerGenres, chipGroupGenres, selectedGenres);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);
        btnRegister.setOnClickListener(v -> attemptRegister());
        tvLogin.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } catch (Exception e) {
                android.util.Log.e("RegisterActivity", "Error navigating to LoginActivity", e);
            }
        });
    }

    private void attemptRegister() {
        String name = editName.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dbHelper.getUserByEmail(email) != null) {
            Toast.makeText(this, "Este e-mail já está cadastrado.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedGenres.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um gênero literário.", Toast.LENGTH_SHORT).show();
            return;
        }
        String favoriteGenres = String.join(", ", selectedGenres);
        String passwordHash = PasswordUtil.hashPassword(password);
        
        Toast.makeText(this, "Criando conta...", Toast.LENGTH_SHORT).show();
        
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        long userId = dbHelper.insertUser(name, email, passwordHash, "", favoriteGenres, "READER");
                        if (userId > 0) {
                            SessionManager session = new SessionManager(RegisterActivity.this);
                            session.createLoginSession(userId, name, email, "READER");
                            Toast.makeText(RegisterActivity.this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
                            try {
                                Intent intent = new Intent(RegisterActivity.this, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } catch (Exception e) {
                                android.util.Log.e("RegisterActivity", "Error navigating to HomeActivity", e);
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erro ao salvar perfil no banco local.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido";
                        Toast.makeText(RegisterActivity.this, "Erro ao criar conta no Firebase: " + errMsg, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
