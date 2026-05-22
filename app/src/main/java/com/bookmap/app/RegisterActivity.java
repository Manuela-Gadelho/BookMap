package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import com.google.android.material.chip.ChipGroup;
import android.widget.TextView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.bookmap.app.util.FirebaseErrorTranslator;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private TextView tvGenreSelection;
    private List<String> selectedGenres = new ArrayList<>();
    private boolean[] checkedItems;
    private DatabaseHelper dbHelper;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = DatabaseHelper.getInstance(this);
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("pt-BR");
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        tvGenreSelection = findViewById(R.id.tvGenreSelection);
        
        String[] genres = com.bookmap.app.util.GenreUtil.getGenresArray();
        checkedItems = new boolean[genres.length];
        
        tvGenreSelection.setOnClickListener(v -> {
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("Selecione os Gêneros");
            
            builder.setMultiChoiceItems(genres, checkedItems, (dialog, which, isChecked) -> {
                checkedItems[which] = isChecked;
            });
            
            builder.setPositiveButton("OK", (dialog, which) -> {
                selectedGenres.clear();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedGenres.add(genres[i]);
                    }
                }
                if (selectedGenres.isEmpty()) {
                    tvGenreSelection.setText("Nenhum gênero selecionado");
                } else {
                    tvGenreSelection.setText(selectedGenres.size() + " gêneros selecionados");
                }
            });
            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
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
                        com.google.firebase.auth.FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(RegisterActivity.this,
                                                    "E-mail de verificação enviado! Verifique sua caixa de entrada.",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            android.util.Log.e("RegisterActivity",
                                                    "Erro ao enviar e-mail de verificação",
                                                    verificationTask.getException());
                                        }
                                    });
                        }
                        long userId = dbHelper.insertUser(name, email, passwordHash, "", favoriteGenres, "READER");
                        if (userId > 0) {
                            new android.app.AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle("Conta Criada!")
                                    .setMessage(
                                            "Sua conta foi criada com sucesso. Enviamos um e-mail de verificação para "
                                                    + email
                                                    + ". Por favor, confirme seu cadastro clicando no link enviado ao seu e-mail antes de fazer login.")
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        mAuth.signOut();
                                        finish();
                                    })
                                    .show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "Erro ao salvar perfil no banco local.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        String errMsg = FirebaseErrorTranslator.translate(task.getException());
                        Toast.makeText(RegisterActivity.this, errMsg,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
