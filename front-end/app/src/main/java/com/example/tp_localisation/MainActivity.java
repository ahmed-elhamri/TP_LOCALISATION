package com.example.tp_localisation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.tp_localisation.apis.PositionApi;
import com.example.tp_localisation.apis.RetrofitClient;
import com.example.tp_localisation.classes.Position;
import com.example.tp_localisation.viewmodels.PositionViewModel;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_PERMISSIONS = 1;

    private LocationManager locationManager;
    private TelephonyManager telephonyManager;
    private PositionViewModel viewModel;
    private GoogleMap mMap;
    private MapView mapView;

    private Button btnSend, btnRefreshLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSend = findViewById(R.id.btn_send);
        btnRefreshLocation = findViewById(R.id.btn_refresh_location);
        mapView = findViewById(R.id.mapView);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        viewModel = new ViewModelProvider(this).get(PositionViewModel.class);
        viewModel.init(this);

        viewModel.getResponseLiveData().observe(this, result -> {
            Log.d("MainActivity", result);
        });

        btnSend.setOnClickListener(v -> {
            if (checkPermissions()) {
                sendCurrentPosition();
            } else {
                requestPermissions();
            }
        });

        btnRefreshLocation.setOnClickListener(v -> {
            if (checkPermissions() && mMap != null) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.setMyLocationEnabled(false);
                mMap.setMyLocationEnabled(true);
                Log.d("MainActivity", "Blue dot refreshed");
            } else {
                requestPermissions();
            }
        });

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE
        }, REQUEST_PERMISSIONS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (checkPermissions()) {
                sendCurrentPosition();
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    fetchLocations();
                }
            } else {
                Log.d("MainActivity", "Permissions refusées");
            }
        }
    }

    private void sendCurrentPosition() {
        try {
            if (checkPermissions()) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    updatePosition(location);
                    fetchLocations(); // refresh markers
                } else {
                    Log.d("MainActivity", "Position GPS non disponible");
                }
            } else {
                Log.d("MainActivity", "Permissions non accordées");
            }
        } catch (SecurityException e) {
            Log.e("MainActivity", "Erreur sécurité : " + e.getMessage());
        }
    }

    private void updatePosition(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        String imei = getImei();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Position position = new Position(lat, lon, imei, date);
        viewModel.sendPosition(position);
    }

    private String getImei() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                return telephonyManager.getImei();
            } catch (SecurityException e) {
                Log.e("MainActivity", "IMEI error: " + e.getMessage());
                return "IMEI not available";
            }
        } else {
            return "IMEI not available";
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkPermissions()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        fetchLocations();
    }

    private void fetchLocations() {
        PositionApi positionApi = RetrofitClient.getRetrofitInstance().create(PositionApi.class);
        Call<List<Position>> call = positionApi.getAllPositions();

        call.enqueue(new Callback<List<Position>>() {
            @Override
            public void onResponse(Call<List<Position>> call, Response<List<Position>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Position> positions = response.body();
                    mMap.clear();
                    for (Position position : positions) {
                        LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Position"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Pas de données disponibles", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Position>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Erreur de récupération des données", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
