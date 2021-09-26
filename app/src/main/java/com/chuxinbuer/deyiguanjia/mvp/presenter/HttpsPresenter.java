package com.chuxinbuer.deyiguanjia.mvp.presenter;

import android.content.DialogInterface;

import com.alibaba.fastjson.JSON;
import com.chuxinbuer.deyiguanjia.config.Constant;
import com.chuxinbuer.deyiguanjia.http.Api.ApiUtils;
import com.chuxinbuer.deyiguanjia.http.call.FileUploadObserver;
import com.chuxinbuer.deyiguanjia.http.call.UploadFileRequestBody;
import com.chuxinbuer.deyiguanjia.http.exception.ApiException;
import com.chuxinbuer.deyiguanjia.http.exception.ExceptionEngine;
import com.chuxinbuer.deyiguanjia.http.observer.HttpRxObservable;
import com.chuxinbuer.deyiguanjia.http.observer.HttpRxObserver;
import com.chuxinbuer.deyiguanjia.mvp.view.iface.IBaseView;
import com.chuxinbuer.deyiguanjia.utils.LogUtils;
import com.chuxinbuer.deyiguanjia.utils.ToastUtil;
import com.chuxinbuer.deyiguanjia.widget.RLoadingDialog;
import com.trello.rxlifecycle2.android.ActivityEvent;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;

/**
 * 登录Presenter
 */

public class HttpsPresenter extends BasePresenter<IBaseView, RxAppCompatActivity> {
    private RLoadingDialog mLoadingDialog;
    String p3DesJsonPara = "";

    private boolean isShowDialog = true;
    private RxAppCompatActivity mActivity;

    private boolean isOtherLogin = false;

    public HttpsPresenter(IBaseView view, RxAppCompatActivity activity) {
        super(view, activity);
        mActivity = activity;
        mLoadingDialog = new RLoadingDialog(mActivity, true);
    }


    public HttpsPresenter(IBaseView view, RxAppCompatActivity activity, boolean show) {
        super(view, activity);
        mActivity = activity;
        isShowDialog = show;
    }


    public void request(Map<String, String> model, String url, final boolean show) {
        isShowDialog = show;
        execute(model, url,url);
    }

    public void request(Map<String, String> model, String url) {
        isShowDialog = true;
        execute(model, url,url);
    }

    public void request(Map<String, String> model, String url, String tag) {
        execute(model, url, tag);
    }

    public void request(Map<String, String> model, String url, String tag, boolean show) {
        this.isShowDialog = show;
        execute(model, url, tag);
    }

    public void execute(Map<String, String> model, String url,String tag) {
        //构建请求数据
        final HttpRxObserver httpRxObserver = new HttpRxObserver(tag) {

            @Override
            protected void onStart(Disposable d) {
                if (isShowDialog) {
                    if (mLoadingDialog != null && !mActivity.isFinishing())
                        mLoadingDialog.show();
                }
            }

            @Override
            protected void onError(ApiException e, String mTag) {
                LogUtils.w("onError code:" + e.getCode() + " msg:" + e.getMsg());
                if (isShowDialog) {
                    if (mLoadingDialog != null && mLoadingDialog.isShowing() && !mActivity.isFinishing()) {
                        mLoadingDialog.dismiss();
                    }
                }
                if (getView() != null)
                    getView().showResult(e.getCode(), e.getMsg(), mTag);
                if(mTag.equals(Constant.LOGIN_SCAN)){

                }else {
                    ToastUtil.showShort(e.getMsg());
                }
            }

            @Override
            protected void onSuccess(Object response, String mTag) {
                LogUtils.w("onSuccess response:" + response.toString());
                if (isShowDialog) {
                    if (mLoadingDialog != null && mLoadingDialog.isShowing() && !mActivity.isFinishing()) {
                        mLoadingDialog.dismiss();
                    }
                }
                if (getView() != null)
                    getView().showResult(ExceptionEngine._SUCCESS, response.toString(), mTag);
            }
        };

        /**
         * 切入后台移除RxJava监听
         * ActivityEvent.PAUSE(FragmentEvent.PAUSE)
         * 手动管理移除RxJava监听,如果不设置此参数默认自动管理移除RxJava监听（onCrete创建,onDestroy移除）
         */

        LogUtils.e("mParams=" + JSON.toJSONString(model));
        HttpRxObservable.getObservable(ApiUtils.getUserApi().postRequest(url, model), getActivity(), ActivityEvent.PAUSE, tag).subscribe(httpRxObserver);
        mLoadingDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //取消请求
                if (!httpRxObserver.isDisposed()) {
                    httpRxObserver.cancel();
                    if (getView() != null)
                        getView().showResult(ExceptionEngine._CONNECT_FAIL, "newwork_cancel", "");
                }
            }
        });
    }

    /**
     * 单上传文件的封装
     *
     * @param url                完整的接口地址
     * @param fileList           需要上传的文件集合
     * @param fileUploadObserver 上传回调
     */
    public void upLoadFile(String url, List<File> fileList, FileUploadObserver<ResponseBody> fileUploadObserver) {
        Map<String, UploadFileRequestBody> params = new HashMap<>();
//        params.put("pParam", convertToRequestBody(p3DesJsonPara, fileUploadObserver));
        List<MultipartBody.Part> partList = filesToMultipartBodyParts(fileList, fileUploadObserver);
        ApiUtils.getUserApi()
                .uploadFile(url, params, partList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(fileUploadObserver);
    }


    private UploadFileRequestBody convertToRequestBody(String param, FileUploadObserver<ResponseBody> fileUploadObserver) {
        UploadFileRequestBody requestBody = new UploadFileRequestBody(param, fileUploadObserver);
        return requestBody;
    }

    private List<MultipartBody.Part> filesToMultipartBodyParts(List<File> files, FileUploadObserver<ResponseBody> fileUploadObserver) {
        List<MultipartBody.Part> parts = new ArrayList<>(files.size());
        for (File file : files) {
            UploadFileRequestBody requestBody = new UploadFileRequestBody(file, fileUploadObserver);
            MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
            parts.add(part);
        }
        return parts;
    }
}
