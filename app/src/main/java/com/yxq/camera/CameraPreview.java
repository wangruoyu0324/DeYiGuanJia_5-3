//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.yxq.camera;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.view.SurfaceHolder.Callback;
import android.view.View.MeasureSpec;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class CameraPreview extends FrameLayout {
    private int mPreviewWidth;
    private int mPreviewHeight;
    private SurfaceView mSurfaceView;
    private View mDebugView;

    public CameraPreview(Context context) {
        this(context, (AttributeSet)null);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CameraPreview(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mPreviewWidth = -1;
        this.mPreviewHeight = -1;
        this.mSurfaceView = null;
        this.mDebugView = null;
        this.initViews();
    }

    public int getScreenOrientation() {
        return this.getResources().getConfiguration().orientation;
    }

    public void updatePreviewSize(int previewWidth, int previewHeight) {
        this.mPreviewWidth = previewWidth;
        this.mPreviewHeight = previewHeight;
        this.requestLayout();
    }

    public void drawRect(int color, Rect rect) {
        if (rect != null && rect.left < rect.right && rect.top < rect.bottom) {
            LayoutParams params;
            if (this.mDebugView == null) {
                this.mDebugView = new View(this.getContext());
                this.mDebugView.setBackgroundColor(color);
                params = new LayoutParams(-2, -2);
                params.width = rect.right - rect.left;
                params.height = rect.bottom - rect.top;
                params.leftMargin = rect.left;
                params.topMargin = rect.top;
                this.addView(this.mDebugView, params);
            } else {
                this.mDebugView.setBackgroundColor(color);
                params = (LayoutParams)this.mDebugView.getLayoutParams();
                params.width = rect.right - rect.left;
                params.height = rect.bottom - rect.top;
                params.leftMargin = rect.left;
                params.topMargin = rect.top;
                this.updateViewLayout(this.mDebugView, params);
            }
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mPreviewWidth >= 0 && this.mPreviewHeight >= 0) {
            int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
            int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
            int calculatedHeight = originalWidth * this.mPreviewWidth / this.mPreviewHeight;
            int finalWidth;
            int finalHeight;
            if (calculatedHeight < originalHeight) {
                finalWidth = originalHeight * this.mPreviewHeight / this.mPreviewWidth;
                finalHeight = originalHeight;
            } else {
                finalWidth = originalWidth;
                finalHeight = calculatedHeight;
            }

            super.onMeasure(MeasureSpec.makeMeasureSpec(finalWidth, 1073741824), MeasureSpec.makeMeasureSpec(finalHeight, 1073741824));
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    void addSurfaceCallback(Callback callback) {
        this.mSurfaceView.getHolder().addCallback(callback);
    }

    private void initViews() {
        this.mSurfaceView = new SurfaceView(this.getContext());
        this.addView(this.mSurfaceView);
    }
}
