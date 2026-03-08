package com.bookmap.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.SessionManager;

/**
 * ReportActivity - report a user or content.
 */
public class ReportActivity extends AppCompatActivity {

    public static final String EXTRA_REPORTED_USER_ID = "reported_user_id";
    public static final String EXTRA_REPORTED_CONTENT_ID = "reported_content_id";
    public static final String EXTRA_CONTENT_TYPE = "content_type";

    private RadioGroup radioReportType;
    private com.google.android.material.textfield.TextInputEditText editReason;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long reportedUserId;
    private long reportedContentId;
    private String contentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        reportedUserId = getIntent().getLongExtra(EXTRA_REPORTED_USER_ID, -1);
        reportedContentId = getIntent().getLongExtra(EXTRA_REPORTED_CONTENT_ID, 0);
        contentType = getIntent().getStringExtra(EXTRA_CONTENT_TYPE);
        if (contentType == null) contentType = "USER";

        radioReportType = findViewById(R.id.radioReportType);
        editReason = findViewById(R.id.editReason);

        Button btnSubmit = findViewById(R.id.btnSubmitReport);
        TextView btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> submitReport());
    }

    private void submitReport() {
        if (!session.isLoggedIn()) {
            Toast.makeText(this, "Faca login para denunciar", Toast.LENGTH_SHORT).show();
            return;
        }

        String reason = editReason.getText().toString().trim();

        int selectedId = radioReportType.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Selecione o tipo de denuncia", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selected = findViewById(selectedId);
        String type = selected.getText().toString();

        if (reason.isEmpty()) {
            Toast.makeText(this, "Descreva o motivo da denuncia", Toast.LENGTH_SHORT).show();
            return;
        }

        long result = dbHelper.insertReport(
                session.getUserId(),
                reportedUserId,
                reportedContentId,
                contentType,
                type + ": " + reason
        );

        if (result > 0) {
            Toast.makeText(this, "Denuncia enviada com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao enviar denuncia", Toast.LENGTH_SHORT).show();
        }
    }
}
