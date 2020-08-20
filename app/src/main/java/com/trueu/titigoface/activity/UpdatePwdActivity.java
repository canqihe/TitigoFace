package com.trueu.titigoface.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.trueu.titigoface.R;
import com.trueu.titigoface.common.Constants;
import com.trueu.titigoface.util.PreUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpdatePwdActivity extends AppCompatActivity {

    @BindView(R.id.back_icon)
    ImageButton backIcon;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.old_pwd)
    EditText oldPwd;
    @BindView(R.id.new_pwd)
    EditText newPwd;
    @BindView(R.id.confirm_pwd)
    EditText confirmPwd;
    @BindView(R.id.submit_update)
    Button submitUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pwd);
        ButterKnife.bind(this);

        toolbarTitle.setText("修改密码");
    }

    @OnClick({R.id.back_icon, R.id.submit_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_icon:
                this.finish();
                break;
            case R.id.submit_update:
                if (oldPwd.getText().toString().trim().isEmpty() || newPwd.getText().toString().trim().isEmpty()
                        || confirmPwd.getText().toString().trim().isEmpty()) {
                    Toast.makeText(UpdatePwdActivity.this, "请填写完整", Toast.LENGTH_SHORT).show();
                } else
                    updatePwd(oldPwd.getText().toString().trim(), newPwd.getText().toString().trim(), confirmPwd.getText().toString().trim());
                break;
        }
    }

    public void updatePwd(String oldP, String newP, String confirmP) {
        if (!oldP.equals(PreUtils.getString(this, Constants.ADMIN_PASSWORD, "111111"))) {
            Toast.makeText(UpdatePwdActivity.this, "旧密码输入错误！", Toast.LENGTH_SHORT).show();
        } else if (!newP.equals(confirmP)) {
            Toast.makeText(UpdatePwdActivity.this, "新密码两次输入不一致！", Toast.LENGTH_SHORT).show();
        } else {
            PreUtils.setString(this, Constants.ADMIN_PASSWORD, newP);
            Toast.makeText(UpdatePwdActivity.this, "修改成功！", Toast.LENGTH_SHORT).show();
            this.finish();
        }

    }
}