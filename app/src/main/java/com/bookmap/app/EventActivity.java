package com.bookmap.app;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.util.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

/**
 * EventActivity - create a new event within a club.
 */
public class EventActivity extends AppCompatActivity {

    public static final String EXTRA_CLUB_ID = "club_id";

    private TextInputEditText editTitle, editDescription, editLocation, editBook;
    private TextView tvSelectedDate, tvSelectedTime;
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long clubId;
    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        clubId = getIntent().getLongExtra(EXTRA_CLUB_ID, -1);
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
                tvSelectedDate.setText(selectedDate);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        btnSelectTime.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new TimePickerDialog(this, (view, hour, minute) -> {
                selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
                tvSelectedTime.setText(selectedTime);
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
        });

        btnCreateEvent.setOnClickListener(v -> createEvent());
    }

    private void createEvent() {
        String title = editTitle.getText().toString().trim();
        String description = editDescription.getText().toString().trim();
        String location = editLocation.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Titulo do evento e obrigatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Selecione data e hora", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateTime = selectedDate + " " + selectedTime;

        long result = dbHelper.insertEvent(clubId, title, description, dateTime, location, 0, session.getUserId());
        if (result > 0) {
            Toast.makeText(this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao criar evento", Toast.LENGTH_SHORT).show();
        }
    }
}
