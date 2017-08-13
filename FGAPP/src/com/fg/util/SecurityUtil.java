package com.fg.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SecurityUtil {
    private final static int BUF_SIZE=10240;
    /**
     *
     * @param file
     *            要操作的文件或文件夹
     * @param file_
     *            压缩加密后存放的文件
     *
     */
    public static void encryptFile(File file, File file_)
            throws Exception {// 原来keyStr是String类型
        //File srcfile = new File(file);
        //File desfile = new File(file_);
        FileInputStream fis = new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file_);
        int ENCRYPT = 1;
        int addlen = 0;
        // 得到文件长度
        // int size= fis.available();
        int n=0;
        //一次从文件读BUF_SIZE个字节
        byte []buffer=new byte[BUF_SIZE];
        while ((n = fis.read(buffer)) != -1) {
            //System.out.println(n);
            //一次实际读取的字节
            int size = n;

            // 填充后的长度
            int si = 0;

            // 如果不需要填充
            if (size % 16 == 0) {
                si = size;
            } else// 需要填充
            {
                // 得到需要填充的长度
                addlen = 16 - (size % 16);
                //System.out.println("addlen" + addlen);
                si = size + addlen;
            }
            //System.out.println("si " + si);
            byte[] in = new byte[si];
            for(int i=0;i<n;i++)
                in[i]=buffer[i];
            //fis.read(in);
            //fis.close();
            SMS4 sm4 = new SMS4();
            // 进行填充,最后一个字节存放要填充的字节数addlen
            if (size % 16 != 0) {
                for (int j = size; j < si; j++) {

//					if (j == si - 1)
//						in[si - 1] = (byte) addlen; // 最后一个字节放填充的字节数
//					else
                    in[j] = (byte) 0; // 其他字节用0填充
                }
            }
            byte[] out = new byte[si];
            byte[] key=GetKey.getKey();
            SMS4.sms4(in, si, GetKey.getKey(), out, ENCRYPT);
            fos.write(out);
            //fos.flush();

        }
        System.out.println("addlen:::"+addlen);
        //最后一个字节保留填充个数，不能放在原来的填充的16个字节的最后一位，因为它是要被加密的，这样解密读出来后将不对
        fos.write((byte)addlen);

        fos.close();
        fis.close();
    }

    /**

     * @param file
     *            要解密和解压缩的文件名
     * @param file_
     *            解压缩后的目录

     */
    public static void decryptFile(File file, File file_) throws Exception {
        //File destf=new File(destfile);
        //File srcf=new File(srcfile);
        FileInputStream fis0=new FileInputStream(file);
        FileOutputStream fos = new FileOutputStream(file_);
        int DECRYPT=0;
        int times=0;
        int size=fis0.available();
        //一次从文件读BUF_SIZE个字节
        byte []buffer=new byte[BUF_SIZE];
        //一次实际读取的字节
        int n=0;
        System.out.println("字节数"+size);

        //得到填充的字节数
        fis0.skip((size-1));
        int number=fis0.read();
        System.out.println("填充字节数:"+number);
        fis0.close();

        FileInputStream fis=new FileInputStream(file);

        while((n = fis.read(buffer)) != -1)
        {
            //System.out.println("每次实际读取字节数"+n);
            SMS4 sm4=new SMS4();
            byte[] out=new byte[n];
            byte[] in=new byte[n];
            for(int i=0;i<n;i++)
                in[i]=buffer[i];

            SMS4.sms4(in, n, GetKey.getKey(), out, DECRYPT);
            times+=n;
            if(times<(size-1))
            {
                fos.write(out);
            }
            else
            {//假如明文为16字节，并没有进行填充，但是这里原来写的算法去把它认为是填充了，取最后一个字节作为填充了的，所以解密有问题了，现在改正了
                //int addlen=out[n-1];
                //System.out.println("addlen"+addlen);
                //System.out.println("size "+size+"n"+n);
                //n为实际读取长度
                System.out.println("lastlen::"+n);
                int outLen=0;
                if(n==BUF_SIZE){//最后一个10240，填充的字节数在下一个10240，不用管了
                    outLen=n-number;
                }
                else{//将填充位也读取出来了，所以要减1
                    outLen=n-number-1;
                }
                byte[] outf=new byte[outLen];
                for(int i=0;i<outLen;i++)
                {
                    outf[i]=out[i];
                }
                fos.write(outf);
                break;
            }
        }
        fis.close();
        fos.close();
    }
}

