package com.chuxinbuer.deyiguanjia.http.Api;

import com.chuxinbuer.deyiguanjia.http.call.UploadFileRequestBody;
import com.chuxinbuer.deyiguanjia.http.retrofit.HttpResponseBody;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Url;

public interface UserApi {


    @POST
    @FormUrlEncoded
    Observable<HttpResponseBody> postRequest(@Url String url, @FieldMap Map<String, String> map);

    @Multipart
    @POST
    Observable<ResponseBody> uploadFile(@Url String url, @PartMap Map<String, UploadFileRequestBody> options, @Part List<MultipartBody.Part> parts);

    @GET("api/getVioceRange")
    Call<HttpResponseBody> getVideoTime();

//    /**
//     * 单张图片上传
//     * retrofit 2.0的上传和以前略有不同，需要借助@Multipart注解、@Part和MultipartBody实现。
//     *
//     * @param url
//     * @param file
//     * @return
//     */
//    @Multipart
//    @POST("{url}")
//    Call<HttpResult<HttpResponse>> upload(@Path("url") String url, @Part MultipartBody.Part file);
//
//    /**
//     * 多张图片上传
//     *
//     * @param map
//     * @return
//     */
//    @Multipart
//    @POST("upload/upload")
//    Call<HttpResult<HttpResponse>> upload(@PartMap Map<String, MultipartBody.Part> map);

}
