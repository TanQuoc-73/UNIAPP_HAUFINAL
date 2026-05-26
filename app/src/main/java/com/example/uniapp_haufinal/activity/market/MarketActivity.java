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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MarketActivity extends AppCompatActivity {

    TextView txtBack, txtAddItem;
    EditText edtSearchProduct;
    LinearLayout productContainer;

    FirebaseFirestore db;

    String searchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        txtBack = findViewById(R.id.txtBack);
        txtAddItem = findViewById(R.id.txtAddItem);
        edtSearchProduct = findViewById(R.id.edtSearchProduct);
        productContainer = findViewById(R.id.productContainer);

        db = FirebaseFirestore.getInstance();

        txtBack.setOnClickListener(view -> finish());
        txtAddItem.setOnClickListener(view -> {
            Intent intent = new Intent(MarketActivity.this, AddMarketItemActivity.class);
            startActivity(intent);
        });

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
            intent.putExtra("sellerName", sellerName);
            intent.putExtra("phone", phone);
            intent.putExtra("location", location);
            intent.putExtra("price", price);
            startActivity(intent);
        });
    }

}
