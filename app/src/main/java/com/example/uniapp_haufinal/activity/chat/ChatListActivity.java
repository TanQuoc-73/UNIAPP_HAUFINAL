package com.example.uniapp_haufinal.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatListActivity extends AppCompatActivity {

    private ListView listView;
    private List<ChatSummary> chatList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        listView = findViewById(R.id.listViewChats);
        chatList = new ArrayList<>();
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;

        findViewById(R.id.txtBack).setOnClickListener(v -> finish());
        setupBottomNavigation();

        if (currentUserId != null) {
            loadChatRooms();
        }
    }

    private void setupBottomNavigation() {
        findViewById(R.id.navHome).setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        findViewById(R.id.navMarket).setOnClickListener(v -> startActivity(new Intent(this, MarketActivity.class)));
        findViewById(R.id.navPost).setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.post.CreatePostActivity.class)));
        findViewById(R.id.navMap).setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        findViewById(R.id.navProfile).setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        findViewById(R.id.navChat).setOnClickListener(v -> { /* current */ });
    }

    private void loadChatRooms() {
        db.collection("chatRooms")
                .whereArrayContains("users", currentUserId)
                .orderBy("lastUpdated", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    chatList.clear();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            String roomId = doc.getId();
                            String lastMessage = doc.getString("lastMessage");
                            List<String> users = (List<String>) doc.get("users");
                            
                            String partnerId = "";
                            for (String uid : users) {
                                if (!uid.equals(currentUserId)) {
                                    partnerId = uid;
                                    break;
                                }
                            }
                            
                            String partnerName = doc.getString("userName_" + partnerId);
                            chatList.add(new ChatSummary(roomId, partnerId, partnerName, lastMessage));
                        }
                        updateUI();
                    }
                });
    }

    private void updateUI() {
        ArrayAdapter<ChatSummary> adapter = new ArrayAdapter<ChatSummary>(this, android.R.layout.simple_list_item_2, android.R.id.text1, chatList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text1 = view.findViewById(android.R.id.text1);
                TextView text2 = view.findViewById(android.R.id.text2);

                ChatSummary chat = chatList.get(position);
                text1.setText(chat.partnerName != null ? chat.partnerName : "Người dùng (" + chat.partnerId + ")");
                text2.setText(chat.lastMessage != null ? chat.lastMessage : "Chưa có tin nhắn");
                
                return view;
            }
        };

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            ChatSummary selected = chatList.get(position);
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_PARTNER_ID, selected.partnerId);
            intent.putExtra(ChatActivity.EXTRA_PARTNER_NAME, selected.partnerName);
            startActivity(intent);
        });
    }

    private static class ChatSummary {
        String roomId, partnerId, partnerName, lastMessage;
        ChatSummary(String roomId, String partnerId, String partnerName, String lastMessage) {
            this.roomId = roomId;
            this.partnerId = partnerId;
            this.partnerName = partnerName;
            this.lastMessage = lastMessage;
        }
    }
}
