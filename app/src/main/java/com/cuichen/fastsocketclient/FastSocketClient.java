package com.cuichen.fastsocketclient;

import android.os.SystemClock;
import android.util.Log;

import com.cuichen.fastsocketclient.interfaces.OnSocketClientCallBackList;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 */

public class FastSocketClient {

    private static final String TAG = "SocketClient";
    private static FastSocketClient socketClient;
    private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private Socket socket;
    private DataInputStream dis;
    private OutputStream out;
    private boolean neverReconnect = false; //不在重新连接


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

    /**
     * 创建socket客户端
     */
    public void connect() {
        if (socket == null) socket = new Socket();
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socket.connect(new InetSocketAddress(SocketConfig.IP, SocketConfig.PORT), 5 * 1000);
                    if (socket.isConnected()) {
                        out = socket.getOutputStream();
                        InputStream in = socket.getInputStream();
                        dis = new DataInputStream(in);
                        if (onSocketClientCallBackList != null)
                            onSocketClientCallBackList.onSocketConnectionSuccess(SocketConfig.IP + ":" + SocketConfig.PORT);
                        neverReconnect = false;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (onSocketClientCallBackList != null)
                        onSocketClientCallBackList.onSocketConnectionFailed(null, e);
                    if (e instanceof SocketTimeoutException) {
                        SystemClock.sleep(5000);
                        connect();
//                    toastMsg("连接超时");
                    } else if (e instanceof NoRouteToHostException) {
//                    toastMsg("该地址不存在，请检查");
                    } else if (e instanceof ConnectException) {
//                    toastMsg("连接异常或被拒绝，请检查");
                    } else if (e instanceof SocketException) {
//                    if (TextUtils.equals(e.getMessage(),"already connected"))
//                        toastMsg("当前已连接，请勿再次连接");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while (true) {
                    try { //阻塞式读取数据
                        byte[] buff = new byte[1024 * 5];
                        int len = dis.read(buff);
                        byte[] data = Arrays.copyOfRange(buff, 0, len);
                        if (onSocketClientCallBackList != null)
                            onSocketClientCallBackList.onSocketReadResponse(data);
                    } catch (Exception e) {
                        e.fillInStackTrace();
                    }

                }
            }
        });


    }

    /**
     * 发送消息
     */
    public synchronized void send(final byte[] datas) {
        if (!isConnected()) {
            if (onSocketClientCallBackList != null)
                onSocketClientCallBackList.onSocketDisconnection("未连接", null);
            return;
        }
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (null != out) {
                        out.write(datas);
                        out.flush();
                        if (onSocketClientCallBackList != null)
                            onSocketClientCallBackList.onSocketWriteResponse(datas);
                    }
                } catch (Exception e) {
                    //出现异常说明Socker已经断开连接 断线重连
                    if (!neverReconnect) connect();
                    if (onSocketClientCallBackList != null)
                        onSocketClientCallBackList.onSocketDisconnection("", e);
                    e.printStackTrace();
                }
            }
        });

    }

    public void close() {
        if (socket != null) {
            cachedThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        socket.close();
                        dis.close();
                        out.close();
                        if (onSocketClientCallBackList != null)
                            onSocketClientCallBackList.onSocketDisconnection("主动断开连接", null);
                        neverReconnect = true;
                        socket = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public boolean isConnected() {
        if (socket == null) return false;
        return socket.isConnected();
    }


    private OnSocketClientCallBackList onSocketClientCallBackList;

    public void setOnSocketClientCallBackList(OnSocketClientCallBackList onSocketClientCallBackList) {
        this.onSocketClientCallBackList = onSocketClientCallBackList;
    }
}
