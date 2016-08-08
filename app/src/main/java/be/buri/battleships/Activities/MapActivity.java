package be.buri.battleships.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

import java.util.Timer;
import java.util.TimerTask;

import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Units.Harbor;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener {

    public static final Double LON_MIN = 7.34d;
    public static final Double LON_MAX = 13.5d;
    public static final Double LAT_MIN = 53.5d;
    public static final Double LAT_MAX = 58.5d;

    private GoogleMap mMap;
    ClientService clientService;
    private boolean mBound = false;
    private Harbor currentHarbor;

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
        // service
        Intent intent = new Intent(this, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, -1);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

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

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ClientService.ClientBinder binder = (ClientService.ClientBinder) iBinder;
            Log.e("BS.map", "OK");
            clientService = binder.getService();
            mBound = true;
            for (Harbor harbor : clientService.harbors) {
                if (harbor.getPlayer() != null) {
                    Log.d("BS.Map.serCon", harbor.toString());
                    currentHarbor = harbor;
                    // Add a marker in the players' harbor
                    LatLng harborPosition = new LatLng(harbor.getGpsN(), harbor.getGpsE());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(harborPosition).title(harbor.getPlayer().getName()));
                    marker.setFlat(true);
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.harbor);
                    marker.setIcon(descriptor);
                    mMap.addCircle(new CircleOptions().center(harborPosition).radius(8000).clickable(false).fillColor(Color.RED).strokeWidth(0));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(harborPosition, 8));
                }
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public void makeShip(View view) {
        Integer time = 10;
        while(time > 0) {
            TextView timerNewShip = (TextView) findViewById(R.id.timerNewShip);
            timerNewShip.setText("New ship for "+ Integer.toString(time));
            time -= 1;
            try {
                wait(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Add a new ship in the players' harbor
        LatLng shipPosition = new LatLng(currentHarbor.getGpsN(), currentHarbor.getGpsE());
        Marker marker = mMap.addMarker(new MarkerOptions().position(shipPosition));
        marker.setFlat(true);
        marker.setDraggable(true);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ship3b);
        marker.setIcon(descriptor);
    }
}
