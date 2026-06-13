package com.example.uniapp_haufinal.activity.profile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.auth.LoginActivity;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.market.AddMarketItemActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {

    private TextView txtDisplayName, txtDisplayEmail, txtDisplayPhone, txtDisplayRole;
    private EditText edtName, edtPhone;
    private LinearLayout layoutInfoDisplay, layoutInfoEdit, postContainer;
    private ImageButton btnSettings;
    private ImageView imgAvatar;
    private Button btnSaveProfile, btnCancelEdit;
    
    private TextView navHome, navMarket, navPost, navMap, navProfile, navChat;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private String uid;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    android.net.Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        uploadAvatar(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Firebase init
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        uid = currentUser.getUid();

        // Bind Views
        btnSettings = findViewById(R.id.btnSettings);
        imgAvatar = findViewById(R.id.imgAvatar);
        txtDisplayName = findViewById(R.id.txtDisplayName);
        txtDisplayEmail = findViewById(R.id.txtDisplayEmail);
        txtDisplayPhone = findViewById(R.id.txtDisplayPhone);
        txtDisplayRole = findViewById(R.id.txtDisplayRole);

        layoutInfoDisplay = findViewById(R.id.layoutInfoDisplay);
        layoutInfoEdit = findViewById(R.id.layoutInfoEdit);
        edtName = findViewById(R.id.edtName);
        edtPhone = findViewById(R.id.edtPhone);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);

        postContainer = findViewById(R.id.postContainer);

        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        navChat = findViewById(R.id.navChat);

        // Setup Buttons
        btnSettings.setOnClickListener(this::showSettingsMenu);
        imgAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnCancelEdit.setOnClickListener(v -> toggleEditMode(false));

        setupNavigation();
        loadUserProfile();
        loadMyPosts();
    }

    private void showSettingsMenu(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.getMenu().add("Sửa thông tin");
        popup.getMenu().add("Đăng xuất");

        popup.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals("Sửa thông tin")) {
                toggleEditMode(true);
            } else if (item.getTitle().equals("Đăng xuất")) {
                logout();
            }
            return true;
        });
        popup.show();
    }

    private void toggleEditMode(boolean isEdit) {
        if (isEdit) {
            layoutInfoDisplay.setVisibility(View.GONE);
            layoutInfoEdit.setVisibility(View.VISIBLE);
            edtName.setText(txtDisplayName.getText());
            // Lấy phone từ text hiển thị (bỏ phần "SĐT: ")
            String currentPhone = txtDisplayPhone.getText().toString().replace("SĐT: ", "");
            edtPhone.setText(currentPhone);
        } else {
            layoutInfoDisplay.setVisibility(View.VISIBLE);
            layoutInfoEdit.setVisibility(View.GONE);
        }
    }

    private void loadUserProfile() {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String name = document.getString("displayName");
                        if (name == null || name.isEmpty()) {
                            name = "User_" + uid.substring(0, 5);
                        }
                        txtDisplayName.setText(name);
                        txtDisplayEmail.setText("Email: " + document.getString("email"));
                        txtDisplayPhone.setText("SĐT: " + (document.getString("phone") != null ? document.getString("phone") : "Chưa cập nhật"));
                        txtDisplayRole.setText("Vai trò: " + (document.getString("role") != null ? document.getString("role") : "Thành viên"));

                        if (!isDestroyed()) {
                            String avatarUrl = document.getString("avatarUrl");
                            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                Glide.with(this).load(avatarUrl).circleCrop().into(imgAvatar);
                            }
                        }
                    }
                });
    }

    private void saveProfile() {
        String newName = edtName.getText().toString().trim();
        String newPhone = edtPhone.getText().toString().trim();

        if (newName.isEmpty()) {
            Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users").document(uid).update(
                "displayName", newName,
                "phone", newPhone,
                "updatedAt", FieldValue.serverTimestamp()
        ).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
            loadUserProfile();
            toggleEditMode(false);
        }).addOnFailureListener(e -> Toast.makeText(this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show());
    }

    private void uploadAvatar(android.net.Uri uri) {
        String fileName = "avatars/" + uid;
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    db.collection("users").document(uid).update("avatarUrl", downloadUri.toString())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Đã cập nhật ảnh đại diện", Toast.LENGTH_SHORT).show();
                                if (!isDestroyed()) {
                                    Glide.with(this).load(downloadUri).circleCrop().into(imgAvatar);
                                }
                            });
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi tải ảnh", Toast.LENGTH_SHORT).show());
    }

    private void loadMyPosts() {
        postContainer.removeAllViews();
        // Lấy bài đăng Market
        db.collection("marketItems")
                .whereEqualTo("sellerId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        TextView marketHeader = new TextView(this);
                        marketHeader.setText("Rao vặt");
                        marketHeader.setTextSize(16);
                        marketHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                        marketHeader.setPadding(16, 16, 16, 8);
                        postContainer.addView(marketHeader);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            addMarketItemToUI(doc);
                        }
                    }
                    loadMyFeedPosts();
                })
                .addOnFailureListener(e -> loadMyFeedPosts());
    }

    private void loadMyFeedPosts() {
        db.collection("posts")
                .whereEqualTo("authorId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        TextView feedHeader = new TextView(this);
                        feedHeader.setText("Bài viết Newsfeed");
                        feedHeader.setTextSize(16);
                        feedHeader.setTypeface(null, android.graphics.Typeface.BOLD);
                        feedHeader.setPadding(16, 16, 16, 8);
                        postContainer.addView(feedHeader);

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            addFeedPostToUI(doc);
                        }
                    }

                    if (postContainer.getChildCount() == 0) {
                        TextView tvEmpty = new TextView(this);
                        tvEmpty.setText("Bạn chưa có bài đăng nào");
                        tvEmpty.setGravity(android.view.Gravity.CENTER);
                        tvEmpty.setPadding(0, 50, 0, 50);
                        postContainer.addView(tvEmpty);
                    }
                });
    }

    private void addMarketItemToUI(QueryDocumentSnapshot doc) {
        String itemId = doc.getId();
        String title = doc.getString("title");
        String status = doc.getString("status");
        Long price = doc.getLong("price");

        LinearLayout itemLayout = createItemCard();

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextSize(18);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvInfo = new TextView(this);
        tvInfo.setText(price + " VND • " + status);
        tvInfo.setTextColor(Color.GRAY);

        LinearLayout actionLayout = new LinearLayout(this);
        actionLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionLayout.setPadding(0, 16, 0, 0);

        Button btnEdit = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnEdit.setText("Sửa");
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddMarketItemActivity.class);
            intent.putExtra("isEdit", true);
            intent.putExtra("itemId", itemId);
            startActivity(intent);
        });

        Button btnDelete = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnDelete.setText("Xóa");
        btnDelete.setTextColor(Color.RED);
        btnDelete.setOnClickListener(v -> {
            db.collection("marketItems").document(itemId).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã xóa bài đăng", Toast.LENGTH_SHORT).show();
                        loadMyPosts();
                    });
        });

        actionLayout.addView(btnEdit);
        actionLayout.addView(btnDelete);

        itemLayout.addView(tvTitle);
        itemLayout.addView(tvInfo);
        itemLayout.addView(actionLayout);

        postContainer.addView(itemLayout);
    }

    private void addFeedPostToUI(QueryDocumentSnapshot doc) {
        String postId = doc.getId();
        String content = doc.getString("content");
        String imageUrl = doc.getString("imageUrl");

        LinearLayout itemLayout = createItemCard();

        TextView tvContent = new TextView(this);
        tvContent.setText(content);
        tvContent.setTextSize(15);
        tvContent.setTextColor(Color.BLACK);
        tvContent.setMaxLines(3);
        tvContent.setEllipsize(android.text.TextUtils.TruncateAt.END);

        itemLayout.addView(tvContent);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            ImageView imageView = new ImageView(this);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 400);
            imgParams.setMargins(0, 8, 0, 8);
            itemLayout.addView(imageView, imgParams);
            if (!isDestroyed()) {
                Glide.with(this).load(imageUrl).into(imageView);
            }
        }

        Button btnDelete = new Button(this, null, android.R.attr.buttonStyleSmall);
        btnDelete.setText("Xóa bài viết");
        btnDelete.setTextColor(Color.RED);
        btnDelete.setOnClickListener(v -> {
            db.collection("posts").document(postId).delete()
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã xóa bài viết", Toast.LENGTH_SHORT).show();
                        loadMyPosts();
                    });
        });
        itemLayout.addView(btnDelete);

        postContainer.addView(itemLayout);
    }

    private LinearLayout createItemCard() {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(32, 32, 32, 32);
        itemLayout.setBackgroundColor(Color.WHITE);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 8, 0, 8);
        itemLayout.setLayoutParams(params);
        return itemLayout;
    }

    private void setupNavigation() {
        navHome.setOnClickListener(v -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(v -> startActivity(new Intent(this, MarketActivity.class)));
        navPost.setOnClickListener(v -> startActivity(new Intent(this, CreatePostActivity.class)));
        navMap.setOnClickListener(v -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(v -> { /* current */ });
        navChat.setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.chat.ChatListActivity.class)));
    }

    private void logout() {
        auth.signOut();
        Intent intent = new Intent(this, com.example.uniapp_haufinal.activity.auth.LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
