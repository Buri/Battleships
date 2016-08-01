package be.buri.battleships;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
        startService(startClient);
        Intent showPlayerList = new Intent(this, PlayerListActivity.class);
        startActivity(showPlayerList);
    }
}
