package com.bookmap.app;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.PasswordUtil;
import com.google.android.material.textfield.TextInputEditText;
public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputEditText editEmail, editNewPassword, editConfirmPassword;
    private Button btnResetPassword;
    private TextView tvStatus;
    private DatabaseHelper dbHelper;
    private boolean emailVerified = false;
    private User foundUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        dbHelper = DatabaseHelper.getInstance(this);
        editEmail = findViewById(R.id.editEmail);
        editNewPassword = findViewById(R.id.editNewPassword);
        editConfirmPassword = findViewById(R.id.editConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvStatus = findViewById(R.id.tvStatus);
        TextView btnBack = findViewById(R.id.btnBack);
        editNewPassword.setEnabled(false);
        editConfirmPassword.setEnabled(false);
        Button btnVerifyEmail = findViewById(R.id.btnVerifyEmail);
        btnVerifyEmail.setOnClickListener(v -> verifyEmail());
        btnResetPassword.setOnClickListener(v -> resetPassword());
        btnBack.setOnClickListener(v -> finish());
    }
    private void verifyEmail() {
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail", Toast.LENGTH_SHORT).show();
            return;
        }
        foundUser = dbHelper.getUserByEmail(email);
        if (foundUser == null) {
            tvStatus.setText("E-mail nao encontrado no sistema");
            return;
        }
        emailVerified = true;
        tvStatus.setText("E-mail verificado! Digite a nova senha.");
        editEmail.setEnabled(false);
        editNewPassword.setEnabled(true);
        editConfirmPassword.setEnabled(true);
        btnResetPassword.setEnabled(true);
    }
    private void resetPassword() {
        if (!emailVerified || foundUser == null) {
            Toast.makeText(this, "Verifique seu e-mail primeiro", Toast.LENGTH_SHORT).show();
            return;
        }
        String newPassword = editNewPassword.getText().toString().trim();
        String confirmPassword = editConfirmPassword.getText().toString().trim();
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Digite a nova senha", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPassword.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "As senhas nao coincidem", Toast.LENGTH_SHORT).show();
            return;
        }
        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean updated = dbHelper.updateUserPassword(foundUser.getId(), newHash);
        if (updated) {
            Toast.makeText(this, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao alterar senha. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
}
