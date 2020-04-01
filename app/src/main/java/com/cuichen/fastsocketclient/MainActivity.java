package com.cuichen.fastsocketclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cuichen.fastsocketclient.interfaces.OnSocketClientCallListener;
import com.cuichen.fastsocketclient.utils.HexUtil;
import com.cuichen.fastsocketclient.utils.L;

public class MainActivity extends AppCompatActivity {

    private FastSocketClient fastSocketClient;
    private Button btnspulse , btnis;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fastSocketClient = FastSocketClient.getInstance();
        btnspulse = findViewById(R.id.pulse);
        btnis = findViewById(R.id.isconnect);
        initList();
        pulstThread.start();

    }

    private boolean isPulse;
    private void initList() {
        fastSocketClient.setOnSocketClientCallBackList(new OnSocketClientCallListener(){
            @Override
            public void onSocketConnectionFailed(String msg, Exception e) {
                super.onSocketConnectionFailed(msg, e);
                L.c("连接失败"+e.getMessage());
            }

            @Override
            public void onSocketConnectionSuccess(String msg) {
                super.onSocketConnectionSuccess(msg);
                L.c("连接成功"+msg);
            }

            @Override
            public void onSocketDisconnection(String msg, Exception e) {
                super.onSocketDisconnection(msg, e);
                L.c("连接断开"+ (e == null ? "": e.getMessage()));
            }

            @Override
            public void onSocketReadResponse(byte[] bytes) {
                super.onSocketReadResponse(bytes);
                L.c("Read:"+HexUtil.byte2HexStr(bytes));
            }

            @Override
            public void onSocketWriteResponse(byte[] bytes) {
                super.onSocketWriteResponse(bytes);
                L.c("Write:"+HexUtil.byte2HexStr(bytes));
            }
        });
        btnspulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPulse = !isPulse;
                btnspulse.setText(isPulse?"正在心跳":"开启心跳");
            }
        });
        btnis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                L.c(fastSocketClient.isConnected());
            }
        });
    }

    Thread pulstThread = new Thread(new Runnable() {
        @Override
        public void run() {
            if (isPulse && fastSocketClient.isConnected()){
                fastSocketClient.send(HexUtil.hexStringToByte(SocketConfig.Pulse));
            }
            SystemClock.sleep(5000);
            pulstThread.run();
        }
    });

    public void connent(View view) {
        if (fastSocketClient.isConnected()){
            fastSocketClient.close();
        }else {
            fastSocketClient.connect();
        }
    }

}
