package com.fg.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import static com.fg.util.UploadUtil.getSDCardPath;

/**
 * Created by EisenloCarter on 2017/5/3.
 */

public class DownloadUtil {
    private static DownloadUtil downloadUtil;
    private static final String BOUNDARY =  UUID.randomUUID().toString(); // 边界标识 随机生成
    private static final String PREFIX = "--";
    private static final String LINE_END = "\r\n";
    private static final String CONTENT_TYPE = "multipart/form-data"; // 内容类型

    public static void runDecryptFile(File file, File _file) {
        // TODO Auto-generated method stub
        try {
            // 读取文件内容 (输入流)
            FileInputStream out = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(out);
            int ch = 0;
            while ((ch = isr.read()) != -1) {
                System.out.print((char) ch);
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        SMS4 sm4 = new SMS4();
        try {
            SecurityUtil.decryptFile(file,_file);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 从服务器下载文件
     * @param path 下载文件的地址
     * @param FileName 文件名字
     */
    public static void downLoad(final String path, final String FileName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setReadTimeout(5000);
                    con.setConnectTimeout(5000);
                    con.setRequestProperty("Charset", "UTF-8");
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == 200) {
                        //获取输入流
                        InputStream is = con.getInputStream();
                        //文件输出流
                        FileOutputStream fileOutputStream = null;
                        if (is != null) {
                            FileUtils fileUtils = new FileUtils();
                            //指定文件保存路径
                            fileOutputStream = new FileOutputStream(fileUtils.createFile(FileName));
                            byte[] buf = new byte[1024];
                            int ch;
                            while ((ch = is.read(buf)) != -1) {
                                //将获取到的流写入文件中
                                fileOutputStream.write(buf, 0, ch);
                            }
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
       // uploadFile(_file, fileKey, RequestURL, param);
    }
}
