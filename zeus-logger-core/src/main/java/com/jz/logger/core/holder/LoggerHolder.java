package com.jz.logger.core.holder;

public class LoggerHolder {

    private static ThreadLocal<Boolean> IS_RECORDED = new ThreadLocal<>();

    private static ThreadLocal<Object> OLD_OBJECT = new ThreadLocal<>();

    private static ThreadLocal<Object> NEW_OBJECT = new ThreadLocal<>();

    public static void log(Object oldObject, Object newObject) {
        OLD_OBJECT.set(oldObject);
        NEW_OBJECT.set(newObject);
        IS_RECORDED.set(true);
    }

    public static Object getOldObject() {
        return OLD_OBJECT.get();
    }

    public static Object getNewObject() {
        return NEW_OBJECT.get();
    }

    public static boolean isRecorded() {
        return Boolean.TRUE.equals(IS_RECORDED.get());
    }

}
