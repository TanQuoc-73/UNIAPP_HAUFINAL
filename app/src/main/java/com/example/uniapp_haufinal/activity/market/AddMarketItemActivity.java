package com.example.uniapp_haufinal.activity.market;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.map.MapActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMarketItemActivity extends AppCompatActivity {

    TextView txtBack, navHome, navMarket, navPost, navMap, navProfile;
    EditText edtTitle, edtDescription, edtPrice, edtPhone, edtLocation;
    Button btnSubmitItem;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_market_item);

        txtBack = findViewById(R.id.txtBack);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtPhone = findViewById(R.id.edtPhone);
        edtLocation = findViewById(R.id.edtLocation);
        btnSubmitItem = findViewById(R.id.btnSubmitItem);
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(view -> finish());
        btnSubmitItem.setOnClickListener(view -> submitItem());

        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> finish());
        navPost.setOnClickListener(view -> startActivity(new Intent(this, CreatePostActivity.class)));
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    private void submitItem() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chua dang nhap", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceText = edtPrice.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || priceText.isEmpty()
                || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Vui long nhap day du thong tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long price;
        try {
            price = Long.parseLong(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Gia khong hop le", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String sellerName = document.getString("displayName");
                    if (sellerName == null || sellerName.isEmpty()) {
                        sellerName = document.getString("email");
                    }
                    if (sellerName == null || sellerName.isEmpty()) {
                        sellerName = "Nguoi ban";
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("sellerId", uid);
                    item.put("sellerName", sellerName);
                    item.put("title", title);
                    item.put("description", description);
                    item.put("price", price);
                    item.put("contactPhone", phone);
                    item.put("pickupLocation", location);
                    item.put("status", "available");
                    item.put("createdAt", FieldValue.serverTimestamp());
                    item.put("updatedAt", FieldValue.serverTimestamp());

                    db.collection("marketItems").add(item)
                            .addOnSuccessListener(documentReference -> {
                                documentReference.update("itemId", documentReference.getId());
                                Toast.makeText(this, "Dang san pham thanh cong", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Dang san pham that bai", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong lay duoc thong tin user", Toast.LENGTH_SHORT).show();
                });
    }
}
