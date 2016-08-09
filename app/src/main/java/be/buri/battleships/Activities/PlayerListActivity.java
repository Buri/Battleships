package be.buri.battleships.Activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import be.buri.battleships.Engine.Const;
import be.buri.battleships.Player;
import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;

public class PlayerListActivity extends AppCompatActivity {

    private boolean mBound;
    ArrayAdapter playerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
        Intent intent = new Intent(this, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, ClientService.GET_PLAYER_LIST);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        playerList = new ArrayAdapter(this, R.layout.player_name);
        ListView playerListView = (ListView) findViewById(R.id.playerList);
        playerListView.setAdapter(playerList);

        registerReceiver(mUpdatePlayerListReceiver, new IntentFilter(Const.BROADCAST_UPDATE_PLAYER_LIST));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        unregisterReceiver(mUpdatePlayerListReceiver);
    }

    private ClientService clientService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ClientService.ClientBinder binder = (ClientService.ClientBinder) iBinder;
            clientService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };


    public void launchGame(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    BroadcastReceiver mUpdatePlayerListReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            playerList.clear();
            synchronized (clientService) {
                synchronized (playerList) {
                    for (Player player : clientService.players) {
                        playerList.add(player.getName() + "\n" + player.getHarbor().getName());
                    }
                }
            }
            Log.d("BS.UI.updatePlayerList", Integer.toString(clientService.players.size()));
        }
    };
}
