package com.example.recycle;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class Pop_up extends Activity {
    TextView txtText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_popup);

        //UI 객체생성
        //txtText = (TextView)findViewById(R.id.metalMax);

        //데이터 가져오기
        //Intent intent = getIntent();
       // String data = intent.getStringExtra("data");
        //txtText.setText(data);
    }

    //확인 버튼 클릭
    public void mOnClose(View v) throws JSONException {
        //데이터 전달하기
        Intent intent = new Intent();
        intent.putExtra("result", "Close Popup");
        setResult(RESULT_OK, intent);

        String url="http://13.124.99.108:3000/android/getSettingMax";
        EditText metalTxt=(EditText) findViewById(R.id.metalMax);
        EditText glassTxt=(EditText)findViewById(R.id.glassMax);
        EditText plasticTxt=(EditText)findViewById(R.id.plasticMax);
        EditText trashTxt=(EditText)findViewById(R.id.trashMax);


        JSONObject temp=new JSONObject();
        temp.put("glass",glassTxt.getText());
        temp.put("Metal",metalTxt.getText());
        temp.put("plastic",plasticTxt.getText());
        temp.put("trash",trashTxt.getText());
        //String tmp="{ glass: "+glassTxt.getText()+", Metal: "+metalTxt.getText()+", plastic: "+plasticTxt.getText()+", trash: "+trashTxt.getText()+" }";
        System.out.println("전달할 tmp 내용은 "+temp);
        String sendtmp=temp.toString();
        Pop_up.NetworkTask networkTask = new Pop_up.NetworkTask(url, sendtmp);
        networkTask.execute();


        //액티비티(팝업) 닫기
        finish();
    }
//취소 버튼 클릭
    public void mOnCancle(View v){
        //데이터 전달하기
        //액티비티(팝업) 닫기
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }


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
    }
    public class NetworkTask extends AsyncTask<Void, Void, String> {

        private String url;
        private String values;
        private JSONObject jsonObject;

        public NetworkTask(String url, String values) {
            this.url = url;
            this.values = values;
        }
        public NetworkTask(String url,JSONObject object){
            this.url=url;
            this.jsonObject=object;
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
