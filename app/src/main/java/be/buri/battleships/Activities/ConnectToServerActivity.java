package be.buri.battleships.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;


import be.buri.battleships.Network.Helper;
import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;

public class ConnectToServerActivity extends AppCompatActivity {
    ClientService clientService;
    private boolean mBound = false;
    private ArrayAdapter<String> servers;


    private BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent showHarborList = new Intent(ConnectToServerActivity.this, HarborListActivity.class);
            startActivity(showHarborList);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReciever);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReciever, new IntentFilter(ClientService.INTENT_SELECT_HARBOR));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_activity_select_server);
        setContentView(R.layout.activity_connect_to_server);

        Intent intent = new Intent(this, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, -1);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ClientService.ClientBinder binder = (ClientService.ClientBinder) iBinder;
            clientService = binder.getService();
            mBound = true;

            servers = new ArrayAdapter<String>(ConnectToServerActivity.this, R.layout.device_name);
            ListView availableServersList = (ListView) findViewById(R.id.availableServersList);
            availableServersList.setAdapter(servers);
            fillServerList();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public void fillServerList() {
        servers.clear();
        Button refreshServerListButton = (Button) findViewById(R.id.refreshServerList);
        refreshServerListButton.setEnabled(false);
        do {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (clientService.isWorking());
        Log.i("BS.SelectServer", "# of servers found: " + Integer.toString(clientService.localNetworkServers.size()));
        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        refreshServerListButton.setEnabled(true);

        for (ClientService.ServerInfo info : clientService.localNetworkServers) {
            if (info.version.equals(pInfo.versionName)) {
                Log.i("BS.SelectServer", info.name + "\n" + info.ip);
                servers.add(info.name + "\n" + info.ip);
            } else {
                Log.d("BS.SelectServer", "Version mismatch: " + info.name + "\n" + info.ip + "\n" + info.version + " != " + pInfo.versionName);
            }
        }
    }

    public void onClickRefreshServers(View view) {
        Intent intent = new Intent(this, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, ClientService.FIND_SERVERS);
        startService(intent);
        fillServerList();
    }

    public void onClickDirectConnect(View view) {
        Intent intent = new Intent(this, DirectConnectActivity.class);
        startActivity(intent);
    }

    public void onClickSelectServer(View view) {
        AppCompatTextView view1 = (AppCompatTextView)view;
        Helper.connectToServer(this, view1.getText().toString().split("\n")[1]);
    }
}
