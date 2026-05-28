package com.bookmap.app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.util.GenreUIHelper;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.User;
import com.bookmap.app.util.PhotoHelper;
import com.bookmap.app.util.SessionManager;

public class ProfileActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 3001;
    private EditText editName, editBio;
    private RecyclerView recyclerGenres;
    private SwitchCompat switchPrivateProfile;
    private List<String> selectedGenres = new ArrayList<>();
    private TextView tvUserName, tvUserEmail, tvUserRole;
    private ImageView imgAvatar;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private PhotoHelper photoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        photoHelper = new PhotoHelper(this);
        if (savedInstanceState != null) {
            String savedPath = savedInstanceState.getString("photo_path");
            if (savedPath != null) {
                photoHelper.setCurrentPhotoPath(savedPath);
            }
        }
        editName = findViewById(R.id.editName);
        editBio = findViewById(R.id.editBio);
        recyclerGenres = findViewById(R.id.recyclerGenres);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvUserRole = findViewById(R.id.tvUserRole);
        imgAvatar = findViewById(R.id.imgAvatar);
        switchPrivateProfile = findViewById(R.id.switchPrivateProfile);
        Button btnSave = findViewById(R.id.btnSave);
        TextView btnBack = findViewById(R.id.btnBack);
        TextView btnLogout = findViewById(R.id.btnLogout);
        loadUserData();
        selectedGenres = GenreUIHelper.setupGenreRecycler(this, recyclerGenres, selectedGenres, null);
        imgAvatar.setOnClickListener(v -> showPhotoOptions());
        btnSave.setOnClickListener(v -> saveProfile());
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> {
            try {
                session.logout();
                try {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                } catch (Exception ignored) {
                }
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } catch (Exception e) {
                android.util.Log.e("ProfileActivity", "Error during logout", e);
                android.widget.Toast.makeText(this, "Erro ao sair.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (photoHelper.getCurrentPhotoPath() != null) {
            outState.putString("photo_path", photoHelper.getCurrentPhotoPath());
        }
    }

    private void showPhotoOptions() {
        String[] options = { "Tirar Foto", "Escolher da Galeria", "Remover Foto" };
        new AlertDialog.Builder(this)
                .setTitle("Foto de Perfil")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openCamera();
                            break;
                        case 1:
                            openGallery();
                            break;
                        case 2:
                            removePhoto();
                            break;
                    }
                })
                .show();
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA }, PERMISSION_REQUEST_CAMERA);
            return;
        }
        Intent cameraIntent = photoHelper.createCameraIntent();
        if (cameraIntent != null) {
            startActivityForResult(cameraIntent, PhotoHelper.REQUEST_CAMERA);
        } else {
            Toast.makeText(this, "Erro ao abrir a câmera.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = photoHelper.createGalleryIntent();
        startActivityForResult(galleryIntent, PhotoHelper.REQUEST_GALLERY);
    }

    private void removePhoto() {
        User user = dbHelper.getUserById(session.getUserId());
        if (user != null && user.getPhotoPath() != null) {
            PhotoHelper.deletePhoto(user.getPhotoPath());
            user.setPhotoPath("");
            dbHelper.updateUser(user);
            imgAvatar.setImageResource(0);
            imgAvatar.setBackgroundColor(getResources().getColor(R.color.gray_text));
            Toast.makeText(this, "Foto removida!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permissão da câmera negada.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        String photoPath = null;
        if (requestCode == PhotoHelper.REQUEST_CAMERA) {
            photoPath = photoHelper.processCameraResult();
        } else if (requestCode == PhotoHelper.REQUEST_GALLERY && data != null) {
            Uri imageUri = data.getData();
            photoPath = photoHelper.processGalleryResult(imageUri);
        }
        if (photoPath != null) {
            User user = dbHelper.getUserById(session.getUserId());
            if (user != null) {
                if (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty()) {
                    PhotoHelper.deletePhoto(user.getPhotoPath());
                }
                user.setPhotoPath(photoPath);
                dbHelper.updateUser(user);
            }
            PhotoHelper.loadImageIntoView(imgAvatar, photoPath);
            Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
        }
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
            case "ADMIN":
                roleLabel = "Administrador";
                break;
            case "ORGANIZER":
                roleLabel = "Organizador";
                break;
            case "READER":
                roleLabel = "Leitor";
                break;
            default:
                roleLabel = "Convidado";
                break;
        }
        tvUserRole.setText(roleLabel);
        editName.setText(user.getName());
        editBio.setText(user.getBio());
        if (user.getFavoriteGenres() != null && !user.getFavoriteGenres().isEmpty()) {
            selectedGenres.clear();
            for (String g : user.getFavoriteGenres().split(",")) {
                selectedGenres.add(g.trim());
            }
        }
        if (user.getPhotoPath() != null && !user.getPhotoPath().isEmpty()) {
            PhotoHelper.loadImageIntoView(imgAvatar, user.getPhotoPath());
        }
        if (switchPrivateProfile != null) {
            switchPrivateProfile.setChecked(user.isPrivate());
        }
    }

    private void saveProfile() {
        String name = editName.getText().toString().trim();
        String bio = editBio.getText().toString().trim();
        if (selectedGenres.isEmpty()) {
            Toast.makeText(this, "Selecione pelo menos um gênero literário.", Toast.LENGTH_SHORT).show();
            return;
        }
        String genres = String.join(", ", selectedGenres);
        if (name.isEmpty()) {
            Toast.makeText(this, "O nome não pode estar vazio.", Toast.LENGTH_SHORT).show();
            return;
        }
        User user = dbHelper.getUserById(session.getUserId());
        if (user == null)
            return;
        user.setName(name);
        user.setBio(bio);
        user.setFavoriteGenres(genres);
        user.setLanguage("Português");
        if (switchPrivateProfile != null) {
            user.setPrivate(switchPrivateProfile.isChecked());
        }
        if (dbHelper.updateUser(user)) {
            session.createLoginSession(user.getId(), user.getName(), user.getEmail(), user.getRole());
            tvUserName.setText(name);
            Toast.makeText(this, "Perfil atualizado!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Erro ao salvar o perfil.", Toast.LENGTH_SHORT).show();
        }
    }
}
