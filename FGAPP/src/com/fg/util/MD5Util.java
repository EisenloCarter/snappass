package com.fg.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {
    private static MessageDigest md5 = null;
    public	static byte[] encode( byte[] input ){
        if ( md5 == null )
            try {
                md5 = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        return md5.digest(input);
    }
}
