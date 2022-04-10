/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.database;

import android.util.Log;

import com.chuxinbuer.deyiguanjia.MyApplication;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;


public class CommonLog {
    private String tag = "CommonLog";
    public static int logLevel = Log.VERBOSE;
    public static boolean isDebug = true;
    public static boolean openlogfile = false;
    private LogWriter logwriter = null;
    private String logPath;

    public CommonLog() {
        if (openlogfile) {
            logPath = MyApplication.mContext.getExternalCacheDir() + "/log.txt";
            if (logwriter == null) {
                logwriter = new LogWriter(logPath);
            }
        }
    }

    public CommonLog(String tag) {
        this.tag = tag;
        if (openlogfile) {
            logPath = MyApplication.mContext.getExternalCacheDir() + "/log.txt";
            if (logwriter == null) {
                logwriter = new LogWriter(logPath);
            }
        }
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private String getFunctionName() {
        StackTraceElement[] sts = Thread.currentThread().getStackTrace();

        if (sts == null) {
            return null;
        }


        for (StackTraceElement st : sts) {
            if (st.isNativeMethod()) {
                continue;
            }

            if (st.getClassName().equals(Thread.class.getName())) {
                continue;
            }

            if (st.getClassName().equals(this.getClass().getName())) {
                continue;
            }

            return "[" + Thread.currentThread().getId() + ": " + st.getFileName() + ":" + st.getLineNumber() + "]";
        }

        return null;
    }

    public void info(Object str) {
        if (logLevel <= Log.INFO) {
            String name = getFunctionName();
            String ls = (name == null ? str.toString() : (name + " - " + str));
            Log.i(tag, ls);
            if (openlogfile) {
                logwriter.print(ls);
            }
        }
    }

    public void i(Object str) {
        if (isDebug) {
            info(str);
        }
    }

    public void verbose(Object str) {
        if (logLevel <= Log.VERBOSE) {
            String name = getFunctionName();
            String ls = (name == null ? str.toString() : (name + " - " + str));
            Log.v(tag, ls);
            if (openlogfile) {
                logwriter.print(ls);
            }
        }
    }

    public void v(Object str) {
        if (isDebug) {
            verbose(str);
        }
    }

    public void warn(Object str) {
        if (logLevel <= Log.WARN) {
            String name = getFunctionName();
            String ls = (name == null ? str.toString() : (name + " - " + str));
            Log.w(tag, ls);
            if (openlogfile) {
                logwriter.print(ls);
            }
        }
    }

    public void w(Object str) {
        if (isDebug) {
            warn(str);
        }
    }

    public void error(Object str) {
        if (logLevel <= Log.ERROR) {
            String name = getFunctionName();
            String ls = (name == null ? str.toString() : (name + " - " + str));
            Log.e(tag, ls);
            if (openlogfile) {
                logwriter.print(ls);
            }
        }
    }

    public void error(Exception ex) {
        if (logLevel <= Log.ERROR) {
            StringBuffer sb = new StringBuffer();
            String name = getFunctionName();
            StackTraceElement[] sts = ex.getStackTrace();

            if (name != null) {
                sb.append(name + " - " + ex + "\r\n");
            } else {
                sb.append(ex + "\r\n");
            }

            if (sts != null && sts.length > 0) {
                for (StackTraceElement st : sts) {
                    if (st != null) {
                        sb.append("[ " + st.getFileName() + ":" + st.getLineNumber() + " ] " + st.toString() + "\r\n");
                    }
                }
            }

            Log.e(tag, sb.toString());
            if (openlogfile) {
                logwriter.print(sb.toString());
            }
        }
    }

    public void e(Object str) {
        if (isDebug) {
            error(str);
        }
    }

    public void e(Exception ex) {
        if (isDebug) {
            error(ex);
        }
    }

    public void debug(Object str) {
        if (logLevel <= Log.DEBUG) {
            String name = getFunctionName();
            String ls = (name == null ? str.toString() : (name + " - " + str));
            Log.d(tag, ls);
            if (openlogfile) {
                logwriter.print(ls);
            }
        }
    }

    public void d(Object str) {
        if (isDebug) {
            debug(str);
        }
    }

    public class LogWriter {

        private String mPath;

        private Writer mWriter;

        private LogWriter(String file_path) {
            try {
                this.mPath = file_path;
                mWriter = new BufferedWriter(new FileWriter(file_path), 2048);
            } catch (Exception e) {
                Log.e("IO EXCEPTION", e.getStackTrace().toString());
            }
        }


        public void close() throws IOException {
            mWriter.close();
        }

        public void print(String log) {
            try {
                mWriter.write(log);
                mWriter.write("\n");
                mWriter.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}