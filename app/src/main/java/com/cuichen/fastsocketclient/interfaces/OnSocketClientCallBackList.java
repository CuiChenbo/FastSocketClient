package com.cuichen.fastsocketclient.interfaces;

public interface OnSocketClientCallBackList{
        void onSocketConnectionSuccess(String msg);
        void onSocketConnectionFailed(String msg , Exception e);
        void onSocketDisconnection(String msg , Exception e);
        void onSocketReadResponse(byte[] bytes);
        void onSocketWriteResponse(byte[] bytes);
    }