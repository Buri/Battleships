package be.buri.battleships.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import be.buri.battleships.Engine.Const;
import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Unit;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraChangeListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapLoadedCallback {

    public static final Double LON_MIN = 7.34d;
    public static final Double LON_MAX = 13.5d;
    public static final Double LAT_MIN = 53.5d;
    public static final Double LAT_MAX = 58.5d;

    private GoogleMap mMap;
    ClientService clientService;
    private boolean mBound = false;
    private Harbor currentHarbor;
    private Bitmap waterMap;

    LatLng actualLatLng;

    private BroadcastReceiver mUnitUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Unit unit = ClientService.units.get(intent.getIntExtra("UNIT_ID", -1));
            createMarkerForUnit(unit);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        registerReceiver(mUnitUpdateReciever, new IntentFilter(Const.BROADCAST_UPDATE_UNITS));
        /* Code to create a static google map -> it's already saved
        try {
            saveImage("http://maps.googleapis.com/maps/api/staticmap?center=56,10.42&zoom=7&size=6500x5000&sensor=false&visual_refresh=true%20&style=feature:water|color:0x00FF00&style=element:labels|visibility:off%20&style=feature:transit|visibility:off%20&style=feature:poi|visibility:off&style=feature:road|visibility:off%20&style=feature:administrative|visibility:off&key=AIzaSyBEVR6YpRzJ6qeTa3se_95mxTCBAxpgyCQ"
                        , "waterMap.png");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        // Set the image with a water map
        ImageView imageView =  new ImageView(this);
        imageView.setImageResource(R.drawable.watermap);
        waterMap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
        }
        unregisterReceiver(mUnitUpdateReciever);
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
        mMap.setOnMarkerDragListener(this);
        mMap.setOnMapLoadedCallback(this);
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
            clientService = binder.getService();
            mBound = true;
            currentHarbor = clientService.getCurrentPlayer().getHarbor();
            for (Harbor harbor : clientService.harbors) {
                if (harbor.getPlayer() != null) {
                    Log.d("BS.Map.serCon", harbor.toString());
//                    currentHarbor = harbor;
                    // Add a marker in the players' harbor
                    LatLng harborPosition = new LatLng(harbor.getGpsN(), harbor.getGpsE());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(harborPosition).title(harbor.getName() + " (" + harbor.getPlayer().getName() + ")"));
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
        // Add a new ship in the players' harbor
        clientService.requestNewUnit("Ship");
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        actualLatLng = marker.getPosition();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        double[] point = translateCoords(marker.getPosition());
        if (isWater((int)point[0], (int)point[1])) {
            Log.d("WATER", "is water");
        } else {
            Log.d("WATER", "land");
            //marker.setPosition(actualLatLng);
        }
    }

    /**
     * Returns true if the color is for water
     * @return boolean
     */
    private boolean isWater(int x, int y) {
        int pixel = waterMap.getPixel(x,y);
        Log.d("WATER", "pixel " + "RGB(" +Color.red(pixel)+ ", " + Color.green(pixel) + ", " + Color.blue(pixel) + ")");
        // check if the pixel is water
        if (Color.red(pixel) < 110 && Color.green(pixel) < 110 && Color.blue(pixel) < 110) {
            return true;
        }
        return false;
    }

    protected double[] translateCoords(LatLng source) {
        double dy = 640 -(source.latitude - LAT_MIN) / (LAT_MAX - LAT_MIN) * 640,
                dx = (source.longitude - LON_MIN) / (LON_MAX - LON_MIN) * 640;
        Log.d("WATER", "coords "+ dx + " " + dy );
        return new double[]{dx, dy};
    }

    @Override
    public void onMapLoaded() {
        Iterator it = ClientService.units.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            createMarkerForUnit((Unit) pair.getValue());
        }
    }

    private void createMarkerForUnit(Unit unit) {
        LatLng shipPosition = new LatLng(unit.getGpsN(), unit.getGpsE());
        Marker marker = mMap.addMarker(new MarkerOptions().position(shipPosition));
        marker.setFlat(true);
        marker.setDraggable(true);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.ship3b);
        marker.setIcon(descriptor);
    }
}
