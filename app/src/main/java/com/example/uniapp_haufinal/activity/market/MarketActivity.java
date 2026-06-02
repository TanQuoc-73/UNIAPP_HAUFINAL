package com.example.uniapp_haufinal.activity.market;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.util.Map;

public class MarketActivity extends AppCompatActivity {

    TextView txtBack, txtAddItem, txtMyItems;
    TextView navHome, navMarket, navPost, navMap, navProfile;
    EditText edtSearchProduct;
    LinearLayout productContainer;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String searchText = "";
    int loadRequestId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        txtBack = findViewById(R.id.txtBack);
        txtAddItem = findViewById(R.id.txtAddItem);
        txtMyItems = findViewById(R.id.txtMyItems);
        navHome = findViewById(R.id.navHome);
        navMarket = findViewById(R.id.navMarket);
        navPost = findViewById(R.id.navPost);
        navMap = findViewById(R.id.navMap);
        navProfile = findViewById(R.id.navProfile);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        productContainer = findViewById(R.id.productContainer);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        txtBack.setOnClickListener(view -> finish());
        txtAddItem.setOnClickListener(view -> {
            Intent intent = new Intent(MarketActivity.this, AddMarketItemActivity.class);
            startActivity(intent);
        });
        txtMyItems.setOnClickListener(view -> {
            Intent intent = new Intent(MarketActivity.this, MyMarketItemsActivity.class);
            startActivity(intent);
        });
        navHome.setOnClickListener(view -> startActivity(new Intent(this, HomeActivity.class)));
        navMarket.setOnClickListener(view -> {
            // dang o trang Market
        });
        navPost.setOnClickListener(view -> startActivity(new Intent(this, CreatePostActivity.class)));
        navMap.setOnClickListener(view -> startActivity(new Intent(this, MapActivity.class)));
        navProfile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));

        edtSearchProduct.setOnEditorActionListener((v, actionId, event) -> {
            searchText = edtSearchProduct.getText().toString().trim().toLowerCase();
            loadMarketItems();
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (productContainer != null) {
            loadMarketItems();
        }
    }

    private void loadMarketItems() {
        productContainer.removeAllViews();
        int currentRequestId = ++loadRequestId;

        db.collection("marketItems")
//                .whereEqualTo("status", "available")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (currentRequestId != loadRequestId) {
                        return;
                    }

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemId = document.getId();
                        String title = document.getString("title");
                        String description = document.getString("description");
                        String sellerName = document.getString("sellerName");
                        String phone = document.getString("contactPhone");
                        String location = document.getString("pickupLocation");
                        String status = document.getString("status");
                        String sellerId = document.getString("sellerId");
                        Long price = laySoLong(document.getData(), "price");

                        if (title == null) title = "";
                        if (description == null) description = "";
                        if (sellerName == null) sellerName = "Nguoi ban";
                        if (phone == null) phone = "";
                        if (location == null) location = "";
                        status = chuanHoaStatus(status);
                        if (sellerId == null) sellerId = "";
                        if (price == null) price = 0L;

                        FirebaseUser currentUser = auth.getCurrentUser();
                        if (currentUser != null && sellerId.equals(currentUser.getUid())) {
                            continue;
                        }

                        if (!status.equals("available")) {
                            continue;
                        }

                        suaStatusCuNeuCan(itemId, document.getString("status"));

                        if (!searchText.isEmpty() && !title.toLowerCase().contains(searchText)) {
                            continue;
                        }

                        addItemView(itemId, title, description, sellerId, sellerName, phone, location, price);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Khong tai duoc market", Toast.LENGTH_SHORT).show();
                });
    }

    private void addItemView(String itemId, String title, String description, String sellerId, String sellerName,
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

        Button btnBuy = new Button(this);
        btnBuy.setText("Chi tiet");

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 12, 0, 0);

        itemLayout.addView(txtTitle);
        itemLayout.addView(txtPrice);
        itemLayout.addView(txtDescription);
        itemLayout.addView(txtInfo);
        itemLayout.addView(btnBuy, buttonParams);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        itemParams.setMargins(0, 0, 0, 16);

        productContainer.addView(itemLayout, itemParams);

        btnBuy.setOnClickListener(view -> {
            Intent intent = new Intent(MarketActivity.this, BuyActivity.class);
            intent.putExtra("itemId", itemId);
            intent.putExtra("title", title);
            intent.putExtra("description", description);
            intent.putExtra("sellerId", sellerId);
            intent.putExtra("sellerName", sellerName);
            intent.putExtra("phone", phone);
            intent.putExtra("location", location);
            intent.putExtra("price", price);
            startActivity(intent);
        });
    }

    private String chuanHoaStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return "available";
        }

        status = status.trim().toLowerCase();

        //truoc day co chuc nang giu hang, gio bo roi nen locked cho ve available
        if (status.equals("locked")) {
            return "available";
        }

        return status;
    }

    private void suaStatusCuNeuCan(String itemId, String statusCu) {
        if (statusCu == null || statusCu.trim().isEmpty() || statusCu.equals("locked")) {
            db.collection("marketItems").document(itemId).update(
                    "status", "available",
                    "updatedAt", FieldValue.serverTimestamp()
            );
        }
    }

    private Long laySoLong(Map<String, Object> data, String fieldName) {
        Object value = data.get(fieldName);

        if (value == null) {
            return null;
        }

        if (value instanceof Long) {
            return (Long) value;
        }

        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }

        if (value instanceof Double) {
            return ((Double) value).longValue();
        }

        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

}
