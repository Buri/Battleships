package be.buri.battleships.Network;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import be.buri.battleships.Services.ClientService;

/**
 * Created by buri on 8.8.16.
 */
public class Helper {
    public static void connectToServer (Context context, String serverIp) {
        Toast.makeText(context, "Connecting to " + serverIp, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, ClientService.class);
        intent.putExtra(ClientService.INTENT_TYPE, ClientService.CONNECT_TO_HOST);
        intent.putExtra(ClientService.HOST_NAME, serverIp);
        context.startService(intent);
    }

    public static void onFailedToConnect(Context context) {
        Toast.makeText(context, "Failed to connect to the server", Toast.LENGTH_SHORT).show();
    }
}
