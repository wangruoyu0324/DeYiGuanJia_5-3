package com.chuxinbuer.deyiguanjia.face;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;

import com.dface.dto.FaceLandmark;

import java.text.DecimalFormat;

/**
 * Created by Pinhole.ai(小孔成像科技) on 5/10/201８.
 */

/**
 * Render face frame, 3D pose, landmarks information
 */
public class FaceOverlayView extends View {

    private Paint mPaint;
    private Paint mTextPaint;
    private Paint BoxTextPaint;
    private int mDisplayOrientation;
    private int mOrientation;
    private int previewWidth;
    private int previewHeight;
    private FaceResult[] mFaces;
    private double fps;
    private boolean isFront = false;
    private FaceLandmark[] poseMarks;
    private boolean isDrawLandMarks = false;
    private boolean isDrawTracker = false;
    private boolean isDrawLive = false;
    private boolean isDrawCover = false;

    private int[] trackBoxColor = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN, Color.RED, Color.BLACK, Color.DKGRAY, Color.GRAY, Color.MAGENTA, Color.LTGRAY};


    public FaceOverlayView(Context context) {
        super(context);
        initialize();
    }

    public FaceOverlayView(Context context, AttributeSet attrs) {
        super(context,attrs);
        initialize();
    }

    public void initialize() {
        // We want a green box around the face:
        DisplayMetrics metrics = getResources().getDisplayMetrics();

        int stroke = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, metrics);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);

        mPaint.setStrokeWidth(stroke);
        mPaint.setStyle(Paint.Style.STROKE);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setDither(true);
        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, metrics);
        mTextPaint.setTextSize(size);
        mTextPaint.setColor(Color.RED);
        mTextPaint.setStyle(Paint.Style.FILL);

        BoxTextPaint = new Paint();
        BoxTextPaint.setAntiAlias(true);
        BoxTextPaint.setDither(true);
        BoxTextPaint.setTextSize(80);
        BoxTextPaint.setColor(Color.GREEN);
        BoxTextPaint.setStyle(Paint.Style.FILL);
    }

    public void setFPS(double fps) {
        this.fps = fps;
    }

    public void setFaces(FaceResult[] faces) {
        mFaces = faces;
        invalidate();
    }

    public void setPoseMarks(FaceLandmark[] poseMarks){
        this.poseMarks = poseMarks;
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    public void setDisplayOrientation(int displayOrientation) {
        mDisplayOrientation = displayOrientation;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFaces != null && mFaces.length > 0) {

            int w = getWidth();

            float scaleX = (float) getWidth() / (float) previewWidth;
            float scaleY = (float) getHeight() / (float) previewHeight;

            canvas.save();
            RectF rectF = new RectF();
            for (FaceResult face : mFaces) {
                rectF.set(new RectF(
                        (float)face.getX()*scaleX,
                        (float)face.getY()*scaleY,
                        (float)((face.getX()+face.getWidth())*scaleX),
                        (float)((face.getY()+face.getHeight())*scaleY)
                ));

                if (isFront) {
                    float left = rectF.left;
                    float right = rectF.right;
                    rectF.left = getWidth() - right;
                    rectF.right = getWidth() - left;
                }

                if(!face.getName().isEmpty()) {
                    BoxTextPaint.setColor(face.getColor());
                    canvas.drawText(face.getName(), rectF.left, rectF.top, BoxTextPaint);
                }

                mPaint.setColor(face.getColor());

                //Render tracking id
                if(isDrawTracker){
                    int drawColor = trackBoxColor[face.getId() % trackBoxColor.length];
                    mPaint.setColor(drawColor);
                    BoxTextPaint.setColor(drawColor);
                    if(!face.getName().isEmpty()){
                        canvas.drawText(face.getName(), rectF.left, rectF.top, BoxTextPaint);
                    }else{
                        canvas.drawText("id=" + face.getId(), rectF.left, rectF.top, BoxTextPaint);
                    }
                }


                Path mPath = new Path();
                mPath.moveTo(rectF.left, rectF.top + rectF.height() / 4);
                mPath.lineTo(rectF.left, rectF.top);
                mPath.lineTo(rectF.left + rectF.width() / 4, rectF.top);
                //Upper right
                mPath.moveTo(rectF.right - rectF.width() / 4, rectF.top);
                mPath.lineTo(rectF.right, rectF.top);
                mPath.lineTo(rectF.right, rectF.top + rectF.height() / 4);
                //Bottom right
                mPath.moveTo(rectF.right, rectF.bottom - rectF.height() / 4);
                mPath.lineTo(rectF.right, rectF.bottom);
                mPath.lineTo(rectF.right - rectF.width() / 4, rectF.bottom);
                //Bottom left
                mPath.moveTo(rectF.left + rectF.width() / 4, rectF.bottom);
                mPath.lineTo(rectF.left, rectF.bottom);
                mPath.lineTo(rectF.left, rectF.bottom - rectF.height() / 4);
                canvas.drawPath(mPath, mPaint);

            }

            DecimalFormat df2 = new DecimalFormat(".##");
//            canvas.drawText("Frame/s: " + df2.format(fps) + " @ " + previewWidth + "x" + previewHeight, mTextPaint.getTextSize(), mTextPaint.getTextSize(), mTextPaint);

            //display face pose(roll, pitch, yaw) information
            if(poseMarks != null && poseMarks.length !=0 ){
                int i = 2;
                for( FaceLandmark pmark : poseMarks){
                    float text_x = mTextPaint.getTextSize();
                    float text_y = i* mTextPaint.getTextSize() + 10;
                    i++;
                    if(pmark.getPose3D() != null) {
                        double[] pose = pmark.getPose3D();
                        String showPose = String.format("yaw:%.3f pitch:%.3f roll:%.3f z:%.3f", pose[0], pose[1], pose[2], pose[5]);
                        canvas.drawText(showPose, text_x, text_y, mTextPaint);
                    }
                }
            }

            if(isDrawLandMarks){
                if(poseMarks != null && poseMarks.length != 0) {
                    for (FaceLandmark pmark : poseMarks) {
                        int left, top, right, bottom;
                        int pointCount = pmark.getLandmarks2D().length;
                        float[] drawPoint = new float[pointCount];

                        for (int i = 0; i < pointCount; i++) {
                            if( i%2 == 0)
                                drawPoint[i] = (float) pmark.getLandmarks2D()[i] * scaleX;
                            else
                                drawPoint[i] = (float) pmark.getLandmarks2D()[i] * scaleY;
                        }
                        if (isFront) {
                            for (int i = 0; i < pointCount; i++) {
                                if( i%2 == 0)
                                    drawPoint[i] = (float)(getWidth()-drawPoint[i]);
                            }
                        }
                        canvas.drawPoints(drawPoint, mPaint);
                    }
                }
            }
            canvas.restore();
        }

    }

    public void setPreviewWidth(int previewWidth) {
        this.previewWidth = previewWidth;
    }

    public void setPreviewHeight(int previewHeight) {
        this.previewHeight = previewHeight;
    }

    public void setFront(boolean front) {
        isFront = front;
    }

    public boolean isDrawLandMarks() {
        return isDrawLandMarks;
    }

    public void setDrawLandMarks(boolean drawLandMarks) {
        isDrawLandMarks = drawLandMarks;
    }

    public boolean isDrawTracker() {
        return isDrawTracker;
    }

    public void setDrawTracker(boolean drawTracker) {
        isDrawTracker = drawTracker;
    }

    public void setDrawLive(boolean drawLive) {
        isDrawLive = drawLive;
    }

    public void setPreviewSize(int width, int height){
        previewWidth = width;
        previewHeight = height;
    }
}
