package com.android.system.core.sometools;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by guang on 2017/9/19.
 */

public class GSwipeRefreshLayout extends SwipeRefreshLayout {
    private int mTouchSlop;
//    private float mPrevX;
    private OnLRDragListener onLRDragListener;
    private boolean isLRMove = false;

    public GSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(onLRDragListener != null)
        {
            isLRMove = onLRDragListener.onEvent(event);
        }
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
////                mPrevX = MotionEvent.obtain(event).getX();
//                isLRMove = false;
//                break;
//
////            case MotionEvent.ACTION_MOVE:
////                final float eventX = event.getX();
////                float xDiff = Math.abs(eventX - mPrevX);
////
////                if (xDiff > mTouchSlop) {
////                    return false;
////                }
//        }
//        if(isLRMove)
//            return false;
        return super.onInterceptTouchEvent(event);
    }

    public void setOnLRDragListener(OnLRDragListener onLRDragListener)
    {
        this.onLRDragListener = onLRDragListener;
    }

    interface OnLRDragListener
    {
        boolean onEvent(MotionEvent event);
    }
}
