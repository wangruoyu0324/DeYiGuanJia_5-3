package com.chuxinbuer.deyiguanjia.http.Api;

import com.chuxinbuer.deyiguanjia.http.retrofit.RetrofitUtils;

/**
 * 接口工具类
 */

public class ApiUtils {

    private static UserApi userApi;

    public static UserApi getUserApi() {
        if (userApi == null) {
            userApi = RetrofitUtils.get().retrofit().create(UserApi.class);
        }
        return userApi;
    }

}
