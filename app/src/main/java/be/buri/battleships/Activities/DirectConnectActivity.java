package be.buri.battleships.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import be.buri.battleships.Network.Helper;
import be.buri.battleships.R;
import be.buri.battleships.Services.ClientService;

public class DirectConnectActivity extends AppCompatActivity {

    private BroadcastReceiver mReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent showHarborList = new Intent(DirectConnectActivity.this, HarborListActivity.class);
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
        setContentView(R.layout.activity_direct_connect);
    }

    public void onClickConnect(View view) {
        EditText field = (EditText) findViewById(R.id.ip_field);
        Helper.connectToServer(this, field.getText().toString());
    }
}
