package com.example.ble_light;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

@SuppressWarnings({"MissingPermission"}) // all needed permissions granted in MainActivity.onCreate()
@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private final ArrayList <BluetoothGattExt> listBluetoothGattsExt = new ArrayList<>();

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "scanner_1.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "scanner_1.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "scanner_1.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "scanner_1.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "scanner_1.EXTRA_DATA";

    public final static String STR_FILTER_SERVICE_UUID = AllGattServices.lookup("Light manage");
    public final static ParcelUuid PARCEL_FILTER_SERVICE_UUID = ParcelUuid.fromString(STR_FILTER_SERVICE_UUID);

    public class BluetoothGattExt {
        private BluetoothGatt mBluetoothGatt;
        private String mBluetoothDeviceAddress;
        private int mConnectionState = STATE_DISCONNECTED;
        private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                String intentAction;
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    broadcastUpdate(intentAction);

                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());
                }else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    broadcastUpdate(intentAction);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                }else{
                    Log.w(TAG, "onServicesDiscovered received: " + status);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt,
                                             BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("TEST_READ_CB_ADDRESS", gatt.getDevice().getAddress());
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt,
                                              BluetoothGattCharacteristic characteristic, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt,
                                                BluetoothGattCharacteristic characteristic) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        };

        public BluetoothGatt getBluetoothGatt() {
            return mBluetoothGatt;
        }

        public String getBluetoothDeviceAddress() {
            return mBluetoothDeviceAddress;
        }

        public int getConnectionState() {
            return mConnectionState;
        }

        public BluetoothGattCallback getGattCallback() {
            return mGattCallback;
        }

        public void setBluetoothGatt(BluetoothGatt gatt) {
            mBluetoothGatt = gatt;
        }

        public void setBluetoothDeviceAddress(String address) {
            mBluetoothDeviceAddress = address;
        }

        public void setConnectionState(int state) {
            mConnectionState = state;
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        //remove special handling for time being
        Log.w(TAG, "broadcastUpdate()");
        final byte[] data = characteristic.getValue();
        Log.v(TAG, "data.length: " + data.length);
        if (data != null && data.length > 0) {  
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for(byte byteChar : data) {
                stringBuilder.append(String.format("%02X ", byteChar));
                Log.v(TAG, String.format("%02X ", byteChar));
            }
            intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }
    private final IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(
     *         android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        BluetoothGattExt gatt = new BluetoothGattExt();
        // Previously connected device.Try to reconnect.
        if(Objects.equals(gatt.getBluetoothDeviceAddress(), address) && gatt.getBluetoothGatt() != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (gatt.getBluetoothGatt().connect()) {
                gatt.setConnectionState(STATE_CONNECTED);
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        BluetoothGatt mBluetoothGatt = device.connectGatt(this, false, gatt.getGattCallback());
        gatt.setBluetoothGatt(mBluetoothGatt);
        Log.d(TAG, "Trying to create a new connection.");
        gatt.setBluetoothDeviceAddress(address);
        gatt.setConnectionState(STATE_CONNECTING);
        listBluetoothGattsExt.add(gatt);
        return true;
    }

    public boolean multiconnect(final ArrayList<String> listAddresses) {
        boolean err = false;
        for (String address :listAddresses) {
            err = connect(address);
            if (!err) {
                return false;
            }
        }
        return err;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(
     * android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || listBluetoothGattsExt.isEmpty()) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        for (BluetoothGattExt gatt : listBluetoothGattsExt) {
            gatt.getBluetoothGatt().disconnect();
        }
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (listBluetoothGattsExt.isEmpty()) {
            return;
        }
        for (BluetoothGattExt gatt : listBluetoothGattsExt) {
            gatt.getBluetoothGatt().close();
            listBluetoothGattsExt.set(listBluetoothGattsExt.indexOf(gatt), null);
        }
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(
     * android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param init_characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic init_characteristic) {
        if (mBluetoothAdapter == null || listBluetoothGattsExt.isEmpty()) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        for (BluetoothGattExt gatt : listBluetoothGattsExt) {
            UUID uuid_service = init_characteristic.getService().getUuid();
            UUID uuid_char = init_characteristic.getUuid();

            BluetoothGattCharacteristic characteristic = gatt.getBluetoothGatt()
                    .getService(uuid_service).getCharacteristic(uuid_char);

            gatt.getBluetoothGatt().readCharacteristic(characteristic);
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic init_characteristic) {
        if (mBluetoothAdapter == null || listBluetoothGattsExt.isEmpty()) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        for (BluetoothGattExt gatt : listBluetoothGattsExt) {
            UUID uuid_service = init_characteristic.getService().getUuid();
            UUID uuid_char = init_characteristic.getUuid();

            BluetoothGattCharacteristic characteristic = gatt.getBluetoothGatt()
                    .getService(uuid_service).getCharacteristic(uuid_char);

            characteristic.setValue(init_characteristic.getValue());
            characteristic.setWriteType(init_characteristic.getWriteType());

            gatt.getBluetoothGatt().writeCharacteristic(characteristic);
        }
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param init_characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic init_characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || listBluetoothGattsExt.isEmpty()) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        for (BluetoothGattExt gatt : listBluetoothGattsExt) {
            UUID uuid_service = init_characteristic.getService().getUuid();
            UUID uuid_char = init_characteristic.getUuid();

            BluetoothGattCharacteristic characteristic = gatt.getBluetoothGatt()
                    .getService(uuid_service).getCharacteristic(uuid_char);

            characteristic.setValue(init_characteristic.getValue());
            characteristic.setWriteType(init_characteristic.getWriteType());

            gatt.getBluetoothGatt().setCharacteristicNotification(characteristic, enabled);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (listBluetoothGattsExt.isEmpty()) return null;
        return listBluetoothGattsExt.get(0).getBluetoothGatt().getServices(); // all devices have equals services
    }



}