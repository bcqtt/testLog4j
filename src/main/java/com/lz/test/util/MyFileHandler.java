package com.lz.test.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.ErrorManager;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/** 
 * 自定义文件输出管理类，参考JDK FileHandler实现 
 */  
class MyFileHandler extends StreamHandler {  
  
    private MeteredStream meter;  
    private boolean append;  
    private int limit; // zero => no limit.  
    private int count;  
    private String pattern;  
    private String lockFileName;  
    private FileOutputStream lockStream;  
    private File files[];  
    private static final int MAX_LOCKS = 100;  
    private static java.util.HashMap<String, String> locks = new java.util.HashMap<String, String>();  
  
    private class MeteredStream extends OutputStream {  
  
        OutputStream out;  
        int written;  
  
        MeteredStream(OutputStream out, int written) {  
            this.out = out;  
            this.written = written;  
        }  
  
        public void write(int b) throws IOException {  
            out.write(b);  
            written++;  
        }  
  
        @Override  
        public void write(byte buff[]) throws IOException {  
            out.write(buff);  
            written += buff.length;  
        }  
  
        @Override  
        public void write(byte buff[], int off, int len) throws IOException {  
            out.write(buff, off, len);  
            written += len;  
        }  
  
        @Override  
        public void flush() throws IOException {  
            out.flush();  
        }  
  
        @Override  
        public void close() throws IOException {  
            out.close();  
        }  
    }  
  
    private void open(File fname, boolean append) throws IOException {  
        int len = 0;  
        if (append) {  
            len = (int) fname.length();  
        }  
        FileOutputStream fout = new FileOutputStream(fname.toString(), append);  
        BufferedOutputStream bout = new BufferedOutputStream(fout);  
        meter = new MeteredStream(bout, len);  
        setOutputStream(meter);  
    }  
  
    public MyFileHandler() throws IOException, SecurityException {  
        openFiles();  
    }  
  
    public MyFileHandler(String pattern) throws IOException, SecurityException {  
        if (pattern.length() < 1) {  
            throw new IllegalArgumentException();  
        }  
        this.pattern = pattern;  
        this.limit = 0;  
        this.count = 1;  
        openFiles();  
    }  
  
    public MyFileHandler(String pattern, boolean append) throws IOException, SecurityException {  
        if (pattern.length() < 1) {  
            throw new IllegalArgumentException();  
        }  
        this.pattern = pattern;  
        this.limit = 0;  
        this.count = 1;  
        this.append = append;  
        openFiles();  
    }  
  
    public MyFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {  
        if (limit < 0 || count < 1 || pattern.length() < 1) {  
            throw new IllegalArgumentException();  
        }  
        this.pattern = pattern;  
        this.limit = limit;  
        this.count = count;  
        openFiles();  
    }  
  
    public MyFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {  
        if (limit < 0 || count < 1 || pattern.length() < 1) {  
            throw new IllegalArgumentException();  
        }  
        this.pattern = pattern;  
        this.limit = limit;  
        this.count = count;  
        this.append = append;  
        openFiles();  
    }  
  
    private void openFiles() throws IOException {  
        LogManager manager = LogManager.getLogManager();  
        manager.checkAccess();  
        if (count < 1) {  
            throw new IllegalArgumentException("file count = " + count);  
        }  
        if (limit < 0) {  
            limit = 0;  
        }  
  
        InitializationErrorManager em = new InitializationErrorManager();  
        setErrorManager(em);  
  
        int unique = -1;  
        for (;;) {
            unique++;  
            if (unique > MAX_LOCKS) {  
                throw new IOException("Couldn't get lock for " + pattern);  
            }  
            lockFileName = generate(pattern, 0, unique).toString() + ".lck";  
            synchronized (locks) {  
                if (locks.get(lockFileName) != null) {  
                    continue;  
                }  
                FileChannel fc;  
                try {  
                    lockStream = new FileOutputStream(lockFileName);  
                    fc = lockStream.getChannel();  
                } catch (IOException ix) {  
                    continue;  
                }  
                try {  
                    FileLock fl = fc.tryLock();  
                    if (fl == null) {  
                        continue;  
                    }  
                } catch (IOException ix) {  
                }  
                locks.put(lockFileName, lockFileName);  
                break;  
            }  
        }  
  
        files = new File[count];  
        for (int i = 0; i < count; i++) {  
            files[i] = generate(pattern, i, unique);  
        }  
        if (append) {  
            open(files[0], true);  
        } else {  
            rotate();  
        }  
        Exception ex = em.lastException;  
        if (ex != null) {  
            if (ex instanceof IOException) {  
                throw (IOException) ex;  
            } else if (ex instanceof SecurityException) {  
                throw (SecurityException) ex;  
            } else {  
                throw new IOException("Exception: " + ex);  
            }  
        }  
        setErrorManager(new ErrorManager());  
    }  
  
    private File generate(String pattern, int generation, int unique) throws IOException {  
        File file = null;  
        String word = "";  
        int ix = 0;  
        boolean sawg = false;  
        boolean sawu = false;  
        while (ix < pattern.length()) {  
            char ch = pattern.charAt(ix);  
            ix++;  
            char ch2 = 0;  
            if (ix < pattern.length()) {  
                ch2 = Character.toLowerCase(pattern.charAt(ix));  
            }  
            if (ch == '/') {  
                if (file == null) {  
                    file = new File(word);  
                } else {  
                    file = new File(file, word);  
                }  
                word = "";  
                continue;  
            } else if (ch == '%') {  
                if (ch2 == 't') {  
                    String tmpDir = System.getProperty("java.io.tmpdir");  
                    if (tmpDir == null) {  
                        tmpDir = System.getProperty("user.home");  
                    }  
                    file = new File(tmpDir);  
                    ix++;  
                    word = "";  
                    continue;  
                } else if (ch2 == 'h') {  
                    file = new File(System.getProperty("user.home"));  
                    if (isSetUID()) {  
                        throw new IOException("can't use %h in set UID program");  
                    }  
                    ix++;  
                    word = "";  
                    continue;  
                } else if (ch2 == 'g') {  
                    word = word + generation;  
                    sawg = true;  
                    ix++;  
                    continue;  
                } else if (ch2 == 'u') {  
                    word = word + unique;  
                    sawu = true;  
                    ix++;  
                    continue;  
                } else if (ch2 == '%') {  
                    word = word + "%";  
                    ix++;  
                    continue;  
                }  
            }  
            word = word + ch;  
        }  
        if (count > 1 && generation > 0 && !sawg) {  
            word = word + "." + generation;  
        }  
        if (unique > 0 && !sawu) {  
            word = word + "." + unique;  
        }  
        if (word.length() > 0) {  
            if (file == null) {  
                file = new File(word);  
            } else {  
                file = new File(file, word);  
            }  
        }  
        return file;  
    }  
  
    private synchronized void rotate() {  
        Level oldLevel = getLevel();  
        setLevel(Level.OFF);  
  
        super.close();  
        for (int i = count - 2; i >= 0; i--) {  
            File f1 = files[i];  
            File f2 = files[i + 1];  
            if (f1.exists()) {  
                if (f2.exists()) {  
                    f2.delete();  
                }  
                f1.renameTo(f2);  
            }  
        }  
        try {  
            open(files[0], false);  
        } catch (IOException ix) {  
            reportError(null, ix, ErrorManager.OPEN_FAILURE);  
  
        }  
        setLevel(oldLevel);  
    }  
  
    @Override  
    public synchronized void publish(LogRecord record) {  
        if (!isLoggable(record)) {  
            return;  
        }  
        super.publish(record);  
        flush();  
        if (limit > 0 && meter.written >= limit) {  
            AccessController.doPrivileged(new PrivilegedAction<Object>() {  
  
                public Object run() {  
                    rotate();  
                    return null;  
                }  
            });  
        }  
    }  
  
    @Override  
    public synchronized void close() throws SecurityException {  
        super.close();  
        if (lockFileName == null) {  
            return;  
        }  
        try {  
            lockStream.close();  
        } catch (Exception ex) {  
        }  
        synchronized (locks) {  
            locks.remove(lockFileName);  
        }  
        new File(lockFileName).delete();  
        lockFileName = null;  
        lockStream = null;  
    }  
  
    private static class InitializationErrorManager extends ErrorManager {  
  
        Exception lastException;
  
        @Override  
        public void error(String msg, Exception ex, int code) {  
            lastException = ex;  
        }  
    }  
  
    private static native boolean isSetUID();  
  
}  
