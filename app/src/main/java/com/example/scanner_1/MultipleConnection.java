package com.example.scanner_1;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

@SuppressWarnings({"MissingPermission"}) // all needed permissions granted in onCreate() of MainActivity
@RequiresApi(api = Build.VERSION_CODES.S)
public class MultipleConnection extends AppCompatActivity {
    private final static String TAG = ControlActivity.class.getSimpleName();

    private final String[] devicesAddress = {"24:0A:C4:EF:22:16", "9C:9C:1F:C8:42:B6"};
    private final int DEVICE_CONNECTIONS = devicesAddress.length;

    private final ServiceConnection[] serviceConnections = new ServiceConnection[DEVICE_CONNECTIONS];
    private BluetoothLeService bleService = new BluetoothLeService();

    private void multiConnect() {
        Context ctxt = getApplicationContext();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        for (int i = 0; i < DEVICE_CONNECTIONS; i++) {
            int finalI = i;
            serviceConnections[i] = new ServiceConnection() {
                private final int idx = finalI;

                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    bleService = ((BluetoothLeService.LocalBinder) binder).getService();
                    if(!bleService.initialize()) {
                        Log.e(TAG, "Unable to init Bluetooth!");
                        finish();
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    bleService.connect(devicesAddress[idx]);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    bleService = null;
                }
            };
            ctxt.bindService(gattServiceIntent, serviceConnections[i],  Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_connection);

        multiConnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Context ctxt = getApplicationContext();

        for (int i = 0; i < DEVICE_CONNECTIONS; i++) {
            ctxt.unbindService(serviceConnections[i]);
            bleService = null;
        }
    }

}