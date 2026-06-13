package com.example.uniapp_haufinal.activity.market;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMarketItemActivity extends AppCompatActivity {

    TextView txtBack;
    EditText edtTitle, edtDescription, edtPrice, edtPhone, edtLocation;
    Button btnSubmitItem;

    FirebaseAuth auth;
    FirebaseFirestore db;

    private boolean isEditMode = false;
    private String editItemId;

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

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Check if editing
        isEditMode = getIntent().getBooleanExtra("isEdit", false);
        if (isEditMode) {
            editItemId = getIntent().getStringExtra("itemId");
            loadItemData();
            btnSubmitItem.setText("Cập nhật sản phẩm");
        }

        txtBack.setOnClickListener(view -> finish());
        btnSubmitItem.setOnClickListener(view -> submitItem());
    }

    private void loadItemData() {
        db.collection("marketItems").document(editItemId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        edtTitle.setText(doc.getString("title"));
                        edtDescription.setText(doc.getString("description"));
                        edtPrice.setText(String.valueOf(doc.getLong("price")));
                        edtPhone.setText(doc.getString("contactPhone"));
                        edtLocation.setText(doc.getString("pickupLocation"));
                    }
                });
    }

    private void submitItem() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceText = edtPrice.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();

        if (title.isEmpty() || description.isEmpty() || priceText.isEmpty()
                || phone.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long price;
        try {
            price = Long.parseLong(priceText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Giá không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            updateItem(title, description, price, phone, location);
        } else {
            createNewItem(currentUser.getUid(), title, description, price, phone, location);
        }
    }

    private void updateItem(String title, String description, long price, String phone, String location) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("description", description);
        updates.put("price", price);
        updates.put("contactPhone", phone);
        updates.put("pickupLocation", location);
        updates.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("marketItems").document(editItemId).update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show());
    }

    private void createNewItem(String uid, String title, String description, long price, String phone, String location) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String sellerName = document.getString("displayName");
                    if (sellerName == null || sellerName.isEmpty()) {
                        sellerName = "Người bán";
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
                    item.put("buyerId", null);
                    item.put("lockedUntil", null);
                    item.put("createdAt", FieldValue.serverTimestamp());
                    item.put("updatedAt", FieldValue.serverTimestamp());

                    db.collection("marketItems").add(item)
                            .addOnSuccessListener(documentReference -> {
                                documentReference.update("itemId", documentReference.getId());
                                Toast.makeText(this, "Đăng sản phẩm thành công", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Đăng sản phẩm thất bại", Toast.LENGTH_SHORT).show());
                });
    }
}
