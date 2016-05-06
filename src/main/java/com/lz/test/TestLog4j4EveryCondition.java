package com.lz.test;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.mail.MessagingException;

import com.lz.test.util.JdkLoggerUtils;


public class TestLog4j4EveryCondition {
	
	public static void main(String [] args) throws MessagingException, IOException{
		Logger logger = Logger.getLogger(TestLog4j4EveryCondition.class.toString());  
		
		String filepath = "d:/myLogger.log";
		
		System.out.println("输入一个数:");
		Scanner input = new Scanner(System.in);
		int i = input.nextInt();    //接收float数据
		try{
			switch(i){
				case 1:
					throw new RuntimeException();
				case 2:
					Integer.parseInt("aa");
				case 3:
					throw new MyException1();    //不紧急
				case 4:
					throw new MyException2(MyConstant.URGENCY);    //一般紧急入库
				case 5:
					throw new MyException3(MyConstant._ORDINARY,MyConstant.URGENCY);    //紧急，发邮件
				default:
			}
		}catch(Exception e){
			JdkLoggerUtils.consoleHandler(logger,"提示信息", e);
			JdkLoggerUtils.fileHandler(logger,"提示信息laizhiwen", e, filepath);
			JdkLoggerUtils.saveIntoDatabase(logger,"提示信息laizhiwen", e);
			JdkLoggerUtils.sendEmail(logger,"提示信息laizhiwen", e);
			JdkLoggerUtils.sendEmailAndSaveIntoDB(logger,"提示信息laizhiwen", e);
			JdkLoggerUtils.sendEmailAndDbAndFile(logger,"提示信息laizhiwen", e, filepath);
		}
	}
}

//自定义异常，一般异常类
class MyException1 extends Exception{
	public MyException1(){
		System.out.println("自定义异常:本异常不紧急！");
	}
}

//自定义异常，一般紧急异常类
class MyException2 extends Exception{
	public MyException2(String str){
		System.out.println("自定义异常:一般紧急！入库");
	}
}

//自定义异常，紧急异常类
class MyException3 extends Exception{
	public MyException3(int i,String str){
		System.out.println("自定义异常:紧急！发邮件");
	}
}

interface MyConstant {
	public final static int _ORDINARY = 1;
	public final static String ORDINARY = "一般异常！正常范围。存库。";
	public final static String ORDINARY_URGENCY = "一般紧急异常！存库。";
	public final static String URGENCY = "紧急异常！发送邮件并存库。";
}
