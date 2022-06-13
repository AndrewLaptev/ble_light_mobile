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
import android.widget.Toast;

import com.example.ble_light.dev.MainActivityDev;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends MainActivityDev {
    private static final int ACCESS_BLUETOOTH_PERMISSION = 85;
    private static final int SCAN_PERIOD = 3000;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    List<BluetoothDeviceExt> listBluetoothDevice;
    private Handler mHandler;

    private ImageButton btnStartScan;
    private ProgressBar scanProgressBar;

    private boolean scanState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();
        listBluetoothDevice = new ArrayList<>();
        getBluetoothAdapterAndLeScanner();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
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

        btnStartScan = (ImageButton) findViewById(R.id.start_scan_button);
        btnStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanState = !scanState;
                if (scanState) {
                    btnStartScan.setImageAlpha(75);
                    scanProgressBar.setVisibility(View.VISIBLE);
                    scanBleDevices();
                } else {
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

    private void scanBleDevices() {
        if (!listBluetoothDevice.isEmpty()) {
            listBluetoothDevice.clear();
        }
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
                mBluetoothLeScanner.stopScan(scanCallback);
                btnStartScan.setImageAlpha(255);
                scanProgressBar.setVisibility(View.INVISIBLE);
                scanState = false;
                btnStartScan.setEnabled(true);
                final Intent intent = new Intent(MainActivity.this, LightManageActivity.class);
                startActivity(intent);
                for (BluetoothDeviceExt device : listBluetoothDevice) {
                    Log.i("TEST", device.getDevice().getAddress());
                }
            }
        }, SCAN_PERIOD);
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
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