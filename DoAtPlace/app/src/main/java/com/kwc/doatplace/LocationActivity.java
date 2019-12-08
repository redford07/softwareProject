package com.kwc.doatplace;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

public class LocationActivity extends AppCompatActivity {
    SupportMapFragment mapFragment;
    GoogleMap map;
    GridParser gridParser = new GridParser();
    Map<String, Object> grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.d("MainActivity", "GoogleMap 객체가 준비됨.");
                map = googleMap;
            }
        });

        MapsInitializer.initialize(this);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestMyLocation();
            }
        });
    } // end of onCreate

    public void requestMyLocation() {
        long minTime = 10000;
        float minDistance = 0;

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                int permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);

                if(permissionCheck == PackageManager.PERMISSION_DENIED){

                    // 권한 없음
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            1000);
//                             CDialog.onHide();
                }


            }
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, listener);
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, listener);
        }catch (Exception e){
            Log.d("gpserror",e.toString());
        }
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) { //위치가 넘어올 때 작동되는 callback 함수
            showCurrentLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) { }

        @Override
        public void onProviderEnabled(String s) { }

        @Override
        public void onProviderDisabled(String s) { }
    };

    public void showCurrentLocation(Location location) {
        // 현재 위도경도 위치 저장 객체
        LatLng curPoint = new LatLng(location.getLatitude(), location.getLongitude());
        // 해당 위치로 줌하여 보여줌.
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(curPoint, 15));
        grid = gridParser.getGridxy(location.getLatitude(), location.getLongitude());
        Toast.makeText(this, "gridxy 좌표 : " + grid.get("x") + ", " + grid.get("y"), Toast.LENGTH_LONG).show();
    }
}
