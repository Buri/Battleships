package be.buri.battleships.Activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.Arrays;

import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;
import be.buri.battleships.Units.Harbor;

public class HarborListActivity extends AppCompatActivity {

    private RadioGroup harborsRadioGroup;
    ClientService clientService;
    private boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_harbor_list);
        // get the radio group of harbors
        harborsRadioGroup = (RadioGroup) findViewById(R.id.harborsRadioGroup);
        Intent intent = new Intent(this, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, -1);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        // hide the Next button
        Button nextButton = (Button) findViewById(R.id.nextButton);
        nextButton.setVisibility(View.GONE);

        EditText playerName = (EditText) findViewById(R.id.editTextPlayerName);
        Cursor c = getApplication().getContentResolver().query(ContactsContract.Profile.CONTENT_URI, null, null, null, null);
        c.moveToFirst();
        playerName.setText(c.getString(c.getColumnIndex("display_name")));
        c.close();


        //ClientService service = (ClientService);
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

            // make harbors as a radio button
            for (Harbor item : clientService.harbors) {
                makeRadioButton(item.getName());
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };

    public void launchPlayerList(View view)
    {
        // save the player name and the selected harbor
        Intent setPlayer = new Intent(this, ClientService.class);
        EditText playerName = (EditText) findViewById(R.id.editTextPlayerName);
        RadioButton selectedHarbor = (RadioButton) findViewById(harborsRadioGroup.getCheckedRadioButtonId());
        setPlayer.putExtra(ClientService.INTENT_TYPE, ClientService.SET_PLAYER_NAME);
        setPlayer.putExtra("Player name", playerName.getText().toString());
        setPlayer.putExtra("Harbor name", selectedHarbor.getText().toString());
        startService(setPlayer);

        Intent showPlayerList = new Intent(this, PlayerListActivity.class);
        startActivity(showPlayerList);
    }

    private void makeRadioButton(String name)
    {
        RadioButton newRadioButton = new RadioButton(this);
        newRadioButton.setText(name);
        newRadioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show the next button
                Button nextButton = (Button) findViewById(R.id.nextButton);
                nextButton.setVisibility(View.VISIBLE);
            }
        });
        harborsRadioGroup.addView(newRadioButton);
    }
}
