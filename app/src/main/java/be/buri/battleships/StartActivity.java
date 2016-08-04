package be.buri.battleships;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import be.buri.battleships.Activities.ConnectToServer;
import be.buri.battleships.Activities.PlayerListActivity;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Services.ServerService;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }

    public void hostGame(View view) {
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
        Intent showPlayerList = new Intent(this, PlayerListActivity.class);
        startActivity(showPlayerList);
    }

    public void joinGame(View view) {
        Intent showServerList = new Intent(this, ConnectToServer.class);
        startActivity(showServerList);
    }
}
