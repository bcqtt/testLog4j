package com.lz.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;


public class TestLog4j {
	
	public static void main(String[]args){
		final Logger log = Logger.getLogger(TestLog4j.class);
		try{
			System.out.println("抛异常：");
			Double.parseDouble("ad");
			throw new RuntimeException();
		}catch(Exception e){
			System.out.println("[Exception Name]:" + e.getClass());
			
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			String errorLog = "";
			e.printStackTrace(pw);
			errorLog = sw.toString();
			log.error(errorLog,e);
			
			System.out.println("[errorLog]:\n" + errorLog);
			
			pw.close();
			try {
				sw.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

}
