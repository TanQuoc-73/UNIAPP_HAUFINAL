package com.example.uniapp_haufinal.features.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.uniapp_haufinal.R;
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

        // Cấu hình osmdroid
        Configuration.getInstance().load(
                this,
                PreferenceManager.getDefaultSharedPreferences(this)
        );

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_map);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        txtBack = findViewById(R.id.txtBack);

        btnMyLocation = findViewById(R.id.btnMyLocation);

        txtBack.setOnClickListener(view -> finish());

        fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(this);

        // Khởi tạo bản đồ
        map = findViewById(R.id.map);

        if (map != null) {

            map.setTileSource(TileSourceFactory.MAPNIK);

            map.setMultiTouchControls(true);

            IMapController mapController =
                    map.getController();

            mapController.setZoom(15.0);

            GeoPoint startPoint =
                    new GeoPoint(21.0285, 105.8542);

            mapController.setCenter(startPoint);
        }

        // Button vị trí hiện tại
        btnMyLocation.setOnClickListener(v -> {
            getCurrentLocation();
        });
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },
                    REQUEST_LOCATION
            );

            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {

                    if (location != null) {

                        double lat =
                                location.getLatitude();

                        double lon =
                                location.getLongitude();

                        GeoPoint myLocation =
                                new GeoPoint(lat, lon);

                        map.getController()
                                .animateTo(myLocation);

                        map.getController()
                                .setZoom(18.0);

                        map.getOverlays().clear();

                        Marker marker =
                                new Marker(map);

                        marker.setPosition(myLocation);

                        marker.setTitle("Vi tri cua ban");

                        map.getOverlays().add(marker);

                        map.invalidate();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode == REQUEST_LOCATION) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                getCurrentLocation();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (map != null)
            map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (map != null)
            map.onPause();
    }
}