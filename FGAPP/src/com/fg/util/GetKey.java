package com.fg.util;

import android.telephony.TelephonyManager;
import java.io.UnsupportedEncodingException;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.fg.activity.MainActivity.IMEI;


public class GetKey {
    private	static	byte[] key = null;
    public	static	byte[] getKey(){
        if ( key == null ){
            System.out.println("imei---"+IMEI);
            if ( IMEI == null )
                return null;
            try {
                key = MD5Util.encode(IMEI.getBytes("utf-8"));
                return key;
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }else{
            return key;
        }
    }
}

