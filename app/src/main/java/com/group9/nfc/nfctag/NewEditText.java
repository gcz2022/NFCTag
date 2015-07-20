package com.group9.nfc.nfctag;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by yang on 15/7/19.
 */
public class NewEditText extends TextView {

    Drawable mADD, mSUB, mSUB_UNABLE;

    public NewEditText(Context context) {
        super(context);
    }

    public NewEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();//初始化
    }

    /**
     * 初始化，设置初始值
     */
    private void init() {
        mSUB = getCompoundDrawables()[0];
        mADD = getCompoundDrawables()[2];
        this.setText(1 + "");//设置初始值为0
    }

    /**
     * 获取点击时X值，判断其所在位置来虚拟点击事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int num;
        float X, sub_left, sub_right, add_left, add_right;
        X = event.getX();
        sub_right = getTotalPaddingLeft();
        sub_left = getPaddingLeft();
        add_right = getWidth() - getPaddingRight();
        add_left = getWidth() - getTotalPaddingRight();
        if (TextUtils.isEmpty(this.getText().toString())) {
            num = 0;
        } else {
            num = Integer.parseInt(getText().toString());
        }
        if (X > sub_left && X < sub_right) {
            num--;
            num = setCheckadle(num);//检验当前输入框数字,若为0则设置减号按钮不可见
            this.setText(num + "");

        } else if (X > add_left && X < add_right) {
            num++;
            num = setCheckadle(num);//检验当前输入框数字,若不为0则设置减号按钮可见
            this.setText(num + "");
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断num值，对加减号进行设置：若num = 0，减号不可用；若num = 99，加号不可用（限制num在0~99区间）
     *
     * @param num
     * @return
     */
    private int setCheckadle(int num) {
        if (num == 0) {
            setCompoundDrawables(null, getCompoundDrawables()[1], mADD, getCompoundDrawables()[3]);
        } else {
            setCompoundDrawables(mSUB, getCompoundDrawables()[1], mADD, getCompoundDrawables()[3]);
        }
        return num;
    }
}