package be.buri.battleships.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import be.buri.battleships.R;

public class PlayerListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_list);
    }

    public void launchGame(View view)
    {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }
}
