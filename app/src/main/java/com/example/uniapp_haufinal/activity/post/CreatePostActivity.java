package com.example.uniapp_haufinal.activity.post;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uniapp_haufinal.R;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

//firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreatePostActivity extends AppCompatActivity {

    TextView txtBack;
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
        edtContent = findViewById(R.id.edtContent);
        btnSubmitPost = findViewById(R.id.btnSubmitPost);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(view->{
            finish();
        });

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