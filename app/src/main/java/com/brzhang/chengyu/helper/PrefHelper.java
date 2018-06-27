package com.brzhang.chengyu.helper;

import android.content.Context;

import com.pixplicity.easyprefs.library.Prefs;

public class PrefHelper {

    private static PrefHelper prefHelper;

    private Context context;

    private static final String KEY_VERSION = "com.brzhang.chengyu.helper.PrefHelper.VERSION";
    private static final String KEY_INDEX = "com.brzhang.chengyu.helper.PrefHelper.INDEX";
    private static final String KEY_REQUEST_ID = "com.brzhang.chengyu.helper.PrefHelper.REQUEST_ID";

    private PrefHelper() {
    }

    public static PrefHelper getInstance() {
        if (prefHelper == null) {
            synchronized (PrefHelper.class) {
                if (prefHelper == null) {
                    prefHelper = new PrefHelper();
                }
            }
        }
        return prefHelper;
    }

    /**
     * 拿到当前版本
     *
     * @return
     */
    public int getVersion() {
        return Prefs.getInt(KEY_VERSION, 0);
    }

    /**
     * 当前关卡数
     *
     * @return
     */
    public int getIndex() {
        return Prefs.getInt(KEY_INDEX, 1);
    }


    /**
     * 设置题库版本
     *
     * @param version
     */
    public void setVersion(int version) {
        Prefs.putInt(KEY_VERSION, version);
    }

    /**
     * 设置关卡
     *
     * @param index
     */
    public void setIndex(int index) {
        Prefs.putInt(KEY_INDEX, index);
    }

    /**
     * 下载题目的request id
     *
     * @param request
     */
    public void setRequestId(long request) {
        Prefs.putLong(KEY_REQUEST_ID, request);
    }

    public long getRequestId() {
        return Prefs.getLong(KEY_REQUEST_ID, 0L);
    }
}
