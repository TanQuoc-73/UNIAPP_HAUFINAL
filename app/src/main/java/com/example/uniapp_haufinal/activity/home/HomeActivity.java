package com.example.uniapp_haufinal.activity.home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.uniapp_haufinal.R;

//cac activity nav chuyen trang
import com.example.uniapp_haufinal.activity.friends.FriendsActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;

//firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;


public class HomeActivity extends AppCompatActivity {
    TextView navHome, navMap, navMarket, navProfile, navPost, navFriends;

    LinearLayout postContainer, friendContainer;
    FirebaseFirestore db;
    FirebaseAuth auth;
    HomePost homePost;
    HomeFriend homeFriend;

    @Override
    //reload bai viet
    protected void onResume(){
        super.onResume();
        if(homePost !=null){
            homePost.loadPost();
        }
        if(homeFriend !=null){
            homeFriend.loadFriends();
        }
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        postContainer = findViewById(R.id.postContainer);
        friendContainer = findViewById(R.id.friendContainer);
        homePost = new HomePost(this, postContainer, db);
        homeFriend = new HomeFriend(this, friendContainer, db, auth);

        //Nav
        //Home
        navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(view -> {
            homePost.loadPost();
            homeFriend.loadFriends();
            Toast.makeText(this, "Da tai lai bai viet", Toast.LENGTH_SHORT).show();
        });

        //Market
        navMarket = findViewById(R.id.navMarket);
        navMarket.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, MarketActivity.class);
            startActivity(intent);
        });

        //Ban be
        navFriends = findViewById(R.id.navFriends);
        navFriends.setOnClickListener(view ->{
            Intent intent = new Intent(HomeActivity.this, FriendsActivity.class);
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
