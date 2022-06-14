package com.example.ble_light;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

public class LightManageActivity extends AppCompatActivity {
    public ArrayList<String> listDevicesAddresses = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_manage);

        Intent intent = getIntent();
        Bundle serAddresses = intent.getBundleExtra("BundleAddresses");
        listDevicesAddresses = (ArrayList<String>) serAddresses.getSerializable("Addresses");

        Log.i("TEST_LIGHT_MANAGE", listDevicesAddresses.toString());
    }
}