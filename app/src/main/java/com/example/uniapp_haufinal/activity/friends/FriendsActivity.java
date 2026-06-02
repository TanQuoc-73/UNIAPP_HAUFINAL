package com.example.uniapp_haufinal.activity.friends;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity {
    TextView navHome, navMarket, navPost, navFriends, navMap, navProfile;
    LinearLayout userContainer;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navFriends = findViewById(R.id.navFriends);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        userContainer = findViewById(R.id.userContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> startActivity(new Intent(this, MarketActivity.class)));
        navPost.setOnClickListener(view -> startActivity(new Intent(this, CreatePostActivity.class)));
        navFriends.setOnClickListener(view -> {
            //dang o trang Ban be
        });
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));

        loadUsers();
    }

    private void loadUsers() {
        userContainer.removeAllViews();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chua dang nhap", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String uid = document.getId();

                        if (uid.equals(currentUser.getUid())) {
                            continue;
                        }

                        String name = document.getString("displayName");
                        String email = document.getString("email");
                        String phone = document.getString("phone");

                        if (name == null || name.isEmpty()) {
                            name = "Nguoi dung";
                        }
                        if (email == null) {
                            email = "";
                        }
                        if (phone == null || phone.isEmpty()) {
                            phone = "Chua cap nhat SDT";
                        }

                        loadFriendStatus(currentUser.getUid(), uid, name, email, phone);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong tai duoc danh sach ban be", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFriendStatus(String currentUserId, String otherUserId, String name, String email, String phone) {
        String friendshipId = taoFriendshipId(currentUserId, otherUserId);

        db.collection("friendships").document(friendshipId).get()
                .addOnSuccessListener(document -> {
                    String status = "none";
                    String senderId = "";

                    if (document.exists()) {
                        status = document.getString("status");
                        senderId = document.getString("senderId");
                    }

                    if (status == null || status.isEmpty()) {
                        status = "none";
                    }
                    if (senderId == null) {
                        senderId = "";
                    }

                    addUserView(currentUserId, otherUserId, name, email, phone, status, senderId);
                })
                .addOnFailureListener(e -> {
                    addUserView(currentUserId, otherUserId, name, email, phone, "none", "");
                });
    }

    private void addUserView(String currentUserId, String otherUserId, String name, String email, String phone,
                             String friendStatus, String senderId) {
        LinearLayout userLayout = new LinearLayout(this);
        userLayout.setOrientation(LinearLayout.VERTICAL);
        userLayout.setPadding(20, 20, 20, 20);
        userLayout.setBackgroundColor(Color.rgb(242, 242, 242));

        TextView txtName = new TextView(this);
        txtName.setText(name);
        txtName.setTextColor(Color.BLACK);
        txtName.setTextSize(18);
        txtName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtInfo = new TextView(this);
        txtInfo.setText("Email: " + email + "\nSDT: " + phone);
        txtInfo.setTextColor(Color.DKGRAY);
        txtInfo.setTextSize(14);

        TextView txtStatus = new TextView(this);
        txtStatus.setText(getStatusText(currentUserId, friendStatus, senderId));
        txtStatus.setTextColor(Color.rgb(25, 118, 210));
        txtStatus.setTextSize(14);
        txtStatus.setPadding(0, 8, 0, 8);

        TextView btnFriend = new TextView(this);
        btnFriend.setText(getButtonText(currentUserId, friendStatus, senderId));
        btnFriend.setTextColor(Color.WHITE);
        btnFriend.setTextSize(15);
        btnFriend.setGravity(android.view.Gravity.CENTER);
        btnFriend.setBackgroundColor(Color.BLACK);
        btnFriend.setPadding(16, 14, 16, 14);
        btnFriend.setOnClickListener(view -> xuLyKetBan(currentUserId, otherUserId, friendStatus, senderId));

        userLayout.addView(txtName);
        userLayout.addView(txtInfo);
        userLayout.addView(txtStatus);

        if (!friendStatus.equals("accepted")) {
            userLayout.addView(btnFriend);
        }

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);

        userContainer.addView(userLayout, params);
    }

    private String getStatusText(String currentUserId, String status, String senderId) {
        if (status.equals("accepted")) {
            return "Trang thai: Ban be";
        }

        if (status.equals("pending")) {
            if (senderId.equals(currentUserId)) {
                return "Trang thai: Da gui loi moi";
            } else {
                return "Trang thai: Dang cho ban chap nhan";
            }
        }

        return "Trang thai: Chua ket ban";
    }

    private String getButtonText(String currentUserId, String status, String senderId) {
        if (status.equals("pending")) {
            if (senderId.equals(currentUserId)) {
                return "Huy loi moi";
            } else {
                return "Chap nhan";
            }
        }

        return "Them ban";
    }

    private void xuLyKetBan(String currentUserId, String otherUserId, String status, String senderId) {
        String friendshipId = taoFriendshipId(currentUserId, otherUserId);

        if (status.equals("pending")) {
            if (senderId.equals(currentUserId)) {
                huyLoiMoi(friendshipId);
            } else {
                chapNhanKetBan(friendshipId);
            }
        } else {
            guiLoiMoi(currentUserId, otherUserId, friendshipId);
        }
    }

    private void guiLoiMoi(String currentUserId, String otherUserId, String friendshipId) {
        Map<String, Object> data = new HashMap<>();
        data.put("user1Id", currentUserId);
        data.put("user2Id", otherUserId);
        data.put("senderId", currentUserId);
        data.put("receiverId", otherUserId);
        data.put("status", "pending");
        data.put("createdAt", FieldValue.serverTimestamp());
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("friendships").document(friendshipId).set(data)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Da gui loi moi ket ban", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gui loi moi that bai", Toast.LENGTH_SHORT).show();
                });
    }

    private void chapNhanKetBan(String friendshipId) {
        db.collection("friendships").document(friendshipId).update(
                "status", "accepted",
                "updatedAt", FieldValue.serverTimestamp()
        ).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Da chap nhan ket ban", Toast.LENGTH_SHORT).show();
            loadUsers();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Chap nhan that bai", Toast.LENGTH_SHORT).show();
        });
    }

    private void huyLoiMoi(String friendshipId) {
        db.collection("friendships").document(friendshipId).delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Da huy loi moi", Toast.LENGTH_SHORT).show();
                    loadUsers();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Huy loi moi that bai", Toast.LENGTH_SHORT).show();
                });
    }

    private String taoFriendshipId(String uid1, String uid2) {
        if (uid1.compareTo(uid2) < 0) {
            return uid1 + "_" + uid2;
        } else {
            return uid2 + "_" + uid1;
        }
    }
}
