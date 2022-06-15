package com.example.ble_light;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import com.example.ble_light.light_picker.ColorPicker;
import com.example.ble_light.light_picker.listeners.SimpleColorSelectionListener;

public class LightManageActivity extends AppCompatActivity {
    public ArrayList<String> listDevicesAddresses = new ArrayList<String>();

    private ImageView image_view_light_picker;
    private TextView colorTempView;
    private TextView brightTempView;

    private int colorTemperature;
    private int colorBright;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_manage);

        Intent intent = getIntent();
        Bundle serAddresses = intent.getBundleExtra("BundleAddresses");
        listDevicesAddresses = (ArrayList<String>) serAddresses.getSerializable("Addresses");

        Log.i("TEST_LIGHT_MANAGE", listDevicesAddresses.toString());

        image_view_light_picker = findViewById(R.id.image_view_light_picker);
        colorTempView = findViewById(R.id.colorTempView);
        brightTempView = findViewById(R.id.brightTempView);

        final ColorPicker colorPicker = findViewById(R.id.light_picker);
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color, float angle,
                                        float coeffBright, @NonNull String id) {
                if (id.equals("color_temp")) {
                    if (angle > 0 && angle <= 60) {
                        colorTemperature = (int)((420 - (angle + 360)) * 20.84 + 3400);
                    } else {
                        colorTemperature = (int)((420 - angle) * 20.84 + 3400);
                    }
                } else if (id.equals("bright_temp")) {
                    if (coeffBright >= 0) {
                        colorBright = (int)(coeffBright * 100);
                    } else {
                        colorBright = 0;
                    }
                }

                image_view_light_picker.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                colorTempView.setText(getString(R.string.color_kelvin, colorTemperature));
                brightTempView.setText(getString(R.string.color_bright, colorBright));
            }
        });
    }
}