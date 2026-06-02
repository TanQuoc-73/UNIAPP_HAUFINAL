package com.example.uniapp_haufinal.activity.map;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uniapp_haufinal.R;
import com.example.uniapp_haufinal.activity.home.HomeActivity;
import com.example.uniapp_haufinal.activity.market.MarketActivity;
import com.example.uniapp_haufinal.activity.post.CreatePostActivity;
import com.example.uniapp_haufinal.activity.profile.ProfileActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class MapActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION = 1;
    private MapView map = null;
    private TextView txtBack;
    private Button btnMyLocation;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure osmdroid
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_map);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtBack = findViewById(R.id.txtBack);
        btnMyLocation = findViewById(R.id.btnMyLocation);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        txtBack.setOnClickListener(view -> finish());

        // Initialize Map
        map = findViewById(R.id.map);
        if (map != null) {
            map.setTileSource(TileSourceFactory.MAPNIK);
            map.setMultiTouchControls(true);

            IMapController mapController = map.getController();
            mapController.setZoom(15.0);
            GeoPoint startPoint = new GeoPoint(21.0285, 105.8542); // Hanoi
            mapController.setCenter(startPoint);
        }

        // My Location Button
        btnMyLocation.setOnClickListener(v -> getCurrentLocation());

        // Setup Bottom Navigation
        setupBottomNavigation();

        // Setup Search
        EditText edtSearchPlace = findViewById(R.id.edtSearchPlace);
        if (edtSearchPlace != null) {
            edtSearchPlace.setOnEditorActionListener((v, actionId, event) -> {
                String query = edtSearchPlace.getText().toString();
                if (!query.isEmpty()) {
                    Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
                }
                return false;
            });
        }
    }

    private void setupBottomNavigation() {
        TextView navHome = findViewById(R.id.navHome);
        TextView navMarket = findViewById(R.id.navMarket);
        TextView navPost = findViewById(R.id.navPost);
        TextView navMap = findViewById(R.id.navMap);
        TextView navProfile = findViewById(R.id.navProfile);

        // Highlight current tab
        if (navMap != null) {
            navMap.setTextColor(Color.parseColor("#1976D2")); // Blue
            navMap.setTypeface(null, android.graphics.Typeface.BOLD);
        }

        if (navHome != null) navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        if (navMarket != null) navMarket.setOnClickListener(v -> {
            startActivity(new Intent(this, MarketActivity.class));
            finish();
        });
        if (navPost != null) navPost.setOnClickListener(v -> {
            startActivity(new Intent(this, CreatePostActivity.class));
        });
        if (navProfile != null) navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                GeoPoint myLocation = new GeoPoint(lat, lon);

                if (map != null) {
                    map.getController().animateTo(myLocation);
                    map.getController().setZoom(18.0);

                    // Add or update marker
                    map.getOverlays().removeIf(overlay -> overlay instanceof Marker && "My Location".equals(((Marker) overlay).getTitle()));

                    Marker marker = new Marker(map);
                    marker.setPosition(myLocation);
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                    marker.setTitle("My Location");
                    map.getOverlays().add(marker);
                    map.invalidate();
                }
            } else {
                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }
}
