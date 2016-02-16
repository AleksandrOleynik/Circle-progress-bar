package com.cleveroad.progresscircle;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.LinearInterpolator;

public class ProgressCircle extends View {

    private Paint paint;
    private Paint pDone;
    private RectF bounds;
    private  Path path;

    private ValueAnimator startAngleRotate;
    private ValueAnimator progressAnimator;
    private ValueAnimator changeSize;
    private ValueAnimator restoreSize;

    private ProgressCircleListener listener;

    private volatile float currentProgress;
    private float maxProgress;
    private int thickness;
    private int endColor;
    private int startColor;
    private int color;
    private int doneColor;
    private float startAngle;
    private float actualProgress;
    private float sweepAngle;
    private int paddingLeft;
    private int paddingTop;
    private boolean fillOval;
    private boolean showDone;
    private int size = 0;
    private boolean isStartRotation = true;

    public ProgressCircle(Context context) {
        super(context);
        init(null, 0);
    }

    public ProgressCircle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ProgressCircle(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    protected void init(AttributeSet attrs, int defStyle) {
        initAttributes(attrs, defStyle);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pDone = new Paint(Paint.ANTI_ALIAS_FLAG);
        pDone.setColor(doneColor);
        pDone.setStyle(Paint.Style.STROKE);
        pDone.setStrokeWidth(thickness);
        bounds = new RectF();
        updatePaint();
    }

    private void initAttributes(AttributeSet attrs, int defStyle) {
        final TypedArray typedArray = getContext().obtainStyledAttributes(
                attrs, R.styleable.CircularProgress, defStyle, 0);
        Resources resources = getResources();
        currentProgress = typedArray.getFloat(R.styleable.CircularProgress_progress,
                resources.getInteger(R.integer.defaultProgress));
        maxProgress = typedArray.getFloat(R.styleable.CircularProgress_maxProgress,
                resources.getInteger(R.integer.defaultMaxProgress));
        thickness = typedArray.getDimensionPixelSize(R.styleable.CircularProgress_thicknessLine,
                resources.getInteger(R.integer.defaultThickness));
        startAngle = typedArray.getFloat(R.styleable.CircularProgress_startAngle,
                resources.getInteger(R.integer.defaultStartAngle));
        showDone = typedArray.getBoolean(R.styleable.CircularProgress_showDone, false);
        startColor = typedArray.getColor(R.styleable.CircularProgress_startColor, Color.RED);
        endColor = typedArray.getColor(R.styleable.CircularProgress_startColor, Color.GREEN);
        doneColor = typedArray.getColor(R.styleable.CircularProgress_doneColor, Color.WHITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int xPad = getPaddingLeft() + getPaddingRight();
        int yPad = getPaddingTop() + getPaddingBottom();
        int width = getMeasuredWidth() - xPad;
        int height = getMeasuredHeight() - yPad;
        size = (width < height) ? width : height;
        setMeasuredDimension(size + xPad, size + yPad);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        size = (w < h) ? w : h;
        updateBounds();
    }

    private void updateBounds() {
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        bounds.set(paddingLeft + thickness, paddingTop + thickness, size - paddingLeft - thickness, size - paddingTop - thickness);
    }

    private void updatePaint() {
        paint.setColor(color);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(thickness);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        sweepAngle =  actualProgress / maxProgress * 360;
        if (fillOval) {
            path = new Path();
            canvas.drawOval(bounds, paint);
            path.moveTo(bounds.centerX() - bounds.width() / 4, bounds.centerY());
            path.lineTo(bounds.centerX(), bounds.centerY() + bounds.width() / 4);
            path.lineTo(bounds.centerX() + bounds.width() / 2 , bounds.centerY() - bounds.width() / 4);
            canvas.drawPath(path, pDone);
        } else {
            canvas.drawArc(bounds, startAngle, sweepAngle, false, paint);
        }
    }

    private void setColor(int color) {
        this.color = color;
        updatePaint();
        invalidate();
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
    }

    public float getProgress() {
        return currentProgress;
    }

    public void setProgress(final float currentProgress) {
        if(currentProgress < 0 || maxProgress < 0)
            throw new IllegalArgumentException();
        this.currentProgress = currentProgress;
        if (progressAnimator != null && progressAnimator.isRunning())
            progressAnimator.cancel();
        if(isStartRotation){
            resetAnimation();
            isStartRotation = false;
            startRotationAnimation();
        }
        progressAnimator = ValueAnimator.ofFloat(actualProgress, this.currentProgress);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new LinearInterpolator());
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                actualProgress = (Float) animation.getAnimatedValue();
                if (actualProgress >= getMaxProgress()) {
                    if(progressAnimator.isRunning() && showDone)
                        startDoneChangeSize();
                    else {
                        listener.endAnimation();
                        dropValue();
                    }
                    startAngleRotate.cancel();
                    progressAnimator.cancel();
                }
                setColor(interpolateColor(startColor, endColor, (actualProgress / getMaxProgress())));
            }
        });
        progressAnimator.start();
    }

    private void startDoneChangeSize() {
        if (changeSize != null && changeSize.isRunning())
            changeSize.cancel();
        changeSize = ValueAnimator.ofFloat(0, (size/2)-(thickness));
        changeSize.setDuration(1000);
        changeSize.setInterpolator(new AnticipateOvershootInterpolator(0.75f));
        changeSize.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float actualSize = (float) animation.getAnimatedValue();
                bounds.set(paddingLeft + thickness + actualSize, paddingTop + thickness + actualSize,
                        size - paddingLeft - thickness - actualSize, size - paddingTop - thickness - actualSize);
                invalidate();
            }
        });
        changeSize.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                startDoneRestoreSize();
                changeSize.cancel();
            }
        });
        changeSize.start();
    }

    private void startDoneRestoreSize() {
        fillOval = true;
        paint.setStyle(Paint.Style.FILL);
        if (restoreSize != null && restoreSize.isRunning())
            restoreSize.cancel();
        restoreSize = ValueAnimator.ofFloat((size / 2),0);
        restoreSize.setDuration(1000);
        restoreSize.setInterpolator(new LinearInterpolator());
        restoreSize.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float actualSize = (float) animation.getAnimatedValue();
                bounds.set(paddingLeft + thickness + actualSize, paddingTop + thickness + actualSize,
                        size - paddingLeft - thickness - actualSize, size - paddingTop - thickness - actualSize);
                invalidate();
            }
        });
        restoreSize.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                listener.endAnimation();
                dropValue();
                restoreSize.cancel();
            }
        });
        restoreSize.start();
    }


    private  float interpolate(final float a, final float b, final float proportion) {
        return (a + ((b - a) * proportion));
    }

    private  int interpolateColor(final int a, final int b, final float proportion) {
        final float[] hsva = new float[3];
        final float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }

    private void dropValue() {
        currentProgress = 0f;
        sweepAngle = 0f;
        actualProgress = 0f;
        isStartRotation = true;
    }

    public void startRotationAnimation() {
        startAngleRotate = ValueAnimator.ofFloat(startAngle, startAngle + 360);
        startAngleRotate.setDuration(800);
        startAngleRotate.setRepeatCount(ValueAnimator.INFINITE);
        startAngleRotate.setRepeatMode(ValueAnimator.RESTART);
        startAngleRotate.setInterpolator(new LinearInterpolator());
        startAngleRotate.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                startAngle = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        startAngleRotate.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });
        startAngleRotate.start();
    }

    public void resetAnimation() {
        if(startAngleRotate != null && startAngleRotate.isRunning())
            startAngleRotate.cancel();
        if(progressAnimator != null && progressAnimator.isRunning())
            progressAnimator.cancel();
        if(changeSize != null && changeSize.isRunning())
            changeSize.cancel();
        if(restoreSize != null && restoreSize.isRunning())
            restoreSize.cancel();
        fillOval = false;
        updateBounds();
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(startAngleRotate != null) {
            startAngleRotate.cancel();
            startAngleRotate = null;
        }
        if(progressAnimator != null) {
            progressAnimator.cancel();
            progressAnimator = null;
        }
        if(changeSize != null) {
            changeSize.cancel();
            changeSize = null;
        }
        if(restoreSize != null) {
            restoreSize.cancel();
            restoreSize = null;
        }
    }

    public void stopProgress(){
        resetAnimation();
        dropValue();
    }

    public int getEndColor() {
        return endColor;
    }

    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }

    public int getStartColor() {
        return startColor;
    }

    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }

    public void setListener(ProgressCircleListener listener) {
        if(listener != null)
            this.listener = listener;
    }
}
