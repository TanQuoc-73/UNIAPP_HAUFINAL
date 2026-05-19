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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

//luu tt user dang array
import java.util.ArrayList;

public class ProfileActivity extends AppCompatActivity {

//    TextView txtName, txtEmail, txtPhone, txtRole;
    EditText edtName, edtPhone;
    TextView txtEmail, txtRole;
    Button btnSaveProfile, btnLogout;

    FirebaseAuth auth;
    FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        edtName = findViewById(R.id.edtName);
        txtEmail = findViewById(R.id.txtEmail);
        edtPhone = findViewById(R.id.edtPhone);
        txtRole = findViewById(R.id.txtRole);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
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


        //hien thi thong tin user
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

                        edtName.setText(name);
                        txtEmail.setText(email);
                        edtPhone.setText(phone);
                        txtRole.setText(role);
                    }   else {
                        Toast.makeText(this, "Khong tim thay thong tin user", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->{
                    Toast.makeText(this, "Loi tai profile", Toast.LENGTH_SHORT).show();
                });

        //luu tt user
        btnSaveProfile.setOnClickListener(view->{
            String name = edtName.getText().toString().trim();
            String phone = edtPhone.getText().toString().trim();

            db.collection("users").document(uid).update(
                    "displayName", edtName.getText().toString().trim(),
                    "phone", edtPhone.getText().toString().trim(),
                    "updatedAt", FieldValue.serverTimestamp()
            ).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Luu thanh cong thong tin", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e->{
                Toast.makeText(this, "Luu thong tin that bai", Toast.LENGTH_SHORT).show();
            })

            ;
        });


        //dang xuat
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