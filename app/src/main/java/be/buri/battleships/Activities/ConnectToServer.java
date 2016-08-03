package be.buri.battleships.Activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;

import be.buri.battleships.R;

public class ConnectToServer extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_activity_select_server);
        setContentView(R.layout.activity_connect_to_server);
        // Fetch the bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // Make sure that the device supports bluetooth
        if (mBluetoothAdapter == null) {
            return; // The device does not support bluetooth
        }

        // This is a bluetooth app, so check right away that bluetooth is enabled
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mNewDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        ListView newDevicesListView = (ListView) findViewById(R.id.newDevicesList);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);

        mPairedDevicesArrayAdapter = new ArrayAdapter<>(this, R.layout.device_name);
        ListView pairedDevicesListView = (ListView) findViewById(R.id.pairedDevicesList);
        pairedDevicesListView.setAdapter(mPairedDevicesArrayAdapter);

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        for (BluetoothDevice device : pairedDevices) {
            mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        }

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothAdapter.cancelDiscovery();
    }

    public void onClickDeviceSearch(View view) {
        mBluetoothAdapter.startDiscovery();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Toast.makeText(this, "The user turned on Bluetooth!", Toast.LENGTH_SHORT).show();
                break;
        }
    }
    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // If it's already paired, skip it, because it's been listed already in the paired
                    // devices
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    }
                    break;
            }
        }
    };
}
