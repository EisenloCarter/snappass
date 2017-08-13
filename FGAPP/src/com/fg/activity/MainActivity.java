package com.fg.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fg.sm.SM2;
import com.fg.sm.SM2Utils;
import com.fg.sm.Util;
import com.fg.util.UploadUtil;
import com.fg.util.UploadUtil.OnUploadProcessListener;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Base64Encoder;

import static com.fg.activity.DownloadListActivity.getData;

/**
 * 说明：主要用于选择文件和上传文件操作
 */
public class MainActivity extends Activity implements OnClickListener,OnUploadProcessListener{
	private static final String TAG = "uploadFile";
	public static String IMEI;
	public static String IMEI_KEY;
	public static String privateKEY;
	public static String IP = "192.168.43.48";
	private static String requestURL = "http://"+IP+":8080/FileDownLoad/servlet/FileUpLoadServlet";;
	/***
	 * 这里的这个URL是我服务器的javaEE环境URL
	 */
	/**
	 * 去上传文件
	 */
	protected static final int TO_UPLOAD_FILE = 1;  
	/**
	 * 上传文件响应
	 */
	protected static final int UPLOAD_FILE_DONE = 2;  //
	/**
	 * 选择文件
	 */
	public static final int TO_SELECT_FILE = 3;
	/**
	 * 上传初始化
	 */
	private static final int UPLOAD_INIT_PROCESS = 4;
	/**
	 * 上传中
	 */
	private static final int UPLOAD_IN_PROCESS = 5;
	/**
	 * 去下载文件
	 */
	protected static final int TO_DOWNLOAD_FILE = 6;

	private Button selectButton,uploadButton,downloadButton;
	private ImageView imageView;
	private TextView loadFileResult;
	
	private String filePath = null;
	private ProgressDialog progressDialog;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initView();
    }
    /**
     * 初始化数据
     */
	public void initView() {
        selectButton = (Button) this.findViewById(R.id.selectFile);
        uploadButton = (Button) this.findViewById(R.id.uploadFile);
		downloadButton = (Button) this.findViewById(R.id.downloadFile);

        selectButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);
		downloadButton.setOnClickListener(this);

		imageView = (ImageView) this.findViewById(R.id.imageView);
		loadFileResult = (TextView) findViewById(R.id.uploadFileResult);
        progressDialog = new ProgressDialog(this);

		TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
		IMEI = telephonyManager.getDeviceId();

		String path = Environment.getExternalStorageDirectory().toString() + "/SnapPass";
		File rootpath = new File(path);
		if (!rootpath.exists()) {
			rootpath.mkdirs();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.selectFile:
			Intent intent = new Intent(this,SelectFileActivity.class);
			startActivityForResult(intent, TO_SELECT_FILE);
			break;
		case R.id.uploadFile:
			if(filePath!=null)
			{
				handler.sendEmptyMessage(TO_UPLOAD_FILE);
			}else{
				Toast.makeText(this, "上传的文件路径出错", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.downloadFile:
			handler.sendEmptyMessage(TO_DOWNLOAD_FILE);
			Intent intent2 = new Intent(this,DownloadListActivity.class);
			startActivityForResult(intent2, TO_SELECT_FILE);
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode==Activity.RESULT_OK && requestCode == TO_SELECT_FILE)
		{
			filePath = data.getStringExtra(SelectFileActivity.KEY_FILE_PATH);
			Log.i(TAG, "最终选择的文件="+filePath);
			Bitmap bm = BitmapFactory.decodeFile(filePath);
			imageView.setImageBitmap(bm);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	/**
	 * 上传服务器响应回调
	 */
	@Override
	public void onUploadDone(int responseCode, String message) {
		progressDialog.dismiss();
		Message msg = Message.obtain();
		msg.what = UPLOAD_FILE_DONE;
		msg.arg1 = responseCode;
		msg.obj = message;
		handler.sendMessage(msg);
	}

	private void toUploadFile() throws IOException {
		loadFileResult.setText("正在上传中...");
		progressDialog.setMessage("正在上传文件...");
		progressDialog.show();
		String fileKey = "img";
		UploadUtil uploadUtil = UploadUtil.getInstance();
		uploadUtil.setOnUploadProcessListener(this);  //设置监听器监听上传状态
		
		Map<String, String> params = new HashMap<String, String>();
		getIMEI_KEY();
		getPrivateKEY();
		params.put(privateKEY, IMEI_KEY);
		System.out.print("就是这里");
		System.out.print("privateKEY:"+privateKEY);
		System.out.print("IMEI_KEY"+IMEI_KEY);
		uploadUtil.uploadFile( filePath,fileKey, requestURL,params);
	}

	@Override
	public void onUploadProcess(int uploadSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_IN_PROCESS;
		msg.arg1 = uploadSize;
		handler.sendMessage(msg);
	}

	@Override
	public void initUpload(int fileSize) {
		Message msg = Message.obtain();
		msg.what = UPLOAD_INIT_PROCESS;
		msg.arg1 = fileSize;
		handler.sendMessage(msg );
	}
		
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TO_UPLOAD_FILE:
				try {
					toUploadFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;

			case UPLOAD_INIT_PROCESS:				
				break;

			case UPLOAD_IN_PROCESS:				
				break;

			case UPLOAD_FILE_DONE:
				String result ="上传结果："+ msg.obj;
				loadFileResult.setText(result);
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
		
	};

	public static String getIMEI_KEY() throws IOException {
		String  userId = "userID";

		String prik="0000000000000000000000000000000000000000000000000"+IMEI;
		String prikS = new String(Base64.encode(Util.hexToByte(prik)));
		String plainText = "1213";
		byte[] sourceData = plainText.getBytes();

		byte[] c = SM2Utils.sign(userId.getBytes(), Base64.decode(prikS.getBytes()), sourceData);
		IMEI_KEY=Util.getHexString(c);
		return IMEI_KEY;
	}

	public static String getPrivateKEY() throws IOException {
		SM2 sm2 = SM2.Instance();
		String prik="0000000000000000000000000000000000000000000000000"+IMEI;
		BigInteger k = new BigInteger(prik, 16);
		ECPoint sk=sm2.ecc_point_g.multiply(k);
		byte[] kkk=sk.getEncoded();
		privateKEY = Util.getHexString(kkk);
		return privateKEY;
	}

	public static int writeFileList(String fileContent) {
		String file_list_path = Environment.getExternalStorageDirectory().toString() + "/SnapPass" + "/$FILE_LIST_IMP.txt";
		File mFileList = new File(file_list_path);

		try {
			//判断文件是否存在，如不存在则调用createNewFile()创建新目录，否则跳至异常处理代码
			if (!mFileList.exists()) {
				mFileList.createNewFile();
				BufferedWriter out = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(mFileList, true), "UTF-8"));
				out.write(""+"\r\n"); // \r\n即为换行
				out.flush(); // 把缓存区内容压入文件
				out.close(); // 最后记得关闭文件
				System.out.print("翻皮水："+fileContent);
				Log.e(TAG, "checkFileList: 成功写入文件列表:"+fileContent );
			}
/*
			for(int i = 1;getData().get(i) != null;i++)
			{
				Log.e(TAG, "checkFileList: 检查文件名是否重复:"+fileContent );
				if(fileContent.equals(getData().get(i)))
					System.out.print("测试"+getData().get(i));
					fileContent = null;
			}
*/
			//下面把数据写入创建的文件，首先新建文件名为参数创建FileWriter对象
			BufferedWriter out = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(mFileList, true), "UTF-8"));
			Log.e(TAG, "checkFileList: 成功写入文件列表:"+fileContent );
			out.write(fileContent+"\r\n"); // \r\n即为换行
			out.flush(); // 把缓存区内容压入文件
			out.close(); // 最后记得关闭文件
		} catch (Exception e) {
			System.out.println("无法创建新文件！");
			e.printStackTrace();
		}
		return 0;
	}

}