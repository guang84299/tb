package com.qinglu.ad.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.AbsoluteLayout;

/**
 * Created by guang on 2017/9/25.
 */

public class GEventWindowView extends AbsoluteLayout {
    public GEventWindowView(Context context) {
        super(context);
    }

    public GEventWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GEventWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
            if (mOnKeyListener != null) {
                mOnKeyListener.onKey(this, KeyEvent.KEYCODE_BACK, event);
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    OnKeyListener mOnKeyListener = null;

    @Override
    public void setOnKeyListener(OnKeyListener l) {
        mOnKeyListener = l;

        super.setOnKeyListener(l);
    }
}
