package com.example.uniapp_haufinal.activity.auth;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

//firebase auth
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    EditText edtEmail, edtPassword;
    Button btnLogin, btnRegister;
    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            edtEmail = findViewById(R.id.edtEmail);
            edtPassword = findViewById(R.id.edtPassword);
            btnLogin = findViewById(R.id.btnLogin);
            btnRegister = findViewById(R.id.btnRegister);
            auth = FirebaseAuth.getInstance();
            db = FirebaseFirestore.getInstance();



            //btnRegister
            btnRegister.setOnClickListener(view->{
                String email=edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if(email.isEmpty()|| password.isEmpty()){
                    Toast.makeText(this,"Nhap email !",Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.createUserWithEmailAndPassword(email, password) .addOnCompleteListener(task->{
                    if(task.isSuccessful()){
//
//                        //Logic don gian
//                        Toast.makeText(this, "Dang ky thanh cong",Toast.LENGTH_SHORT).show();
//                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                        startActivity(intent);
//                        finish();

                        //Logic de luu user vao collection
                        String uid = auth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("uid", uid);
                        user.put("email", email);
                        user.put("displayName", "");
                        user.put("phone","");
                        user.put("avatarUrl","");
                        user.put("role","user");
                        user.put("status", "active");
                        user.put("createdAt", FieldValue.serverTimestamp());
                        user.put("updatedAt",FieldValue.serverTimestamp());

                        db.collection("users").document(uid).set(user)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Dang ky thanh cong", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                })

                                .addOnFailureListener(e->{
//                                    Toast.makeText(this,"Luu thong tin user that bai", Toast.LENGTH_SHORT).show();
                                    Toast.makeText(this, "Loi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });

                    } else {
                        Toast.makeText(this, "Dang ky that bai", Toast.LENGTH_SHORT).show();
                    }
                });

            });

            //btnLogin
            btnLogin.setOnClickListener(view->{
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();

                if(email.isEmpty() || password.isEmpty()){
                    Toast.makeText(this, "Nhap email", Toast.LENGTH_SHORT).show();
                    return;
                }
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task ->{
                    if(task.isSuccessful()){
                        Toast.makeText(this, "Dang nhap thanh cong", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Dang nhap that bai", Toast.LENGTH_SHORT).show();

                    }
                });

            } );


            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
