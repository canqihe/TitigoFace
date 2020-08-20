package com.trueu.titigoface.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


import com.trueu.titigoface.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.back_icon)
    ImageButton backIcon;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.reg_btn)
    TextView regBtn;
    @BindView(R.id.list_btn)
    TextView listBtn;
    @BindView(R.id.pwd_btn)
    TextView pwdBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.back_icon, R.id.reg_btn, R.id.list_btn, R.id.pwd_btn})
    public void onViewClicked(View view) {

        switch (view.getId()) {
            case R.id.back_icon:
                this.finish();
                break;
            case R.id.reg_btn:
                startActivity(new Intent(SettingActivity.this, FaceAddActivity.class));
                break;
            case R.id.list_btn:
                startActivity(new Intent(SettingActivity.this, FaceListActivity.class));
                break;
            case R.id.pwd_btn:
                startActivity(new Intent(SettingActivity.this, UpdatePwdActivity.class));
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startActivity(new Intent(this, RegisterAndRecognizeActivity.class));
    }
}