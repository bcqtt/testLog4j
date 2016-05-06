package com.lz.test.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.mail.MessagingException;

public class Log4jUtils2 {
	
	private static String LEVEL_DEBUG = "DEBUG";  
    private static String LEVEL_INFO = "INFO";  
    private static String LEVEL_WARN = "WARN";  
    private static String LEVEL_ERROR = "ERROR";
    
    private Level logOutPutLevel;  
    private boolean HANDLER_CONSOLE = false; 
    private boolean HANDLER_FILE = false; 
    private boolean HANDLER_EMAIL = false; 
    
    private static Log4jUtils2 INSTANCE = new Log4jUtils2();
    private static ArrayList<String> logList = new ArrayList<String>();  
    SimpleDateFormat format = new SimpleDateFormat();
    
    private Log4jUtils2() {  
        setLogLevel("INFO");  
  
        HANDLER_CONSOLE = true; //控制台输出  
        HANDLER_FILE = false;   //文件输出 
        HANDLER_EMAIL = true;   //发送邮件
  
        //如果不想使用文件打LOG，即 HANDLER_FILE = false;，则不用关心以下数据  
        //自动创建路径文件夹  
//        if (HANDLER_FILE) {  
//  
//            /**
//             * 1. LOG文件夹在class文件夹目录下  
//             * LOG_FILE_PATH = new File(Log.class.getClassLoader().getResource(".").getFile()).toString() + "/log/";
//             * 2. 如果是web项目，LOG文件夹在项目根目录下  
//             */
//            LOG_FILE_PATH = new File(Log4jUtils.class.getClassLoader().getResource("/").getFile()).toString() + "/../../log/";  
//  
//            FILE_SIZE = 2000000; //每个文件最大2M  
//            FILE_MAX = 10;       //每天最多生成10个文件,最多不可超过100  
//  
//            LOG_FILE_LASTNAME = ".log"; //文件后缀名  
//        }  
    }  
    
    /** 
     * 设置输出等级 
     * @param level 自定义等级,可使用 debug,info,warn,error 忽略大小写 
     */  
    public final void setLogLevel(String level) {  
        logOutPutLevel = getLogLevelFromString(level);  
    }  
    
    /**
     * 异常信息输出到控制台
     * @param errorInfo  异常信息
     * @throws MessagingException 
     * @throws IOException 
     */
    public static void errorToConsole(String errorInfo) throws MessagingException, IOException{
    	INSTANCE.log(LEVEL_ERROR, "[" + LEVEL_ERROR + "] " + errorInfo, null);  
    }
    
    /**
     * 异常信息发送邮件
     * @param errorInfo  异常信息
     * @throws MessagingException 
     * @throws IOException 
     */
    public static void errorToEmail(String errorInfo) throws MessagingException, IOException{
    	INSTANCE.log(LEVEL_ERROR, "[" + LEVEL_ERROR + "] " + errorInfo, null);  
    }
    
    /** 
     * 处理记录信息 
     * @param level　信息等级 
     * @param info 　信息内容 
     * @param exception 　异常信息 
     * @throws MessagingException 
     * @throws IOException 
     */  
    private void log(String level, String info, Throwable exception) throws MessagingException, IOException {  
        Logger logger = Logger.getLogger(level);  
        if (!logList.contains(level)) {  
            logList.add(level);  
            dealHander(logger);  
            dealInfoFormat(logger);  
        }  
        logger.setLevel(logOutPutLevel);
        logger.log(getLogLevelFromString(level), info, exception);
    }  
    
    /** 
     * 输出日志
     * @param logger jdk log 
     * @throws MessagingException 
     * @throws IOException 
     */  
    private void dealHander(Logger logger) throws MessagingException, IOException {  
    	
        boolean alreadyHasConsole = false;
        boolean alreadyHasEmail = false;
        
        Handler[] handlers = logger.getHandlers();  
        for (int i = 0; i < handlers.length; i++) {  
            if (handlers[i] instanceof ConsoleHandler) {  
                alreadyHasConsole = true;  
            }
        }  
  
        if (HANDLER_CONSOLE && !alreadyHasConsole) {
            ConsoleHandler ch = new ConsoleHandler();  
            ch.setLevel(logOutPutLevel);  
            logger.addHandler(ch);  
        }
        
//        if (HANDLER_FILE && !alreadyHasEmail) {  
//        	EmailHandler eh = new EmailHandler();
//        	eh.setLevel(logOutPutLevel);
//            logger.addHandler(eh);  
//        }
    }  
 
    /** 
     * 内容格式化，默认内容格式为　时间 文件名(行数):[等级] 内容 
     * @param logger JDK Log 
     */  
    private void dealInfoFormat(Logger logger) {  
        logger.setUseParentHandlers(false);  
  
        StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();  
  
        final StackTraceElement element = stackTraceElement[stackTraceElement.length - 1];  
  
        for (Handler handler : logger.getHandlers()) {  
            handler.setFormatter(new Formatter() {  
  
                @Override  
                public String format(LogRecord record) {  
                    StringBuilder formatMessage = new StringBuilder();  
                    formatMessage.append(getDateFormat("yyyy-MM-dd HH:mm:ss", new Date()));  
                    formatMessage.append(" ").append(element.getFileName()).append("(Line:")  
                                    .append(element.getLineNumber()).append(")").append("\n")  
                                    .append(record.getMessage()).append("\n");  
  
                    if (record.getThrown() != null) {  
                        try {  
                            StringWriter sw = new StringWriter();  
                            PrintWriter pw = new PrintWriter(sw);  
                            record.getThrown().printStackTrace(pw);  
                            pw.close();  
                            formatMessage.append(sw.toString());  
                        } catch (Exception ex) {  
                        }  
                    }
  
                    return formatMessage.toString();  
                }  
            });  
        }  
  
    }
    
    /** 
     * 格式化时间到字符串 
     * @param pattern　格式 
     * @param date　时间 
     * @return 格式化的字符串 
     */  
    private String getDateFormat(String pattern, Date date) {  
        format.applyPattern(pattern);  
        return format.format(date);  
    }
    
    /** 
     * 从字符串转换为JDKLOG的LEVEL等级 
     * @param strLevel　自定义的等级字符串 
     * @return JDKLOG的等级 
     */  
    private Level getLogLevelFromString(String strLevel) {  
        Level l = Level.INFO;  
  
        if (LEVEL_DEBUG.equalsIgnoreCase(strLevel)) {  
            l = Level.FINE;  
        }  
  
        if (LEVEL_WARN.equalsIgnoreCase(strLevel)) {  
            l = Level.WARNING;  
        }  
  
        if (LEVEL_ERROR.equalsIgnoreCase(strLevel)) {  
            l = Level.SEVERE;  
        }  
  
        return l;  
    }  

}
