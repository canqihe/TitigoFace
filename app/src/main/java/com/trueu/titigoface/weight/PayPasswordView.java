package com.trueu.titigoface.weight;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trueu.titigoface.R;


/**
 * Description:
 * Data：7/10/2018-10:18 AM
 *
 * @author yanzhiwen
 */

public class PayPasswordView extends LinearLayout implements View.OnClickListener, PasswordEditText.PasswordFullListener {
    private LinearLayout mKeyBoardView;
    private PasswordEditText mPasswordEditText;
    private TextView textView;
    private ImageView imageViewIcon;

    private BtnClickListner btnClickListner;

    public PayPasswordView(Context context) {
        this(context, null);
    }

    public PayPasswordView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PayPasswordView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.pay_password_layout, this);
        mKeyBoardView = findViewById(R.id.keyboard);
        mPasswordEditText = findViewById(R.id.passwordEdt);
        textView = findViewById(R.id.tool_title);
        imageViewIcon = findViewById(R.id.close_icon);
        mPasswordEditText.setPasswordFullListener(this);
        textView.setText("输入管理员密码");
        imageViewIcon.setVisibility(VISIBLE);
        imageViewIcon.setImageResource(R.drawable.ic_close);
        setItemClickListener(mKeyBoardView);

        imageViewIcon.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClickListner.onClick(v);
            }
        });

    }

    /**
     * 给每一个自定义数字键盘上的View 设置点击事件
     *
     * @param view
     */
    private void setItemClickListener(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                //不断的给里面所有的View设置setOnClickListener
                View childView = ((ViewGroup) view).getChildAt(i);
                setItemClickListener(childView);
            }
        } else {
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
            String number = ((TextView) v).getText().toString().trim();
            mPasswordEditText.addPassword(number);
        }
        if (v instanceof ImageView) {
            mPasswordEditText.deletePassword();
        }
    }

    @Override
    public void passwordFull(String password) {
//        Toast.makeText(getContext(), "你输入的密码是：" + password, Toast.LENGTH_SHORT).show();
        btnClickListner.returnPwd(password);

    }


    public void setPwdOnclick(BtnClickListner btnClickListner) {
        this.btnClickListner = btnClickListner;
    }

    public interface BtnClickListner {
        void onClick(View view);
//        void forgetPwd(View view);
        void returnPwd(String password);
    }
}
