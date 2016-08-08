package be.buri.battleships.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;
import java.util.Vector;

import be.buri.battleships.Player;
import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Units.Harbor;

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

        playerList = new ArrayAdapter(this, R.layout.device_name);
        ListView playerListView = (ListView)findViewById(R.id.playerList);
        playerListView.setAdapter(playerList);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    private ClientService clientService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ClientService.ClientBinder binder = (ClientService.ClientBinder) iBinder;
            clientService = binder.getService();
            mBound = true;

            // make harbors as a radio button
            clientService.getPlayers();
            for (Player player : clientService.players) {
                playerList.add(player.getName());
            }

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
}
