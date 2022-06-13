package com.example.ble_light;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.example.ble_light.dev.MainActivityDev;
import com.example.ble_light.dev.MultiConnectionActivityDev;

public class MainActivity extends AppCompatActivity {
    private ImageButton btnStartScan;
    private ProgressBar scanProgressBar;
    private boolean scanState = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @RequiresApi(api = Build.VERSION_CODES.S)
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