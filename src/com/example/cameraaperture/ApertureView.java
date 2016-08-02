package com.example.cameraaperture;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/**
 * 手指竖直滑动可以调节光圈大小；
 * 调用setApertureChangedListener设置光圈值变动监听接口；
 * @author willhua
 *
 */
public class ApertureView extends View {

    public interface ApertureChanged {
        public void onApertureChanged(float newapert);
    }

    private static final float COS_30 = 0.866025f;
    private int mCircleRadius = 250;
    private int mBladeColor = 0xFF123456;
    private int mBackgroundColor = 0xFF789456;
    private int mSpace = 20;
    private float mMaxApert = 1;
    private float mMinApert = 0.2f;
    private float mCurrentApert = 0.5f;

    private Point[] mPoints = new Point[6];
    private Bitmap mBlade;
    private Paint mPaint;
    private Path mPath;
    private ApertureChanged mApertureChanged;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mPrevX;
    private float mPrevY;

    public ApertureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPath = new Path();
        mPath.addCircle(0, 0, mCircleRadius, Path.Direction.CW);
        for (int i = 0; i < 6; i++) {
            mPoints[i] = new Point();
        }
        createBlade();
        mScaleGestureDetector = new ScaleGestureDetector(getContext(),
                new ScaleGestureDetector.OnScaleGestureListener() {

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        // TODO Auto-generated method stub
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        // TODO Auto-generated method stub
                        return true;
                    }

                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        // TODO Auto-generated method stub
                        setCurrentApert(mCurrentApert
                                * detector.getScaleFactor());
                        Log.d("lyh", "onscale " + detector.getScaleFactor());
                        return true;
                    }
                });
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.save();
        calculatePoints();
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.clipPath(mPath);
        canvas.drawColor(mBackgroundColor);

        for (int i = 0; i < 6; i++) {
            canvas.save();
            canvas.translate(mPoints[i].x, mPoints[i].y);
            canvas.rotate(-i * 60);
            canvas.drawBitmap(mBlade, 0, 0, mPaint);
            canvas.restore();
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
     //   mScaleGestureDetector.onTouchEvent(event);
        if (event.getPointerCount() > 1) {
            return false;
        }
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mPrevX = event.getX();
            mPrevY = event.getY();
            break;
        case MotionEvent.ACTION_MOVE:
            if (Math.abs((event.getX() - mPrevX)) < Math
                    .abs((event.getY() - mPrevY))) {
                float diff = (event.getX() - mPrevX) * (event.getX() - mPrevX)
                        + (event.getY() - mPrevY) * (event.getY() - mPrevY);
                diff = (float) Math.sqrt(diff) / mCircleRadius * mMaxApert;
                if (event.getY() > mPrevY) {
                    setCurrentApert(mCurrentApert - diff);
                } else {
                    setCurrentApert(mCurrentApert + diff);
                }
                mPrevX = event.getX();
                mPrevY = event.getY();
            }
            break;
        default:
            break;
        }
        return true;
    }

    private void calculatePoints() {
        int curRadius = (int) (mCurrentApert / mMaxApert * (mCircleRadius - mSpace));
        mPoints[0].x = curRadius / 2;
        mPoints[0].y = -(int) (curRadius * COS_30);
        mPoints[1].x = -mPoints[0].x;
        mPoints[1].y = mPoints[0].y;
        mPoints[2].x = -curRadius;
        mPoints[2].y = 0;
        mPoints[3].x = mPoints[1].x;
        mPoints[3].y = -mPoints[1].y;
        mPoints[4].x = -mPoints[3].x;
        mPoints[4].y = mPoints[3].y;
        mPoints[5].x = curRadius;
        mPoints[5].y = 0;
    }

    private void createBlade() {
        mBlade = Bitmap.createBitmap(mCircleRadius,
                (int) (mCircleRadius * 2 * COS_30), Config.ARGB_8888);
        Path path = new Path();
        Canvas canvas = new Canvas(mBlade);
        path.moveTo(mSpace / 2 / COS_30, mSpace);
        path.lineTo(mBlade.getWidth(), mBlade.getHeight());
        path.lineTo(mBlade.getWidth(), mSpace);
        path.close();
        canvas.clipPath(path);
        canvas.drawColor(mBladeColor);
    }

    public void setBladeColor(int bladeColor) {
        mBladeColor = bladeColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
    }
    
    public void setSpace(int space) {
        mSpace = space;
    }
    
    public void setCircleRadius(int circleRadius) {
        mCircleRadius = circleRadius;
    }

    public void setMaxApert(float maxApert) {
        mMaxApert = maxApert;
    }

    public void setMinApert(float mMinApert) {
        this.mMinApert = mMinApert;
    }

    public float getCurrentApert() {
        return mCurrentApert;
    }

    public void setCurrentApert(float currentApert) {
        if (currentApert > mMaxApert) {
            currentApert = mMaxApert;
        }
        if (currentApert < mMinApert) {
            currentApert = mMinApert;
        }
        if (mCurrentApert == currentApert) {
            return;
        }
        mCurrentApert = currentApert;
        invalidate();
        if (mApertureChanged != null) {
            mApertureChanged.onApertureChanged(currentApert);
        }
    }

    public void setApertureChangedListener(ApertureChanged listener) {
        mApertureChanged = listener;
    }

}
