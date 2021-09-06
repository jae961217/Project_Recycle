package com.example.recycle;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import app.akexorcist.bluetotohspp.library.BluetoothState;

public class HowToUse extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_use);

    }
    public void onStart() {
        super.onStart();
        Intent cintent=new Intent(getApplicationContext(),BluetoothService.class);
        startService(cintent);
    }
}
