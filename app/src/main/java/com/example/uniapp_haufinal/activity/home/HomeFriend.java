package com.example.uniapp_haufinal.activity.home;

import android.app.Activity;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class HomeFriend {
    Activity activity;
    LinearLayout friendContainer;
    FirebaseFirestore db;
    FirebaseAuth auth;

    public HomeFriend(Activity activity, LinearLayout friendContainer, FirebaseFirestore db, FirebaseAuth auth) {
        this.activity = activity;
        this.friendContainer = friendContainer;
        this.db = db;
        this.auth = auth;
    }

    public void loadFriends() {
        friendContainer.removeAllViews();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            addFriendIcon("Ban be");
            return;
        }

        db.collection("friendships")
                .whereEqualTo("status", "accepted")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String user1Id = document.getString("user1Id");
                        String user2Id = document.getString("user2Id");
                        String friendId = null;

                        if (user1Id != null && user1Id.equals(currentUser.getUid())) {
                            friendId = user2Id;
                        } else if (user2Id != null && user2Id.equals(currentUser.getUid())) {
                            friendId = user1Id;
                        }

                        if (friendId != null && !friendId.isEmpty()) {
                            count++;
                            loadFriendName(friendId);
                        }
                    }

                    if (count == 0) {
                        addFriendIcon("Chua co");
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(activity, "Khong tai duoc ban be", Toast.LENGTH_SHORT).show();
                    addFriendIcon("Ban be");
                });
    }

    private void loadFriendName(String friendId) {
        db.collection("users").document(friendId).get()
                .addOnSuccessListener(document -> {
                    String name = document.getString("displayName");
                    String email = document.getString("email");

                    if (name == null || name.isEmpty()) {
                        name = email;
                    }
                    if (name == null || name.isEmpty()) {
                        name = "Ban be";
                    }

                    addFriendIcon(shortName(name));
                });
    }

    private void addFriendIcon(String name) {
        int size = dp(72);
        int margin = dp(12);

        TextView txtFriend = new TextView(activity);
        txtFriend.setText(name);
        txtFriend.setTextColor(Color.BLACK);
        txtFriend.setTextSize(13);
        txtFriend.setGravity(android.view.Gravity.CENTER);
        txtFriend.setBackgroundColor(Color.rgb(238, 238, 238));
        txtFriend.setSingleLine(false);
        txtFriend.setMaxLines(2);
        txtFriend.setPadding(4, 4, 4, 4);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                size,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        params.setMargins(0, 0, margin, 0);

        friendContainer.addView(txtFriend, params);
    }

    private int dp(int value) {
        return (int) (value * activity.getResources().getDisplayMetrics().density);
    }

    private String shortName(String name) {
        name = name.trim();
        if (name.length() > 10) {
            return name.substring(0, 10);
        }
        return name;
    }
}
