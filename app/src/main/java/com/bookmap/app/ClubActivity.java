package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bookmap.app.adapter.EventAdapter;
import com.bookmap.app.adapter.UserAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.model.Club;
import com.bookmap.app.model.ClubMember;
import com.bookmap.app.model.Event;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * ClubActivity - view a specific club's details, members, and events.
 */
public class ClubActivity extends AppCompatActivity {

    public static final String EXTRA_CLUB_ID = "club_id";

    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long clubId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);

        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        clubId = getIntent().getLongExtra(EXTRA_CLUB_ID, -1);
        if (clubId == -1) {
            finish();
            return;
        }

        Club club = dbHelper.getClubById(clubId);
        if (club == null) {
            finish();
            return;
        }

        // Header
        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Club info
        TextView tvClubName = findViewById(R.id.tvClubName);
        TextView tvClubDescription = findViewById(R.id.tvClubDescription);
        TextView tvClubType = findViewById(R.id.tvClubType);

        tvClubName.setText(club.getName());
        tvClubDescription.setText(club.getDescription());
        tvClubType.setText(club.isPublic() ? "Publico" : "Privado");

        // Members - convert ClubMember list to User list for adapter
        RecyclerView recyclerMembers = findViewById(R.id.recyclerMembers);
        recyclerMembers.setLayoutManager(new LinearLayoutManager(this));
        List<ClubMember> clubMembers = dbHelper.getClubMembers(clubId);
        List<User> memberUsers = new ArrayList<>();
        for (ClubMember cm : clubMembers) {
            User u = dbHelper.getUserById(cm.getUserId());
            if (u != null) memberUsers.add(u);
        }
        UserAdapter memberAdapter = new UserAdapter(memberUsers, user -> {
            Intent intent = new Intent(this, PublicProfileActivity.class);
            intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, user.getId());
            startActivity(intent);
        }, false);
        recyclerMembers.setAdapter(memberAdapter);

        // Events
        RecyclerView recyclerEvents = findViewById(R.id.recyclerEvents);
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        List<Event> events = dbHelper.getClubEvents(clubId);
        EventAdapter eventAdapter = new EventAdapter(events);
        recyclerEvents.setAdapter(eventAdapter);

        TextView tvNoEvents = findViewById(R.id.tvNoEvents);
        if (events.isEmpty()) {
            tvNoEvents.setVisibility(View.VISIBLE);
        } else {
            tvNoEvents.setVisibility(View.GONE);
        }

        // Join club button
        Button btnJoinClub = findViewById(R.id.btnJoinClub);
        Button btnCreateEvent = findViewById(R.id.btnCreateEvent);

        if (session.isLoggedIn()) {
            boolean isMember = dbHelper.getClubMember(clubId, session.getUserId()) != null;
            if (isMember) {
                btnJoinClub.setText("Membro");
                btnJoinClub.setEnabled(false);
                btnCreateEvent.setVisibility(View.VISIBLE);
            } else {
                btnJoinClub.setOnClickListener(v -> {
                    long result = dbHelper.addClubMember(clubId, session.getUserId(), "MEMBER", "PENDING");
                    if (result > 0) {
                        Toast.makeText(this, "Solicitacao enviada!", Toast.LENGTH_SHORT).show();
                        btnJoinClub.setText("Pendente");
                        btnJoinClub.setEnabled(false);
                    }
                });
                btnCreateEvent.setVisibility(View.GONE);
            }

            // Create event
            btnCreateEvent.setOnClickListener(v -> {
                Intent intent = new Intent(this, EventActivity.class);
                intent.putExtra(EventActivity.EXTRA_CLUB_ID, clubId);
                startActivity(intent);
            });
        } else {
            btnJoinClub.setVisibility(View.GONE);
            btnCreateEvent.setVisibility(View.GONE);
        }
    }
}
