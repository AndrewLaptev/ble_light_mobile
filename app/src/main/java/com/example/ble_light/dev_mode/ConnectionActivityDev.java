package com.example.ble_light.dev_mode;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_light.BluetoothLeService;
import com.example.ble_light.R;
import com.example.ble_light.gatt_attr.AllGattCharacteristics;
import com.example.ble_light.gatt_attr.AllGattServices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"MissingPermission"}) // all needed permissions granted in onCreate() of MainActivity
@RequiresApi(api = Build.VERSION_CODES.S)
public class ConnectionActivityDev extends AppCompatActivity {
    private final static String TAG = ConnectionActivityDev.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private int connection_attempts;

    private String mDeviceName;
    private String mDeviceAddress;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private BluetoothLeService mBluetoothLeService;

    private TextView textViewState;
    private ExpandableListView mGattServicesList;

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to init Bluetooth!");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            boolean err_connection = mBluetoothLeService.connect(mDeviceAddress);
            int counter_connection = 0;
            if(!err_connection) {
                while(!err_connection && counter_connection != connection_attempts) {
                    Log.i(TAG, "Trying connection to device");
                    err_connection = mBluetoothLeService.connect(mDeviceAddress);
                    counter_connection++;
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };


    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState("GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState("GATT_DISCONNECTED");
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mGattServicesList.setAdapter((SimpleExpandableListAdapter) null);
    }

    private void updateConnectionState(final String st) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewState.setText(st);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            textViewState.setText(data);
        }
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        final String UNKNOWN_SERVICE_STR = "Unknown Service";
        final String UNKNOWN_CHARA_STR = "Unknown Characteristic";
        String uuid = null;
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData =
                new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();

            currentServiceData.put(
                    LIST_NAME, AllGattServices.lookup(uuid, UNKNOWN_SERVICE_STR));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();

                charas.add(gattCharacteristic);
                currentCharaData.put(
                        LIST_NAME, AllGattCharacteristics.lookup(uuid, UNKNOWN_CHARA_STR));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] {android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] {LIST_NAME, LIST_UUID},
                new int[] {android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }

    private void showSentDialog(Context c, BluetoothGattCharacteristic characteristic) {
        final EditText taskEditText = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("Send data")
                .setMessage("Enter your string message")
                .setView(taskEditText)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        characteristic.setValue(String.valueOf(taskEditText.getText()));
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
                        mBluetoothLeService.writeCharacteristic(characteristic);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // If a given GATT characteristic is selected, check for supported features.  This sample
    // demonstrates 'Read' and 'Notify' features.  See
    // http://d.android.com/reference/android/bluetooth/BluetoothGatt.html for the complete
    // list of supported characteristic features.
    private final ExpandableListView.OnChildClickListener servicesListClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                            int childPosition, long id) {
                    if (mGattCharacteristics != null) {
                        final BluetoothGattCharacteristic characteristic =
                                mGattCharacteristics.get(groupPosition).get(childPosition);
                        final int charaProp = characteristic.getProperties();

                        if ((charaProp - BluetoothGattCharacteristic.PROPERTY_READ) == 0) {
                            // If there is an active notification on a characteristic, clear
                            // it first so it doesn't update the data field on the user interface.
                            if (mNotifyCharacteristic != null) {
                                mBluetoothLeService.setCharacteristicNotification(
                                        mNotifyCharacteristic, false);
                                mNotifyCharacteristic = null;
                            }
                            mBluetoothLeService.readCharacteristic(characteristic);
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Readable!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Notify!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Writable no response!",
                                    Toast.LENGTH_SHORT).show();
                            showSentDialog(ConnectionActivityDev.this, characteristic);
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_WRITE) == 0) {
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Writable!",
                                    Toast.LENGTH_SHORT).show();
                            showSentDialog(ConnectionActivityDev.this, characteristic);
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_BROADCAST) == 0) {
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Broadcast!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0) {
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Indicate!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_WRITE - BluetoothGattCharacteristic.PROPERTY_READ
                        - BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                            Toast.makeText(ConnectionActivityDev.this,
                                    "Read, Write and Notify!",
                                    Toast.LENGTH_SHORT).show();

                            showSentDialog(ConnectionActivityDev.this, characteristic);
                        }
                        return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection_dev);

        loadSettings();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        TextView textViewDeviceName = (TextView)findViewById(R.id.textDeviceName);
        TextView textViewDeviceAddr = (TextView)findViewById(R.id.textDeviceAddress);
        textViewState = (TextView)findViewById(R.id.textState);

        textViewDeviceName.setText(mDeviceName);
        textViewDeviceAddr.setText(mDeviceAddress);

        mGattServicesList = (ExpandableListView) findViewById(R.id.gatt_services);
        mGattServicesList.setOnChildClickListener(servicesListClickListener);

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSettings();
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
        unbindService(mServiceConnection);
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

    private static HashMap<String, String> attributes = new HashMap();

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }

    private void loadSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        connection_attempts = Integer.parseInt(sharedPreferences.getString(
                getString(R.string.reconnections_attempts_key),
                getString(R.string.reconnections_attempts_default)
        ));
    }
}