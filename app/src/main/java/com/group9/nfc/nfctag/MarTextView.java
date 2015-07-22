package com.group9.nfc.nfctag;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by yang on 15/7/22.
 */
public class MarTextView extends TextView {
    public MarTextView(Context con) {
        super(con);
    }

    public MarTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MarTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    public boolean isFocused() {
        return true;
    }
    @Override
    protected void onFocusChanged(boolean focused, int direction,
                                  Rect previouslyFocusedRect) {
    }
}