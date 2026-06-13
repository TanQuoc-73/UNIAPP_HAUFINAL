package com.example.uniapp_haufinal.activity.chat;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "item_id";
    public static final String EXTRA_PARTNER_ID = "partner_id";
    public static final String EXTRA_PARTNER_NAME = "partner_name";

    private String partnerId, itemId, partnerName, currentUserId, chatRoomId;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private LinearLayout chatContainer;
    private EditText edtMessage;
    private Button btnSend;
    private TextView txtHeaderTitle, txtBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        chatContainer = findViewById(R.id.chatContainer);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        txtHeaderTitle = findViewById(R.id.txtHeaderTitle);
        txtBack = findViewById(R.id.txtBack);

        txtBack.setOnClickListener(v -> finish());
        
        setupBottomNavigation();
        handleIntentData();

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.navMarket).setOnClickListener(v -> startActivity(new Intent(this, MarketActivity.class)));
        findViewById(R.id.navPost).setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.post.CreatePostActivity.class)));
        findViewById(R.id.navMap).setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.navChat).setOnClickListener(v -> startActivity(new Intent(this, ChatListActivity.class)));
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        if (intent == null) return;

        itemId = intent.getStringExtra(EXTRA_ITEM_ID);
        partnerId = intent.getStringExtra(EXTRA_PARTNER_ID);
        partnerName = intent.getStringExtra(EXTRA_PARTNER_NAME);

        if (partnerId == null || currentUserId == null) {
            Toast.makeText(this, "Lỗi: Không xác định được người nhận", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtHeaderTitle.setText(partnerName != null ? partnerName : "Chat");

        // Tạo Chat Room ID duy nhất giữa 2 người
        String[] ids = {currentUserId, partnerId};
        Arrays.sort(ids);
        chatRoomId = ids[0] + "_" + ids[1];

        updateChatRoomMetadata(); // Lưu thông tin phòng chat để hiển thị trong danh sách
        listenForMessages();
    }

    private void updateChatRoomMetadata() {
        // Lưu/Cập nhật thông tin phòng chat để cả 2 user đều thấy trong danh sách "Chat đã từng chat"
        Map<String, Object> roomData = new HashMap<>();
        roomData.put("users", Arrays.asList(currentUserId, partnerId));
        roomData.put("lastUpdated", FieldValue.serverTimestamp());
        
        // Metadata cho người dùng hiện tại
        roomData.put("userName_" + currentUserId, auth.getCurrentUser().getDisplayName());
        roomData.put("userName_" + partnerId, partnerName);

        db.collection("chatRooms").document(chatRoomId).set(roomData, com.google.firebase.firestore.SetOptions.merge());
    }

    private void listenForMessages() {
        db.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                String senderId = dc.getDocument().getString("senderId");
                                String text = dc.getDocument().getString("text");
                                addMessageToUI(senderId, text);
                            }
                        }
                    }
                });
    }

    private void sendMessage() {
        String msgText = edtMessage.getText().toString().trim();
        if (msgText.isEmpty()) return;

        Map<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("text", msgText);
        message.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chatRooms")
                .document(chatRoomId)
                .collection("messages")
                .add(message);

        // Cập nhật tin nhắn cuối cùng để hiện ở danh sách chat
        Map<String, Object> lastMsgUpdate = new HashMap<>();
        lastMsgUpdate.put("lastMessage", msgText);
        lastMsgUpdate.put("lastUpdated", FieldValue.serverTimestamp());
        db.collection("chatRooms").document(chatRoomId).update(lastMsgUpdate);

        edtMessage.setText("");
    }

    private void addMessageToUI(String senderId, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setPadding(35, 20, 35, 20);
        tv.setTextSize(16);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 10, 0, 10);

        if (senderId != null && senderId.equals(currentUserId)) {
            tv.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
            tv.setTextColor(Color.BLACK);
            params.gravity = Gravity.END;
        } else {
            tv.setBackgroundResource(android.R.drawable.editbox_dropdown_dark_frame);
            tv.setTextColor(Color.WHITE);
            params.gravity = Gravity.START;
        }

        chatContainer.addView(tv, params);
    }
}
