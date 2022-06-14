package com.example.ble_light;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import java.util.ArrayList;

import com.example.ble_light.light_picker.ColorPicker;
import com.example.ble_light.light_picker.listeners.SimpleColorSelectionListener;

public class LightManageActivity extends AppCompatActivity {
    public ArrayList<String> listDevicesAddresses = new ArrayList<String>();

    private ImageView image_view_light_picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_manage);

        Intent intent = getIntent();
        Bundle serAddresses = intent.getBundleExtra("BundleAddresses");
        listDevicesAddresses = (ArrayList<String>) serAddresses.getSerializable("Addresses");

        Log.i("TEST_LIGHT_MANAGE", listDevicesAddresses.toString());

        image_view_light_picker = findViewById(R.id.image_view_light_picker);

        final ColorPicker colorPicker = findViewById(R.id.light_picker);
        colorPicker.setColorSelectionListener(new SimpleColorSelectionListener() {
            @Override
            public void onColorSelected(int color) {
                // Do whatever you want with the color
                image_view_light_picker.getBackground().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            }
        });
    }
}