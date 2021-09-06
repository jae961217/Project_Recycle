package com.example.recycle;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class BluetoothService extends Service {
    private static final String TAG="MyService";
    private BluetoothSPP bt;
    Handler dieHandler=new Handler();
    private AlertDialog mAlertDialog;
    static final int TIME_OUT = 5000;
    static final int MSG_DISMISS_DIALOG = 0;

    public BluetoothService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate() 콜콜");
        //bt = new BluetoothSPP(this);
        bt=((MainActivity)MainActivity.context).bt;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand() 콜콜");
        if(intent==null){
            Toast.makeText(getApplicationContext()
                    , "재실행"
                    , Toast.LENGTH_SHORT).show();
            return Service.START_STICKY;
        }else{
            bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
                public void onDataReceived(byte[] data, String message) {
                    Toast.makeText(BluetoothService.this, message, Toast.LENGTH_SHORT).show();
                    String warning = "1";
                    String idle = "0";
                    String s1 = message;
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    //preference 선언
                    if (s1.equals(idle)) {
                        System.out.println("값은 0이 온다");
                        SharedPreferences.Editor editor = pref.edit();
                        //preference 0으로 초기화
                        editor.putInt("value", 0);
                        editor.commit();
                    } else if (s1.equals((warning))) {
                        int val = pref.getInt("value", 0);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putInt("value", val + 1);
                        //preference 값 1씩 추가
                        editor.commit();
                        System.out.println("값은 1이 온다");
                        if (val + 1 == 5) {//5초동안 1을 받아왔을때
                            Intent intent = new Intent(BluetoothService.this, erPage.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            //경고창 띄우는 액티비티로 이동
                            editor.putInt("value", 0);
                            editor.commit();
                        }
                    }
                }
            });
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("서비스 중지");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


}
