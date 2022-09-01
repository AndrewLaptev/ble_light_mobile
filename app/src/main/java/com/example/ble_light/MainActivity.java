package com.example.ble_light;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

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
import android.content.SharedPreferences;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ble_light.dev_mode.MainActivityDev;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * The main class of the application, it contains entrypoint, access to settings, advanced mode, 
 * and it is from it that device scanning is started with subsequent connection
 */
@SuppressLint("MissingPermission")
@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends MainActivityDev {

    private static final int ACCESS_BLUETOOTH_PERMISSION = 85;

    private static int scan_period;
    public static int rssiThreshold = 65;

    private SeekBar rssiSeekBar;
    private TextView rssiThreshText;

    private final Handler mHandler = new Handler();

    public static BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    private ImageButton btnStartScan;
    private ProgressBar scanProgressBar;
    private TextView stateFindView;

    private boolean scanState = false;

    private final HashMap<String, ArrayList<Integer>> mapDeviceRSSI = new HashMap<>();

    private final Kalman mKalman = new Kalman();

    /**
     * Implementing the Kalman filter has one public method filter(int init_rssi, ArrayList<Integer> rssi_list),
     * which directly performs filtering
     */
    private static class Kalman {
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

    /**
     * Calls private methods for initializing, configuring and scanning Bluetooth, 
     * as well as access rights for Bluetooth and checking for BLE support.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSettings();

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

        rssiSeekBar = findViewById(R.id.rssi_thresh_bar);
        rssiThreshText = findViewById(R.id.rssi_thresh_text);

        rssiThreshText.setText(R.string.rssi_threshold_dsc);

        rssiSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rssiThreshold = progress;
                rssiThreshText.setText(getString(R.string.rssi_dbm, rssiThreshold));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @RequiresApi(api = Build.VERSION_CODES.S)
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    /**
     * Init objects of the Bluetooth Adapter and Bluetooth Le Scanner classes
     */
    private void getBluetoothAdapterAndLeScanner() {
        final BluetoothManager mBluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    /**
     * Starts scanning Bluetooth devices with a certain scan_period time, 
     * passes a list of device addresses to the LightManageActivity class
     */
    private void scanBleDevices(boolean start) {
        stateFindView.setText(R.string.state_find_search);

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
                                        mKalman.filter(-rssiThreshold,
                                                Objects.requireNonNull(mapDeviceRSSI.get(address)));

                                int sumRSSI = meanRSSI(list_rssi_filtered);

                                Log.i("RSSI_THRESHOLD", String.valueOf(rssiThreshold));
                                Log.i("FILTERED_ARRAY_RSSI", String.valueOf(list_rssi_filtered));
                                Log.i("FILTERED_MEAN_RSSI", String.valueOf(sumRSSI));

                                if (sumRSSI <= rssiThreshold) {
                                    stateFindView.setText(R.string.state_find_found);
                                    listDeviceAddresses.add(address);
                                }
                            }
                            if (!listDeviceAddresses.isEmpty()) {
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("Addresses", (Serializable) listDeviceAddresses);
                                intent.putExtra("BundleAddresses", bundle);

                                startActivity(intent);
                            } else {
                                stateFindView.setText(R.string.state_find_not_found);
                            }
                        } else {
                            stateFindView.setText(R.string.state_find_not_found);
                        }
                    }
                }
            }, scan_period);
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * Calculates the average value of the RSSI signal strength from the input array of values
     */
    private int meanRSSI(ArrayList<Integer> rssi_list) {
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

            Objects.requireNonNull(mapDeviceRSSI.get(devAddress)).add(devRSSI);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(MainActivity.this,
                    "onScanFailed: " + String.valueOf(errorCode),
                    Toast.LENGTH_LONG).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        stateFindView.setText(R.string.state_find_init);
        loadSettings();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemChoose = item.getItemId();
        if (itemChoose == R.id.dev_mode_item) {
            final Intent intent = new Intent(this, MainActivityDev.class);
            startActivity(intent);
            return true;
        }
        if (itemChoose == R.id.settings_item) {
            final Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Loads the settings (scan_period) from root_preferences.xml
     */
    private void loadSettings(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        scan_period = Integer.parseInt(sharedPreferences.getString(
                getString(R.string.scan_period_key),
                getString(R.string.scan_period_default))
        );
    }
}