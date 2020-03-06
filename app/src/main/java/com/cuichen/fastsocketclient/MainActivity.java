package com.cuichen.fastsocketclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private FastSocketClient fastSocketClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fastSocketClient = FastSocketClient.getInstance();
        fastSocketClient.setOnSocketClientCallBackList(new FastSocketClient.OnSocketClientCallBackList() {
            @Override
            public void onCallBack(String msg) {
                Log.i("ccb", "onCallBack: "+msg);
            }
        });
    }

    public void connent(View view) {
        if (!fastSocketClient.isAlive()) {
            fastSocketClient.start();
        }else {
            fastSocketClient.close();
        }
    }

    public void send(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                fastSocketClient.sendMsg(new byte[]{0x01,0x02,0x03});
            }
        }).start();

    }
}
