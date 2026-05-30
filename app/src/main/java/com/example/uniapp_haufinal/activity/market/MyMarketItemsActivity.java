package com.example.uniapp_haufinal.activity.market;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MyMarketItemsActivity extends AppCompatActivity {

    TextView txtBack, navHome, navMarket, navPost, navMap, navProfile;
    LinearLayout myProductContainer;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_market_items);

        txtBack = findViewById(R.id.txtBack);
        myProductContainer = findViewById(R.id.myProductContainer);
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(view -> finish());
        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> startActivity(new Intent(this, MarketActivity.class)));
        navPost.setOnClickListener(view -> startActivity(new Intent(this, CreatePostActivity.class)));
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyItems();
    }

    private void loadMyItems() {
        myProductContainer.removeAllViews();

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chua dang nhap", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("marketItems")
                .whereEqualTo("sellerId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemId = document.getId();
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String phone = document.getString("contactPhone");
                        String location = document.getString("pickupLocation");
                        String status = document.getString("status");
                        Long price = document.getLong("price");

                        if (title == null) title = "";
                        if (description == null) description = "";
                        if (phone == null) phone = "";
                        if (location == null) location = "";
                        if (status == null) status = "";
                        if (price == null) price = 0L;

                        addMyItemView(itemId, title, description, phone, location, status, price);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong tai duoc san pham", Toast.LENGTH_SHORT).show();
                });
    }

    private void addMyItemView(String itemId, String title, String description, String phone,
                               String location, String status, Long price) {
        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(20, 20, 20, 20);
        itemLayout.setBackgroundColor(Color.rgb(242, 242, 242));

        TextView txtTitle = new TextView(this);
        txtTitle.setText(title);
        txtTitle.setTextColor(Color.BLACK);
        txtTitle.setTextSize(18);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtInfo = new TextView(this);
        txtInfo.setText("Gia: " + price + " VND\nMo ta: " + description
                + "\nDia diem: " + location + "\nSDT: " + phone + "\nTrang thai: " + status);
        txtInfo.setTextColor(Color.DKGRAY);
        txtInfo.setTextSize(14);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btnEdit = new Button(this);
        btnEdit.setText("Sua");

        Button btnDelete = new Button(this);
        btnDelete.setText("Xoa");

        Button btnStatus = new Button(this);
        if (status.equals("sold")) {
            btnStatus.setText("Ban lai");
        } else {
            btnStatus.setText("Da ban");
        }

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        buttonParams.setMargins(4, 12, 4, 0);

        buttonLayout.addView(btnEdit, buttonParams);
        buttonLayout.addView(btnDelete, buttonParams);
        buttonLayout.addView(btnStatus, buttonParams);

        itemLayout.addView(txtTitle);
        itemLayout.addView(txtInfo);
        itemLayout.addView(buttonLayout);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.setMargins(0, 0, 0, 16);
        myProductContainer.addView(itemLayout, itemParams);

        btnEdit.setOnClickListener(view -> showEditDialog(itemId, title, description, phone, location, price));
        btnDelete.setOnClickListener(view -> deleteItem(itemId));
        btnStatus.setOnClickListener(view -> changeStatus(itemId, status));
    }

    private void showEditDialog(String itemId, String title, String description, String phone,
                                String location, Long price) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 20, 30, 0);

        EditText edtTitle = new EditText(this);
        edtTitle.setHint("Ten san pham");
        edtTitle.setText(title);

        EditText edtDescription = new EditText(this);
        edtDescription.setHint("Mo ta");
        edtDescription.setText(description);

        EditText edtPrice = new EditText(this);
        edtPrice.setHint("Gia");
        edtPrice.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        edtPrice.setText(String.valueOf(price));

        EditText edtPhone = new EditText(this);
        edtPhone.setHint("So dien thoai");
        edtPhone.setText(phone);

        EditText edtLocation = new EditText(this);
        edtLocation.setHint("Dia diem");
        edtLocation.setText(location);

        layout.addView(edtTitle);
        layout.addView(edtDescription);
        layout.addView(edtPrice);
        layout.addView(edtPhone);
        layout.addView(edtLocation);

        new AlertDialog.Builder(this)
                .setTitle("Sua san pham")
                .setView(layout)
                .setPositiveButton("Luu", (dialog, which) -> {
                    String newTitle = edtTitle.getText().toString().trim();
                    String newDescription = edtDescription.getText().toString().trim();
                    String newPriceText = edtPrice.getText().toString().trim();
                    String newPhone = edtPhone.getText().toString().trim();
                    String newLocation = edtLocation.getText().toString().trim();

                    if (newTitle.isEmpty() || newPriceText.isEmpty() || newPhone.isEmpty() || newLocation.isEmpty()) {
                        Toast.makeText(this, "Nhap du thong tin", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    long newPrice = Long.parseLong(newPriceText);

                    db.collection("marketItems").document(itemId).update(
                            "title", newTitle,
                            "description", newDescription,
                            "price", newPrice,
                            "contactPhone", newPhone,
                            "pickupLocation", newLocation,
                            "updatedAt", FieldValue.serverTimestamp()
                    ).addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Da cap nhat", Toast.LENGTH_SHORT).show();
                        loadMyItems();
                    });
                })
                .setNegativeButton("Huy", null)
                .show();
    }

    private void deleteItem(String itemId) {
        db.collection("marketItems").document(itemId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Da xoa", Toast.LENGTH_SHORT).show();
                    loadMyItems();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Xoa that bai", Toast.LENGTH_SHORT).show();
                });
    }

    private void changeStatus(String itemId, String currentStatus) {
        String newStatus;
        if (currentStatus.equals("sold")) {
            newStatus = "available";
        } else {
            newStatus = "sold";
        }

        db.collection("marketItems").document(itemId).update(
                "status", newStatus,
                "buyerId", null,
                "lockedUntil", null,
                "updatedAt", FieldValue.serverTimestamp()
        ).addOnSuccessListener(unused -> {
            Toast.makeText(this, "Da cap nhat trang thai", Toast.LENGTH_SHORT).show();
            loadMyItems();
        });
    }
}
