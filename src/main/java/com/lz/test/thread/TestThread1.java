package com.lz.test.thread;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TestThread1 extends Thread {
	
	String threadName;
	
	public TestThread1(){
		
	}
	
	public TestThread1(String threadName){
		this.threadName = threadName;
	}
	
	public void run(){
		long t1 = System.currentTimeMillis();
		System.out.println(threadName + " 运行中...");
		File file = new File("F:/testThread/"+threadName+".txt");
		 if(!file.exists()){
			 try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		 }
		
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<9000;i++){
			sb.append(threadName + " 运行     " + i +"ms\n");
//			System.out.println(threadName + " 运行     " + i);
			
		}
		
		BufferedWriter bw =null;
		try {
			bw = new BufferedWriter(new FileWriter(file));
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		long t2 = System.currentTimeMillis(); 
		System.out.println(threadName + "  运行结束，耗时：  "+ (t2-t1) );
	}
	
	public static void main(String[]args){
//		TestThread1 test1 = new TestThread1("线程A");
//		TestThread1 test2 = new TestThread1("线程B");
		
		TestThread1 testThread = null;
		
		for(int i=1;i<500;i++){
			testThread = new TestThread1("线程_"+i);
			testThread.start();
		}
//		test1.start();
//		test2.start();
	}
	

}
