package com.example.uniapp_haufinal.activity.market;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.uniapp_haufinal.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class MarketActivity extends AppCompatActivity {

    TextView txtBack;
    EditText edtSearchProduct, edtTitle, edtDescription, edtPrice, edtLocation, edtPhone;
    Button btnAddItem;
    LinearLayout productContainer;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        txtBack = findViewById(R.id.txtBack);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        edtTitle = findViewById(R.id.edtTitle);
        edtDescription = findViewById(R.id.edtDescription);
        edtPrice = findViewById(R.id.edtPrice);
        edtLocation = findViewById(R.id.edtLocation);
        edtPhone = findViewById(R.id.edtPhone);
        btnAddItem = findViewById(R.id.btnAddItem);
        productContainer = findViewById(R.id.productContainer);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(view -> finish());

        btnAddItem.setOnClickListener(view -> addMarketItem());

        edtSearchProduct.setOnEditorActionListener((v, actionId, event) -> {
            searchText = edtSearchProduct.getText().toString().trim().toLowerCase();
            loadMarketItems();
            return false;
        });

        loadMarketItems();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productContainer != null) {
            loadMarketItems();
        }
    }

    private void addMarketItem() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Ban chua dang nhap", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = edtTitle.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        String priceText = edtPrice.getText().toString().trim();
        String location = edtLocation.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();

        if (title.isEmpty() || priceText.isEmpty() || location.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Nhap du ten, gia, dia diem, so dien thoai", Toast.LENGTH_SHORT).show();
            return;
        }

        long price = Long.parseLong(priceText);
        String uid = currentUser.getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    String sellerName = document.getString("displayName");

                    if (sellerName == null || sellerName.isEmpty()) {
                        sellerName = document.getString("email");
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("sellerId", uid);
                    item.put("sellerName", sellerName);
                    item.put("title", title);
                    item.put("description", description);
                    item.put("quantity", 1);
                    item.put("price", price);
                    item.put("pickupLocation", location);
                    item.put("contactPhone", phone);
                    item.put("category", "other");
                    item.put("status", "available");
                    item.put("lockedBy", null);
                    item.put("lockedUntil", null);
                    item.put("createdAt", FieldValue.serverTimestamp());
                    item.put("updatedAt", FieldValue.serverTimestamp());

                    db.collection("marketItems").add(item)
                            .addOnSuccessListener(documentReference -> {
                                String itemId = documentReference.getId();

                                documentReference.update("itemId", itemId)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Dang vat pham thanh cong", Toast.LENGTH_SHORT).show();
                                            clearInput();
                                            loadMarketItems();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Dang vat pham that bai", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong lay duoc thong tin user", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMarketItems() {
        productContainer.removeAllViews();

        db.collection("marketItems")
//                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemId = document.getId();
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String sellerName = document.getString("sellerName");
                        String phone = document.getString("contactPhone");
                        String location = document.getString("pickupLocation");
                        String status = document.getString("status");
                        Long price = document.getLong("price");

                        if (title == null) title = "";
                        if (description == null) description = "";
                        if (sellerName == null) sellerName = "Nguoi ban";
                        if (phone == null) phone = "";
                        if (location == null) location = "";
                        if (status == null) status = "";
                        if (price == null) price = 0L;

                        if (!status.equals("available")) {
                            continue;
                        }

                        if (!searchText.isEmpty() && !title.toLowerCase().contains(searchText)) {
                            continue;
                        }

                        addItemView(itemId, title, description, sellerName, phone, location, price);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong tai duoc market", Toast.LENGTH_SHORT).show();
                });
    }

    private void addItemView(String itemId, String title, String description, String sellerName,
                             String phone, String location, Long price) {

        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(20, 20, 20, 20);
        itemLayout.setBackgroundColor(Color.rgb(242, 242, 242));

        TextView txtTitle = new TextView(this);
        txtTitle.setText(title);
        txtTitle.setTextColor(Color.BLACK);
        txtTitle.setTextSize(18);
        txtTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView txtPrice = new TextView(this);
        txtPrice.setText("Gia: " + price + " VND");
        txtPrice.setTextColor(Color.rgb(229, 57, 53));
        txtPrice.setTextSize(15);

        TextView txtDescription = new TextView(this);
        txtDescription.setText(description);
        txtDescription.setTextColor(Color.DKGRAY);
        txtDescription.setTextSize(14);

        TextView txtInfo = new TextView(this);
        txtInfo.setText("Nguoi ban: " + sellerName + "\nDia diem: " + location + "\nSDT: " + phone);
        txtInfo.setTextColor(Color.DKGRAY);
        txtInfo.setTextSize(14);

        LinearLayout buttonLayout = new LinearLayout(this);
        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        Button btnContact = new Button(this);
        btnContact.setText("Lien he");

        Button btnBuy = new Button(this);
        btnBuy.setText("Chi tiet");

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        buttonParams.setMargins(0, 12, 8, 0);

        LinearLayout.LayoutParams buttonParams2 = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1
        );
        buttonParams2.setMargins(8, 12, 0, 0);

        buttonLayout.addView(btnContact, buttonParams);
        buttonLayout.addView(btnBuy, buttonParams2);

        itemLayout.addView(txtTitle);
        itemLayout.addView(txtPrice);
        itemLayout.addView(txtDescription);
        itemLayout.addView(txtInfo);
        itemLayout.addView(buttonLayout);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.setMargins(0, 0, 0, 16);

        productContainer.addView(itemLayout, itemParams);

        btnContact.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        });

        btnBuy.setOnClickListener(view -> {
            Intent intent = new Intent(MarketActivity.this, BuyActivity.class);
            intent.putExtra("itemId", itemId);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("sellerName", sellerName);
            intent.putExtra("phone", phone);
            intent.putExtra("location", location);
            intent.putExtra("price", price);
            startActivity(intent);
        });
    }

    private void clearInput() {
        edtTitle.setText("");
        edtDescription.setText("");
        edtPrice.setText("");
        edtLocation.setText("");
        edtPhone.setText("");
    }
}
