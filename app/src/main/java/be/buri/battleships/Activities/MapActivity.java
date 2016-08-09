package be.buri.battleships.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import be.buri.battleships.Engine.Const;
import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Units.Harbor;
import be.buri.battleships.Units.Ship;
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

    private Marker currentlyDraggedMarker;

    LatLng actualLatLng;


    private BroadcastReceiver mUnitAddReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Unit unit = ClientService.units.get(intent.getIntExtra("UNIT_ID", -1));
            if (null != unit) {
                createMarkerForUnit(unit);
            }
        }
    };

    private BroadcastReceiver mUnitUpdateReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Unit unit = ClientService.units.get(intent.getIntExtra("UNIT_ID", -1));
            if (null != unit) {
                if (unit.getMarker() != null && unit.getMarker() != currentlyDraggedMarker) {
                    unit.getMarker().setPosition(new LatLng(unit.getGpsN(), unit.getGpsE()));
                    if (unit instanceof Ship && ((Ship) unit).getArrow() != null) {
                        Polyline arrow = ((Ship) unit).getArrow();
                        arrow.setVisible(((Ship) unit).isMoving());
                        ((Ship) unit).getArrow().setPoints(getLineFromUnit((Ship) unit));
                    }
                }
            }
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
        registerReceiver(mUnitAddReciever, new IntentFilter(Const.BROADCAST_ADD_UNITS));
        registerReceiver(mUnitUpdateReciever, new IntentFilter(Const.BROADCAST_UPDATE_UNITS));
        // Set the image with a water map
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.watermap_2);
        waterMap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        waterMap.prepareToDraw();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
        }
        unregisterReceiver(mUnitUpdateReciever);
        unregisterReceiver(mUnitAddReciever);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
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
                    // Add a marker in the players' harbor
                    LatLng harborPosition = new LatLng(harbor.getGpsN(), harbor.getGpsE());
                    Marker marker = mMap.addMarker(new MarkerOptions().position(harborPosition).title(harbor.getName() + " (" + harbor.getPlayer().getName() + ")"));
                    marker.setFlat(true);
                    BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.mipmap.harbor);
                    marker.setIcon(descriptor);
                    mMap.addCircle(new CircleOptions().center(harborPosition).radius(8000).clickable(false).fillColor(harbor.getPlayer().getColor()).strokeWidth(0));
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
        currentlyDraggedMarker = marker;
        actualLatLng = marker.getPosition();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        double[] point = translateCoords(marker.getPosition());
        if (isWater((int) point[0], (int) point[1])) {
            Log.d("WATER", "is water");
            clientService.orderUnitMovement(marker.getPosition(), clientService.findShipByMarker(marker));
        } else {
            Log.d("WATER", "land");
            Toast.makeText(this, R.string.invalid_order_target_land, Toast.LENGTH_SHORT).show();
            marker.setPosition(actualLatLng);
        }
        currentlyDraggedMarker = null;
        actualLatLng = null;
    }

    /**
     * Returns true if the color is for water
     *
     * @return boolean
     */
    private boolean isWater(int x, int y) {
        int pixel = waterMap.getPixel(x, y);
        Log.d("WATER", "pixel (" + x + "," + y + ") " + "RGB(" + Color.red(pixel) + ", " + Color.green(pixel) + ", " + Color.blue(pixel) + ") " + pixel);
        // check if the pixel is water
        return (Color.red(pixel) < 110 && Color.green(pixel) < 110 && Color.blue(pixel) < 110);
    }

    protected double[] translateCoords(LatLng source) {
        double dy = 640 - (source.latitude - LAT_MIN) / (LAT_MAX - LAT_MIN) * 640,
                dx = (source.longitude - LON_MIN) / (LON_MAX - LON_MIN) * 640;
        return new double[]{dx, dy};
    }

    @Override
    public void onMapLoaded() {
        Iterator it = ClientService.units.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
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
        unit.setMarker(marker);

        Ship ship = (Ship) unit;
        Polyline line = mMap.addPolyline(new PolylineOptions());
        line.setClickable(false);
        line.setPoints(getLineFromUnit(ship));
        line.setColor(unit.getPlayer().getColor());
        ship.setArrow(line);
    }

    private ArrayList<LatLng> getLineFromUnit(Ship ship) {
        ArrayList<LatLng> points = new ArrayList<LatLng>(2);
        points.add(new LatLng(ship.getGpsN(), ship.getGpsE()));
        points.add(new LatLng(ship.getDestLat(), ship.getDestLon()));
        return points;
    }
}
