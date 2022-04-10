package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.utils.Common;

public class SplashActivity extends AppCompatActivity {

//    private Disposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Common.openActivity(SplashActivity.this, LoginActivity.class);
                SplashActivity.this.finish();
            }
        }, 1);
//        mDisposable = Observable.timer(1, TimeUnit.SECONDS)
//            .subscribe(this::jumpToMain,Throwable::printStackTrace);
    }

//    private void jumpToMain(Long aLong) {
//        startActivity(new Intent(this, LoginActivity.class));
//        finish();
//    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mDisposable != null) {
//            mDisposable.dispose();
//        }
//    }
}
