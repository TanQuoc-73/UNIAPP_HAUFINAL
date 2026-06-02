package com.example.uniapp_haufinal.activity.market;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
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

public class BuyActivity extends AppCompatActivity {

    TextView txtBack, txtTitle, txtPrice, txtDescription, txtSeller, txtPhone, txtLocation;
    TextView navHome, navMarket, navPost, navMap, navProfile;
    Button btnBuy, btnCall, btnSms;

    FirebaseAuth auth;
    FirebaseFirestore db;
    String itemId, title, phone, sellerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy);

        txtBack = findViewById(R.id.txtBack);
        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtDescription = findViewById(R.id.txtDescription);
        txtSeller = findViewById(R.id.txtSeller);
        txtPhone = findViewById(R.id.txtPhone);
        txtLocation = findViewById(R.id.txtLocation);
        btnBuy = findViewById(R.id.btnBuy);
        btnCall = findViewById(R.id.btnCall);
        btnSms = findViewById(R.id.btnSms);
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        itemId = getIntent().getStringExtra("itemId");
        sellerId = getIntent().getStringExtra("sellerId");

        title = getIntent().getStringExtra("title");
        String description = getIntent().getStringExtra("description");
        String sellerName = getIntent().getStringExtra("sellerName");
        phone = getIntent().getStringExtra("phone");
        String location = getIntent().getStringExtra("location");
        long price = getIntent().getLongExtra("price", 0L);

        txtTitle.setText(title);
        txtPrice.setText("Gia: " + price + " VND");
        txtDescription.setText(description);
        txtSeller.setText("Nguoi ban: " + sellerName);
        txtPhone.setText("SDT: " + phone);
        txtLocation.setText("Dia diem: " + location);

        txtBack.setOnClickListener(view -> finish());
        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> finish());
        navPost.setOnClickListener(view -> startActivity(new Intent(this, CreatePostActivity.class)));
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));

        btnBuy.setOnClickListener(view -> {
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(this, "Chua dang nhap", Toast.LENGTH_SHORT).show();
                return;
            }

            if (sellerId != null && sellerId.equals(currentUser.getUid())) {
                Toast.makeText(this, "Khong the mua san pham cua chinh minh", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("marketItems").document(itemId).update(
                    "status", "sold",
                    "updatedAt", FieldValue.serverTimestamp()
            ).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Da mua san pham", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Mua san pham that bai", Toast.LENGTH_SHORT).show();
            });
        });

        btnCall.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        btnSms.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("smsto:" + phone));
            intent.putExtra("sms_body", "Chao ban, minh muon mua: " + title);
            startActivity(intent);
        });
    }
}
