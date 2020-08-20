package com.trueu.titigoface.activity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trueu.titigoface.R;
import com.trueu.titigoface.util.MyTTS;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate {
    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;
    @BindView(R.id.back_icon)
    ImageButton backIcon;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    private ZXingView mZXingView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ButterKnife.bind(this);

        mZXingView = findViewById(R.id.zxingview);
        mZXingView.setDelegate(this);

        toolbarTitle.setText("扫描二维码");

        MyTTS.getInstance().init(ScanActivity.this);//初始化语音

    }

    @Override
    protected void onStart() {
        super.onStart();
        mZXingView.startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT); // 打开前置摄像头开始预览，但是并未开始识别
        mZXingView.startSpotAndShowRect(); // 显示扫描框，并开始识别
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        Log.d("数据，扫码结果", result);
        MyTTS.getInstance().speak("正在处理");
        executeResult(result);
    }


    /***
     * 处理扫描结果
     * @param json
     */
    public void executeResult(String json) {
        EasyHttp.post("/visit/license/verify")
                .baseUrl("http://192.168.0.110:8080")
                .upJson(json)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        Log.d("数据，扫码处理结果", e.toString());
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d("数据，扫码处理结果", s);
                        ScanActivity.this.finish();
                    }
                });
    }


    @Override
    protected void onStop() {
        mZXingView.stopCamera(); // 关闭摄像头预览，并且隐藏扫描框
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mZXingView.onDestroy(); // 销毁二维码扫描控件
        startActivity(new Intent(this, RegisterAndRecognizeActivity.class));
    }


    @Override
    public void onCameraAmbientBrightnessChanged(boolean isDark) {
        String tipText = mZXingView.getScanBoxView().getTipText();
        String ambientBrightnessTip = "\n环境过暗，请打开闪光灯";
        if (isDark) {
            if (!tipText.contains(ambientBrightnessTip)) {
                mZXingView.getScanBoxView().setTipText(tipText + ambientBrightnessTip);
            }
        } else {
            if (tipText.contains(ambientBrightnessTip)) {
                tipText = tipText.substring(0, tipText.indexOf(ambientBrightnessTip));
                mZXingView.getScanBoxView().setTipText(tipText);
            }
        }
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        Log.e(TAG, "打开相机出错");
    }


    @OnClick(R.id.back_icon)
    public void onViewClicked() {
        this.finish();
    }
}