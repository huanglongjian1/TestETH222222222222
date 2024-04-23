package com.example.mylibrary_eth;

import android.util.Log;

public class Loge {
    private static boolean cancel = false;

    public static void setLogeCancel(boolean cancel) {
        Loge.cancel = cancel;
    }

    public static void e(String s) {
        if (cancel) return;
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.e(simpleClassName + "*" + methodName + "======", s);
    }

    public static void v(String s) {
        if (cancel) return;
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.v(simpleClassName + "*" + methodName + "======", s);
    }

    public static void d(String s) {
        if (cancel) return;
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.d(simpleClassName + "*" + methodName + "======", s);
    }

    public static void i(String s) {
        if (cancel) return;
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.i(simpleClassName + "*" + methodName + "======", s);
    }

    public static void w(String s) {
        if (cancel) return;
        String className = Thread.currentThread().getStackTrace()[3].getClassName();
        String simpleClassName = className.substring(className.lastIndexOf(".") + 1);
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        Log.w(simpleClassName + "*" + methodName + "======", s);
    }
}
