package com.example.recycle;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.DialogInterface;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.Console;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import io.socket.client.IO;
import io.socket.client.Socket;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;
import io.socket.emitter.Emitter;


public class MainActivity extends AppCompatActivity {
    public BluetoothSPP bt;
    public static Context context;
    Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Button Use = (Button) findViewById(R.id.Use);
        Button Read = (Button) findViewById(R.id.Read);
        Button btnConnect=findViewById(R.id.btnConnect);
        Button btnPopup=findViewById(R.id.btnPopup);
        ImageButton glassbtn=(ImageButton)findViewById(R.id.glassbtn);
        ImageButton metalbtn=(ImageButton)findViewById(R.id.canbtn);
        ImageButton plasticbtn=(ImageButton)findViewById(R.id.plasticbtn);
        ImageButton trashbtn=(ImageButton)findViewById(R.id.trashbtn);

        bt = new BluetoothSPP(this);
        context=this;
        String url="http://13.124.99.108:3000/android/modify";
        AlertDialog.Builder alert_confirm=new AlertDialog.Builder(this);
        alert_confirm.setMessage("보관함을 비우시겠습니까?");

        Use.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HowToUse.class);
                startActivity(intent);
            }
        });
        Read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ReadMe.class); //너가 가고싶은 페이지 넣으면됨
                startActivity(intent);
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else{
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });//연결 버튼
        btnPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getApplicationContext(),Pop_up.class);
                intent.putExtra("data","Test Popup");
                startActivityForResult(intent,100);
            }
        });

        //쓰레기 버튼들
        glassbtn.setOnClickListener(new View.OnClickListener() {//유리버튼
            @Override
            public void onClick(View v) {
                alert_confirm.setNegativeButton("취소",null);
                alert_confirm.setPositiveButton("비우기",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String tmp="glass";
                        NetworkTask networkTask = new NetworkTask(url, tmp);
                        networkTask.execute();
                    }
                });

                AlertDialog glassalert=alert_confirm.create();
                glassalert.show();
            }
        });
        plasticbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert_confirm.setPositiveButton("비우기",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String tmp="plastic";
                        NetworkTask networkTask = new NetworkTask(url, tmp);
                        networkTask.execute();
                    }
                });
                alert_confirm.setNegativeButton("취소",null);
                AlertDialog plasticalert=alert_confirm.create();
                plasticalert.show();
            }
        });
        trashbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert_confirm.setPositiveButton("비우기",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String tmp="trash";
                        NetworkTask networkTask = new NetworkTask(url, tmp);
                        networkTask.execute();
                    }
                });
                alert_confirm.setNegativeButton("취소",null);
                AlertDialog trashalert=alert_confirm.create();
                trashalert.show();
            }
        });
        metalbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert_confirm.setPositiveButton("비우기",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        String tmp="Metal";
                        NetworkTask networkTask = new NetworkTask(url, tmp);
                        networkTask.execute();
                    }
                });
                alert_confirm.setNegativeButton("취소",null);
                AlertDialog canalert=alert_confirm.create();
                canalert.show();
            }
        });

        //소켓통신
        init();

    }

    public void init(){//서버와 소켓통신 진행
        try{
            socket=IO.socket("http://13.124.99.108:3000");
            socket.connect();

            System.out.println("소켓 통신 시작");
            JSONObject initdata=new JSONObject();
            datainit(initdata);
            socket.emit("SEND",initdata);

            socket.on("SEND", new Emitter.Listener(){
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                               // System.out.println(data);
                                ///////////////////////////////////////////////유리 개수
                                int gNum=Integer.parseInt(data.getString("glass"));
                                double gmNum=Double.parseDouble(data.getString("glassMax"));

                                List<Double> glassList=new ArrayList<Double>();
                                double glassArray[];
                                double tmpgNum=gmNum/6;
                                glassList.add(0.0);
                                for(int k=1;k<=6;k++){
                                    glassList.add(k*tmpgNum);
                                }
                                glassArray=new double[glassList.size()];
                                glassArray[0]=0.0;
                                for(int k=1;k<=6;k++){
                                    ///System.out.println(glassList.get(k));
                                    glassArray[k]=glassList.get(k);
                                }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                ///////////////////////금속 감지 개수
                                int meNum=Integer.parseInt((data.getString("metal")));
                                double mmNum=Double.parseDouble(data.getString("metalMax"));

                                List<Double> metalList=new ArrayList<Double>();
                                double metalArray[];
                                double tmpmNum=mmNum/6;
                                metalList.add(0.0);
                                for(int k=1;k<=6;k++){
                                    metalList.add(k*tmpmNum);
                                }
                                metalArray=new double[metalList.size()];
                                metalArray[0]=0.0;
                                for(int k=1;k<=6;k++){
                                    ///System.out.println(glassList.get(k));
                                    metalArray[k]=metalList.get(k);
                                }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                ///////////////////////////////////////일반쓰레기 감지 개수
                                int tNum=Integer.parseInt((data.getString("trash")));
                                double tmNum=Double.parseDouble(data.getString("trashmax"));

                                List<Double> trashList=new ArrayList<Double>();
                                double trashArray[];
                                double tmptNum=tmNum/6;
                                trashList.add(0.0);
                                for(int k=1;k<=6;k++){
                                    trashList.add(k*tmptNum);
                                }
                                trashArray=new double[trashList.size()];
                                trashArray[0]=0.0;
                                for(int k=1;k<=6;k++){
                                    ///System.out.println(glassList.get(k));
                                    trashArray[k]=trashList.get(k);
                                }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                                ///////////////////////////////////////플라스틱 감지 개수
                                int pNum=Integer.parseInt((data.getString("plastic")));
                                double pmNum=Double.parseDouble(data.getString("plasticMax"));

                                List<Double> plasticList=new ArrayList<Double>();
                                double plasticArray[];
                                double tmppNum=pmNum/6;
                                plasticList.add(0.0);
                                for(int k=1;k<=6;k++){
                                    plasticList.add(k*tmppNum);
                                }
                                plasticArray=new double[plasticList.size()];
                                plasticArray[0]=0.0;
                                for(int k=1;k<=6;k++){
                                    ///System.out.println(glassList.get(k));
                                    plasticArray[k]=plasticList.get(k);
                                }
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                                ImageButton glassbtn=(ImageButton)findViewById(R.id.glassbtn);
                                ImageButton plasticbtn=(ImageButton)findViewById(R.id.plasticbtn);
                                ImageButton trashbtn=(ImageButton)findViewById(R.id.trashbtn);
                                ImageButton metalbtn=(ImageButton)findViewById(R.id.canbtn);


                                if(gNum==0) {
                                    //System.out.println("gnum은 0이다");
                                    glassbtn.setImageResource(R.drawable.glass_btn);
                                }
                                else if(gNum>=gmNum){
                                    glassbtn.setImageResource(R.drawable.glass_full_btn);
                                }
                                else{
                                    for(int k=1;k<=6;k++){
                                        if(k==1){
                                            if(gNum<=glassArray[k]){
                                                glassbtn.setImageResource(R.drawable.glass1_btn);
                                                break;
                                            }
                                        }
                                        else if(k==2){
                                            if(gNum>glassArray[1]&&gNum<=glassArray[2]){
                                                glassbtn.setImageResource(R.drawable.glass2_btn);
                                                break;
                                            }
                                        }
                                        else if(k==3){
                                            if(gNum>glassArray[2]&&gNum<=glassArray[3]){
                                                glassbtn.setImageResource(R.drawable.glass3_btn);
                                                break;
                                            }
                                        }
                                        else if(k==4){
                                            if(gNum>glassArray[3]&&gNum<=glassArray[4]){
                                                glassbtn.setImageResource(R.drawable.glass4_btn);
                                                break;
                                            }
                                        }
                                        else if(k==5){
                                            if(gNum>glassArray[4]&&gNum<=glassArray[5]){
                                                glassbtn.setImageResource(R.drawable.glass5_btn);
                                                break;
                                            }
                                        }
                                        else if(k==6){
                                            if(gNum>glassArray[5]&&gNum<=glassArray[6]){
                                                glassbtn.setImageResource(R.drawable.glass6_btn);
                                                break;
                                            }
                                        }
                                    }
                                }
/////////////////////////////////////////////////////
                                if(meNum==0){
                                    metalbtn.setImageResource(R.drawable.can_btn);
                                }
                                else if(meNum>=mmNum){
                                    metalbtn.setImageResource(R.drawable.can_full_btn);
                                }
                                else{
                                    for(int k=1;k<=6;k++){
                                        if(k==1){
                                            if(meNum<=metalArray[k]){
                                                metalbtn.setImageResource(R.drawable.can1_btn);
                                                break;
                                            }
                                        }
                                        else if(k==2){
                                            if(meNum>metalArray[1]&&meNum<=metalArray[2]){
                                                metalbtn.setImageResource(R.drawable.can2_btn);
                                                break;
                                            }
                                        }
                                        else if(k==3){
                                            if(meNum>metalArray[2]&&meNum<=metalArray[3]){
                                                metalbtn.setImageResource(R.drawable.can3_btn);
                                                break;
                                            }
                                        }
                                        else if(k==4){
                                            if(meNum>metalArray[3]&&meNum<=metalArray[4]){
                                                metalbtn.setImageResource(R.drawable.can4_btn);
                                                break;
                                            }
                                        }
                                        else if(k==5){
                                            if(meNum>metalArray[4]&&meNum<=metalArray[5]){
                                                metalbtn.setImageResource(R.drawable.can5_btn);
                                                break;
                                            }
                                        }
                                        else if(k==6){
                                            if(meNum>metalArray[5]&&meNum<=metalArray[6]){
                                                metalbtn.setImageResource(R.drawable.can6_btn);
                                                break;
                                            }
                                        }
                                    }
                                }

//////////////////////////////////////////////////////일반쓰레기
                                if(tNum==0){
                                    trashbtn.setImageResource(R.drawable.trash_btn);
                                }
                                else if(tNum>=tmNum){
                                    trashbtn.setImageResource(R.drawable.trash_full_btn);
                                }
                                else {
                                    for(int k=1;k<=6;k++){
                                        if(k==1){
                                            if(tNum<=trashArray[k]){
                                                trashbtn.setImageResource(R.drawable.trash1_btn);
                                                break;
                                            }
                                        }
                                        else if(k==2){
                                            if(tNum>trashArray[1]&&tNum<=trashArray[2]){
                                                trashbtn.setImageResource(R.drawable.trash2_btn);
                                                break;
                                            }
                                        }
                                        else if(k==3){
                                            if(tNum>trashArray[2]&&tNum<=trashArray[3]){
                                                trashbtn.setImageResource(R.drawable.trash3_btn);
                                                break;
                                            }
                                        }
                                        else if(k==4){
                                            if(tNum>trashArray[3]&&tNum<=trashArray[4]){
                                                trashbtn.setImageResource(R.drawable.trash4_btn);
                                                break;
                                            }
                                        }
                                        else if(k==5){
                                            if(tNum>trashArray[4]&&tNum<=trashArray[5]){
                                                trashbtn.setImageResource(R.drawable.trash5_btn);
                                                break;
                                            }
                                        }
                                        else if(k==6){
                                            if(tNum>trashArray[5]&&tNum<=trashArray[6]){
                                                trashbtn.setImageResource(R.drawable.trash6_btn);
                                                break;
                                            }
                                        }
                                    }
                                }
/////////////////////////////////////////////////////플라스틱
                                if(pNum==0){
                                    plasticbtn.setImageResource(R.drawable.plastic_btn);
                                }
                                else if(pNum>=pmNum){
                                    plasticbtn.setImageResource(R.drawable.plastic_full_btn);
                                }
                                else {
                                    for(int k=1;k<=6;k++){
                                        if(k==1){
                                            if(pNum<=plasticArray[k]){
                                                plasticbtn.setImageResource(R.drawable.plastic1_btn);
                                                break;
                                            }
                                        }
                                        else if(k==2){
                                            if(pNum>plasticArray[1]&&pNum<=plasticArray[2]){
                                                plasticbtn.setImageResource(R.drawable.plastic2_btn);
                                                break;
                                            }
                                        }
                                        else if(k==3){
                                            if(pNum>plasticArray[2]&&pNum<=plasticArray[3]){
                                                plasticbtn.setImageResource(R.drawable.plastic3_btn);
                                                break;
                                            }
                                        }
                                        else if(k==4){
                                            if(pNum>plasticArray[3]&&pNum<=plasticArray[4]){
                                                plasticbtn.setImageResource(R.drawable.plastic4_btn);
                                                break;
                                            }
                                        }
                                        else if(k==5){
                                            if(pNum>plasticArray[4]&&pNum<=plasticArray[5]){
                                                plasticbtn.setImageResource(R.drawable.plastic5_btn);
                                                break;
                                            }
                                        }
                                        else if(k==6){
                                            if(pNum>plasticArray[5]&&pNum<=plasticArray[6]){
                                                plasticbtn.setImageResource(R.drawable.plastic6_btn);
                                                break;
                                            }
                                        }
                                    }
                                }

                                JSONObject please=new JSONObject();
                                datainit(please);
                                socket.emit("SEND",please);
                            } catch(Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
        catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG)
                    .show();
            System.out.println("연결 실패");
        }
    }//서버와 소켓 통신 진행

    public void datainit(JSONObject temp) throws JSONException {
        temp=new JSONObject();
        temp.put("glass","값을 보내주세요");
        temp.put("glassMax","값을 보내주세요");
        temp.put("metal","값을 보내주세요");
        temp.put("metalMax","값을 보내주세요");
        temp.put("plastic","값을 보내주세요");
        temp.put("plasticMax","값을 보내주세요");
        temp.put("trash","값을 보내주세요");
        temp.put("trashMax","값을 보내주세요");
    }//서버에 소켓통신시 보내는 형식

    public void onDestroy() {
        super.onDestroy();
        System.out.println("끝났습니다.");
        bt.stopService(); //블루투스 중지
        socket.emit("disconnect", null);//소켓 중지
        socket.disconnect();
    }

    public void onStart() {
        super.onStart();

        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
                setup();
            }else{
                Intent cintent=new Intent(getApplicationContext(),BluetoothService.class);
                startService(cintent);
            }
        }
    }//블루투스 통신

    public void setup() {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK){
                bt.connect(data);
                Intent bintent=new Intent(getApplicationContext(),BluetoothService.class);
                startService(bintent);
            }
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                setup();
            } else {
                System.out.println("블루투스 사용불가");
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        else if(requestCode==100){
            if(resultCode==RESULT_OK){
                String result=data.getStringExtra("result");
            }
        }
    }
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private String values;

        public NetworkTask(String url, String values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result=null; // 요청 결과를 저장할 변수.
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, values); // 해당 URL로 부터 결과물을 얻어온다.
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //doInBackground()로 부터 리턴된 값이 onPostExecute()의 매개변수로 넘어오므로 s를 출력한다.
        }
    }
}
