package com.example.uniapp_haufinal.activity.market;
import com.example.uniapp_haufinal.activity.chat.ChatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;


public class BuyActivity extends AppCompatActivity {

    TextView txtBack, txtTitle, txtPrice, txtDescription, txtSeller, txtPhone, txtLocation;
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

            String buyerId = currentUser.getUid();

            // Khóa 3 phút bằng Date (Firestore sẽ tự hiểu là Timestamp)
            Date lockUntilDate = new Date(System.currentTimeMillis() + (3 * 60 * 1000));

            db.collection("marketItems")
                    .document(itemId)
                    .update(
                            "status", "pending",
                            "buyerId", buyerId,
                            "lockedUntil", lockUntilDate,
                            "updatedAt", FieldValue.serverTimestamp()
                    )
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Đã giữ hàng 3 phút", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(BuyActivity.this, ChatActivity.class);
                        // Sử dụng đúng key mà ChatActivity yêu cầu
                        intent.putExtra(ChatActivity.EXTRA_ITEM_ID, itemId);
                        intent.putExtra(ChatActivity.EXTRA_PARTNER_ID, sellerId);
                        intent.putExtra(ChatActivity.EXTRA_PARTNER_NAME, sellerName);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {

                        Toast.makeText(
                                this,
                                "Dat mua that bai",
                                Toast.LENGTH_SHORT
                        ).show();
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
