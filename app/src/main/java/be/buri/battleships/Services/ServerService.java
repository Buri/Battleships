package be.buri.battleships.Services;

import android.content.Intent;

/**
 * Created by buri on 1.8.16.
 */
public class ServerService extends EngineService {
    public ServerService() {
        super("ServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
    }
}
