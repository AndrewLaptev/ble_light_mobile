package com.example.ble_light;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_light.dev_mode.MainActivityDev;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends MainActivityDev {
    private static final int ACCESS_BLUETOOTH_PERMISSION = 85;
    private static final int SCAN_PERIOD = 4000;
    private static final int RSSI_THRESHOLD = 75;

    private final Handler mHandler = new Handler();

    public static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    public List<BluetoothDeviceExt> listBluetoothDevice;

    private ImageButton btnStartScan;
    private ProgressBar scanProgressBar;
    private TextView stateFindView;

    private boolean scanState = false;

    private final HashMap<String, ArrayList<Integer>> mapDeviceRSSI = new HashMap<>();

    private Kalman mKalman = new Kalman();

    private class Kalman {
        private final double Q = 0.05;      // process_noise
        private final double R = 45.333332; // sensor_noise
        private double P = 0;               // estimated_error
        private final double P_init = 13.666667;

        private double x = 0;
        private double k = 0;

        public ArrayList<Integer> filter(int init_rssi, ArrayList<Integer> rssi_list) {
            ArrayList<Integer> filtered_rssi = new ArrayList<>();
            for (int i = 0; i < rssi_list.size(); i++) {
                if (i == 0) {
                    P = P_init + Q;
                    k = P / (P + R);
                    x = init_rssi + k * (rssi_list.get(i) - init_rssi);
                    P = (1 - k) * P;
                } else {
                    P = P + Q;
                    k = P / (P + R);
                    x = x + k * (rssi_list.get(i) - x);
                    P = (1 - k) * P;
                }
                filtered_rssi.add((int)x);
            }
            return filtered_rssi;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listBluetoothDevice = new ArrayList<>();
        getBluetoothAdapterAndLeScanner();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, ACCESS_BLUETOOTH_PERMISSION);
        }

        // Check if BLE is supported on the device
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,
                    "BLE not supported in this device!", Toast.LENGTH_SHORT).show();
            finish();
        }

        scanProgressBar = findViewById(R.id.progressBar);
        scanProgressBar.setVisibility(View.INVISIBLE);

        stateFindView = findViewById(R.id.stateFindView);

        btnStartScan = (ImageButton) findViewById(R.id.start_scan_button);
        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanState = !scanState;
                if (scanState) {
                    btnStartScan.setImageAlpha(75);
                    scanProgressBar.setVisibility(View.VISIBLE);
                    scanBleDevices(true);
                } else {
                    scanBleDevices(false);
                    btnStartScan.setImageAlpha(255);
                    scanProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void getBluetoothAdapterAndLeScanner() {
        final BluetoothManager mBluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    private void scanBleDevices(boolean start) {
        stateFindView.setText(R.string.state_find_search);
        if (!listBluetoothDevice.isEmpty()) {
            listBluetoothDevice.clear();
        }

        if (start) {
            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(BluetoothLeService.PARCEL_FILTER_SERVICE_UUID)
                    .build();
            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
            scanFilters.add(scanFilter);
            ScanSettings scanSettings =
                    new ScanSettings.Builder().build();
            mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(scanState) {
                        mBluetoothLeScanner.stopScan(scanCallback);

                        btnStartScan.setImageAlpha(255);
                        scanProgressBar.setVisibility(View.INVISIBLE);
                        scanState = !scanState;

                        final Intent intent = new Intent(MainActivity.this,
                                LightManageActivity.class);

                        ArrayList<String> listDeviceAddresses = new ArrayList<String>();

                        if (!mapDeviceRSSI.isEmpty()) {
                            for (String address : mapDeviceRSSI.keySet()) {
                                ArrayList<Integer> list_rssi_filtered =
                                        mKalman.filter(-65, mapDeviceRSSI.get(address));

                                int sumRSSI = filterRSSI(list_rssi_filtered);

                                if (sumRSSI <= RSSI_THRESHOLD) {
                                    stateFindView.setText(R.string.state_find_found);
                                    listDeviceAddresses.add(address);
                                }
                            }
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("Addresses", (Serializable)listDeviceAddresses);
                            intent.putExtra("BundleAddresses", bundle);

                            startActivity(intent);
                        } else {
                            stateFindView.setText(R.string.state_find_not_found);
                        }
                    }
                }
            }, SCAN_PERIOD);
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    private int filterRSSI(ArrayList<Integer> rssi_list) {
        int sumRSSI = 0;
        int divider = 0;

        for (int rssi : rssi_list) {
            sumRSSI += Math.abs(rssi);
            divider++;
        }
        sumRSSI = sumRSSI/divider;
        return sumRSSI;
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            String devAddress = result.getDevice().getAddress();
            int devRSSI = result.getRssi();

            if (mapDeviceRSSI.get(devAddress) == null) {
                mapDeviceRSSI.put(devAddress, new ArrayList<Integer>());
            }

            mapDeviceRSSI.get(devAddress).add(devRSSI);

            BluetoothDeviceExt device = new BluetoothDeviceExt();
            device.setDevice(result.getDevice());
            device.setRawRSSI(result.getRssi());
            addBluetoothDevice(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult result : results) {
                BluetoothDeviceExt device = new BluetoothDeviceExt();
                device.setDevice(result.getDevice());
                device.setRawRSSI(result.getRssi());
                addBluetoothDevice(device);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this,
                    "onScanFailed: " + String.valueOf(errorCode),
                    Toast.LENGTH_LONG).show();
        }
    };

    private void addBluetoothDevice(BluetoothDeviceExt device){
        if(!listBluetoothDevice.contains(device)){
            listBluetoothDevice.add(device);
        } else {
            listBluetoothDevice.get(listBluetoothDevice.indexOf(device)).setRawRSSI(device.getRawRSSI());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stateFindView.setText(R.string.state_find_init);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.activity_main_menu) {
            final Intent intent = new Intent(this, MainActivityDev.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}