package com.example.myapplication;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;


import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.provider.ContactsContract.CommonDataKinds.Website.URL;


//톡방 안에 내용
public class TalkRoom extends AppCompatActivity {

    private static final String TAG = "jsonExample";
    public static final int LOAD_SUCCESS = 101;

    private ListView talk_contents;
    private EditText talk_edit;
    private Button talk_send_btn;
    private TextView tr_opposit_id;
    private Socket socket;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.talk_room);


        try {
            //socket = IO.socket(URLS.URL_TALK);//
            socket = IO.socket("ec2-13-125-213-203.ap-northeast-2.compute.amazonaws.com:3000");
            socket.connect();
            //socket.on(Socket.EVENT_CONNECT, onConnect);
            //socket.on("serverMessage", onMessageReceived);
            socket.emit("connection",socket);
            socket.on("send", new Emitter.Listener() {//on은 서버에서 받아오는거
                @Override
                public void call(final Object... args) {//send라는 이름의 함수

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject data = (JSONObject) args[0];
                                tr_opposit_id.setText(data.getString("message"));
                                talk_edit.setText("");
                                //TalkAdapter.addTalkItem("나","너",data.getString("message"),"시간");
                            } catch(Exception e) {
                                Toast.makeText(getApplicationContext(), e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            socket.connect();

        } catch(URISyntaxException e) {
            e.printStackTrace();
        }



        tr_opposit_id = (TextView)findViewById(R.id.tr_opposit_id);

        talk_contents = (ListView)findViewById(R.id.talk_contents);//리사이클써야하나?
        TalkAdapter talkAdapter = new TalkAdapter();

        talk_contents.setAdapter(talkAdapter);

        //talkAdapter.addTalkItem("나","상대방","내용","시간");

        //array.add(talk_edit.getText().toString()); //버튼을 클릭하면 array에 추가
        //talkAdapter.notifyDataSetChanged(); //어댑터 새로고침
        //talk_edit.setText("");

        //챗 보내기 눌리면 내용세팅하는거 stText
        talk_edit = (EditText)findViewById(R.id.talk_edit);
        talk_send_btn = (Button)findViewById(R.id.talk_send_btn);
        talk_send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                socket.connect();
                socket.emit("connection",socket);
                if(socket != null) {
                    JSONObject data = new JSONObject();
                    //TalkAdapter.addTalkItem("me","you","11:00:00", talk_edit.getText().toString());

                    socket.on("send", new Emitter.Listener() {//on은 서버에서 받아오는거
                        @Override
                        public void call(final Object... args) {//send라는 이름의 함수

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject data = (JSONObject) args[0];
                                        tr_opposit_id.setText(data.getString("message"));
                                        talk_edit.setText("");
                                        //TalkAdapter.addTalkItem("나","너",data.getString("message"),"시간");
                                    } catch (Exception e) {
                                        Toast.makeText(getApplicationContext(), e.getMessage(),
                                                Toast.LENGTH_LONG).show();
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    });

                    try {
                        data.put("message", talk_edit.getText().toString());

                        socket.emit("send", data);//이게 서버에 데이터 보낸거
                        //talk_edit.setText("");//칸 비우기

                    } catch(Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast
                                .LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }

                /*String stText = talk_edit.getText().toString();
                if(stText.equals("")||stText.isEmpty()){
                    Toast.makeText(TalkRoom.this, "내용을 입력해 주세요", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(TalkRoom.this, stText, Toast.LENGTH_SHORT).show();


                    socket.emit("message_from_client", stText);
                    //socket.broadcast.emit(“EVENT_NAME”, data);써야 다른사람한테 보이는지
                    // stText 대신 배열로 사람이름,시간 넣어서 보낼수 있는지
                    //getJSON();
                    talk_edit.setText("");//칸 비우기
                }*/
            }
        });

        talkAdapter.registerDataSetObserver(new DataSetObserver() {

            @Override

            public void onChanged() {

                super.onChanged();

                //talk_contents.setSelection(TalkAdapter.getCount()-1);
                talk_contents.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);

            }

        });
    }



    private final MyHandler mHandler = new MyHandler(this);


    private static class MyHandler extends Handler {
        private final WeakReference<TalkRoom> weakReference;

        public MyHandler(TalkRoom talkRoom) {
            weakReference = new WeakReference<TalkRoom>(talkRoom);
        }

        @Override
        public void handleMessage(Message msg) {

            TalkRoom talkRoom = weakReference.get();

            if (talkRoom != null) {
                switch (msg.what) {

                    case LOAD_SUCCESS:
                        //talkRoom.progressDialog.dismiss();

                        String jsonString = (String)msg.obj;

                        talkRoom.tr_opposit_id.setText(jsonString);//되나볼라고 텍스트뷰암꺼나해봄
                        break;
                }
            }
        }
    }

public void getJSON(){
    Thread thread = new Thread(new Runnable() {

        public void run() {

            String result;

            try {

                Log.d(TAG, URL);
                java.net.URL url = new URL(URL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(3000);
                httpURLConnection.setConnectTimeout(5000);//3000
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setUseCaches(false);
                httpURLConnection.connect();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {

                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();

                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;


                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();
                httpURLConnection.disconnect();

                result = sb.toString().trim();


            } catch (Exception e) {
                result = e.toString();
            }
            Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
            mHandler.sendMessage(message);
        }

    });
    thread.start();
}

//
    private class TalkContents extends AsyncTask<Void, Void, String> {

        private String owner_id, opposit_id, contents, time;

        TalkContents(String owner_id, String opposit_id, String contents, String time) {

            this.owner_id = owner_id;
            this.opposit_id = opposit_id;
            this.contents = contents;
            this.time = time;

        }

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

        }

        @Override
        protected String doInBackground(Void... voids) {

            RequestHandler requestHandler = new RequestHandler();

            HashMap<String, String> params = new HashMap<>();
            params.put("owner_id", owner_id);
            params.put("opposit_id", opposit_id);
            params.put("contents", contents);
            params.put("time", time);


            return requestHandler.sendPostRequest(URLS.URL_TALK, params);
            //여기 인풋 스트림 추가!

        }

//
        public void onProgressUpdate(Void... values){
        super.onProgressUpdate();
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            Log.i("talk", "Info" + s);

            try {

                JSONObject obj = new JSONObject(s);

                if (!obj.getString("code").equals(404)) {

                    Toast.makeText(getApplicationContext(), obj.getString("message"), Toast.LENGTH_SHORT).show();
                    //JSONObject userJson = obj.getJSONObject("user");


                } else {

                    Toast.makeText(getApplicationContext(), "Some error occur", Toast.LENGTH_SHORT).show();

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }




}

