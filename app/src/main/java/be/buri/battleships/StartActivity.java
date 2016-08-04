package be.buri.battleships;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import be.buri.battleships.Activities.ConnectToServer;
import be.buri.battleships.Activities.HarborListActivity;
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
        Intent startServer = new Intent(this, ServerService.class);
        startService(startServer);
        Intent startClient = new Intent(this, ClientService.class);
        startClient.putExtra(ClientService.INTENT_TYPE, ClientService.CONNECT_TO_HOST);
        startClient.putExtra(ClientService.HOST_NAME, "localhost");
        startService(startClient);
        Intent showHarborList = new Intent(this, HarborListActivity.class);
        startActivity(showHarborList);
    }

    public void joinGame(View view) {
        Intent showServerList = new Intent(this, ConnectToServer.class);
        startActivity(showServerList);
    }
}
