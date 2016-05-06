package com.lz.test.util;

import java.io.BufferedOutputStream;  
import java.io.File;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.OutputStream;  
import java.io.PrintWriter;  
import java.io.StringWriter;  
import java.nio.channels.FileChannel;  
import java.nio.channels.FileLock;  
import java.security.AccessController;  
import java.security.PrivilegedAction;  
import java.text.SimpleDateFormat;  
import java.util.ArrayList;  
import java.util.Date;  
import java.util.logging.ConsoleHandler;  
import java.util.logging.ErrorManager;  
import java.util.logging.Formatter;  
import java.util.logging.Handler;  
import java.util.logging.Level;  
import java.util.logging.LogManager;  
import java.util.logging.LogRecord;  
import java.util.logging.Logger;  
import java.util.logging.StreamHandler;  
  
public class Log4jUtils {  
    	
	private static String LOG_FILE_PATH;  
    private Level logOutPutLevel;  
    private int FILE_SIZE;  
    private int FILE_MAX;  
    private boolean HANDLER_FILE = false;  
    private boolean HANDLER_CONSOLE = false;  
    private static ArrayList<String> logList = new ArrayList<String>();  
    private String LOG_FILE_LASTNAME;  
    private static String LEVEL_DEBUG = "DEBUG";  
    private static String LEVEL_INFO = "INFO";  
    private static String LEVEL_WARN = "WARN";  
    private static String LEVEL_ERROR = "ERROR";  
    private static Log4jUtils INSTANCE = new Log4jUtils();  
    SimpleDateFormat format = new SimpleDateFormat();  
  
    private Log4jUtils() {  
        setLogLevel("INFO");  
  
        HANDLER_CONSOLE = true; //控制台输出  
        HANDLER_FILE = false;   //文件输出  
  
        //如果不想使用文件打LOG，即 HANDLER_FILE = false;，则不用关心以下数据  
        //自动创建路径文件夹  
        if (HANDLER_FILE) {  
  
            /**
             * 1. LOG文件夹在class文件夹目录下  
             * LOG_FILE_PATH = new File(Log.class.getClassLoader().getResource(".").getFile()).toString() + "/log/";
             * 2. 如果是web项目，LOG文件夹在项目根目录下  
             */
            LOG_FILE_PATH = new File(Log4jUtils.class.getClassLoader().getResource("/").getFile()).toString() + "/../../log/";  
  
            FILE_SIZE = 2000000; //每个文件最大2M  
            FILE_MAX = 10;       //每天最多生成10个文件,最多不可超过100  
  
            LOG_FILE_LASTNAME = ".log"; //文件后缀名  
        }  
    }  
  
    //以下内容不必更改
    private String getLogFileName(String level) {  
        String firtFileName = LOG_FILE_PATH + level + "_" + getDateFormat("yyyy-MM-dd", new Date()) + LOG_FILE_LASTNAME;  
        return firtFileName;  
    }  
  
    /** 
     * 从字符串转换为JDKLOG的LEVEL等级 
     *  
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
  
    /** 
     * 低等级记录 
     * @param info 记录信息 
     */  
    public static void debug(String info) {  
        INSTANCE.log(LEVEL_DEBUG, "[" + LEVEL_DEBUG + "] " + info, null);  
    }  
  
    /** 
     * 普通信息记录 
     * @param info 记录信息 
     */  
    public static void info(String info) {  
        INSTANCE.log(LEVEL_INFO, "[" + LEVEL_INFO + "] " + info, null);  
    }  
  
    /** 
     * 警告信息记录 
     * @param info 记录信息 
     */  
    public static void warn(String info) {  
        INSTANCE.log(LEVEL_WARN, "[" + LEVEL_WARN + "] " + info, null);  
    }  
  
    /** 
     * 错误信息记录 
     * @param info 记录信息 
     */  
    public static void error(String info) {  
        INSTANCE.log(LEVEL_ERROR, "[" + LEVEL_ERROR + "] " + info, null);  
    }  
  
    /** 
     * 错误信息记录 
     * @param info 异常 
     */  
    public static void error(Throwable info) {  
        INSTANCE.log(LEVEL_ERROR, "", info);  
    }  
  
    /** 
     * 处理记录信息 
     * @param level　信息等级 
     * @param info 　信息内容 
     * @param exception 　异常信息 
     */  
    private void log(String level, String info, Throwable exception) {  
        checkFileDate(level);  
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
     * 输出处理，控制台和文件,文件输出的文件名格式为 等级_yyyy-MM-dd.log 如果有多个文件，会在文件名后递增数字 
     * @param logger jdk log 
     */  
    private void dealHander(Logger logger) {  
        boolean alreadyHasConsole = false, alreadyHasFile = false;  
        Handler[] handlers = logger.getHandlers();  
        for (int i = 0; i < handlers.length; i++) {  
            if (handlers[i] instanceof ConsoleHandler) {  
                alreadyHasConsole = true;
            } else if (handlers[i] instanceof MyFileHandler) {  
                alreadyHasFile = true;  
            }  
        }  
  
        if (HANDLER_CONSOLE && !alreadyHasConsole) {  
            ConsoleHandler ch = new ConsoleHandler();  
            ch.setLevel(logOutPutLevel);  
            logger.addHandler(ch);
        }
  
        if (HANDLER_FILE && !alreadyHasFile) {  
  
            String file = getLogFileName(logger.getName());  
  
            File f = new File(LOG_FILE_PATH);  
            if (!f.isDirectory()) {  
                f.mkdirs();  
                f.mkdir();  
            }  
            try {  
                MyFileHandler fh = new MyFileHandler(file, FILE_SIZE, FILE_MAX, true);  
                fh.setLevel(logOutPutLevel);
                logger.addHandler(fh);  
            } catch (IOException ex) {  
                Logger.getLogger(Log4jUtils.class.getName()).log(Level.SEVERE, null, ex);  
            } catch (SecurityException ex) {  
                Logger.getLogger(Log4jUtils.class.getName()).log(Level.SEVERE, null, ex);  
            }  
  
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
     * 设置输出等级 
     * @param level 自定义等级,可使用 debug,info,warn,error 忽略大小写 
     */  
    public final void setLogLevel(String level) {  
        logOutPutLevel = getLogLevelFromString(level);  
    }  
  
    private void checkFileDate(String level) {  
        File f = new File(getLogFileName(level));  
        if (!f.exists()) {  
            for (Handler handler : Logger.getLogger(level).getHandlers()) {  
                handler.close();  
            }  
            logList.remove(level);  
        }  
    }  
  
}  
 