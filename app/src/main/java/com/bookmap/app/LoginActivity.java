package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.PasswordUtil;
import com.bookmap.app.util.SessionManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

/**
 * LoginActivity - handles user login with email/password and Google Sign-In.
 * Guests can skip login to browse with limited access.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editEmail, editPassword;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        firebaseAuth = FirebaseAuth.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvGuest = findViewById(R.id.tvGuest);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Configure Google Sign-In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> attemptLogin());
        btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgotPasswordActivity.class));
        });

        tvGuest.setOnClickListener(v -> {
            session.logout();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google Sign-In falhou: " + e.getStatusCode(), e);
            Toast.makeText(this, "Erro ao entrar com Google. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            handleGoogleUser(firebaseUser);
                        }
                    } else {
                        Log.w(TAG, "Firebase Auth falhou", task.getException());
                        Toast.makeText(this, "Erro na autenticacao com Google.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleGoogleUser(FirebaseUser firebaseUser) {
        String email = firebaseUser.getEmail();
        String name = firebaseUser.getDisplayName();
        if (name == null || name.isEmpty()) name = "Usuario Google";
        if (email == null) email = "";

        User existingUser = dbHelper.getUserByEmail(email);

        if (existingUser != null) {
            session.createLoginSession(existingUser.getId(), existingUser.getName(),
                    existingUser.getEmail(), existingUser.getRole());
            Toast.makeText(this, "Bem-vindo de volta, " + existingUser.getName() + "!", Toast.LENGTH_SHORT).show();
        } else {
            String passwordHash = PasswordUtil.hashPassword("google_" + firebaseUser.getUid());
            long userId = dbHelper.insertUser(name, email, passwordHash, "", "", "READER");
            if (userId > 0) {
                session.createLoginSession(userId, name, email, "READER");
                Toast.makeText(this, "Bem-vindo, " + name + "!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao criar conta. Tente novamente.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        startActivity(new Intent(this, HomeActivity.class));
        finish();
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
