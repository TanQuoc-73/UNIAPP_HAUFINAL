package com.example.uniapp_haufinal.activity.chat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.friends.FriendsActivity;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatActivity extends AppCompatActivity {
    TextView navHome, navMarket, navPost, navFriends, navMap, navProfile;

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

        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navFriends = findViewById(R.id.navFriends);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> startActivity(new Intent(this, MarketActivity.class)));
        navPost.setOnClickListener(view -> startActivity(new Intent(this, CreatePostActivity.class)));
        navFriends.setOnClickListener(view -> startActivity(new Intent(this, FriendsActivity.class)));
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));
    }
}
