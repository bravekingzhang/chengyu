package com.brzhang.chengyu.helper;

import android.app.Activity;

import com.brzhang.chengyu.App;
import com.brzhang.chengyu.model.DaoSession;
import com.brzhang.chengyu.model.Subject;
import com.brzhang.chengyu.model.SubjectDao;

import java.util.List;


public class DBHelper {

    private static DBHelper dbHelper;
    private DaoSession mDaoSession;
    private SubjectDao subjectDao;

    private DBHelper(Activity activity) {
        mDaoSession = ((App) activity.getApplication()).getDaoSession();
        subjectDao = mDaoSession.getSubjectDao();
    }

    public static DBHelper getInstance(Activity activity) {
        if (dbHelper == null) {
            synchronized (DBHelper.class) {
                if (dbHelper == null) {
                    dbHelper = new DBHelper(activity);
                }
            }
        }
        return dbHelper;
    }

    /**
     * https://stackoverflow.com/questions/48028734/adding-a-list-in-realm-db-using-insert-not-working
     * <p>
     * 换greedDao，好用很多
     *
     * @param list
     */
    public void insertTimuList(List<Subject> list) {

        subjectDao.deleteAll();
        subjectDao.insertInTx(list);

    }

    public Subject get(int index) {
        List<Subject> subjects = subjectDao.queryBuilder()
                .where(SubjectDao.Properties.MIndex.eq(index))
                .limit(1).list();
        if (subjects != null && subjects.size() > 0) {
            return subjects.get(0);
        } else {
            return null;
        }
    }

    /**
     * 总数
     *
     * @return
     */
    public long count() {
        return subjectDao.count();
    }

}
