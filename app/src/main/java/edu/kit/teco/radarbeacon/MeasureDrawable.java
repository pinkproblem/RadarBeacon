package edu.kit.teco.radarbeacon;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;

import edu.kit.teco.radarbeacon.evaluation.CircleUtils;

/**
 * Created by Iris on 11.11.2015.
 */
public class MeasureDrawable extends View {

    private static final int defaultSegmentCount = 8;

    private int taggedColor = 0xff98f098;
    private int untaggedColor = 0xfff09e98;
    private final int space = 4; //space between segments in degree

    private int numberOfSegments;

    private ArrayList<ShapeDrawable> segments;
    private boolean[] tagged;
    private ShapeDrawable innerCircle;

    private int top;
    private int left;
    private int right;
    private int bottom;

    private int backgroundColor;

    // constructor cant take any other arguments, so we work with a default number of segments
    // until specified otherwise
    public MeasureDrawable(Context context, AttributeSet attrs) {
        super(context, attrs);

        //obtain background color
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground
        });
        backgroundColor = array.getColor(0, 0xFF00FF);
        array.recycle();

        //obtain screen dimensions
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int screenWidth = size.x;
        int screenHeight = size.y;

//        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
//        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2 - 70;
        int width = (int) (screenWidth * 0.95);
        int innerRadius = (int) (screenWidth * 0.3);

        top = centerY - width / 2;
        left = centerX - width / 2;
        right = centerX + width / 2;
        bottom = centerY + width / 2;

        //set center of rotation for this view; this is important since it is not positioned
        // perfectly in the center of the screen (because the ui didnt want to work the way i
        // wanted it to work)
        setPivotX(centerX);
        setPivotY(centerY);

        tagged = new boolean[defaultSegmentCount];
        setSegmentCount(defaultSegmentCount);

        innerCircle = new ShapeDrawable(new ArcShape(0, 360));
        innerCircle.getPaint().setColor(backgroundColor);
        innerCircle.setBounds(centerX - innerRadius, centerY - innerRadius, centerX + innerRadius,
                centerY + innerRadius);
    }

    public void setSegmentCount(int segmentCount) {
        numberOfSegments = segmentCount;
        segments = new ArrayList<>(segmentCount);

        int segmentSize = 360 / segmentCount;
        for (int i = 0; i < segmentCount; i++) {
            ShapeDrawable newSegment = new ShapeDrawable(new ArcShape(270 + i * segmentSize,
                    segmentSize - space));
            newSegment.getPaint().setColor(untaggedColor);
            newSegment.setBounds(left, top, right, bottom);
            segments.add(i, newSegment);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Drawable segment : segments) {
            segment.draw(canvas);
        }
        innerCircle.draw(canvas);
    }

    public void tag(int index) {
        if (index >= numberOfSegments) {
            throw new IllegalArgumentException("Index out of bounds");
        }

        if (!tagged[index]) {
            tagged[index] = true;
            segments.get(index).getPaint().setColor(taggedColor);
            postInvalidate();
        }
    }

    public void tag(float radians) {
        if (radians < -Math.PI || radians >= Math.PI) {
            throw new IllegalArgumentException("Angle out of bounds");
        }

        int index = CircleUtils.getCircleSegment(radians, numberOfSegments);
        tag(index);
    }
}
