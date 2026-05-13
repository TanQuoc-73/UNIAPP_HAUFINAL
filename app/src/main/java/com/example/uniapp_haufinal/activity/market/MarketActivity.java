package com.example.uniapp_haufinal.activity.market;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;


import com.example.uniapp_haufinal.R;


public class MarketActivity extends AppCompatActivity {

    TextView txtBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        txtBack = findViewById(R.id.txtBack);

        txtBack.setOnClickListener(view->{
            finish();
        });


    }
}