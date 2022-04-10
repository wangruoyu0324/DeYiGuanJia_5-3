package com.chuxinbuer.deyiguanjia.imagevideobanner.utils;


import com.chuxinbuer.deyiguanjia.imagevideobanner.bean.BannerBean;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by itservice on 2017/9/14.
 */

public interface OperationParser {
    /**
     * 解析输入流 得到BannerBean对象集合
     * @param is
     * @return
     */
    public List<BannerBean> parse(InputStream is) throws Exception;

    /**
     * 序列化BannerBean对象集合 得到XML形式的字符串
     * @param lists
     * @return
     */
    public String serialize(List<BannerBean> lists, OutputStream is) throws Exception;
}
