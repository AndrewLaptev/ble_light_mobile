package com.example.ble_light;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"MissingPermission"}) // all needed permissions granted in onCreate()
@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;

    Button btnScan;
    Button btnFilter;
    Button btnMultiConnect;
    ListView listViewLE;

    List<BluetoothDeviceRSSI> listBluetoothDevice;
    ListAdapter adapterLeScanResult;

    private boolean mScanning;
    private boolean nodeFilter = false;

    private static final int REQUEST_ENABLE_BLUETOOTH = 1;
    private static final int ACCESS_BLUETOOTH_PERMISSION = 85;

    public class BluetoothDeviceRSSI {
        private BluetoothDevice mBluetoothDevice;
        private int RawRSSI;

        public void setRawRSSI(int RawRSSI) {
            this.RawRSSI = RawRSSI;
        }

        public int getRawRSSI() {
            return RawRSSI;
        }

        public void setDevice(BluetoothDevice mBluetoothDevice) {
            this.mBluetoothDevice = mBluetoothDevice;
        }

        public BluetoothDevice getDevice() {
            return mBluetoothDevice;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof BluetoothDeviceRSSI)) {
                return false;
            }
            BluetoothDeviceRSSI compDevice = (BluetoothDeviceRSSI) obj;
            return compDevice.getDevice().equals(getDevice());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "bluetoothManager.getAdapter()==null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnScan = (Button) findViewById(R.id.scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanLeDevice((String)btnScan.getText());
            }
        });
        btnFilter = (ToggleButton) findViewById(R.id.toggle_filter);
        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nodeFilter = !nodeFilter;
            }
        });
        btnMultiConnect = (Button) findViewById(R.id.mult_connect);
        btnMultiConnect.setEnabled(false);
        btnMultiConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(MainActivity.this,
                        MultipleConnection.class);
                if (mScanning) {
                    mBluetoothLeScanner.stopScan(scanCallback);
                    mScanning = false;
                    btnScan.setText(R.string.scan_btn_enable);
                    btnScan.setBackgroundColor(getColor(R.color.purple_500));
                }

                ArrayList<String> listDeviceAddresses = new ArrayList<String>();

                for (int i = 0; i < listBluetoothDevice.size(); i++) {
                    listDeviceAddresses.add(listBluetoothDevice.get(i).getDevice().getAddress());
                }

                Bundle bundle = new Bundle();
                bundle.putSerializable("Addresses", (Serializable)listDeviceAddresses);
                intent.putExtra("BundleAddresses", bundle);

                startActivity(intent);
            }
        });

        listViewLE = (ListView) findViewById(R.id.lelist);

        listBluetoothDevice = new ArrayList<>();
        adapterLeScanResult = new ArrayAdapter<BluetoothDeviceRSSI>(
                this, android.R.layout.simple_list_item_1, listBluetoothDevice) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                String deviceName, deviceAddress, textView, rawRSSI;

                deviceName = getItem(position).getDevice().getName();
                deviceAddress = getItem(position).getDevice().getAddress();
                rawRSSI = Integer.toString(getItem(position).getRawRSSI());

                if(deviceName == null) {
                    textView = deviceAddress + '\n' + rawRSSI;
                }else{
                    textView = deviceName + '\n' + deviceAddress + '\n' + rawRSSI;
                }
                view.setText(textView);
                return view;
            }
        };
        listViewLE.setAdapter(adapterLeScanResult);
        listViewLE.setOnItemClickListener(scanResultOnItemClickListener);
    }

    AdapterView.OnItemClickListener scanResultOnItemClickListener =
            new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final BluetoothDeviceRSSI device = (BluetoothDeviceRSSI) parent.getItemAtPosition(position);

                    String msg = device.getDevice().getAddress() + "\n"
                            + device.getDevice().getBluetoothClass().toString() + "\n"
                            + getBTDeviceType(device.getDevice());

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle(device.getDevice().getName())
                            .setMessage(msg)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {}
                            })
                            .setNeutralButton("CONNECT", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final Intent intent = new Intent(MainActivity.this,
                                            ControlActivity.class);
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_NAME,
                                            device.getDevice().getName());
                                    intent.putExtra(ControlActivity.EXTRAS_DEVICE_ADDRESS,
                                            device.getDevice().getAddress());

                                    if (mScanning) {
                                        mBluetoothLeScanner.stopScan(scanCallback);
                                        mScanning = false;
                                        btnScan.setText(R.string.scan_btn_enable);
                                        btnScan.setBackgroundColor(getColor(R.color.purple_500));
                                    }
                                    startActivity(intent);
                                }
                            })
                            .show();
                }
            };

    private String getBTDeviceType(BluetoothDevice d){
        String type = "";
        switch (d.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                type = "DEVICE_TYPE_CLASSIC";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                type = "DEVICE_TYPE_DUAL";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                type = "DEVICE_TYPE_LE";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                type = "DEVICE_TYPE_UNKNOWN";
                break;
            default:
                type = "unknown...";
        }
        return type;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
            finish();
            return;
        }

        getBluetoothAdapterAndLeScanner();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "bluetoothManager.getAdapter()==null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void getBluetoothAdapterAndLeScanner() {
        final BluetoothManager mBluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mScanning = false;
    }

    /*
   to call startScan (ScanCallback callback),
   Requires BLUETOOTH_ADMIN permission.
   Must hold ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get results.
    */
    private void scanLeDevice(String btnState) {
        if (btnState.equals(getString(R.string.scan_btn_enable))) {
            listBluetoothDevice.clear();
            listViewLE.invalidateViews();

            //scan specified devices only with ScanFilter
            if (nodeFilter) {
                ScanFilter scanFilter = new ScanFilter.Builder()
                                .setServiceUuid(BluetoothLeService.PARCEL_FILTER_SERVICE_UUID)
                                .build();
                List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
                scanFilters.add(scanFilter);

                ScanSettings scanSettings =
                        new ScanSettings.Builder().build();

                mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);
            } else {
                mBluetoothLeScanner.startScan(scanCallback);
            }

            mScanning = true;
            btnScan.setText(R.string.scan_btn_disable);
            btnScan.setBackgroundColor(Color.RED);
        } else {
            mBluetoothLeScanner.stopScan(scanCallback);
            mScanning = false;
            btnScan.setText(R.string.scan_btn_enable);
            btnScan.setBackgroundColor(getColor(R.color.purple_500));
        }
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDeviceRSSI device = new BluetoothDeviceRSSI();
            device.setDevice(result.getDevice());
            device.setRawRSSI(result.getRssi());
            addBluetoothDevice(device);

            if (!listBluetoothDevice.isEmpty()) {
                btnMultiConnect.setEnabled(nodeFilter);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult result : results){
                BluetoothDeviceRSSI device = new BluetoothDeviceRSSI();
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

        private void addBluetoothDevice(BluetoothDeviceRSSI device){
            if(!listBluetoothDevice.contains(device)){
                listBluetoothDevice.add(device);
            } else {
                listBluetoothDevice.get(listBluetoothDevice.indexOf(device)).setRawRSSI(device.getRawRSSI());
            }
            listViewLE.invalidateViews();
            ((BaseAdapter) listViewLE.getAdapter()).notifyDataSetChanged();
        }
    };
}