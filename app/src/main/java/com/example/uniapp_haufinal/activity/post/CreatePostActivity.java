package com.example.uniapp_haufinal.activity.post;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uniapp_haufinal.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

//firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    private TextView txtBack, txtUserName;
    private TextView navHome, navMarket, navPost, navMap, navProfile, navChat;
    private EditText edtContent;
    private Button btnSubmitPost;
    private LinearLayout btnAddPhoto;
    private ImageView imgSelected;
    private RelativeLayout layoutImagePreview;
    private ImageButton btnRemoveImage;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        imgSelected.setImageURI(selectedImageUri);
                        layoutImagePreview.setVisibility(View.VISIBLE);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        // Bind Views
        txtBack = findViewById(R.id.txtBack);
        txtUserName = findViewById(R.id.txtUserName);
        edtContent = findViewById(R.id.edtContent);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);
        btnAddPhoto = findViewById(R.id.btnAddPhoto);
        imgSelected = findViewById(R.id.imgSelected);
        layoutImagePreview = findViewById(R.id.layoutImagePreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);

        // Bottom Nav
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        navChat = findViewById(R.id.navChat);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        setupUI();
        loadUserInfo();
    }

    private void setupUI() {
        txtBack.setOnClickListener(v -> finish());

        btnAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        btnRemoveImage.setOnClickListener(v -> {
            selectedImageUri = null;
            layoutImagePreview.setVisibility(View.GONE);
        });

        btnSubmitPost.setOnClickListener(v -> submitPost());

        // Navigation
        navHome.setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.home.HomeActivity.class)));
        navMarket.setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.market.MarketActivity.class)));
        navMap.setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.map.MapActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.profile.ProfileActivity.class)));
        navChat.setOnClickListener(v -> startActivity(new Intent(this, com.example.uniapp_haufinal.activity.chat.ChatListActivity.class)));
    }

    private void loadUserInfo() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        txtUserName.setText(doc.getString("displayName"));
                    }
                });
    }

    private void submitPost() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String content = edtContent.getText().toString().trim();
        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitPost.setEnabled(false);
        if (selectedImageUri != null) {
            uploadImageAndPost(currentUser, content);
        } else {
            savePostToFirestore(currentUser, content, null);
        }
    }

    private void uploadImageAndPost(FirebaseUser user, String content) {
        String fileName = "posts/" + UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child(fileName);

        ref.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    savePostToFirestore(user, content, uri.toString());
                }))
                .addOnFailureListener(e -> {
                    btnSubmitPost.setEnabled(true);
                    Toast.makeText(this, "Lỗi tải ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void savePostToFirestore(FirebaseUser currentUser, String content, String imageUrl) {
        Map<String, Object> post = new HashMap<>();
        post.put("authorId", currentUser.getUid());
        post.put("authorName", txtUserName.getText().toString());
        post.put("content", content);
        post.put("status", "approved");
        post.put("likeCount", 0);
        post.put("commentCount", 0);
        post.put("createdAt", FieldValue.serverTimestamp());
        if (imageUrl != null) {
            post.put("imageUrl", imageUrl);
        }

        db.collection("posts").add(post)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Đăng bài thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitPost.setEnabled(true);
                    Toast.makeText(this, "Lỗi đăng bài", Toast.LENGTH_SHORT).show();
                });
    }
}
