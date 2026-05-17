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
import java.util.UUID;
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText editEmail, editPassword;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private boolean isGoogleSignInConfigured = false;
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    if (result.getData() != null) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        Log.w(TAG, "Google Sign-In returned null data");
                        Toast.makeText(this, "Login com Google cancelado.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing Google Sign-In result", e);
                    Toast.makeText(this, "Erro ao processar login com Google.", Toast.LENGTH_SHORT).show();
                }
            });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager quickCheck = new SessionManager(this);
        if (quickCheck.isLoggedIn()) {
            try {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return;
            } catch (Exception e) {
                Log.e(TAG, "Error redirecting logged in user", e);
            }
        }
        setContentView(R.layout.activity_login);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        try {
            firebaseAuth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Log.w(TAG, "Firebase Auth not available", e);
            firebaseAuth = null;
        }
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn);
        TextView tvRegister = findViewById(R.id.tvRegister);
        TextView tvGuest = findViewById(R.id.tvGuest);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        try {
            String webClientId = getWebClientId();
            if (webClientId != null && firebaseAuth != null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(webClientId)
                        .requestEmail()
                        .build();
                googleSignInClient = GoogleSignIn.getClient(this, gso);
                isGoogleSignInConfigured = true;
                btnGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
            } else {
                btnGoogleSignIn.setEnabled(false);
                btnGoogleSignIn.setText("Google Sign-In nao configurado");
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to configure Google Sign-In", e);
            btnGoogleSignIn.setEnabled(false);
            btnGoogleSignIn.setText("Google Sign-In nao disponivel");
        }
        btnLogin.setOnClickListener(v -> attemptLogin());
        tvRegister.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, RegisterActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to RegisterActivity", e);
                Toast.makeText(this, "Erro ao abrir tela de cadastro", Toast.LENGTH_SHORT).show();
            }
        });
        tvForgotPassword.setOnClickListener(v -> {
            try {
                startActivity(new Intent(this, ForgotPasswordActivity.class));
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to ForgotPasswordActivity", e);
                Toast.makeText(this, "Erro ao abrir recuperacao de senha", Toast.LENGTH_SHORT).show();
            }
        });
        tvGuest.setOnClickListener(v -> {
            try {
                session.logout();
                navigateToHome();
            } catch (Exception e) {
                Log.e(TAG, "Error entering as guest", e);
                Toast.makeText(this, "Erro ao entrar como convidado", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void signInWithGoogle() {
        if (!isGoogleSignInConfigured || googleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In nao esta configurado.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                try {
                    Intent signInIntent = googleSignInClient.getSignInIntent();
                    googleSignInLauncher.launch(signInIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error launching Google Sign-In", e);
                    Toast.makeText(this, "Erro ao iniciar login com Google.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error in signInWithGoogle", e);
            Toast.makeText(this, "Erro ao iniciar login com Google.", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null && account.getIdToken() != null) {
                firebaseAuthWithGoogle(account.getIdToken());
            } else {
                Log.w(TAG, "Google Sign-In: account or idToken is null");
                Toast.makeText(this, "Erro ao obter dados do Google. Tente novamente.", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.w(TAG, "Google Sign-In falhou: " + e.getStatusCode(), e);
            if (e.getStatusCode() == 12501) {
                Toast.makeText(this, "Login com Google cancelado.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao entrar com Google (codigo: " + e.getStatusCode() + "). Tente novamente.",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in Google Sign-In result", e);
            Toast.makeText(this, "Erro inesperado. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
    private void firebaseAuthWithGoogle(String idToken) {
        if (firebaseAuth == null) {
            Toast.makeText(this, "Firebase nao esta disponivel. Use login com email.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, task -> {
                        try {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    handleGoogleUser(firebaseUser);
                                } else {
                                    Toast.makeText(this, "Erro: usuario nao encontrado apos autenticacao.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.w(TAG, "Firebase Auth falhou", task.getException());
                                Toast.makeText(this, "Erro na autenticacao com Google.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing Firebase auth result", e);
                            Toast.makeText(this, "Erro ao processar autenticacao.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error in firebaseAuthWithGoogle", e);
            Toast.makeText(this, "Erro na autenticacao. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleGoogleUser(FirebaseUser firebaseUser) {
        try {
            String email = firebaseUser.getEmail();
            String name = firebaseUser.getDisplayName();
            if (name == null || name.isEmpty())
                name = "Usuario Google";
            if (email == null || email.isEmpty()) {
                Toast.makeText(this, "Erro: conta Google sem e-mail. Use login com senha.", Toast.LENGTH_SHORT).show();
                return;
            }
            User existingUser = dbHelper.getUserByEmail(email);
            if (existingUser != null) {
                session.createLoginSession(existingUser.getId(), existingUser.getName(),
                        existingUser.getEmail(), existingUser.getRole());
                Toast.makeText(this, "Bem-vindo de volta, " + existingUser.getName() + "!", Toast.LENGTH_SHORT).show();
            } else {
                String passwordHash = PasswordUtil.hashPassword(UUID.randomUUID().toString());
                long userId = dbHelper.insertUser(name, email, passwordHash, "", "", "READER");
                if (userId > 0) {
                    session.createLoginSession(userId, name, email, "READER");
                    Toast.makeText(this, "Bem-vindo, " + name + "!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao criar conta. Tente novamente.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            navigateToHome();
        } catch (Exception e) {
            Log.e(TAG, "Error handling Google user", e);
            Toast.makeText(this, "Erro ao processar login. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
    private String getWebClientId() {
        try {
            int resId = getResources().getIdentifier("default_web_client_id", "string", getPackageName());
            if (resId == 0)
                return null;
            String value = getString(resId);
            if (value.isEmpty() || "PLACEHOLDER".equals(value))
                return null;
            return value;
        } catch (Exception e) {
            Log.w(TAG, "Error getting web client ID", e);
            return null;
        }
    }
    private void attemptLogin() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
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
            navigateToHome();
        } catch (Exception e) {
            Log.e(TAG, "Error during login attempt", e);
            Toast.makeText(this, "Erro ao fazer login. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }
    private void navigateToHome() {
        try {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Error navigating to HomeActivity", e);
            Toast.makeText(this, "Erro ao abrir tela principal", Toast.LENGTH_SHORT).show();
        }
    }
}
