package com.example.ble_light;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import com.example.ble_light.dev_mode.MultiConnectionActivityDev;
import com.example.ble_light.gatt_attr.AllGattCharacteristics;
import com.example.ble_light.gatt_attr.AllGattServices;
import com.example.ble_light.light_picker.Picker;
import com.example.ble_light.light_picker.listeners.SimpleLightSelectionListener;

@SuppressLint("ClickableViewAccessibility")
@RequiresApi(api = Build.VERSION_CODES.S)
public class LightManageActivity extends AppCompatActivity {
    private final static String TAG = LightManageActivity.class.getSimpleName();
    private final int AUTH_DELAY = 1000;
    private final int COLOR_ARC_ANGLE_RANGE = 120;

    private String access_token;
    private int effect_color_temp_min;
    private int effect_color_temp_max;
    private float color_temp_angle_step;

    public ArrayList<String> listDevicesAddresses = new ArrayList<String>();
    private ServiceConnection serviceConnection;
    private BluetoothLeService mBluetoothLeService;

    private final Handler mHandler = new Handler();

    private final String authServiceUUID = AllGattServices.lookup("Authentication");
    private final String authCharacteristicUUID = AllGattCharacteristics.lookup("Authorization data");

    private final String lightServiceUUID = AllGattServices.lookup("Light manage");
    private final String lightCharacteristicUUID = AllGattCharacteristics.lookup("Level of light");

    private TextView stateConnectView;

    private ImageButton btnLightPickerSend;
    private TextView colorTempView;
    private TextView brightTempView;

    private int colorTemperature = -1;
    private int colorBright = -1;

    private boolean authApprove = false;
    private boolean sendingApprove = false;

    private void initServiceConnection() {
        Context context = getApplicationContext();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder binder) {
                mBluetoothLeService = ((BluetoothLeService.LocalBinder) binder).getService();
                if(!mBluetoothLeService.initialize()) {
                    Log.e(TAG, "Unable to init Bluetooth!");
                    finish();
                }
                // Automatically connects to the device upon successful start-up initialization.
                boolean err_connection = mBluetoothLeService.multiconnect(listDevicesAddresses);
                int counter_connection = 0;
                if(!err_connection) {
                    while(!err_connection && counter_connection != MultiConnectionActivityDev.connection_attempts) {
                        Log.i(TAG, "Trying connection to devices");
                        err_connection = mBluetoothLeService.multiconnect(listDevicesAddresses);
                        counter_connection++;
                    }
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mBluetoothLeService = null;
            }
        };
        context.bindService(gattServiceIntent, serviceConnection,  Context.BIND_AUTO_CREATE);
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState("Devices connected");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState("Devices disconnected");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!authApprove) {
                            authDataSending(access_token);
                        }
                    }
                }, AUTH_DELAY);
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                // Something later
            }
        }
    };

    private void updateConnectionState(final String state) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String text = state + ": " + String.valueOf(listDevicesAddresses.size());
                stateConnectView.setText(text);
            }
        });
    }

    private void authDataSending(String token) {
        mBluetoothLeService.writeCharacteristic(authServiceUUID, authCharacteristicUUID, token);
        authApprove = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
        color_temp_angle_step = (float)(effect_color_temp_max - effect_color_temp_min)
                / COLOR_ARC_ANGLE_RANGE;
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Context context = getApplicationContext();

        context.unbindService(serviceConnection);
        mBluetoothLeService.disconnect();
        mBluetoothLeService = null;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_manage);

        loadSettings();
        color_temp_angle_step = (float)(effect_color_temp_max - effect_color_temp_min)
                / COLOR_ARC_ANGLE_RANGE;

        stateConnectView = findViewById(R.id.stateConnectView);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Intent intent = getIntent();
        Bundle serAddresses = intent.getBundleExtra("BundleAddresses");
        listDevicesAddresses = (ArrayList<String>) serAddresses.getSerializable("Addresses");

        Log.i(TAG, listDevicesAddresses.toString());

        btnLightPickerSend = findViewById(R.id.image_button_light_picker);
        btnLightPickerSend.setImageAlpha(0);
        btnLightPickerSend.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        btnLightPickerSend.setImageAlpha(255);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (sendingApprove) {
                            String value = String.valueOf(colorBright) +
                                    "/" + String.valueOf(colorTemperature);
                            mBluetoothLeService.writeCharacteristic(lightServiceUUID, lightCharacteristicUUID, value);
                        }
                        btnLightPickerSend.setImageAlpha(0);
                        break;
                }
                return false;
            }
        });

        colorTempView = findViewById(R.id.colorTempView);
        brightTempView = findViewById(R.id.brightTempView);

        final Picker colorPicker = findViewById(R.id.light_picker);
        colorPicker.setColorSelectionListener(new SimpleLightSelectionListener() {
            @Override
            public void onLightComponentSelection(int color, float angle,
                                                  float brightness, @NonNull String id) {
                if (id.equals("color_temp")) {
                    // angle of color temp acr from 60 to 300 in reverse order (equal 120)
                    if (angle > 0 && angle <= 60) {
                        colorTemperature = (int)((420 - (angle + 360)) * color_temp_angle_step + effect_color_temp_min);
                    } else {
                        colorTemperature = (int)((420 - angle) * color_temp_angle_step + effect_color_temp_min);
                    }
                } else if (id.equals("bright_temp")) {
                    if (brightness >= 0) {
                        colorBright = (int)(brightness * 100);
                    } else {
                        colorBright = 0;
                    }
                }
                if (colorTemperature != -1 && colorBright != -1){
                    btnLightPickerSend.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                    colorTempView.setText(getString(R.string.color_kelvin, colorTemperature));
                    brightTempView.setText(getString(R.string.color_bright, colorBright));
                    sendingApprove = true;
                }
            }
        });
        initServiceConnection();
    }

    private void loadSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        access_token = sharedPreferences.getString(
                getString(R.string.access_token_key),
                getString(R.string.access_token_default)
        );
        effect_color_temp_min = Integer.parseInt(sharedPreferences.getString(
                getString(R.string.effect_color_temp_min_key),
                getString(R.string.effect_color_temp_min_default)
        ));
        effect_color_temp_max = Integer.parseInt(sharedPreferences.getString(
                getString(R.string.effect_color_temp_max_key),
                getString(R.string.effect_color_temp_max_default)
        ));
    }
}