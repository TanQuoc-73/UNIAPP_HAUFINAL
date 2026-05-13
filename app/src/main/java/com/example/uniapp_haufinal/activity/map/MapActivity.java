package com.example.uniapp_haufinal.activity.map;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.TextView;

import com.example.uniapp_haufinal.R;


public class MapActivity extends AppCompatActivity {
    TextView txtBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        txtBack = findViewById(R.id.txtBack);
        txtBack.setOnClickListener(view -> {
            finish();
        });

    }
}