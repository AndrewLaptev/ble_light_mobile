package com.example.ble_light;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.ble_light.dev.MainActivityDev;

@RequiresApi(api = Build.VERSION_CODES.S)
public class MainActivity extends AppCompatActivity {
    private static final int ACCESS_BLUETOOTH_PERMISSION = 85;

    private ImageButton btnStartScan;
    private ProgressBar scanProgressBar;
    private boolean scanState = false;

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
                } else {
                    btnStartScan.setImageAlpha(255);
                    scanProgressBar.setVisibility(View.INVISIBLE);
                }
//                btnStartScan.setEnabled(scanState);
            }
        });
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