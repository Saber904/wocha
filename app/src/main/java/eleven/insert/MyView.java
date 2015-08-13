package eleven.insert;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by Eleven on 2015/8/2.
 */
public class MyView extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mSurfaceHolder;


    private double mRotateDegree = 45.0;
    private int noNumBalls = 4;
    private int withNumBalls = 15;

    private int mWidth;
    private int mHeight;

    private Ball[] Balls;

    private float mCircleX;
    private float mCircleY;
    private float mCircleRadiu;

    private float mStickLength;

    private Paint mStickPaint;
    private Paint mBallsPaint;
    private Paint mTextPaint;

    private double mIntervalTime = 40.0;

    private volatile boolean insertFail = false;
    private volatile boolean insertSucess = false;

    private int theFail = -1;

    ScheduledExecutorService scheduledExecutorService;
    ExecutorService executorService;

    Context context;

    public MyView(Context context, double mRotateDegree, int noNumBalls, int withNumBalls) {
        super(context);
        this.mRotateDegree = mRotateDegree;
        this.noNumBalls = noNumBalls;
        this.withNumBalls = withNumBalls;

        this.context = context;

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        Balls = new Ball[noNumBalls + withNumBalls];

        mStickPaint = new Paint();
        mStickPaint.setColor(Color.WHITE);
        mStickPaint.setAntiAlias(true);

        mBallsPaint = new Paint();
        mBallsPaint.setColor(Color.WHITE);
        mStickPaint.setStyle(Paint.Style.FILL);
        mBallsPaint.setAntiAlias(true);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);

        executorService = Executors.newSingleThreadExecutor();

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (!insertFail && !insertSucess)
                        executorService.execute(new insertTask());
                }
                return false;
            }
        });
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    private class insertTask implements Runnable {
        @Override
        public void run() {
            for (int i = 0; i < Balls.length; i++){
                if (!Balls[i].isRounging()) {
                    float oldPosX = Balls[i].getPosX();
                    float oldPosY = Balls[i].getPosY();

                    float newPosX = mCircleX;
                    float newPosY = mCircleY + mStickLength;
                    float radius = Balls[i].getRadius();
                    double angle = 270;
                    for (int j = 0; j < i; j++){
                        float dis = dis(newPosX, newPosY, Balls[j].getPosX(), Balls[j].getPosY());
                        if (dis < radius * 2) {
                            insertFail = true;
                            theFail = i;
                            break;
                        }
                    }
                    Balls[i].setRounging(true);
                    Balls[i].setPosX(newPosX);
                    Balls[i].setPosY(newPosY);
                    Balls[i].setAngle(angle);
                    if (!insertFail && i == Balls.length - 1) {
                        insertSucess = true;
                    }
                    newPosX = oldPosX;
                    newPosY = oldPosY;
                    for (int j = i + 1; j < Balls.length; j++){
                        oldPosX = Balls[j].getPosX();
                        oldPosY = Balls[j].getPosY();

                        Balls[j].setPosX(newPosX);
                        Balls[j].setPosY(newPosY);

                        newPosX = oldPosX;
                        newPosY = oldPosY;
                    }
                    break;
                }
            }
        }
    }

    private class paintTask implements Runnable {
        @Override
        public void run() {
            if (!insertFail && !insertSucess) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.BLACK);
                canvas.drawCircle(mCircleX, mCircleY, mCircleRadiu, mBallsPaint);

                for (int i = 0; i < noNumBalls + withNumBalls; i++) {
                    float posX = Balls[i].getPosX();
                    float posY = Balls[i].getPosY();
                    float radius = Balls[i].getRadius();
                    int num = Balls[i].getNumber();

                    if (num == 0) {
                        canvas.drawCircle(posX, posY, radius, mBallsPaint);
                    } else {
                        canvas.drawCircle(posX, posY, radius, mBallsPaint);
                        canvas.drawText(String.valueOf(num), posX - 2, posY + 2, mTextPaint);
                    }
                    if (Balls[i].isRounging()) {
                        canvas.drawLine(mCircleX, mCircleY, posX, posY, mStickPaint);
                    }
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);

                for (int i = 0; i < Balls.length; i++) {
                    if (Balls[i].isRounging()) {
                        double oldAngle = Balls[i].getAngle();
                        double newAngle = (oldAngle + mRotateDegree * mIntervalTime / 1000) % 360;
                        Balls[i].setAngle(newAngle);
                        float newX = calX(mCircleX, mStickLength, newAngle);
                        float newY = calY(mCircleY, mStickLength, newAngle);
                        Balls[i].setPosX(newX);
                        Balls[i].setPosY(newY);
                    }
                }
            } else if (insertFail){
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.RED);
                canvas.drawCircle(mCircleX, mCircleY, mCircleRadiu, mBallsPaint);

                for (int i = 0; i < noNumBalls + withNumBalls; i++) {
                    float posX = Balls[i].getPosX();
                    float posY = Balls[i].getPosY();
                    float radius = Balls[i].getRadius();
                    int num = Balls[i].getNumber();

                    if (num == 0) {
                        canvas.drawCircle(posX, posY, radius, mBallsPaint);
                    } else {
                        if (i == theFail)
                            canvas.drawCircle(posX, posY, (float)1.5*radius, mBallsPaint);
                        else
                            canvas.drawCircle(posX, posY, radius, mBallsPaint);
                        canvas.drawText(String.valueOf(num), posX - 2, posY + 2, mTextPaint);
                    }
                    if (Balls[i].isRounging()) {
                        canvas.drawLine(mCircleX, mCircleY, posX, posY, mStickPaint);
                    }
                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                scheduledExecutorService.shutdown();
            } else if (insertSucess) {
                Canvas canvas = mSurfaceHolder.lockCanvas();
                canvas.drawColor(Color.GREEN);
                canvas.drawCircle(mCircleX, mCircleY, mCircleRadiu, mBallsPaint);

                for (int i = 0; i < noNumBalls + withNumBalls; i++) {
                    float posX = Balls[i].getPosX();
                    float posY = Balls[i].getPosY();
                    float radius = Balls[i].getRadius();
                    int num = Balls[i].getNumber();

                    if (Balls[i].isRounging()) {
                        canvas.drawLine(mCircleX, mCircleY, posX, posY, mStickPaint);
                    }

                    if (num == 0) {
                        canvas.drawCircle(posX, posY, radius, mBallsPaint);
                    } else {
                        canvas.drawCircle(posX, posY, radius, mBallsPaint);
                        canvas.drawText(String.valueOf(num), posX - 5,
                                posY + mTextPaint.getTextSize()/2, mTextPaint);
                    }

                }
                mSurfaceHolder.unlockCanvasAndPost(canvas);
                scheduledExecutorService.shutdown();
            }
        }
    }

    private static class Ball {
        private float radius = 1;
        private volatile boolean rounging = false;
        private int number;
        private double angle = 0.0;

        public synchronized double getAngle() {
            return angle;
        }

        public synchronized void setAngle(double angle) {
            this.angle = angle;
        }

        private float posX = 0;
        private float posY = 0;

        public synchronized float getRadius() {
            return radius;
        }

        public synchronized int getNumber() {
            return number;
        }

        public synchronized float getPosX() {
            return posX;
        }

        public synchronized float getPosY() {
            return posY;
        }

        public synchronized boolean isRounging() {
            return rounging;
        }

        public synchronized void setRadius(float radius) {
            this.radius = radius;
        }

        public synchronized void setNumber(int number) {
            this.number = number;
        }

        public synchronized void setRounging(boolean b) {
            this.rounging = b;
        }

        public synchronized void setPosX(float posX) {
            this.posX = posX;
        }

        public synchronized void setPosY(float posY) {
            this.posY = posY;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mWidth = holder.getSurfaceFrame().width();
        mHeight = holder.getSurfaceFrame().height();

        mCircleX = ((float) mWidth) / 2;
        mCircleY = ((float) mWidth) / 2;
        mCircleRadiu = ((float) mWidth) / 8;

        mStickLength = 3 * mCircleRadiu;

        mTextPaint.setTextSize(mCircleRadiu / 5);
        mStickPaint.setStrokeWidth(mCircleRadiu / 40);

        for (int i = 0; i < noNumBalls; i++){
            Ball ball = new Ball();
            ball.setRadius(mCircleRadiu / 5);
            double angle = (double) i * (360 / noNumBalls);
            ball.setAngle(angle);
            ball.setPosX(calX(mCircleX, mStickLength, angle));
            ball.setPosY(calY(mCircleY, mStickLength, angle));
            ball.setNumber(0);
            ball.setRounging(true);
            Balls[i] = ball;
        }

        for (int i = 0; i < withNumBalls; i++) {
            Ball ball = new Ball();
            ball.setRadius(mCircleRadiu / 5);
            ball.setPosX(mCircleX);
            ball.setPosY(mCircleY + mStickLength + (i + 1) * mCircleRadiu);
            ball.setNumber(withNumBalls - i);
            Balls[noNumBalls + i] = ball;
        }

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new paintTask(), 0, 40, TimeUnit.MILLISECONDS);

    }

    private final float calX(final float circleX, final float stickLength, double angle) {
        float result = circleX + stickLength * (float)Math.cos(angle/360*(2*Math.PI));
        return result;
    }

    private final float dis(final float posX1, final float posY1, final float posX2, final float posY2) {
        double result = Math.pow((posX1 - posX2), 2) + Math.pow((posY1 - posY2), 2);
        return (float)Math.sqrt(result);
    }

    private final float calY(final float circleY, final float stickLength, double angle) {
        float result = circleY - stickLength * (float)Math.sin(angle/360*(2*Math.PI));
        return result;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        scheduledExecutorService.shutdownNow();
    }
}
