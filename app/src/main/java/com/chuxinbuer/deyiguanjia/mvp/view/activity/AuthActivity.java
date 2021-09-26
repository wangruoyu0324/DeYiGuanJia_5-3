package com.chuxinbuer.deyiguanjia.mvp.view.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.chuxinbuer.deyiguanjia.R;
import com.chuxinbuer.deyiguanjia.face.PropertiesUtil;
import com.chuxinbuer.deyiguanjia.manager.ActivityStackManager;
import com.chuxinbuer.deyiguanjia.utils.Common;
import com.dface.api.Accredit;
import com.dface.dto.AuthInfo;
import com.dface.dto.LicenseInfoType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;


public class AuthActivity extends AppCompatActivity{
    public static final int REQUEST_CODE_AUTH = 1010;
    public static final String TAG = AuthActivity.class.getSimpleName();
    private static final int RC_HANDLE_STORAGE_PERM_MAIN = 3;
    PropertiesUtil mProp;
    private Activity instance;
    private Handler handler;
    Accredit accredit = new Accredit();

    public static void startActivityForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, AuthActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        File sdDir = Environment.getExternalStorageDirectory();//Get SD card root directory
        String configFile = sdDir.toString() + "/dface/config.properties";

        //Initial configuration file class
        mProp = PropertiesUtil.getInstance(this).setFile(configFile).init();
        instance = this;

        handler = new Handler();

        int statusCode = accredit.login();

        if( statusCode == 0 ){
            AuthInfo LicenseInfo = accredit.getInfo(LicenseInfoType.INFO_SN_LICENSE.getType());
            TextView view = (TextView)findViewById(R.id.textView_auth_code);
            view.setTextColor(Color.GREEN);
//            view.setText("Device is activated\n");
            view.setText("Device is authorized\n"+LicenseInfo.getInfo());
        }else{
            TextView view = (TextView)findViewById(R.id.textView_auth_code);
            view.setTextColor(Color.RED);
            view.setText("Not authorized, code="+statusCode);
        }


        findViewById(R.id.edit_auth_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Apply for SD card read and write permissions
                int rc = ActivityCompat.checkSelfPermission(AuthActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (rc != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(RC_HANDLE_STORAGE_PERM_MAIN);
                }

                EditText editText = (EditText)findViewById(R.id.auth_code);
                if(accredit.login() != 0){
                    String autoCode = editText.getText().toString();
                    int result = 1;
                    result = accredit.updateOnline(autoCode);

                    if(result != 0){
                        int finalResult = result;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView view = (TextView)findViewById(R.id.textView_auth_code);
                                view.setText("Authorized failed, error code:"+ finalResult);
                                view.setTextColor(Color.RED);
                            }
                        });

                    }else{
                        mProp.open();
                        mProp.writeString("AUTHCODE", autoCode);
                        mProp.commit();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView view = (TextView) findViewById(R.id.textView_auth_code);
                                view.setText("Authorized successfully");
                                editText.setCursorVisible(false);
                                editText.setFocusable(false);
                                editText.setFocusableInTouchMode(false);
                                view.setTextColor(Color.GREEN);
                            }
                        });
//                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
//                        startActivity(intent);
                    }
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView view = (TextView)findViewById(R.id.textView_auth_code);
                            view.setText("Device is authorized");
                            editText.setCursorVisible(false);
                            editText.setFocusable(false);
                            editText.setFocusableInTouchMode(false);
                        }
                    });

//                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
//                    startActivity(intent);
                }
            }
        });


        //Group authorize set LocalServer
        findViewById(R.id.edit_setLocalServer_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Apply for SD card read and write permissions
                int rc = ActivityCompat.checkSelfPermission(AuthActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (rc != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(RC_HANDLE_STORAGE_PERM_MAIN);
                }

                EditText editText = (EditText)findViewById(R.id.auth_code);
                if(accredit.login() != 0){

                    String hostName = editText.getText().toString();

                    String[] IPPort = hostName.split(":");

                    String IP = IPPort[0];
                    int port = Integer.valueOf(IPPort[1]);
                    int timeoutSeconds = 5;

                    int result = 1;

                    result = accredit.setLocalServer(IP, port, timeoutSeconds);

                    if(result != 0){
                        int finalResult = result;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView view = (TextView)findViewById(R.id.textView_auth_code);
                                view.setText("Set LocalServer failed, error code:"+ finalResult);
                                view.setTextColor(Color.RED);
                            }
                        });

                    }else{
                        mProp.open();
                        mProp.writeString("LOCALSERVER", hostName);
                        mProp.commit();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView view = (TextView) findViewById(R.id.textView_auth_code);
                                view.setText("Set LocalServer successfully");
                                editText.setCursorVisible(false);
                                editText.setFocusable(false);
                                editText.setFocusableInTouchMode(false);
                                view.setTextColor(Color.GREEN);
                            }
                        });
//                        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
//                        startActivity(intent);
                    }
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView view = (TextView)findViewById(R.id.textView_auth_code);
                            view.setText("Device is authorized");
                            editText.setCursorVisible(false);
                            editText.setFocusable(false);
                            editText.setFocusableInTouchMode(false);
                        }
                    });

                }
            }
        });



        //Get device fingerprint
        findViewById(R.id.edit_hdfpt_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Apply for SD card read and write permissions
                int rc = ActivityCompat.checkSelfPermission(AuthActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (rc != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(RC_HANDLE_STORAGE_PERM_MAIN);
                }

                EditText editText = (EditText)findViewById(R.id.auth_code);
                if(accredit.login() != 0) {
                    String authCode = editText.getText().toString();
                    if(authCode.isEmpty()){
                        TextView view = (TextView) findViewById(R.id.textView_auth_code);
                        view.setText("Activation code required to obtain device fingerprint");
                        return;
                    }

                    Accredit accredit = new Accredit();
                    AuthInfo authInfo = accredit.getRequestInfo(authCode);
                    if(authInfo.getCode() != 0){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView view = (TextView) findViewById(R.id.textView_auth_code);
                                view.setText("Failed to obtain device fingerprint,error code:" + authInfo.getCode());
                                view.setTextColor(Color.RED);
                            }
                        });
                    }else {
                        String hdpt = authInfo.getInfo();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                TextView view = (TextView)findViewById(R.id.textView_auth_code);
                                TextView showHDPT = (TextView) findViewById(R.id.tv_showhdpt);
                                view.setText("Successfully obtained device fingerprint");
                                view.setTextColor(Color.RED);
                                showHDPT.setText(hdpt);
                            }
                        });

                        File sdDir = Environment.getExternalStorageDirectory();//Get SD card root directory
                        String hdptFile = sdDir.getPath() + "/dface/request.txt";
                        try {
                            FileOutputStream fileOutputStream = new FileOutputStream(hdptFile);
                            fileOutputStream.write(hdpt.getBytes());
                            fileOutputStream.flush();
                            fileOutputStream.close();
                            mProp.open();
                            mProp.writeString("AUTHCODE", authCode);
                            mProp.commit();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    //The device is activated
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView view = (TextView)findViewById(R.id.textView_auth_code);
                            view.setText("Device is authorized");
                            editText.setCursorVisible(false);
                            editText.setFocusable(false);
                            editText.setFocusableInTouchMode(false);
                        }
                    });

//                    Intent intent = new Intent(AuthActivity.this, MainActivity.class);
//                    startActivity(intent);
                }
            }
        });



        //Offline activation
        findViewById(R.id.edit_authoffline_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Apply for SD card read and write permissions
                int rc = ActivityCompat.checkSelfPermission(AuthActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (rc != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermission(RC_HANDLE_STORAGE_PERM_MAIN);
                }

                EditText editText = (EditText)findViewById(R.id.auth_code);
                if(accredit.login() != 0) {

                    Vector<String> vecFile = new Vector<String>();

                    File sdDir = Environment.getExternalStorageDirectory();//Get SD card root directory
                    File file = new File(sdDir.getPath()+"/dface/");
                    File[] subFile = file.listFiles();

                    for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
                        // Determine if it is a folder
                        if (!subFile[iFileLength].isDirectory()) {
                            String filename = subFile[iFileLength].getName();
                            String[] token = filename.split("\\.");
                            if(token.length < 2){
                                continue;
                            }
                            String pf = token[1];
                            if("upd".equals(pf)){
                                Accredit accredit = new Accredit();
                                String updFile = subFile[iFileLength].getAbsolutePath();
                                int status = accredit.updateOffline(updFile);
                                if(status == 0){
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            TextView view = (TextView)findViewById(R.id.textView_auth_code);
                                            view.setText("Authorized succeeded");
                                            editText.setCursorVisible(false);
                                            editText.setFocusable(false);
                                            editText.setFocusableInTouchMode(false);
                                            view.setTextColor(Color.GREEN);
                                        }
                                    });

                                    Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                }else{
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            TextView view = (TextView)findViewById(R.id.textView_auth_code);
                                            view.setText("Offline authorized failed, error code:"+ status);
                                            view.setTextColor(Color.RED);
                                        }
                                    });

                                }
                            }
                        }
                    }
                }else{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            TextView view = (TextView)findViewById(R.id.textView_auth_code);
                            view.setText("Device is authorized");
                            editText.setCursorVisible(false);
                            editText.setFocusable(false);
                            editText.setFocusableInTouchMode(false);
                        }
                    });

                    Intent intent = new Intent(AuthActivity.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.back_homepage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Common.openActivity(AuthActivity.this, LoginActivity.class);
                ActivityStackManager.getManager().clearActivity();
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void requestStoragePermission(final int RC_HANDLE_CAMERA_PERM_RGB) {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");
        final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_STORAGE_PERM_MAIN);
    }
}