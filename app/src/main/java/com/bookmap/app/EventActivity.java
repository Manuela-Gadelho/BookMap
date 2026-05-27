package com.bookmap.app;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.SessionManager;
import android.widget.EditText;
import java.util.Calendar;
import java.util.Locale;

public class EventActivity extends AppCompatActivity {
    public static final String EXTRA_CLUB_ID = "club_id";
    public static final String EXTRA_EVENT_ID = "event_id";
    private EditText editTitle, editDescription, editLocation, editBook;
    private TextView tvSelectedDate, tvSelectedTime;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long clubId;
    private long eventId = -1;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);
        clubId = getIntent().getLongExtra(EXTRA_CLUB_ID, -1);
        eventId = getIntent().getLongExtra(EXTRA_EVENT_ID, -1);
        if (eventId != -1) {
            com.bookmap.app.model.Event event = dbHelper.getEventById(eventId);
            if (event != null) {
                clubId = event.getClubId();
            }
        }
        if (clubId == -1) {
            finish();
            return;
        }
        editTitle = findViewById(R.id.editEventTitle);
        editDescription = findViewById(R.id.editEventDescription);
        editLocation = findViewById(R.id.editEventLocation);
        editBook = findViewById(R.id.editEventBook);
        tvSelectedDate = findViewById(R.id.tvSelectedDateTime);
        tvSelectedTime = findViewById(R.id.tvSelectedDateTime);
        Button btnSelectDate = findViewById(R.id.btnSelectDate);
        Button btnSelectTime = findViewById(R.id.btnSelectTime);
        Button btnCreateEvent = findViewById(R.id.btnCreateEvent);
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());
        btnSelectDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this, (view, year, month, day) -> {
                selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, day);
                String dt = selectedDate + " " + selectedTime;
                tvSelectedDate.setText(com.bookmap.app.util.DateUtil.formatToBrazilian(dt.trim()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
        btnSelectTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                String dt = selectedDate + " " + selectedTime;
                tvSelectedTime.setText(com.bookmap.app.util.DateUtil.formatToBrazilian(dt.trim()));
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });
        btnCreateEvent.setOnClickListener(v -> createEvent());

        if (eventId != -1) {
            com.bookmap.app.model.Event event = dbHelper.getEventById(eventId);
            if (event != null) {
                editTitle.setText(event.getTitle());
                editDescription.setText(event.getDescription());
                editLocation.setText(event.getLocation());
                String dt = event.getDateTime();
                if (dt != null && dt.contains(" ")) {
                    String[] parts = dt.split(" ");
                    selectedDate = parts[0];
                    selectedTime = parts[1];
                    tvSelectedDate.setText(com.bookmap.app.util.DateUtil.formatToBrazilian(dt));
                } else if (dt != null) {
                    tvSelectedDate.setText(com.bookmap.app.util.DateUtil.formatToBrazilian(dt));
                }
                boolean isCreator = event.getCreatedBy() == session.getUserId();
                TextView tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
                if (isCreator) {
                    if (tvHeaderTitle != null) {
                        tvHeaderTitle.setText("Editar Encontro");
                    }
                    btnCreateEvent.setVisibility(android.view.View.GONE);
                    android.view.View layoutEventActions = findViewById(R.id.layoutEventActions);
                    if (layoutEventActions != null) {
                        layoutEventActions.setVisibility(android.view.View.VISIBLE);
                    }
                    Button btnSaveEventChanges = findViewById(R.id.btnSaveEventChanges);
                    Button btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
                    if (btnSaveEventChanges != null) {
                        btnSaveEventChanges.setOnClickListener(v -> saveEventChanges());
                    }
                    if (btnDeleteEvent != null) {
                        btnDeleteEvent.setOnClickListener(v -> {
                            new AlertDialog.Builder(this)
                                    .setTitle("Excluir Encontro")
                                    .setMessage("Tem certeza de que deseja excluir este encontro?")
                                    .setPositiveButton("Sim", (dialog, which) -> {
                                        if (dbHelper.deleteEvent(eventId)) {
                                            Toast.makeText(this, "Encontro excluído com sucesso!", Toast.LENGTH_SHORT)
                                                    .show();
                                            finish();
                                        } else {
                                            Toast.makeText(this, "Erro ao excluir o encontro.", Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    })
                                    .setNegativeButton("Não", null)
                                    .show();
                        });
                    }
                } else {
                    if (tvHeaderTitle != null) {
                        tvHeaderTitle.setText("Detalhes do Encontro");
                    }
                    editTitle.setEnabled(false);
                    editDescription.setEnabled(false);
                    editLocation.setEnabled(false);
                    editBook.setEnabled(false);
                    btnSelectDate.setVisibility(android.view.View.GONE);
                    btnSelectTime.setVisibility(android.view.View.GONE);
                    btnCreateEvent.setVisibility(android.view.View.GONE);
                    android.view.View layoutEventActions = findViewById(R.id.layoutEventActions);
                    if (layoutEventActions != null) {
                        layoutEventActions.setVisibility(android.view.View.GONE);
                    }
                }
            }
        }
    }

    private void createEvent() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "O título do evento é obrigatório.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Selecione a data e a hora.", Toast.LENGTH_SHORT).show();
            return;
        }
        String dateTime = selectedDate + " " + selectedTime;
        long result = dbHelper.insertEvent(clubId, title, description, dateTime, location, 0, session.getUserId());
        if (result > 0) {
            Toast.makeText(this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao criar o evento.", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveEventChanges() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String location = editLocation.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "O título do evento é obrigatório.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Selecione a data e a hora.", Toast.LENGTH_SHORT).show();
            return;
        }
        String dateTime = selectedDate + " " + selectedTime;
        boolean success = dbHelper.updateEvent(eventId, title, description, dateTime, location, 0L);
        if (success) {
            Toast.makeText(this, "Evento atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao atualizar o evento.", Toast.LENGTH_SHORT).show();
        }
    }
}
