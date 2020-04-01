package com.cuichen.fastsocketclient.utils;

import android.util.Log;

public class L {
    public static void c(Object str){
        if (str instanceof String){
            Log.i("CCB", (String)str);
        }else {
            Log.i("CCB", str.toString());
        }
    }
}
