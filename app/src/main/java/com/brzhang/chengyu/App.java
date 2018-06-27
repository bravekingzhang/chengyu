package com.brzhang.chengyu;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;

import com.brzhang.chengyu.model.DaoMaster;
import com.brzhang.chengyu.model.DaoSession;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.greendao.database.Database;


public class App extends Application {

    static Context mContext;
    public static final boolean ENCRYPTED = true;

    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, ENCRYPTED ? "notes-db-encrypted" : "notes-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public static Context getContext() {
        return mContext;
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
