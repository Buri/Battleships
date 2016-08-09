package be.buri.battleships;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import be.buri.battleships.Activities.ConnectToServerActivity;
import be.buri.battleships.Activities.HarborListActivity;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Services.ServerService;

public class StartActivity extends AppCompatActivity {

    private BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent showHarborList = new Intent(StartActivity.this, HarborListActivity.class);
            startActivity(showHarborList);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        // Set the image on the start screen
        ImageView mImageView = (ImageView) findViewById(R.id.imageView);
        mImageView.setImageResource(R.drawable.seaship);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReciever);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.buttonHostGame).setEnabled(true);
        findViewById(R.id.buttonConnectToHost).setEnabled(true);
        registerReceiver(mReciever, new IntentFilter(ClientService.INTENT_SELECT_HARBOR));
    }

    public void hostGame(View view) {
        view.setEnabled(false);
        if (!ServerService.running) {
            Intent startServer = new Intent(this, ServerService.class);
            startService(startServer);
        }
        if (!ClientService.running) {
            Intent startClient = new Intent(this, ClientService.class);
            startClient.putExtra(ClientService.INTENT_TYPE, ClientService.CONNECT_TO_HOST);
            startClient.putExtra(ClientService.HOST_NAME, "localhost");
            startService(startClient);
        }
    }

    public void joinGame(View view) {
        view.setEnabled(false);
        Intent showServerList = new Intent(this, ConnectToServerActivity.class);
        startActivity(showServerList);
    }
}
