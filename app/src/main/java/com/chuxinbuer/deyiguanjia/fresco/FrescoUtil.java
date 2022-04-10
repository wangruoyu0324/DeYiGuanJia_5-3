package com.chuxinbuer.deyiguanjia.fresco;

/**
 * Created by Administrator on 2017/1/13 0013.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.chuxinbuer.deyiguanjia.MyApplication;
import com.chuxinbuer.deyiguanjia.R;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.concurrent.Executor;


/**
 * Created by snowbean on 16-7-2.
 */
public class FrescoUtil {
    private static final String TAG = FrescoUtil.class.getSimpleName();
    private Context mContext;
    private AnimationDrawable loadingAnimation;

    public FrescoUtil(Context context) {
        mContext = context;
    }

    //Fresco
    public static void display(SimpleDraweeView draweeView, String url) {
        if (TextUtils.isEmpty(url)) {
            draweeView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        GenericDraweeHierarchyBuilder builder =//用来设置加载失败，加载中，加载前图片，等效果
                new GenericDraweeHierarchyBuilder(MyApplication.mContext.getResources());
        GenericDraweeHierarchy hierarchy = builder
                .setFadeDuration(300)
                .setPlaceholderImage(R.mipmap.ic_launcher)
                .setFailureImage(R.mipmap.ic_launcher)
                //      .setOverlays(overlaysList)
                .build();
        draweeView.setHierarchy(hierarchy);
        draweeView.setImageURI(Uri.parse(url));
    }

    public static void display(SimpleDraweeView draweeView, File file) {
        if (file == null) {
            Log.e(TAG, "display: error the file is empty");
            return;
        }
        Uri uri = Uri.fromFile(file);
        if (uri == null) return;
        draweeView.setImageURI(uri);
    }

    public static void display(SimpleDraweeView draweeView, Uri uri) {
        if (uri == null) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        draweeView.setImageURI(uri);
    }

    public static void display(SimpleDraweeView draweeView, Uri uri, int width, int height, boolean isHead) {
        if (uri == null) {
            draweeView.setImageResource(R.mipmap.ic_launcher);
            return;
        }
        GenericDraweeHierarchyBuilder builder =//用来设置加载失败，加载中，加载前图片，等效果
                new GenericDraweeHierarchyBuilder(MyApplication.mContext.getResources());
        if (!isHead) {
            GenericDraweeHierarchy hierarchy = builder
                    .setFadeDuration(300)
                    .setPlaceholderImage(R.mipmap.ic_launcher)
                    .setFailureImage(R.mipmap.ic_launcher)
                    //      .setOverlays(overlaysList)
                    .build();
            draweeView.setHierarchy(hierarchy);
        }
        draweeView.setImageURI(uri);
//        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
//                .setResizeOptions(new ResizeOptions(width, height))
//                .setAutoRotateEnabled(true)
//                .setLocalThumbnailPreviewsEnabled(true)
//                .build();
//
//        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
//                .setOldController(draweeView.getController())
//                .setImageRequest(request)
//                .build();
//
//        draweeView.setController(controller);
    }


    public static void display(SimpleDraweeView draweeView, String url, int width, int height, boolean isHead) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        Uri uri = Uri.parse(url + "-" + width + "X" + height);
        display(draweeView, uri, width, height, isHead);
    }

    public static void display(SimpleDraweeView draweeView, Uri uri, boolean isHead) {
        if (uri == null) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }

        ImageRequest request;
        if (isHead) {
            request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
//                    .setImageType(ImageRequest.ImageType.SMALL)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();
        } else {
            request = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
//                    .setImageType(ImageRequest.ImageType.DEFAULT)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();
        }
        if(Fresco.newDraweeControllerBuilder()!=null){
            PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(request)
                    .build();

            draweeView.setController(controller);
        }
    }


    public static void display(SimpleDraweeView draweeView, String url, boolean isHead) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        Uri uri = Uri.parse(url);
        display(draweeView, uri, isHead);
    }

    public static void prefetchPhoto(Context context, Uri uri, int width, int height) {
        if (uri == null) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(width, height))
                .setAutoRotateEnabled(true)
                .setRequestPriority(Priority.LOW)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();

        Fresco.getImagePipeline().prefetchToDiskCache(request, context);
    }

    public static void prefetchPhoto(Context context, String url, int width, int height) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "display: error the url is empty");
            return;
        }
        Uri uri = Uri.parse(url);
        prefetchPhoto(context, uri, width, height);
    }

    private static final Executor UiExecutor = new Executor() {
        final Handler sHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable runnable) {
            sHandler.post(runnable);
        }
    };

    public static void displayImageView(final ImageView view, final String url) {
        final ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .build();
        Fresco.getImagePipeline()
                .fetchDecodedImage(request, view.getContext().getApplicationContext())
                .subscribe(new BaseBitmapDataSubscriber() {
                    @Override
                    protected void onNewResultImpl(Bitmap bitmap) {
                        if (bitmap != null && !bitmap.equals("") && !bitmap.isRecycled()) {
                            view.setImageBitmap(bitmap);
                        } else {
//                            Glide.with(view.getContext()).load(url).into(view);
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {

                    }
                }, UiExecutor);
    }

    public static void displayImageView(final ImageView view, final String url, int width, int height) {
        final ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse(url))
                .setResizeOptions(new ResizeOptions(width, height))
                .build();
        Fresco.getImagePipeline()
                .fetchDecodedImage(request, null)
                .subscribe(new BaseBitmapDataSubscriber() {
                    @Override
                    protected void onNewResultImpl(Bitmap bitmap) {
                        if (bitmap != null && !bitmap.equals("") && !bitmap.isRecycled()) {
                            view.setImageBitmap(bitmap);
                        } else {
//                            Glide.with(view.getContext()).load(url).into(view);
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {

                    }
                }, UiExecutor);
    }


    public static void displayImageView(final ImageView view, File file) {
        final ImageRequest request = ImageRequestBuilder
                .newBuilderWithSource(Uri.fromFile(file))
                .build();
        Fresco.getImagePipeline()
                .fetchDecodedImage(request, view.getContext().getApplicationContext())
                .subscribe(new BaseBitmapDataSubscriber() {
                    @Override
                    protected void onNewResultImpl(Bitmap bitmap) {
                        if (bitmap != null && !bitmap.isRecycled()) {
                            view.setImageBitmap(bitmap);
                        }
                    }

                    @Override
                    protected void onFailureImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {

                    }
                }, UiExecutor);
    }

    public static void clearCache() {
        Fresco.getImagePipeline().clearCaches();
    }


    private Bitmap changeBitmapSize(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        //设置想要的大小
        int newWidth = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_60);
        int newHeight = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_60);

        //计算压缩的比率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        //获取想要缩放的matrix
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        //获取新的bitmap
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }
}
