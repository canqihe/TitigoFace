package com.trueu.titigoface.activity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trueu.titigoface.R;
import com.trueu.titigoface.common.Constants;
import com.trueu.titigoface.util.GeneralUtils;
import com.trueu.titigoface.util.MD5Utils;
import com.trueu.titigoface.util.MyTTS;
import com.trueu.titigoface.util.NetWorkUtils;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zxing.ZXingView;

public class ScanActivity extends AppCompatActivity implements QRCodeView.Delegate {
    private static final String TAG = ScanActivity.class.getSimpleName();
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
        try {
            JSONObject obj = new JSONObject(result);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mac", MD5Utils.getMD5(NetWorkUtils.getMacAddressFromIp(ScanActivity.this)));
            jsonObject.put("plotDetailId", obj.optInt("plotDetailId"));
            jsonObject.put("startTime", obj.optLong("startTime"));
            jsonObject.put("type", obj.optInt("type"));
            jsonObject.put("userId", obj.optInt("userId"));

            Log.d("数据，扫码Json转换结果", jsonObject.toString());
            executeResult(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /***
     * 处理扫描结果
     * @param json
     */
    public void executeResult(String json) {
        EasyHttp.post(Constants.QRCODE_VERIFY)
                .baseUrl(Constants.BASE_URL)
                .upJson(json)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        Log.d("数据，扫码接口发生错误", e.toString());
                    }

                    @Override
                    public void onSuccess(String s) {
                        Log.d("数据，扫码网络处理结果", s);
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
        MyTTS.getInstance().release();
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