package com.example.uniapp_haufinal.activity.profile;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.uniapp_haufinal.R;

//firebase
import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    TextView txtName, txtEmail, txtPhone, txtRole;
    Button btnLogout;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        txtName = findViewById(R.id.txtName);
        txtEmail = findViewById(R.id.txtEmail);
        txtPhone = findViewById(R.id.txtPhone);
        txtRole = findViewById(R.id.txtRole);
        btnLogout = findViewById(R.id.btnLogout);

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

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document ->{
                    if(document.exists()){
                        String name = document.getString("displayName");
                        String email = document.getString("email");
                        String phone = document.getString("phone");
                        String role = document.getString("role");

                        if(name == null || name.isEmpty()){
                            name = "Chua cap nhat username";
                        }

                        txtName.setText(name);
                        txtEmail.setText(email);
                        txtPhone.setText(phone);
                        txtRole.setText(role);
                    }   else {
                        Toast.makeText(this, "Khong tim thay thong tin user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Loi tai profile", Toast.LENGTH_SHORT).show();
                });
        btnLogout.setOnClickListener(view->{
            auth.signOut();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        ;


    }
}