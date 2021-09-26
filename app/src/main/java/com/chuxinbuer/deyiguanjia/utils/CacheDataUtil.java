package com.chuxinbuer.deyiguanjia.utils;

/**
 * Created by Administrator on 2016/8/4.
 */

import com.chuxinbuer.deyiguanjia.MyApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CacheDataUtil {
    private static int CACHE_TIME = 1000 * 60 * 30;//缓存30分钟后数据失效

    public static boolean saveObject(Serializable ser, String file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = MyApplication.mContext.openFileOutput(file, MyApplication.mContext.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * 读取对象
     *
     * @param file
     * @return
     */
    public static Serializable readObject(String file) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            if (!Common.empty(MyApplication.mContext.openFileInput(file))) {
                fis = MyApplication.mContext.openFileInput(file);
                ois = new ObjectInputStream(fis);
                return (Serializable) ois.readObject();
            }
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static boolean isCacheDataFailure(String cachefile) {
        boolean failure = false;
        File data = MyApplication.mContext.getFileStreamPath(cachefile);
        if (data.exists()
                && (System.currentTimeMillis() - data.lastModified()) > 0)
            failure = true;
        else if (!data.exists())
            failure = true;
        return failure;
    }
}
