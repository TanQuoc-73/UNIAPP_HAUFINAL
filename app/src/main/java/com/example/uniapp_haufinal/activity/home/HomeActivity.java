package com.example.uniapp_haufinal.activity.home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.TextView;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.uniapp_haufinal.R;

//cac activity nav chuyen trang
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;

//firebase
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class HomeActivity extends AppCompatActivity {
    TextView navMap, navMarket, navProfile, navPost;

    LinearLayout postContainer;
    FirebaseFirestore db;


    //ham hien thi bai viet
    private void loadPost(){
        postContainer.removeAllViews();

        db.collection("posts")
                //loc bai viet
//                .whereEqualTo("status", "approved")
//                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (var document : queryDocumentSnapshots){
                        String authorName = document.getString("authorName");
                        String content = document.getString("content");
                        Long likeCount = document.getLong("likeCount");

                        if(authorName == null || authorName.isEmpty()){
                            authorName = "An danh";
                        }
                        if(content == null){
                            content = "";
                        }
                        if(likeCount == null){
                            likeCount = 0L;
                        }
                        LinearLayout postLayout = new LinearLayout(this);
                        postLayout.setOrientation(LinearLayout.VERTICAL);
                        postLayout.setPadding(16, 16, 16, 16);
                        postLayout.setBackgroundColor(Color.WHITE);

                        TextView txtAuthor = new TextView(this);
                        txtAuthor.setText(authorName);
                        txtAuthor.setTextColor(Color.BLACK);
                        txtAuthor.setTextSize(16);
                        txtAuthor.setTypeface(null, android.graphics.Typeface.BOLD);

                        TextView txtContent = new TextView(this);
                        txtContent.setText(content);
                        txtContent.setTextColor(Color.BLACK);
                        txtContent.setTextSize(15);
                        txtContent.setPadding(0, 8, 0, 8);

                        TextView txtLike = new TextView(this);
                        txtLike.setText(likeCount + " luot thich");
                        txtLike.setTextColor(Color.DKGRAY);
                        txtLike.setTextSize(14);

                        postLayout.addView(txtAuthor);
                        postLayout.addView(txtContent);
                        postLayout.addView(txtLike);

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 16);

                        postContainer.addView(postLayout, params);
                    }


                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong tai dc bai viet", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    //reload bai viet
    protected void onResume(){
        super.onResume();
        if(postContainer !=null){
            loadPost();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        postContainer = findViewById(R.id.postContainer);

        //Nav
        //Market
        navMarket = findViewById(R.id.navMarket);
        navMarket.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, MarketActivity.class);
            startActivity(intent);
        });

        //Map
        navMap = findViewById(R.id.navMap);
        navMap.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, MapActivity.class);
            startActivity(intent);
        });

        //profile
        navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(view->{
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        //post
        navPost = findViewById(R.id.navPost);
        navPost.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

    }
}
