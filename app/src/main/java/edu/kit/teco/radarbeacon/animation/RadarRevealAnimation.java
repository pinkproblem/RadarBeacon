package edu.kit.teco.radarbeacon.animation;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Iris on 05.12.2015.
 */
public class RadarRevealAnimation extends View {

    RevealDirection mode;
    float currAngle;

    private int backgroundColor;

    public enum RevealDirection {OPEN, CLOSE}

    public RadarRevealAnimation(Context context) {
        super(context);
        setAnimation(new RevealAnimation(0, 360, 1000));
        mode = RevealDirection.CLOSE;

        //obtain background color
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground
        });
        backgroundColor = array.getColor(0, 0xFF00FF);
        array.recycle();
    }

    public RadarRevealAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRevealDirection(RevealDirection mode) {
        this.mode = mode;
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        Paint p = new Paint();
        p.setColor(backgroundColor);
//        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        RectF oval = new RectF(canvas.getClipBounds());
        canvas.drawArc(oval, 270, currAngle, true, p);
    }

    /* Here we define our nested custom animation */
    public class RevealAnimation extends Animation {
        float startAngle;
        float sweepAngle;

        public RevealAnimation(int startAngle, int sweepAngle, long duration) {
            this.startAngle = startAngle;
            this.sweepAngle = sweepAngle;
            setDuration(duration);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float newAngle;
            if (mode == RevealDirection.CLOSE) {
                newAngle = startAngle + ((sweepAngle - startAngle) * interpolatedTime);
            } else {
                newAngle = 360 - (startAngle + ((sweepAngle - startAngle) * interpolatedTime));
            }
            RadarRevealAnimation.this.currAngle = newAngle;
            invalidate();
        }
    }
}
