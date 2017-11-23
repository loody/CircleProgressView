package me.loody.circleprogressview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Created by loody on 01/11/2017.
 */

public class CircleProgressView extends View {

    private RectF rectF;//绘制范围
    private int strokeWidth = 20;//圆环宽度

    private Paint paintBackground;//背景
    private Paint paintProgress;//填充
    private Paint paintPath;//路径

    private int progressBackgroundColor = Color.parseColor("#7dffffff");
    private int progressColor = Color.WHITE;

    private int startAngle = 270;
    private int sweepAngle = 360;

    private PathMeasure pathMeasure;

    private Path path;

    private float[] pathPoint = new float[2];//bitmap实时坐标

    private Bitmap bitmapDg;//带头大哥

    private int width;//bitmapDg with

    private float progress = 0;//圆环进度
    private float percent = 0;//动画执行进度

    private int minWidthOrHeight;//宽高比较取最小值

    private boolean hasDg = true;

    private int duration = 1000;//默认动画执行时间(毫秒)

    private boolean isFirst = true;//第一次执行

    public CircleProgressView(Context context) {
        this(context, null);
    }

    public CircleProgressView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView);
        progressBackgroundColor = a.getColor(R.styleable.CircleProgressView_cpvProgressBackgroundColor, progressBackgroundColor);
        progressColor = a.getColor(R.styleable.CircleProgressView_cpvProgressColor, progressColor);
        strokeWidth = (int) a.getDimension(R.styleable.CircleProgressView_cpvStrokeWidth, strokeWidth);
        progress = a.getFloat(R.styleable.CircleProgressView_cpvProgress, progress);
        duration = a.getInt(R.styleable.CircleProgressView_cpvDuration, duration);
        bitmapDg = BitmapFactory.decodeResource(getResources(), a.getResourceId(R.styleable.CircleProgressView_cpvDg, R.mipmap.ic_launcher));
        a.recycle();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        minWidthOrHeight = Math.min(w, h);
        width = bitmapDg.getWidth();

        paintBackground = new Paint();
        paintBackground.setAntiAlias(true); // 设置画笔为抗锯齿
        paintBackground.setStrokeWidth(strokeWidth); // 画笔宽度
        paintBackground.setStyle(Paint.Style.STROKE);
        paintBackground.setColor(progressBackgroundColor);

        paintProgress = new Paint();
        paintProgress.setAntiAlias(true); // 设置画笔为抗锯齿
        paintProgress.setStrokeWidth(strokeWidth); // 画笔宽度
        paintProgress.setStrokeCap(Paint.Cap.ROUND);
        paintProgress.setStyle(Paint.Style.STROKE);
        paintProgress.setColor(progressColor);

        paintPath = new Paint();
        paintPath.setAntiAlias(true); // 设置画笔为抗锯齿
        paintPath.setStrokeWidth(width); // 画笔宽度
        initPath();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制底部背景
        canvas.drawArc(rectF, startAngle, sweepAngle, false, paintBackground);
        //绘制进度圆环
        canvas.drawArc(rectF, startAngle, sweepAngle * progress * percent, false, paintProgress);
        //绘制bitmap
        if (hasDg) {
            if (pathPoint[0] == 0 && pathPoint[1] == 0) {//初始状态bitmap的位置
                canvas.drawBitmap(bitmapDg, (minWidthOrHeight - width / 2) / 2, 0, paintPath);
            } else {
                canvas.drawBitmap(bitmapDg, pathPoint[0] - width / 2, pathPoint[1] - width / 2, paintPath);
//            Helper.showLog("loody", "progress:" + progress + "-percent:" + percent + "-pathPoint1:" + pathPoint[0] + "-pathPoint2:" + pathPoint[1]);
            }
        }
    }

    /**
     * 开始动画
     *
     * @param
     */


    public void execute() {
        if (isFirst) {
            isFirst = false;
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startAnimator();
                }
            }, 500);//防止 Path 还没有生成，延迟500ms开始
        } else {
            startAnimator();
        }
    }

    private void startAnimator() {
        if (pathMeasure == null) {
            return;
        }
        final float total = pathMeasure.getLength();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, pathMeasure.getLength());
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                pathMeasure.getPosTan(value, pathPoint, null);
                percent = value / total;
                postInvalidate();
            }

        });
        valueAnimator.start();
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public void setProgressBackgroundColor(int progressBackgroundColor) {
        this.progressBackgroundColor = progressBackgroundColor;
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
    }

    public void setProgress(float progress) {
        this.progress = progress;
        if (progress >= 1) {
            this.progress = 0.999f;
        }
        initPath();
    }

    /**
     * 初始化Path
     */
    private void initPath() {
        rectF = new RectF(width / 2, width / 2, minWidthOrHeight - width / 2, minWidthOrHeight - width / 2);
        path = new Path();
        path.addArc(rectF, startAngle, sweepAngle * progress);
        pathMeasure = new PathMeasure(path, false);
    }

    public void setDg(int resId) {
        this.bitmapDg = BitmapFactory.decodeResource(getResources(), resId);
        if (bitmapDg != null) {
            setHasDg(true);
        }
        postInvalidate();
    }

    public void setHasDg(boolean hasDg) {
        this.hasDg = hasDg;
    }

    /**
     * 设置动画执行时间 毫秒
     *
     * @param duration
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
}