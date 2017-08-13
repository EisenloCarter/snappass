package com.xpy.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Base64;

import com.xpy.sm.SM2;
import com.xpy.sm.SM2Utils;
import com.xpy.sm.Util;

@SuppressWarnings("serial")
public class FileUpLoadServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		 String IMEIPath = null;
/*
		// 得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传
		String savePath = this.getServletContext().getRealPath(
				"/WEB-INF/download");
*/
		// 上传时生成临时文件保存目录
		String tempPath = this.getServletContext().getRealPath("/WEB-INF/temp");
		File tempFile = new File(tempPath);
		if (!tempFile.exists()) {
			// 如果不存在就创建一个
			tempFile.mkdir();
		}

		// 消息提示
		String message = "";
		try {
			// 使用Apache文件上传组件处理上传步骤
			// 1.创建一个DiskFileItemFactory工厂
			DiskFileItemFactory factory = new DiskFileItemFactory();

			// 设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存储于临时目录中
			factory.setSizeThreshold(1024 * 100);// 默认为10kb，在此设置为100kb

			// 设置上传时生成的；临时文件的保存目录
			factory.setRepository(tempFile);

			// 2.创建一个文件上传解析器
			ServletFileUpload upload = new ServletFileUpload(factory);
			// 监听文件上传进度
			upload.setProgressListener(new ProgressListener() {

				@Override
				public void update(long arg0, long arg1, int arg2) {
					System.out.println("文件大小为：" + arg1 + ",当前已处理：" + arg0);

				}
			});

			// 解决上传文件名的中文乱码
			upload.setHeaderEncoding("UTF-8");

			// 3.判断提交上来的数据是否是上传表单的数据
			if (!ServletFileUpload.isMultipartContent(request)) {
				return;
			}

			// 设置上传单个文件的大小的最大值，目前是设置为1024*1024，也就是1MB
			upload.setFileSizeMax(1024 * 1024);

			// 设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和，目前设置为1MB
			upload.setSizeMax(1024 * 1024 * 10);

			// 4.使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
			List<FileItem> list = upload.parseRequest(request);

			for (FileItem item : list) {
				// 如果fileitem中封装的是普通输入项的数据
				if (item.isFormField()) {
					String name = item.getFieldName();
					// 解决普通输入项的数据的中文乱码问题
					String value = item.getString("UTF-8");
					System.out.println(name + "=" + value);
					/***
					 *
					 *将name转换回ECPoint类型，进行验证签名
					 *
					 ***/
					String userId = "nice";
					String plainText = "1213";
					byte[] sourceData = plainText.getBytes();
					
					String prik = name;
					String prikS = new String(Base64.encode(Util.hexToByte(prik)));
					byte[] c = SM2Utils.sign(userId.getBytes(), Base64.decode(prikS.getBytes()), sourceData);
					BigInteger k = new BigInteger(prik, 16);
					SM2 sm2 = SM2.Instance();
					ECPoint sk=sm2.ecc_point_g.multiply(k);
					boolean vs = SM2Utils.verifySign(userId.getBytes(),sk, sourceData, c);
					System.out.println("验证签名结果："+vs);
					if(!vs){return;}
					
					// 得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传
					IMEIPath = this.getServletContext().getRealPath(
							"/"+value.substring(128));
					File IMEIdir = new File(IMEIPath);
					if (!IMEIdir.exists()) {
						// 如果不存在就创建一个
						IMEIdir.mkdir();
					}
					
					
				} else {// 如果fileItem中封装的是上传文件

					// 得到上传的文件名称
					String fileName = item.getName();
					System.out.println(fileName);

					if (fileName == null || fileName.trim().equals("")) {
						continue;
					}
					/*
					 * 注意：不同的浏览器提交的文件名是不一样的，有些浏览器提交上来的文件名是带有路径的， 如：
					 * c:\a\b\1.txt，而有些只是单纯的文件名，如：1.txt
					 */

					// 处理获取到的上传文件的文件名的路径部分，只保留文件名部分
					fileName = fileName
							.substring(fileName.lastIndexOf("\\") + 1);
					// 得到文件的扩展名
					String fileExtName = fileName.substring(fileName
							.lastIndexOf(".") + 1);
					// 如果需要限制上传的文件类型，那么可以通过文件的宽展名来判断

					System.out.println("上传文件的扩展名是：" + fileExtName);

					// 获取item中的上传文件输入流
					InputStream in = item.getInputStream();

					// 得到文件保存的名称;
					String saveFileName = fileName;

					// 得到文件的保存目录
					String realSavePath = makePath(saveFileName, IMEIPath);

					// 创建一个文件输出流
					FileOutputStream out = new FileOutputStream(realSavePath
							+ "\\" + saveFileName);

					// 创建一个缓冲区
					byte buffer[] = new byte[1024];
					int length = 0;
					// 循环将输入流读入到缓冲区当中，(len=in.read(buffer))!=-1就表示in里面还有数据
					while ((length = in.read(buffer)) != -1) {
						out.write(buffer, 0, length);
					}

					// 关闭输入流
					in.close();
					// 关闭输出流
					out.close();
					// 删除处理文件上传时生成的临时文件
				    item.delete();
				}
			}
			message = "文件上传成功！";
		} catch (FileUploadBase.FileSizeLimitExceededException e) {
			e.printStackTrace();
			request.setAttribute("message", "单个文件超出最大值！");
			request.getRequestDispatcher("/message.jsp").forward(request,
					response);
			return;
		} catch (FileUploadBase.SizeLimitExceededException e) {
			e.printStackTrace();
			request.setAttribute("message", "上传文件的总大小超出限制的最大值！");
			request.getRequestDispatcher("/message.jsp").forward(request,
					response);
		} catch (Exception e) {
			message = "文件上传失败！";
			e.printStackTrace();
		}
		request.setAttribute("message", message);
		request.getRequestDispatcher("/message.jsp").forward(request, response);
	}

	/**
	 * 为防止一个目录下面出现太多文件，要使用hash算法打散存储 也可以用时间来命名
	 * 
	 * @param saveFileName
	 * @param savePath
	 * @return String
	 */
	private String makePath(String saveFileName, String savePath) {

		// 得到文件名的hashCode的值，得到的就是fileName这个字符串对象在内存中的地址
		int hashcode = saveFileName.hashCode();

		// 构造新的保存目录
		String dir = savePath;
		System.out.println("dir----->" + dir);

		File file = new File(dir);

		if (!file.exists()) {
			file.mkdirs();
		}
		return dir;
	}

	/**
	 * 生成上传文件的文件名，文件名以:uuid+"_"+文件的原始名称
	 * 
	 * @param fileName
	 * @return uuid+"_"+文件的原始名称
	 */
	/*private String makeFileName(String fileName) {

		return fileName;
	}*/

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
