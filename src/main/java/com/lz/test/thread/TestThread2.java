package com.lz.test.thread;

public class TestThread2 implements Runnable {

	public void run() {
		for(int i=0;i<5;i++){
			System.out.println(Thread.currentThread().getName());
		}
	}
	
	public static void main(String [] args){
		TestThread2 testThread2 = new TestThread2();
		new Thread(testThread2,"A").start();
        new Thread(testThread2,"B").start();
        new Thread(testThread2).start();
		
	}

}
