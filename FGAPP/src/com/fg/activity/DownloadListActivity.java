package com.fg.activity;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.fg.util.DownloadUtil;
import com.fg.util.MyAdapter;
import com.fg.util.MyDividerItemDecoration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.fg.activity.MainActivity.IMEI;
import static com.fg.activity.MainActivity.IMEI_KEY;
import static com.fg.activity.MainActivity.IP;
import static com.fg.util.DownloadUtil.downLoad;
import static com.fg.util.DownloadUtil.runDecryptFile;
import static com.fg.util.UploadUtil.getSDCardPath;

public class DownloadListActivity extends Activity {
    private String mBaseUrl =  "http://"+IP+":8080/FileDownLoad/";
    //private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);
        initData();
        initView();
    }

    private void initData() {
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mAdapter = new MyAdapter(getData());
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        // 设置布局管理器
        mRecyclerView.setLayoutManager(mLayoutManager);
        // 设置adapter
        mRecyclerView.setAdapter(mAdapter);
        //设置RecyclerView的间隔样式
        mRecyclerView.addItemDecoration(new MyDividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        mAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                String _$temp = "_$temp#";
                downLoad(mBaseUrl+IMEI_KEY.substring(128)+"/"+getData().get(position), getData().get(position)+_$temp);

                String path1 = Environment.getExternalStorageDirectory().toString() + "/SnapPass/" +getData().get(position);
                String path2 = Environment.getExternalStorageDirectory().toString() + "/SnapPass/" + getData().get(position)+_$temp;
                //文件解密
                File file = new File(path2);
                final File _file = new File(path1);
                runDecryptFile(file,_file);

                if(!_file.exists())
                {
                    Toast.makeText(DownloadListActivity.this,"下载失败！", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(DownloadListActivity.this,"已下载至文件夹SnapPass", Toast.LENGTH_SHORT).show();
                }
                file.delete();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(DownloadListActivity.this,"long click " + position + " item", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static ArrayList<String> getData() {
        int fileNumber = 0;
        ArrayList<String> namelistArray = new ArrayList<>();

        try {
            String file_list_path = Environment.getExternalStorageDirectory().toString() + "/SnapPass" + "/$FILE_LIST_IMP.txt";
            // 要读取以上路径的文件
            File tempfile = new File(file_list_path);
            // 建立一个输入流对象reader
            InputStreamReader reader = new InputStreamReader(new FileInputStream(tempfile));
            // 建立一个对象，它把文件内容转成计算机能读懂的语言
            BufferedReader br = new BufferedReader(reader);
            String line = "";
            line = br.readLine();
            while (line != null) {
                // 一次读入一行数据
                line = br.readLine();
                namelistArray.add(line);
                fileNumber++;
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        for(int i = 0; i < fileNumber; i++) {
            namelistArray.get(i);
        }
        return namelistArray;
    }
}
