package com.example.uniapp_haufinal.activity.post;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uniapp_haufinal.R;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uniapp_haufinal.activity.friends.FriendsActivity;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;

//firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    TextView txtBack;
    TextView navHome, navMarket, navPost, navFriends, navMap, navProfile;
    EditText edtContent;
    Button btnSubmitPost;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_post);

        txtBack = findViewById(R.id.txtBack);
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navFriends = findViewById(R.id.navFriends);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        edtContent = findViewById(R.id.edtContent);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(view->{
            finish();
        });
        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> startActivity(new Intent(this, MarketActivity.class)));
        navPost.setOnClickListener(view -> {
            // dang o trang dang bai
        });
        navFriends.setOnClickListener(view -> startActivity(new Intent(this, FriendsActivity.class)));
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));

        //dang bai
        btnSubmitPost.setOnClickListener(view ->{
            FirebaseUser currentUser = auth.getCurrentUser();

            if(currentUser ==null){
                Toast.makeText(this, "Chua dang nhap", Toast.LENGTH_SHORT).show();
                return;
            }

            String content = edtContent.getText().toString().trim();

            if(content.isEmpty()){
                Toast.makeText(this, "Chua nhap noi dung bai viet", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(document->{
                        String authorName = document.getString("displayName");

                        if(authorName ==null || authorName.isEmpty()){
                            authorName = document.getString("email");
                        }

                        Map<String, Object> post = new HashMap<>();
                        post.put("authorId",uid);
                        post.put("authorName",authorName);
                        post.put("content",content);
                        post.put("status","approved");
                        post.put("visibility","public");
                        post.put("likeCount", 0);
                        post.put("commentCount", 0);
                        post.put("createdAt",FieldValue.serverTimestamp());
                        post.put("updatedAt", FieldValue.serverTimestamp());

                        db.collection("posts").add(post)
                                .addOnSuccessListener(documentReference ->{
                                    String postId = documentReference.getId();
                                    documentReference.update("postId",postId)
                                            .addOnSuccessListener(unused->{
                                                Toast.makeText(this,"Dang bai thanh cong",Toast.LENGTH_SHORT).show();
                                                finish();
                                            });
                        })
                                .addOnFailureListener(e->{
                                    Toast.makeText(this, "Dang bai that bai",Toast.LENGTH_SHORT).show();

                                });

                    }) .addOnFailureListener(e->{
                        Toast.makeText(this, "Khong lay duoc thong tin user", Toast.LENGTH_SHORT).show();
                    });
        });


    }
}
