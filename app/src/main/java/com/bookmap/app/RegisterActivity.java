package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;
import android.widget.EditText;
import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {
    private EditText editName, editEmail, editPassword, editConfirmPassword;
    private CheckBox checkFantasia, checkTerror, checkRomance, checkFiccao, checkTecnologia, checkLitBrasileira;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbHelper = DatabaseHelper.getInstance(this);
        editName = findViewById(R.id.editName);
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        checkFantasia = findViewById(R.id.checkFantasia);
        checkTerror = findViewById(R.id.checkTerror);
        checkRomance = findViewById(R.id.checkRomance);
        checkFiccao = findViewById(R.id.checkFiccao);
        checkTecnologia = findViewById(R.id.checkTecnologia);
        checkLitBrasileira = findViewById(R.id.checkLitBrasileira);
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
        List<String> genres = new ArrayList<>();
        if (checkFantasia.isChecked())
            genres.add("Fantasia");
        if (checkTerror.isChecked())
            genres.add("Terror");
        if (checkRomance.isChecked())
            genres.add("Romance");
        if (checkFiccao.isChecked())
            genres.add("Ficção Científica");
        if (checkTecnologia.isChecked())
            genres.add("Tecnologia");
        if (checkLitBrasileira.isChecked())
            genres.add("Literatura Brasileira");
        String favoriteGenres = String.join(", ", genres);
        String passwordHash = PasswordUtil.hashPassword(password);
        long userId = dbHelper.insertUser(name, email, passwordHash, "", favoriteGenres, "READER");
        if (userId > 0) {
            SessionManager session = new SessionManager(this);
            session.createLoginSession(userId, name, email, "READER");
            Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();
            try {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                android.util.Log.e("RegisterActivity", "Error navigating to HomeActivity", e);
            }
        } else {
            Toast.makeText(this, "Erro ao criar conta. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
}
