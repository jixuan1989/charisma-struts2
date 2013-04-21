package cn.edu.cqu.utils.files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.DocumentType;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

/**
 * 将一批html结尾的文件修改为jsp结尾，并在开头添加<%page>等标签
 * 该类同时添加了struts2标签，并将url格式都改写为<a href="<s:url ...../>">sdfsd</a>
 * @author hxd
 *
 * 2013-4-21
 */
public class Html2Jsp {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
//	Pattern pattern=Pattern.compile("(href|src)=\"([\\S]+)\"");
//	Matcher matcher=pattern.matcher("href=\"abc\"");
//	System.out.println(matcher.find());
//	System.out.println(matcher.group(2));
			transfer(new File("E:\\data\\code\\github\\charisma\\charisma-struts2"),new File("E:\\data\\code\\github\\charisma\\struts2"));
	//	cpFile2(new File("e:\\QQDownload\\AcrobatPro10.0_Web.exe"), new File("e:\\QQDownload\\AcrobatPro10.0_Web_bk.exe"));
	}

	public static void transfer(File srcFolder,File destFolder){
		File[] srcs=srcFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.equals(".git")){
					return false;
				}if(name.equals("struts2_version")){
					return false;
				}
				return true;
			}
		});
		destFolder.mkdirs();
		for(File src:srcs){
			System.out.println(src.getAbsolutePath());
			File dest=null;
			if(src.isDirectory()){
				dest=new File(destFolder, src.getName());
				dest.mkdir();
				transfer(src, dest);
			}else {
				if(src.getName().endsWith(".html")){
					dest=new File(destFolder, src.getName().substring(0,src.getName().lastIndexOf("."))+".jsp");
					try {
						transferHtml(src,dest);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}else{
					dest=new File(destFolder,  src.getName());
					try {
						boolean ok=cpFile2(src, dest);
						if(!ok){
							System.err.println(src.getAbsolutePath()+"copy failed.");
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public static boolean cpFile(File src,File dest) throws FileNotFoundException{
		int block=4096;
		FileInputStream in=new FileInputStream(src);
		FileOutputStream out=new FileOutputStream(dest);
		FileChannel inC=in.getChannel();
		FileChannel outC=out.getChannel();
		ByteBuffer b=null;
		long time=System.currentTimeMillis();
		try {
			while(true){	
				if(inC.position()==inC.size()){		
					break;
				}
				if((inC.size()-inC.position())<block){
					block=(int)(inC.size()-inC.position());
				}
				b=ByteBuffer.allocateDirect(block);
				inC.read(b);
				b.flip();
				outC.write(b);
				outC.force(false);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally{
			try {
				inC.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				outC.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("size:"+src.length()+"\tcost:"+(System.currentTimeMillis()-time));
		return true;
	}
	
	
	public static boolean cpFile2(File src,File dest) throws FileNotFoundException{
		int block=4096;
		FileInputStream in=new FileInputStream(src);
		FileOutputStream out=new FileOutputStream(dest);
		long time=System.currentTimeMillis();
		byte[] buffer = new byte[block]; 
		int num=0;
		try {
			while((num=in.read(buffer))!=-1){  
                out.write(buffer,0,num);  
            } 
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("size:"+src.length()+"\tcost:"+(System.currentTimeMillis()-time));
		return true;
	}
	public static void transferHtml(File input,File dest) throws IOException{
		Document doc = Jsoup.parse(input,"UTF-8"); 
		String src=doc.toString();
		BufferedReader reader=new BufferedReader(new StringReader(src));
		FileWriter fWriter=new FileWriter(dest);
		String line;
		fWriter.write("<%@ page language=\"java\" contentType=\"text/html; charset=utf-8\"%>\n<%@taglib prefix=\"s\" uri=\"/struts-tags\"%>\n");
		Pattern pattern=Pattern.compile("(href|src)=\"([\\S&&[^\"]]+)\"");
		
		while((line=reader.readLine())!=null){
			Matcher matcher=pattern.matcher(line);
			String newline="";
			if( matcher.find()){
				int start=0;
				int end=line.length();
				newline=line.substring(start,matcher.start(2));
				end=matcher.end(2);
				newline+=getString(matcher.group(2));
				while(matcher.find()){
					newline+=line.substring(end,matcher.start(2));
					newline+=getString(matcher.group(2));
					end=matcher.end(2);
				}
				newline+=line.substring(end,line.length())+"\n";
			}else{
				newline=line+"\n";
			}
			fWriter.write(newline);
		}
		reader.close();
		fWriter.close();
	}
	private static String getString(String group) {
		if(group.startsWith("css/")||group.startsWith("js/")||group.startsWith("misc/")||group.startsWith("img/")||group.startsWith("php-version/")){
			String result="<s:url value=\"/resources/dash/";
			result+=group;
			result+="\"/>";
			return result;
		}else if(group.endsWith(".html")){
			String result="<s:url action=\"";
			result+=group.substring(0,group.indexOf(".html"));
			result+="\"/>";
			return result;
		}
		return group;
	}
}
