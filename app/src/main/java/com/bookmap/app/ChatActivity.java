package com.bookmap.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bookmap.app.adapter.MessageAdapter;
import com.bookmap.app.database.DatabaseHelper;
import com.bookmap.app.database.FirebaseSyncHelper;
import com.bookmap.app.model.Message;
import com.bookmap.app.model.User;
import com.bookmap.app.util.SessionManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    public static final String EXTRA_OTHER_USER_ID = "other_user_id";
    private DatabaseHelper dbHelper;
    private SessionManager session;
    private long otherUserId;
    
    private RecyclerView recyclerChat;
    private MessageAdapter messageAdapter;
    private EditText editMessageContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        dbHelper = DatabaseHelper.getInstance(this);
        session = new SessionManager(this);

        otherUserId = getIntent().getLongExtra(EXTRA_OTHER_USER_ID, -1);
        if (!session.isLoggedIn() || otherUserId == -1) {
            finish();
            return;
        }

        TextView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        ImageView imgChatUserPhoto = findViewById(R.id.imgChatUserPhoto);
        TextView tvChatUserName = findViewById(R.id.tvChatUserName);
        View layoutChatHeader = findViewById(R.id.layoutChatHeader);

        User otherUser = dbHelper.getUserById(otherUserId);
        if (otherUser != null) {
            tvChatUserName.setText(otherUser.getName());
            if (otherUser.getPhotoPath() != null && !otherUser.getPhotoPath().isEmpty()) {
                Glide.with(this).load(otherUser.getPhotoPath()).transform(new CircleCrop()).into(imgChatUserPhoto);
            }
            layoutChatHeader.setOnClickListener(v -> {
                Intent intent = new Intent(this, PublicProfileActivity.class);
                intent.putExtra(PublicProfileActivity.EXTRA_USER_ID, otherUserId);
                startActivity(intent);
            });
        } else {
            tvChatUserName.setText("Usuário Desconhecido");
        }

        recyclerChat = findViewById(R.id.recyclerChat);
        editMessageContent = findViewById(R.id.editMessageContent);
        TextView btnSendMessage = findViewById(R.id.btnSendMessage);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        
        messageAdapter = new MessageAdapter(new ArrayList<>(), session.getUserId(), (msg, position) -> deleteMessage(msg));
        recyclerChat.setAdapter(messageAdapter);

        btnSendMessage.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages();
        syncMessages();
    }

    private void syncMessages() {
        try {
            FirebaseSyncHelper.getInstance(this).pullMessagesFromCloud(session.getUserId(), success -> {
                if (success) {
                    runOnUiThread(this::loadMessages);
                }
            });
        } catch (Exception e) {
            android.util.Log.e("ChatActivity", "Error syncing messages", e);
        }
    }

    private void loadMessages() {
        dbHelper.markMessagesAsRead(session.getUserId(), otherUserId);
        List<Message> messages = dbHelper.getMessagesBetween(session.getUserId(), otherUserId);
        messageAdapter.updateData(messages);
        if (!messages.isEmpty()) {
            recyclerChat.scrollToPosition(messages.size() - 1);
        }
    }

    private void sendMessage() {
        String content = editMessageContent.getText().toString().trim();
        if (content.isEmpty()) return;

        String msgId = UUID.randomUUID().toString();
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Message msg = new Message(msgId, session.getUserId(), otherUserId, content, timestamp, false);
        
        if (dbHelper.insertMessage(msg)) {
            editMessageContent.setText("");
            loadMessages(); // Refresh UI instantly
            
            // Push to cloud
            try {
                FirebaseSyncHelper.getInstance(this).pushMessageToCloud(msg, success -> {
                    if (!success) {
                        runOnUiThread(() -> Toast.makeText(this, "Erro ao enviar para a nuvem.", Toast.LENGTH_SHORT).show());
                    }
                });
            } catch (Exception e) {
                android.util.Log.e("ChatActivity", "Error pushing message", e);
            }
        } else {
            Toast.makeText(this, "Erro ao salvar mensagem.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteMessage(Message msg) {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Excluir Mensagem")
            .setMessage("Deseja apagar esta mensagem? Ela será apagada apenas para você.")
            .setPositiveButton("Apagar", (dialog, which) -> {
                if (dbHelper.deleteMessageLocal(msg.getId())) {
                    loadMessages();
                    try {
                        FirebaseSyncHelper.getInstance(this).softDeleteMessageFromCloud(msg.getId(), session.getUserId());
                    } catch (Exception e) {
                        android.util.Log.e("ChatActivity", "Error on soft delete", e);
                    }
                }
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
}
