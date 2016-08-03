package be.buri.battleships.Activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import be.buri.battleships.R;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    public static final Double LON_MIN = 7.34d;
    public static final Double LON_MAX = 13.5d;
    public static final Double LAT_MIN = 53.5d;
    public static final Double LAT_MAX = 58.5d;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(55, 10);
        Marker marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));

        // a picture of a ship
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ship3);
        marker.setIcon(descriptor);
        marker.setDraggable(true);
        marker.setFlat(true);
        mMap.addCircle(new CircleOptions().center(sydney).radius(5000).clickable(false).fillColor(Color.RED).strokeWidth(0));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 8));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        // limit the selected area
        mMap.setOnCameraChangeListener(this);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        if (cameraPosition.target.longitude < LON_MIN) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(cameraPosition.target.latitude, LON_MIN)));
        }
        if (cameraPosition.target.longitude > LON_MAX) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(cameraPosition.target.latitude, LON_MAX)));
        }
        if (cameraPosition.target.latitude > LAT_MAX) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(LAT_MAX, cameraPosition.target.longitude)));
        }
        if (cameraPosition.target.latitude < LAT_MIN) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(LAT_MIN, cameraPosition.target.longitude)));
        }

    }
}
