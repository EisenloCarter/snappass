package com.xpy.download;

import java.io.File;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 1.在/WEB-INF下创建一个名为download文件夹
 *2. 把下载的文件放于download
 *3.列出download文件夹中所有文件
 */
@SuppressWarnings("serial")
public class ListFileServlet extends HttpServlet {
	//static String fileName = null;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		Map<String, String> fileMap=new HashMap<String, String>();
		String filePath=this.getServletContext().getRealPath("/WEB-INF/download/863726036336283");
        
		listFile(fileMap,filePath);
		request.setAttribute("fileMap", fileMap);
		request.getRequestDispatcher("/fileList.jsp").forward(request, response);
	}

	//将upload下的文件名及路径封装到fileMap中
	private void listFile(Map<String, String> fileMap, String filePath) {
		 
		File file=new File(filePath);
		//获取该目录下的所有文件
		File []files=file.listFiles();
		//获取文件数量
		int i = 0;
		for (File file2 : files) {
			//如果file2代表的不是文件，而是一个目录，就跳过
			if(file.isFile()){
				continue;
			}
			String fileName=file2.getName();
			fileMap.put(fileName, fileName);
			i++;
		}
		
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		doGet(request, response);
	}

}
