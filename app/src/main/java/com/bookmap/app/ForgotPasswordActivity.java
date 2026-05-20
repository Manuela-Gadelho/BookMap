package com.bookmap.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.bookmap.app.util.FirebaseErrorTranslator;

public class ForgotPasswordActivity extends AppCompatActivity {
    private EditText editEmail;
    private Button btnResetPassword;
    private TextView tvStatus;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("pt-BR");

        editEmail = findViewById(R.id.editEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvStatus = findViewById(R.id.tvStatus);
        TextView btnBack = findViewById(R.id.btnBack);

        btnResetPassword.setOnClickListener(v -> sendResetEmail());
        btnBack.setOnClickListener(v -> finish());
    }

    private void sendResetEmail() {
        String email = editEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(this, "Digite seu e-mail", Toast.LENGTH_SHORT).show();
            return;
        }

        tvStatus.setText("Enviando e-mail de recuperação...");
        btnResetPassword.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    btnResetPassword.setEnabled(true);
                    if (task.isSuccessful()) {
                        tvStatus.setText("Link de redefinição enviado! Verifique seu e-mail.");
                        Toast.makeText(ForgotPasswordActivity.this,
                                "E-mail de recuperação enviado com sucesso!", Toast.LENGTH_LONG).show();
                    } else {
                        String errorMessage = FirebaseErrorTranslator.translate(task.getException());
                        tvStatus.setText("Erro ao enviar: " + errorMessage);
                        Toast.makeText(ForgotPasswordActivity.this,
                                errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
