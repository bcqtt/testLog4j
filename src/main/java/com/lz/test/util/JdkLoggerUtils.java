package com.lz.test.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * JdkLoggerUtils是基于JDK Logger对日志进行输出存储的工具类，
 * 封装了日志输出的的各种方法，本类模仿了log4j的输出格式，定义
 * 了1、日志输出到控制台，2、日志输出到文件，3、日志发送邮件，
 * 4、日志存入数据库，5、 日志发送邮件并存入库，6、日志发送邮件和输出到文件
 * 7、日志输出到文件、数据库、邮件 等方法。
 * @author laizhiwen
 * @date 2012-10-29
 */
public class JdkLoggerUtils {
	
	static Logger log = Logger.getLogger(JdkLoggerUtils.class.toString());
	
	static SimpleDateFormat format = new SimpleDateFormat();
	static int limit = 1024*1024;  //文件最大容量
	
	/**
	 * 日志输出到控制台
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 */
	public static void consoleHandler(Logger logger,String msg,Exception e){
		
		//显示所有等级的信息
		logger.setLevel(Level.ALL);
		
		ConsoleHandler consoleHandler = new ConsoleHandler();
		//显示所有等级的信息
		consoleHandler.setLevel(Level.ALL);
		
		consoleHandler.setFormatter(new MyFormatter());
		
		 //设定Handler为!ConsoleHandler
		logger.addHandler(consoleHandler);
		
//		logger.severe("严重信息");
//      logger.warning("警示信息");
//      logger.info("一般信息");
//      logger.config("设定方面的信息");
//      logger.fine("细微的信息");
//      logger.finer("更细微的信息");
//      logger.finest("最细微的信息");
		
		logger.log(new ErrorLevel(), msg , e);

	}
	
	/**
	 * 日志输出到文件
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 * @param filepath(String)  输出文件路径
	 */
	public static void fileHandler(Logger logger,String msg,Exception e,String filepath){
          try {
               FileHandler fileHandler = new FileHandler(filepath,true);
               fileHandler.setFormatter(new MyFormatter());
               logger.addHandler(fileHandler);
               logger.log(new ErrorLevel(), msg , e);
          }catch (SecurityException e1){
               e.printStackTrace();
               JdkLoggerUtils.consoleHandler(log, "JdkLoggerUtils.sendEmail", e1);
          }catch (IOException e2){
               e.printStackTrace();
               JdkLoggerUtils.consoleHandler(log, "JdkLoggerUtils.sendEmail", e2);
          }
	}
	
	/**
	 * 日志发送邮件
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 */
	public static void sendEmail(Logger logger, String msg,Exception e){
		try{
			EmailHandler emailHandler = new EmailHandler();
            logger.addHandler(emailHandler);
     	    logger.log(new ErrorLevel(), msg, e);
        }catch(Exception e1){
     	   e1.printStackTrace();
        }
	}
	
	/**
	 * 日志存入数据库
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 */
	public static void saveIntoDatabase(Logger logger, String msg,Exception e){
		JdbcHandler jdbcHandler = new JdbcHandler();
        logger.addHandler(jdbcHandler);
        logger.log(new ErrorLevel(), msg, e);
	}
	
	/**
	 * 日志发送邮件并存入库
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 */
	public static void sendEmailAndSaveIntoDB(Logger logger, String msg,Exception e){
		try{
			EmailHandler emailHandler = new EmailHandler();
			JdbcHandler jdbcHandler = new JdbcHandler();
            logger.addHandler(jdbcHandler);
            logger.addHandler(emailHandler);
            logger.log(new ErrorLevel(), msg, e);
        }catch(Exception e1){
     	   e.printStackTrace();
        }
	}
	
	/**
	 * 日志发送邮件和输出到文件
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 * @param filepath(String)  输出文件路径
	 */
	public static void sendEmailAndSave2File(Logger logger, String msg,Exception e,String filepath){
		try{
			EmailHandler emailHandler = new EmailHandler();
			FileHandler fileHandler = new FileHandler(filepath,true);
            fileHandler.setFormatter(new MyFormatter());
            logger.addHandler(fileHandler);
			logger.addHandler(emailHandler);
			logger.log(new ErrorLevel(), msg, e);
        }catch(Exception e1){
        	e1.printStackTrace();
        }
	}
	
	/**
	 * 日志输出到文件、数据库、邮件
	 * @param logger(Logger) 日志对象
	 * @param msg(String)  提示消息
	 * @param e(Exception)  异常对象
	 * @param filepath(String)  输出文件路径
	 */
	public static void sendEmailAndDbAndFile(Logger logger,String msg,Exception e,String filepath){
		try{
			EmailHandler emailHandler = new EmailHandler();
			FileHandler fileHandler = new FileHandler(filepath,true);
            JdbcHandler jdbcHandler = new JdbcHandler();
            fileHandler.setFormatter(new MyFormatter());
            logger.addHandler(jdbcHandler);
            logger.addHandler(fileHandler);
			logger.addHandler(emailHandler);
			logger.log(new ErrorLevel(), msg, e);
        }catch(Exception e1){
     	   e1.printStackTrace();
        }
	}
	
	
	public static void main(String[] args) {
		Connection conn = JdbcHandler.getConn4Ms();
	}
	
}

/**
 * 格式化日志输出样式
 * @author laizhiwen
 *
 */
class MyFormatter extends Formatter{
	StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();  
	StackTraceElement element = stackTraceElement[stackTraceElement.length - 1];
	@Override
	public String format(LogRecord record) {
		StringBuffer buf = new StringBuffer();
		buf.append(record.getLevel());             //获得日志级别
		buf.append("-");
		buf.append(new Date().toLocaleString());   //获得日志时间
		buf.append(" - ");
		buf.append(record.getThreadID());          //线程ID
		buf.append(" - ");
		buf.append(record.getSourceClassName());   //报错的类名
		buf.append(".");
		buf.append(record.getSourceMethodName());  //报错的方法名
		buf.append(" - ");
		buf.append(formatMessage(record));         //格式化日志记录数据
		buf.append(" - "); 
		buf.append(element.getLineNumber());       //调用出错方法的入口
		buf.append("\n"); 
		buf.append(record.getMessage());           //日志错误信息提示
		
		StringWriter sw = new StringWriter();  
        PrintWriter pw = new PrintWriter(sw);  
        record.getThrown().printStackTrace(pw);  
        pw.close();  
        buf.append(sw.toString());                  //异常堆栈信息
		
		return buf.toString();
	}
}

/**
 * 输出目的地为Email的类
 * @author Administrator
 *
 */
class EmailHandler extends Handler{
	
	//邮件信息
	private String from    ="lz881228@163.com" ;             //发件人地址
	private String to      ="lz881228@yeah.net" ;            //收件人地址
	private String subject ="Try To Send ErrorMessage" ;     //发送的邮件标题
	//private String content =null;                          //邮件内容
	private String password="angel881228$$$";                //发件人邮箱密码

	@Override
	public void publish(LogRecord record) {
		try{
			//生成SMTP主机名
			int n = from.indexOf('@');
			int m =from.length();
			String mailserver = "smtp." + from.substring(n+1, m);
			
			//建立邮件会话
			Properties properties = new Properties();      //Properties类对象是被Session对象使用，以获得邮件服务器、用户名、密码等信息的，
			                                               //所以要比Session先创建
			properties.put("mail.smtp.host",mailserver);
			properties.put("mail.smtp.auth", "true");
			Session session =Session.getInstance(properties);  //邮件会话
			session.setDebug(true);
			
			//创建一个消息对象
			MimeMessage message = new MimeMessage(session);   //存储发送的电子邮件的实际信息，实例化时需要指定一个mailSession
			
			//设置发件人
			InternetAddress from_mail = new InternetAddress(from);
			message.setFrom(from_mail);
			
			//设置收件人
			InternetAddress to_mail = new InternetAddress(to);
			message.setRecipient(Message.RecipientType.TO, to_mail);
			
			//设置主题
			message.setSubject(subject);
			
			MyFormatter myFormatter = new MyFormatter();
			
			//设置内容
			message.setText(myFormatter.format(record));
			
			//设置发送时间
			message.setSentDate(new Date());
			
			//发送邮件
			message.saveChanges();      //保证报头域同会话内容保持一致
			Transport transport = session.getTransport("smtp");
			transport.connect(mailserver, from, password);
			transport.sendMessage(message, message.getAllRecipients());
			transport.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	@Override
	public void flush() {
		// TODO Auto-generated method stub
	}
	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
	}
}

/**
 * 扩展JDK Logger 的Level,增加一个级别界于SERVER(1000)、WARNING(900)
 * 之间的级别REEOR(9500)
 * @author laizhiwen
 */
class ErrorLevel extends java.util.logging.Level{
    public ErrorLevel(){
        super("ERROR", 950);
    }
}

/**
 * 输出目的地为数据库的类
 * @author Administrator
 *
 */
class JdbcHandler extends Handler{

	@Override
	public void publish(LogRecord record) {
		
		StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();  
		StackTraceElement element = stackTraceElement[stackTraceElement.length - 1];  
		
		String sql = "insert into db_log(OccurTime,ThreadName,Priority,ClassName,Message) values(?,?,?,?,?)" ;
		//String sql = "insert into eb_error_log(OccurTime,ThreadName,Priority,ClassName,Message) values(?,?,?,?,?)" ;
		
		PreparedStatement pstmt = null;
		Connection conn = getConn4Ms();
		int x = 0;
		try {
			pstmt = conn.prepareStatement(sql); 
			pstmt.setString(1, new Date().toLocaleString());
			pstmt.setString(2, ""+record.getThreadID());
			pstmt.setString(3, ""+record.getLevel());
			pstmt.setString(4, record.getSourceClassName()+"."+record.getSourceMethodName()
					+":" + element.getLineNumber());
			
			StringWriter sw = new StringWriter();  
	        PrintWriter pw = new PrintWriter(sw);  
	        record.getThrown().printStackTrace(pw);  
	        pw.close();  
			
			pstmt.setString(5, sw.toString());
			x = pstmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				if(pstmt!=null){
					pstmt.close();
				}
				if(conn !=null ){
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void flush() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close() throws SecurityException {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Jdbc连接数据库
	 * @return
	 */
	public static Connection getConn4Ms(){
		// --Access 数据库路径  
		String dbpath = "D:\\TestSpace\\testLog4j\\src\\main\\resources\\Test.accdb";
		
		// --连接字符串
		//String url = "jdbc:odbc:DRIVER={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+ dbpath;
		
		//JDK1.8支持的方式
		String url = "jdbc:ucanaccess://D:\\TestSpace\\testLog4j\\src\\main\\resources\\Test.accdb";
		
		Connection conn = null;
		
		try {
			//JDK1.8不需要用到驱动
		    //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		    conn = DriverManager.getConnection(url,"","");
		    System.out.println("JDK1.8连接access数据库成功！");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return conn;
	}
	
	/**
	 * 连接mysql数据库
	 * @return
	 */
	public static Connection getConn4Mysql(){
		String url = "jdbc:mysql://192.168.88.21:3306/e_book";
		String username = "super";
		String password = "super";
		
		Connection conn = null;
		try {

			Class.forName("com.mysql.jdbc.Driver");
		    conn = DriverManager.getConnection(url,username,password);
		} catch (ClassNotFoundException e) {
		     e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return conn;
	}
}
