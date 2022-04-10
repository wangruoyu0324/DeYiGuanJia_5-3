package com.chuxinbuer.deyiguanjia.fresco;

import android.graphics.Bitmap;

/**
 * 作者：wry
 * 邮箱：977649708@qq.com
 * 时间：2017/08/15 13:47
 */

public interface LoadFrescoListener {
    void onSuccess(Bitmap bitmap);

    void onFail();
}
