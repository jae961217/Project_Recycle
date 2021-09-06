package com.example.recycle;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;



import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;

public class erPage extends Activity {


    private BluetoothSPP bt;
    private static MediaPlayer mp;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_error);
        mp=MediaPlayer.create(this,R.raw.alert);
        mp.setLooping(true);
        mp.start();

    }

    public void onStart() {
        super.onStart();
        bt=((MainActivity)MainActivity.context).bt;
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            //TextView tvMain = (TextView) findViewById(R.id.tvMain2);
            public void onDataReceived(byte[] data, String message) {
                //Toast.makeText(erPage.this, message, Toast.LENGTH_SHORT).show();
                //tvMain.setText(message);
                String warning = "1";
                String idle = "0";
                String s1 = message;
                System.out.println("경고창에서 받은 값"+s1);
                if (s1.equals(idle)) {
                    Intent cintent=new Intent(getApplicationContext(),BluetoothService.class);
                    startService(cintent);
                    finish();
                }
            }
        });

    }
    public void onDestroy() {
        super.onDestroy();
        System.out.println("끝났습니다.");
        mp.stop();

    }
}
