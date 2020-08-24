package com.trueu.titigoface;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.VersionInfo;
import com.arcsoft.face.enums.RuntimeABI;
import com.trueu.titigoface.activity.BaseActivity;
import com.trueu.titigoface.activity.RegisterAndRecognizeActivity;
import com.trueu.titigoface.common.Constants;
import com.trueu.titigoface.model.DeviceResInfo;
import com.trueu.titigoface.util.ConfigUtil;
import com.trueu.titigoface.util.MD5Utils;
import com.trueu.titigoface.util.NetWorkUtils;
import com.trueu.titigoface.util.PreUtils;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.arcsoft.face.enums.DetectFaceOrientPriority.ASF_OP_ALL_OUT;

public class MainActivity extends BaseActivity {

    @BindView(R.id.startengine)
    Button startEngine;
    @BindView(R.id.plotId_btn)
    Button plotIdBtn;
    @BindView(R.id.set_plotId)
    EditText setPlotId;
    @BindView(R.id.neiwang)
    RadioButton neiwang;
    @BindView(R.id.gongwang)
    RadioButton gongwang;
    @BindView(R.id.rg_iptype)
    RadioGroup radioGroup;

    private int ipType = 20;  //10内网 20公网
    private String resultMsg = "null";
    private int resultCode;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE};
    boolean libraryExists = true;
    // Demo 所需的动态库文件
    private static final String[] LIBRARIES = new String[]{
            // 人脸相关
            "libarcsoft_face_engine.so",
            "libarcsoft_face.so",
            // 图像库相关
            "libarcsoft_image_util.so",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        libraryExists = checkSoFile(LIBRARIES);
        ApplicationInfo applicationInfo = getApplicationInfo();
        Log.i("数据", "onCreate: " + applicationInfo.nativeLibraryDir);
        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
        } else {
            VersionInfo versionInfo = new VersionInfo();
            int code = FaceEngine.getVersion(versionInfo);
            Log.i("数据", "onCreate: getVersion, code is: " + code + ", versionInfo is: " + versionInfo);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch (checkedId) {
                    case R.id.neiwang:
                        ipType = 10;
                        break;
                    case R.id.gongwang:
                        ipType = 20;
                        break;
                }
            }
        });

    }


    @OnClick({R.id.plotId_btn, R.id.startengine})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.plotId_btn:

                if (!TextUtils.isEmpty(setPlotId.getText())) {
                    deviceController();
                } else Toast.makeText(MainActivity.this, "请输入单元ID", Toast.LENGTH_SHORT).show();

                break;
            case R.id.startengine:
                startengine();
                break;
        }
    }

    /***
     * 激活引擎
     */
    public void startengine() {
        if (!libraryExists) {
            showToast(getString(R.string.library_not_found));
            return;
        }
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i("数据，激活引擎", "subscribe: getRuntimeABI() " + runtimeABI);

                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(MainActivity.this, Constants.APP_ID, Constants.SDK_KEY);
                Log.i("数据，激活引擎", "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer activeCode) {

                        //设置全人脸检测模式
                        ConfigUtil.setFtOrient(MainActivity.this, ASF_OP_ALL_OUT);

                        if (activeCode == ErrorInfo.MOK) {
                            showToast(getString(R.string.active_success));
                            startActivity(new Intent(MainActivity.this, RegisterAndRecognizeActivity.class));
                            MainActivity.this.finish();
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
//                            showToast(getString(R.string.already_activated));
                            startActivity(new Intent(MainActivity.this, RegisterAndRecognizeActivity.class));
                            MainActivity.this.finish();
                        } else {
                            showToast(getString(R.string.active_failed, activeCode));
                        }

                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
                        int res = FaceEngine.getActiveFileInfo(MainActivity.this, activeFileInfo);
                        if (res == ErrorInfo.MOK) {
                            Log.i("数据，激活引擎", activeFileInfo.toString());
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /***
     * 设备注册
     */
    public void deviceController() {
        DeviceResInfo deviceResInfo = DeviceResInfo.getInstance();
        deviceResInfo.setIpAddr(NetWorkUtils.getIpAddress(this));
        deviceResInfo.setIpType(ipType);
        deviceResInfo.setMac(MD5Utils.getMD5(NetWorkUtils.getMacAddressFromIp(this)));
        deviceResInfo.setPlotDetailId(Integer.parseInt(setPlotId.getText().toString()));

        PreUtils.setInt(MainActivity.this, Constants.PLOT_ID, Integer.parseInt(setPlotId.getText().toString().trim()));

        Log.d("数据", "IP地址：" + NetWorkUtils.getIpAddress(this)
                + "\nMAC地址：" + MD5Utils.getMD5(NetWorkUtils.getMacAddressFromIp(this))
                + "\nipType：" + ipType
                + "\nPlotId：" + Integer.parseInt(setPlotId.getText().toString()));

        EasyHttp
                .post(Constants.DEVICE_CONTROLLER)
                .baseUrl(Constants.BASE_URL)
                .upObject(deviceResInfo)
                .addConverterFactory(GsonConverterFactory.create())
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        Log.d("数据：访问异常：", e.toString());
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(String result) {
                        Log.d("数据：成功", result);
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            resultCode = jsonObject.optInt("resultCode");
                            if (resultCode == 0) resultMsg = "单元ID设置成功!";
                            else
                                resultMsg = jsonObject.optString("errorMsg");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage(resultMsg);
                        builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.show();
                    }
                });
    }


    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {

        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                startengine();
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    /**
     * 检查能否找到动态链接库，如果找不到，请修改工程配置
     *
     * @param libraries 需要的动态链接库
     * @return 动态库是否存在
     */
    private boolean checkSoFile(String[] libraries) {
        File dir = new File(getApplicationInfo().nativeLibraryDir);
        File[] files = dir.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }
        List<String> libraryNameList = new ArrayList<>();
        for (File file : files) {
            libraryNameList.add(file.getName());
        }
        boolean exists = true;
        for (String library : libraries) {
            exists &= libraryNameList.contains(library);
        }
        return exists;
    }

}