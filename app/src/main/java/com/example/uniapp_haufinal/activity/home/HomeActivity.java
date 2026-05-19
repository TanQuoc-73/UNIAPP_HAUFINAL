package com.example.uniapp_haufinal.activity.home;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.ImageView;

import com.example.uniapp_haufinal.R;

//cac activity de lam nav
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;

public class HomeActivity extends AppCompatActivity {
    ImageView navMap;
    ImageView navMarket;
    ImageView navProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);


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

        navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(view->{
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
