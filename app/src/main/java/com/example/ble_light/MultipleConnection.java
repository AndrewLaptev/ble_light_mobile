package com.example.ble_light;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings({"MissingPermission"}) // all needed permissions granted in onCreate() of MainActivity
@RequiresApi(api = Build.VERSION_CODES.S)
public class MultipleConnection extends AppCompatActivity {
    private final static String TAG = ControlActivity.class.getSimpleName();

    public boolean mConnected = false;

    private TextView textViewState;
    private ExpandableListView mGattServicesList;
    private int DEVICE_CONNECTIONS;

    public ArrayList<String> listDevicesAddresses = new ArrayList<String>();

    private ServiceConnection[] serviceConnections;
    private BluetoothLeService mBluetoothLeService;

    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private void multiConnect() {
        DEVICE_CONNECTIONS = listDevicesAddresses.size();
        serviceConnections = new ServiceConnection[DEVICE_CONNECTIONS];

        Context context = getApplicationContext();
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        for (int i = 0; i < DEVICE_CONNECTIONS; i++) {
            int finalI = i;
            serviceConnections[i] = new ServiceConnection() {
                private final int idx = finalI;

                @Override
                public void onServiceConnected(ComponentName name, IBinder binder) {
                    mBluetoothLeService = ((BluetoothLeService.LocalBinder) binder).getService();
                    if(!mBluetoothLeService.initialize()) {
                        Log.e(TAG, "Unable to init Bluetooth!");
                        finish();
                    }
                    // Automatically connects to the device upon successful start-up initialization.
                    mBluetoothLeService.connect(listDevicesAddresses.get(idx));
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    mBluetoothLeService = null;
                }
            };
            context.bindService(gattServiceIntent, serviceConnections[i],  Context.BIND_AUTO_CREATE);
        }
    }

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
                mConnected = true;
                updateConnectionState("GATT_CONNECTED");
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
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
                            Toast.makeText(MultipleConnection.this,
                                    "Readable!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                            mBluetoothLeService.setCharacteristicNotification(mNotifyCharacteristic, true);
                            Toast.makeText(MultipleConnection.this,
                                    "Notify!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) == 0) {
                            Toast.makeText(MultipleConnection.this,
                                    "Writable!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_BROADCAST) == 0) {
                            Toast.makeText(MultipleConnection.this,
                                    "Broadcast!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_INDICATE) == 0) {
                            Toast.makeText(MultipleConnection.this,
                                    "Indicate!",
                                    Toast.LENGTH_SHORT).show();
                        } else if ((charaProp - BluetoothGattCharacteristic.PROPERTY_WRITE - BluetoothGattCharacteristic.PROPERTY_READ
                                - BluetoothGattCharacteristic.PROPERTY_NOTIFY) == 0) {
                            Toast.makeText(MultipleConnection.this,
                                    "Read, Write and Notify!",
                                    Toast.LENGTH_SHORT).show();

                            showSentDialog(MultipleConnection.this, characteristic);
                        }
                        return true;
                    }
                    return false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_connection);

        Intent intent = getIntent();
        Bundle serAddresses = intent.getBundleExtra("BundleAddresses");
        listDevicesAddresses = (ArrayList<String>) serAddresses.getSerializable("Addresses");
        Log.i("TEST", listDevicesAddresses.toString());

        textViewState = (TextView)findViewById(R.id.multi_gatt_state);

        mGattServicesList = (ExpandableListView) findViewById(R.id.multi_gatt_services);
        mGattServicesList.setOnChildClickListener(servicesListClickListener);

        multiConnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.multiconnect(listDevicesAddresses);
            Log.d(TAG, "Connect request result=" + result);
        }
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

        for (int i = 0; i < DEVICE_CONNECTIONS; i++) {
            context.unbindService(serviceConnections[i]);
        }
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
}