package com.cuichen.fastsocketclient;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.cuichen.fastsocketclient.utils.HexUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Arrays;

/**
 */

public class FastSocketClient extends Thread {

    private static final String TAG = "SocketClient";
    //测试环境
    private static final String IP = "10.30.14.79";
    private static final int PORT = 8080;
    private static FastSocketClient socketClient;
    private Socket socket;
    private DataInputStream dis;
    private OutputStream out;


    public static synchronized FastSocketClient getInstance() {
        if (socketClient == null) {
            synchronized (FastSocketClient.class) {
                if (socketClient == null) {
                    socketClient = new FastSocketClient();
                }
            }
        }
        return socketClient;
    }

    @Override
    public void run() {
        create();
        while (true) {
            try {
                byte[] buff = new byte[1024];
                int len = dis.read(buff);
                byte[] data = Arrays.copyOfRange(buff, 0, len);
                if (onSocketClientCallBackList != null) onSocketClientCallBackList.onCallBack(HexUtil.byte2HexStr(data));
                Log.i(TAG, "run: "+ HexUtil.byte2HexStr(data));
            } catch (Exception e) {
                e.fillInStackTrace();
            }

        }
    }

    /**
     * 创建socket客户端
     */
    private void create() {
            if (socket == null) socket = new Socket();
            try {
                socket.connect(new InetSocketAddress(IP, Integer.valueOf(PORT)), 5 * 1000);
                if (socket.isConnected()) {
                    out = socket.getOutputStream();
                    InputStream in = socket.getInputStream();
                    dis = new DataInputStream(in);
                    String s = "服务器连接成功!";
                    if (onSocketClientCallBackList != null) onSocketClientCallBackList.onCallBack(s);
                    Log.d(TAG, s);
                }
            } catch (IOException e) {
                e.printStackTrace();
                if (e instanceof SocketTimeoutException) {
//                    toastMsg("连接超时，正在重连");


                } else if (e instanceof NoRouteToHostException) {
//                    toastMsg("该地址不存在，请检查");

                } else if (e instanceof ConnectException) {
//                    toastMsg("连接异常或被拒绝，请检查");

                } else if (e instanceof SocketException){
//                    if (TextUtils.equals(e.getMessage(),"already connected"))
//                        toastMsg("当前已连接，请勿再次连接");
                }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送消息
     */
    public synchronized void sendMsg(byte[] msg) {
        try {
            if (null != out) {
                out.write(msg);
                out.flush();
            }
        } catch (Exception e) {
            //出现此异常说明Socker已经断开连接 断线重连
            if (e instanceof SocketException) {
                reconnectServer();
            }
            e.printStackTrace();
        }
    }

    /**
     * 重连tcp
     */
    private void reconnectServer() {
        try {
            socket = new Socket(IP, PORT);
            socket.setSoTimeout(5000);
            out = socket.getOutputStream();
            InputStream in = socket.getInputStream();
            dis = new DataInputStream(in);
            String s = "断线重连服务器: " + IP + ":" + PORT + " 连接成功!";
            if (onSocketClientCallBackList != null) onSocketClientCallBackList.onCallBack(s);
            Log.d(TAG, s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        if (socket != null) {
            try {
                socket.close();
                dis.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnSocketClientCallBackList{
        void onCallBack(String msg);
    }
    private OnSocketClientCallBackList onSocketClientCallBackList;

    public void setOnSocketClientCallBackList(OnSocketClientCallBackList onSocketClientCallBackList){
        this.onSocketClientCallBackList = onSocketClientCallBackList;
    }
}
