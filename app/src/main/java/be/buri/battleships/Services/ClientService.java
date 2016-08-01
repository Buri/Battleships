package be.buri.battleships.Services;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by buri on 1.8.16.
 */
public class ClientService extends EngineService {
    private final IBinder mBinder = new ClientBinder();

    public class ClientBinder extends Binder {
        ClientService getService() {
            return ClientService.this;
        }
    }

    public ClientService() {
        super("ClientService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    @Override
    protected void onHandleIntent(Intent intent) {

    }
}
