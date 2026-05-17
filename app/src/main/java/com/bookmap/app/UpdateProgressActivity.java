package com.bookmap.app;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.UserBook;
import com.bookmap.app.util.SessionManager;
public class UpdateProgressActivity extends AppCompatActivity {
    public static final String EXTRA_BOOK_ID = "book_id";
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long bookId;
    private SeekBar seekProgress;
    private Spinner spinnerStatus;
    private TextView tvProgressValue, tvBookTitle, tvBookAuthor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_progress);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        bookId = getIntent().getLongExtra(EXTRA_BOOK_ID, -1);
        if (bookId == -1 || !session.isLoggedIn()) {
            finish();
            return;
        }
        tvBookTitle = findViewById(R.id.tvBookTitle);
        tvBookAuthor = findViewById(R.id.tvBookAuthor);
        seekProgress = findViewById(R.id.seekProgress);
        tvProgressValue = findViewById(R.id.tvProgressValue);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        Button btnSave = findViewById(R.id.btnSave);
        TextView btnBack = findViewById(R.id.btnBack);
        String[] statuses = {"Lendo", "Lido", "Quero Ler"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, statuses);
        spinnerStatus.setAdapter(adapter);
        loadCurrentProgress();
        seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvProgressValue.setText(progress + "%");
                if (progress == 100) {
                    spinnerStatus.setSelection(1); 
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        btnSave.setOnClickListener(v -> saveProgress());
        btnBack.setOnClickListener(v -> finish());
    }
    private void loadCurrentProgress() {
        UserBook userBook = dbHelper.getUserBook(session.getUserId(), bookId);
        if (userBook != null) {
            tvBookTitle.setText(userBook.getBookTitle());
            tvBookAuthor.setText(userBook.getBookAuthor());
            seekProgress.setProgress(userBook.getProgress());
            tvProgressValue.setText(userBook.getProgress() + "%");
            switch (userBook.getStatus()) {
                case "LENDO":
                    spinnerStatus.setSelection(0);
                    break;
                case "LIDO":
                    spinnerStatus.setSelection(1);
                    break;
                case "QUERO_LER":
                    spinnerStatus.setSelection(2);
                    break;
            }
        }
    }
    private void saveProgress() {
        int progress = seekProgress.getProgress();
        String statusText = spinnerStatus.getSelectedItem().toString();
        String status;
        switch (statusText) {
            case "Lendo":
                status = "LENDO";
                break;
            case "Lido":
                status = "LIDO";
                break;
            default:
                status = "QUERO_LER";
                break;
        }
        if ("LIDO".equals(status)) {
            progress = 100;
        }
        boolean updated = dbHelper.updateUserBookStatus(session.getUserId(), bookId, status, progress);
        if (updated) {
            FirebaseSyncHelper syncHelper = FirebaseSyncHelper.getInstance(this);
            syncHelper.syncUserBookToCloud(session.getUserId(), bookId, status, progress);
            Toast.makeText(this, "Progresso atualizado!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao atualizar progresso", Toast.LENGTH_SHORT).show();
        }
    }
}
