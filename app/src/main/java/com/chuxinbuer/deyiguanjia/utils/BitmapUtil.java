/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * <p/>
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.chuxinbuer.deyiguanjia.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.config.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import pub.devrel.easypermissions.EasyPermissions;

public class BitmapUtil {

    public static final String USERHEAD = "temp.jpg";

    private static final float RADIUS_FACTOR = 8.0f;
    private static final int TRIANGLE_WIDTH = 120;
    private static final int TRIANGLE_HEIGHT = 100;
    private static final int TRIANGLE_OFFSET = 300;

    public Bitmap processImage(Bitmap bitmap) {
        Bitmap bmp;

        bmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        BitmapShader shader = new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);

        float radius = Math.min(bitmap.getWidth(), bitmap.getHeight()) / RADIUS_FACTOR;
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(shader);

        RectF rect = new RectF(TRIANGLE_WIDTH, 0, bitmap.getWidth(), bitmap.getHeight());
        canvas.drawRoundRect(rect, radius, radius, paint);

        Path triangle = new Path();
        triangle.moveTo(0, TRIANGLE_OFFSET);
        triangle.lineTo(TRIANGLE_WIDTH, TRIANGLE_OFFSET - (TRIANGLE_HEIGHT / 2));
        triangle.lineTo(TRIANGLE_WIDTH, TRIANGLE_OFFSET + (TRIANGLE_HEIGHT / 2));
        triangle.close();
        canvas.drawPath(triangle, paint);

        return bmp;
    }

    @TargetApi(19)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else {
            return selectImage(context, uri);
        }
        return null;
    }

    public static String selectImage(Context context, Uri selectedImage) {
        if (selectedImage != null) {
            String uriStr = selectedImage.toString();
            String path = uriStr.substring(10, uriStr.length());
            if (path.startsWith("com.sec.android.gallery3d")) {
                return null;
            }
        }
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(selectedImage, filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);
        cursor.close();
        return picturePath;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically WeightCurveActivity file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        if (EasyPermissions.hasPermissions(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Cursor cursor = null;
            final String column = "_data";
            final String[] projection = {column};
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                if (cursor != null && cursor.moveToFirst()) {
                    final int index = cursor.getColumnIndexOrThrow(column);
                    return cursor.getString(index);
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        } else {
            ToastUtil.showShort("??????????????????????????????????????????");
            return null;
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    // //

    /**
     * ???????????????????????????
     *
     * @param path ????????????
     * @param w    ????????????
     * @param h    ????????????
     * @return ??????????????????
     */
    public static Bitmap createBitmap(String path, int w, int h) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            // ?????????????????????????????????inJustDecodeBounds??????true?????????????????????????????????
            BitmapFactory.decodeFile(path, opts);
            int srcWidth = opts.outWidth;// ???????????????????????????
            int srcHeight = opts.outHeight;// ????????????????????????
            int destWidth = 0;
            int destHeight = 0;
            // ???????????????
            double ratio = 0.0;
            if (srcWidth < w || srcHeight < h) {
                ratio = 0.0;
                destWidth = srcWidth;
                destHeight = srcHeight;
            } else if (srcWidth > srcHeight) {// ??????????????????????????????????????????maxLength?????????????????????????????????
                ratio = (double) srcWidth / w;
                destWidth = w;
                destHeight = (int) (srcHeight / ratio);
            } else {
                ratio = (double) srcHeight / h;
                destHeight = h;
                destWidth = (int) (srcWidth / ratio);
            }
            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            // ???????????????????????????????????????????????????????????????????????????????????????????????????inSampleSize????????????????????????????????????????????????SDK??????????????????2????????????
            newOpts.inSampleSize = (int) ratio + 1;
            // inJustDecodeBounds??????false??????????????????????????????
            newOpts.inJustDecodeBounds = false;
            // ???????????????????????????????????????????????????inSampleSize????????????????????????????????????????????????
            newOpts.outHeight = destHeight;
            newOpts.outWidth = destWidth;
            // ?????????????????????
            return BitmapFactory.decodeFile(path, newOpts);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean delUserHead() {
        File delFile = new File(DIR, USERHEAD);
        if (delFile.exists()) {
            delFile.delete();
            return true;
        } else {
            return false;
        }

    }

    /**
     * ???????????????????????????
     *
     * @param context
     * @param resId
     * @return
     */
    public static Bitmap ReadBitmapById(Context context, int resId) {
        return ReadBitmapById(context, resId, -1, -1);
    }

    public static Bitmap ReadBitmapById(Context context, int resId, int outwidth, int outheight) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        if (-1 != outwidth) {
            opt.outWidth = outwidth;
            opt.outHeight = outheight;
        }
        opt.inJustDecodeBounds = false;
        opt.inPreferredConfig = Config.RGB_565;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        // ??????????????????
        InputStream is = context.getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(is, null, opt);
    }

    public static Bitmap ReadBitmapById1(Context context, int resId) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        InputStream is = context.getResources().openRawResource(resId);
        BitmapFactory.decodeStream(is, null, opt);
        float scale = context.getResources().getDisplayMetrics().density;
        if (scale >= 1.5) {
            opt.inSampleSize = 1;
        } else {
            opt.inSampleSize = 2;
        }
        opt.inJustDecodeBounds = false;
        // ??????????????????
        return BitmapFactory.decodeStream(is, null, opt);
    }

    /***
     * ????????????????????????Bitmap
     *
     * @param context
     * @param drawableId
     * @return
     */
    public static Bitmap ReadBitmapById2(Context context, int drawableId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inInputShareable = true;
        options.inPurgeable = true;
        InputStream stream = context.getResources().openRawResource(drawableId);
        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);
        return getBitmap(bitmap, context);
    }

    public static Bitmap DrawShadowImg(Bitmap bitmap, float radius) {
        BlurMaskFilter blurFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER);
        Paint shadowPaint = new Paint();
        shadowPaint.setColor(0xff81c340);
        shadowPaint.setMaskFilter(blurFilter);
        int[] offsetXY = new int[2];
        Bitmap shadowImage = bitmap.extractAlpha(shadowPaint, offsetXY);
        Bitmap shadow = shadowImage.copy(Config.ARGB_8888, true);
        Canvas c = new Canvas(shadow);
        c.drawBitmap(bitmap, -offsetXY[0], -offsetXY[1], null);
        return shadow;
    }

    // ??????
    public static Bitmap DrawShadowImg(Bitmap bitmap) {
        return DrawShadowImg(bitmap, 3.2f);

    }

    /***
     * ?????????????????????
     *
     * @param bitmap
     * @param context
     * @return
     */
    public static Bitmap getBitmap(Bitmap bitmap, Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHight = dm.heightPixels;
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scale = (float) screenWidth / w;
        float scale2 = (float) screenHight / h;
        scale = scale < scale2 ? scale : scale2;
        // ?????????????????????.
        matrix.postScale(scale, scale);
        // w,h??????????????????.
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }


    /**
     * ???????????????????????????????????????bitmap????????????
     *
     * @param filePath
     * @return
     */
    public static Bitmap getCompileBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    /***
     * ???????????????SD???(JPG)
     *
     * @param bm
     * @param url
     * @param quantity
     */
    private static int FREE_SD_SPACE_NEEDED_TO_CACHE = 1;
    private static int MB = 1024 * 1024;
    public final static String DIR = Constant.STORE_PATH;

    public static String saveBmpToSd(Bitmap bm, int quantity) {
        String savepath = Common.getOneImagePathName();
        File file = new File(savepath);
        try {
            file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, quantity, outStream);
            outStream.flush();
            outStream.close();

            if (file != null) {
                // ??????????????????
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                MyApplication.mContext.sendBroadcast(intent);
            }

            return savepath;

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static Map<String, Object> saveBmpToSd(Bitmap bm) {//?????????????????????sd???
        String savepath = "";
        File file1 = new File(DIR);
        if (!file1.exists()) {
            file1.mkdirs();
        }
        savepath = DIR + System.currentTimeMillis() + "_cut.jpg";
        File file = new File(savepath);
        try {
            file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            outStream.flush();
            outStream.close();
            return compressImage(bm, savepath, false);

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * ???????????????SD???(PNG)
     *
     * @param bm
     * @param url
     * @param quantity
     */
    public static String saveBmpToSdPNG(Bitmap bm, String url, int quantity) {
        // ??????sdcard????????????
        if (FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()) {
            return null;
        }
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
//            Log.i("SDcard", "SDcard Not Found");
            return null;
        }
        String filename = url;
//        // ????????????????????????
//        File dirPath = new File(DIR);
//        if (!dirPath.exists()) {
//            dirPath.mkdirs();
//        }

        File file = new File(filename);
        if (!file.exists()) {//?????????????????????????????????????????????(??????????????????)
            file = new File(DIR + "video" + filename);
        }
        try {
            file.createNewFile();
            OutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, quantity, outStream);
            outStream.flush();
            outStream.close();

            if (file != null) {
                // ??????????????????
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                MyApplication.mContext.sendBroadcast(intent);
            }

            return file.getAbsolutePath();

        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * LOMO??????
     *
     * @param bitmap ?????????
     * @return LOMO????????????
     */
    public static Bitmap lomoFilter(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int dst[] = new int[width * height];
        bitmap.getPixels(dst, 0, width, 0, 0, width, height);

        int ratio = width > height ? height * 32768 / width : width * 32768 / height;
        int cx = width >> 1;
        int cy = height >> 1;
        int max = cx * cx + cy * cy;
        int min = (int) (max * (1 - 0.8f));
        int diff = max - min;

        int ri, gi, bi;
        int dx, dy, distSq, v;

        int R, G, B;

        int value;
        int pos, pixColor;
        int newR, newG, newB;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pos = y * width + x;
                pixColor = dst[pos];
                R = Color.red(pixColor);
                G = Color.green(pixColor);
                B = Color.blue(pixColor);

                value = R < 128 ? R : 256 - R;
                newR = (value * value * value) / 64 / 256;
                newR = (R < 128 ? newR : 255 - newR);

                value = G < 128 ? G : 256 - G;
                newG = (value * value) / 128;
                newG = (G < 128 ? newG : 255 - newG);

                newB = B / 2 + 0x25;

                // ==========????????????==============//
                dx = cx - x;
                dy = cy - y;
                if (width > height)
                    dx = (dx * ratio) >> 15;
                else
                    dy = (dy * ratio) >> 15;

                distSq = dx * dx + dy * dy;
                if (distSq > min) {
                    v = ((max - distSq) << 8) / diff;
                    v *= v;

                    ri = (int) (newR * v) >> 16;
                    gi = (int) (newG * v) >> 16;
                    bi = (int) (newB * v) >> 16;

                    newR = ri > 255 ? 255 : (ri < 0 ? 0 : ri);
                    newG = gi > 255 ? 255 : (gi < 0 ? 0 : gi);
                    newB = bi > 255 ? 255 : (bi < 0 ? 0 : bi);
                }
                // ==========????????????end==============//

                dst[pos] = Color.rgb(newR, newG, newB);
            }
        }

        Bitmap acrossFlushBitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        acrossFlushBitmap.setPixels(dst, 0, width, 0, 0, width, height);
        return acrossFlushBitmap;
    }

    /**
     * ???????????????
     *
     * @param bmp ?????????
     * @return ?????????????????????
     */
    public static Bitmap oldTimeFilter(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        int pixColor = 0;
        int pixR = 0;
        int pixG = 0;
        int pixB = 0;
        int newR = 0;
        int newG = 0;
        int newB = 0;
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {
                pixColor = pixels[width * i + k];
                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);
                newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
                newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
                newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
                int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG,
                        newB > 255 ? 255 : newB);
                pixels[width * i + k] = newColor;
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /**
     * ????????????
     *
     * @param bmp     ?????????
     * @param centerX ???????????????
     * @param centerY ???????????????
     * @return ??????????????????
     */
    public static Bitmap warmthFilter(Bitmap bmp, int centerX, int centerY) {
        final int width = bmp.getWidth();
        final int height = bmp.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);

        int pixR = 0;
        int pixG = 0;
        int pixB = 0;

        int pixColor = 0;

        int newR = 0;
        int newG = 0;
        int newB = 0;
        int radius = Math.min(centerX, centerY);

        final float strength = 150F; // ???????????? 100~150
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int pos = 0;
        for (int i = 1, length = height - 1; i < length; i++) {
            for (int k = 1, len = width - 1; k < len; k++) {
                pos = i * width + k;
                pixColor = pixels[pos];

                pixR = Color.red(pixColor);
                pixG = Color.green(pixColor);
                pixB = Color.blue(pixColor);

                newR = pixR;
                newG = pixG;
                newB = pixB;

                // ????????????????????????????????????????????????????????????????????????????????????
                int distance = (int) (Math.pow((centerY - i), 2) + Math.pow(centerX - k, 2));
                if (distance < radius * radius) {
                    // ??????????????????????????????????????????
                    int result = (int) (strength * (1.0 - Math.sqrt(distance) / radius));
                    newR = pixR + result;
                    newG = pixG + result;
                    newB = pixB + result;
                }

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                pixels[pos] = Color.argb(255, newR, newG, newB);
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    /***
     * ?????????????????????
     *
     * @param url
     * @return
     */
    public static boolean Exist(String url) {
        File file = new File(DIR + url);
        return file.exists();
    }


    /**
     * ??????sdcard?????????????????? * @return
     */
    private static int freeSpaceOnSd() {
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double sdFreeMB = ((double) stat.getAvailableBlocks() * (double) stat.getBlockSize()) / MB;

        return (int) sdFreeMB;
    }

    public static void removeOneCache(String path) {
        File fileame = new File(path);
        if (fileame.exists()) {
            fileame.delete();
        }
    }

    public static Bitmap getimagebmpCompress(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        // ??????????????????????????????options.inJustDecodeBounds ??????true???
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// ????????????bm??????

        newOpts.inJustDecodeBounds = false;
        float w = newOpts.outWidth;
        float h = newOpts.outHeight;
        float hh = 800f;// ?????????????????????800f
        float ww = 480f;// ?????????????????????480f
        // ????????????????????????????????????????????????????????????????????????????????????????????????
        int be = 1;// be=1???????????????
        if (w > h && w > ww) {// ???????????????????????????????????????????????????
            be = (int) Math.round((double) (w / ww));
        } else if (w < h && h > hh) {// ???????????????????????????????????????????????????
            be = (int) Math.round((double) (h / hh));
        } else if (w == h) {
            be = (int) Math.round((double) (w / ww));
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;// ??????????????????
        // ??????????????????????????????????????????options.inJustDecodeBounds ??????false???
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;
    }

    public static Map<String, Object> getimageCompress(String srcPath) {//????????????  4.5M?????????90kb??????
        if (FileUtils.getFileSize(srcPath) <= 1024 * 1024) {//??????0.5M?????????
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("path", srcPath);
            return map;
        } else {
            String path1 = srcPath.substring(0, srcPath.lastIndexOf("."));
            if (path1.contains("/")) {
                String path2 = path1.split("/")[path1.split("/").length - 1];
                String savepath = Constant.STORE_PATH + path2 + "_compress.jpg";
                if (new File(savepath).exists()) {
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("path", savepath);
                    return map;
                }
            }


            BitmapFactory.Options newOpts = new BitmapFactory.Options();
            // ??????????????????????????????options.inJustDecodeBounds ??????true???
            newOpts.inJustDecodeBounds = true;
            Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);// ????????????bm??????

            newOpts.inJustDecodeBounds = false;
            float w = newOpts.outWidth;
            float h = newOpts.outHeight;
            float hh = 1280f;// ?????????????????????800f
            float ww = 720f;// ?????????????????????480f
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            int be = 1;// be=1???????????????
            if (w / h < 9 / (float) 16) {//??????????????????????????????720????????????????????????
                if (h > 13000) {
                    if (w <= ww) {
                        be = (int) Math.round((double) (h / 13000));
                    } else {
                        be = (int) Math.round((double) (ww / w));
                    }
                } else {
                    if (w <= ww) {
                        be = (int) Math.round((double) (w / ww));
                    } else {
                        be = (int) Math.round((double) (ww / w));
                    }
                }
            } else {
                if (w > h && w > ww) {// ???????????????????????????????????????????????????
                    be = (int) Math.round((double) (w / ww));
                } else if (w < h && h > hh) {// ???????????????????????????????????????????????????
                    be = (int) Math.round((double) (h / hh));
                } else if (w == h) {
                    be = (int) Math.round((double) (w / ww));
                }
            }
            if (be <= 0)
                be = 1;
            newOpts.inSampleSize = be;// ??????????????????
            // ??????????????????????????????????????????options.inJustDecodeBounds ??????false???
            bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
            if (bitmap != null) {
                return compressImage(bitmap, srcPath, false);// ?????????????????????????????????????????????
            } else {
                return null;
            }
        }
    }

    public static Map<String, Object> compressImage(Bitmap image, String path, boolean isDelete) {
        String savepath = "";
        int Maxsize = 0;
        Map<String, Object> map = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ???????????????????????????100????????????????????????????????????????????????baos???

        //??????????????????????????????????????????????????????????????????????????????
        if (path != null) {
            String fileName = new File(path).getName();
            savepath = Constant.STORE_PATH + fileName.substring(0, fileName.lastIndexOf(".")) + "_compress.jpg";
            try {
                new File(savepath).createNewFile();
                if (isDelete) {
                    File deletefile = new File(path);
                    deletefile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int options = 100;
        int baosbyte = baos.toByteArray().length / 1024;
        if (baosbyte > 1024) {//??????????????????1M??????????????????????????????????????????
            Maxsize = 1024;
            while (baos.toByteArray().length / 1024 > Maxsize) { // ?????????????????????????????????????????????Maxsize,??????????????????
                baos.reset();// ??????baos?????????ba9os
                options -= 20;// ???????????????10
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);// ????????????options%?????????????????????????????????baos???
            }
        }
        if (image != null && !image.isRecycled()) {
            image.recycle();
            image = null;
        }
        try {
            File file = new File(savepath);
            FileOutputStream fos = new FileOutputStream(savepath);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            map = new HashMap<String, Object>();
            map.put("path", savepath);
            baos.reset();
            baos.close();
            if (file != null) {
                // ??????????????????
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                MyApplication.mContext.sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static Map<String, Object> compressImage(Bitmap image, String path, boolean isDelete, int compressbyte) {
        String savepath = "";
        int Maxsize = 0;
        Map<String, Object> map = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// ???????????????????????????100????????????????????????????????????????????????baos???

        //??????????????????????????????????????????????????????????????????????????????
        if (path != null) {
            String fileName = new File(path).getName();
            savepath = Constant.STORE_PATH + fileName.substring(0, fileName.lastIndexOf(".")) + "_compress.jpg";
            try {
                new File(savepath).createNewFile();
                if (isDelete) {
                    File deletefile = new File(path);
                    deletefile.delete();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int options = 100;
        int baosbyte = baos.toByteArray().length / 200;
        if (baosbyte > compressbyte) {//??????????????????1M??????????????????????????????????????????
            Maxsize = compressbyte;
            while (baos.toByteArray().length / 200 > Maxsize) { // ?????????????????????????????????????????????Maxsize,??????????????????
                baos.reset();// ??????baos?????????ba9os
                options -= 5;// ???????????????5
                image.compress(Bitmap.CompressFormat.JPEG, options, baos);// ????????????options%?????????????????????????????????baos???
            }
        }
        if (image != null && !image.isRecycled()) {
            image.recycle();
            image = null;
        }
        try {
            File file = new File(savepath);
            FileOutputStream fos = new FileOutputStream(savepath);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            map = new HashMap<String, Object>();
            map.put("path", savepath);
            baos.reset();
            baos.close();
            if (file != null) {
                // ??????????????????
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                MyApplication.mContext.sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


    /**
     * ????????????
     *
     * @param bitmap
     * @param zf
     * @return
     */

    public static Bitmap zoom(Bitmap bitmap, float zf) {
        Matrix matrix = new Matrix();
        matrix.postScale(zf, zf);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * ????????????
     *
     * @param bitmap
     * @param wf
     * @return
     */
    public static Bitmap zoom(Bitmap bitmap, float wf, float hf) {
        Matrix matrix = new Matrix();
        matrix.postScale(wf, hf);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * ??????????????????
     *
     * @param bitmap
     * @param roundPX
     * @return
     */
    public static Bitmap getRCB(Bitmap bitmap, float roundPX) {
        // RCB means
        // Rounded
        // Corner Bitmap
        Bitmap dstbmp = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(dstbmp);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPX, roundPX, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return dstbmp;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????
     *
     * @param path ?????????????????????
     * @return ?????????????????????
     */
    public static int getBitmapDegree(String path) {
        int degree = 0;
        try {
            // ?????????????????????????????????????????????EXIF??????
            ExifInterface exifInterface = new ExifInterface(path);
            // ???????????????????????????
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static String createVideoThumbnail(String filePath, int viewId) {
        if (new File(Constant.STORE_PATH + viewId + ".jpg").exists()) {
            return Constant.STORE_PATH + viewId + ".jpg";
        } else {
            Bitmap bitmap = null;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                if (filePath.startsWith("http://")
                        || filePath.startsWith("https://")
                        || filePath.startsWith("widevine://")) {
                    retriever.setDataSource(filePath, new Hashtable<String, String>());
                } else {
                    retriever.setDataSource(filePath);
                }
                bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); //retriever.getFrameAtTime(-1);
            } catch (IllegalArgumentException ex) {
                // Assume this is WeightCurveActivity corrupt video file
                ex.printStackTrace();
            } catch (RuntimeException ex) {
                // Assume this is WeightCurveActivity corrupt video file.
                ex.printStackTrace();
            } finally {
                try {
                    retriever.release();
                } catch (RuntimeException ex) {
                    // Ignore failures while cleaning up.
                    ex.printStackTrace();
                }
            }

            if (bitmap == null) {
                return null;
            }

            return (String) compressImage(bitmap, Constant.STORE_PATH + viewId + ".jpg", false, 20).get("path");
        }
    }
}
