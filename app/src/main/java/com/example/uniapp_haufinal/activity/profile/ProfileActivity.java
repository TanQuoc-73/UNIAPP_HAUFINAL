package com.example.uniapp_haufinal.activity.profile;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FieldValue;

import com.example.uniapp_haufinal.R;

//firebase
import com.example.uniapp_haufinal.activity.auth.LoginActivity;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

//luu tt user dang array
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.google.firebase.firestore.SetOptions;

public class ProfileActivity extends AppCompatActivity {

//    TextView txtName, txtEmail, txtPhone, txtRole;
    EditText edtName, edtPhone;
    TextView txtEmail;
    TextView txtRole;
    TextView navHome, navMarket, navPost, navMap, navProfile;
    Button btnSaveProfile, btnLogout;

    FirebaseAuth auth;
    FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtName = findViewById(R.id.edtName);
        txtEmail = findViewById(R.id.txtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        txtRole = findViewById(R.id.txtRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnLogout = findViewById(R.id.btnLogout);
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        if(currentUser == null){
            Intent intent = new Intent(ProfileActivity.this,LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String uid = currentUser.getUid();


        //hien thi thong tin user
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document ->{
                    if(document.exists()){
                        String name = document.getString("displayName");
                        String email = document.getString("email");
                        String phone = document.getString("phone");
                        String role = document.getString("role");

                        if(name != null && !name.isEmpty()){
                            edtName.setText(name);
                        } else {
                            edtName.setHint("Chua cap nhat username");
                        }

                        txtEmail.setText("Email: " + email);

                        if(phone != null && !phone.isEmpty()){
                            edtPhone.setText(phone);
                        } else {
                            edtPhone.setHint("Chua cap nhat so dien thoai");
                        }

                        txtRole.setText("Vai tro: " + role);
                    }   else {
                        Toast.makeText(this, "Khong tim thay thong tin user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Loi tai profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        //luu tt user
        btnSaveProfile.setOnClickListener(view->{
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            if(name.isEmpty()){
                Toast.makeText(this, "Vui long nhap ten!", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> userUpdates = new HashMap<>();
            userUpdates.put("displayName", name);
            userUpdates.put("phone", phone);
            userUpdates.put("updatedAt", FieldValue.serverTimestamp());

            db.collection("users").document(uid)
                    .set(userUpdates, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Luu thanh cong thong tin", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e->{
                        Toast.makeText(this, "Luu thong tin that bai: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });


        //dang xuat
        btnLogout.setOnClickListener(view->{
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        navHome.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
            startActivity(intent);
        });

        navMarket.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, MarketActivity.class);
            startActivity(intent);
        });

        navPost.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        navMap.setOnClickListener(view -> {
            Intent intent = new Intent(ProfileActivity.this, MapActivity.class);
            startActivity(intent);
        });

        navProfile.setOnClickListener(view -> {
            // dang o trang Profile
        });

        ;


    }
}
