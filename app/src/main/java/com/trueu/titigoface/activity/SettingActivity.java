package com.trueu.titigoface.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.trueu.titigoface.R;
import com.trueu.titigoface.common.Constants;
import com.trueu.titigoface.util.GeneralUtils;
import com.zhouyou.http.EasyHttp;
import com.zhouyou.http.callback.DownloadProgressCallBack;
import com.zhouyou.http.callback.SimpleCallBack;
import com.zhouyou.http.exception.ApiException;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.converter.gson.GsonConverterFactory;

public class SettingActivity extends BaseActivity {

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
    @BindView(R.id.check_upgrade)
    TextView checkUpgrade;
    @BindView(R.id.version_tx)
    TextView versionTx;
    @BindView(R.id.feedback_btn)
    TextView feedbackBtn;

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        dialog = new ProgressDialog(this);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setMessage("正在下载更新，请勿直接退出...");
        dialog.setIcon(R.drawable.ic_baseline_cloud_download_24);
        dialog.setTitle("更新");
        dialog.setMax(100);
        dialog.setCancelable(false);

        versionTx.setText("© 和诚科技 v" + GeneralUtils.getAppVersionName(this));

    }

    @OnClick({R.id.back_icon, R.id.reg_btn, R.id.list_btn, R.id.pwd_btn, R.id.feedback_btn, R.id.check_upgrade})
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
            case R.id.check_upgrade:
                checkUpdate();
                break;
            case R.id.feedback_btn:
                new AlertDialog.Builder(SettingActivity.this).setTitle("联系我们")
                        .setMessage("邮件\ntitigo@services.com\n\n致电\n0731-84282827")
                        .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).create().show();
                break;
        }
    }


    /***
     * 检查更新
     */
    public void checkUpdate() {

        EasyHttp.get(Constants.UPDATE_APP)
                .baseUrl(Constants.BASE_URL)
                .readTimeOut(30 * 1000)//局部定义读超时 ,可以不用定义
                .writeTimeOut(30 * 1000)
                .connectTimeout(30 * 1000)
                .params("clientType", "20")
                .params("type", "10")
                .params("appVersion", String.valueOf(GeneralUtils.getAppVersionCode(SettingActivity.this)))
                .addConverterFactory(GsonConverterFactory.create())
                .timeStamp(true)
                .execute(new SimpleCallBack<String>() {
                    @Override
                    public void onError(ApiException e) {
                        showToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(String result) {
                        if (result != null) {
                            Log.e("数据，检查更新", result);
                            try {
                                JSONObject jsonObject = new JSONObject(result);
                                if (jsonObject.optInt("resultCode") == 0) {
                                    JSONObject jsonObject1 = jsonObject.optJSONObject("data");
                                    final String apkUrl = jsonObject1.optString("appUrl");
                                    String desc = jsonObject1.optString("description");
                                    new AlertDialog.Builder(SettingActivity.this).setTitle("发现新版本")
                                            .setIcon(R.drawable.ic_baseline_system_update_24)
                                            .setMessage(desc).setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            onDownloadApk(apkUrl);
                                        }
                                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    }).create().show();

                                } else
                                    showToast("resultCode：" + jsonObject.optInt("resultCode"));
                            } catch (JSONException e) {
                                Log.e("数据，更新出错", e.toString());
                            }
                        }
                    }
                });
    }


    /**
     * 下载apk
     *
     * @param apkUrl
     */
    public void onDownloadApk(String apkUrl) {//下载回调是在异步里处理的
        EasyHttp.downLoad(apkUrl)
                .savePath(Environment.getExternalStorageDirectory().getPath() + "/titigo/")//默认在：/storage/emulated/0/Android/data/包名/files/1494647767055
                .saveName("titigo_face_" + System.currentTimeMillis() + ".apk")//默认名字是时间戳生成的
                .execute(new DownloadProgressCallBack<String>() {
                    @Override
                    public void update(long bytesRead, long contentLength, boolean done) {
                        int progress = (int) (bytesRead * 100 / contentLength);
                        dialog.setProgress(progress);
                        Log.e("数据，下载进度", progress + "% ");
                    }

                    @Override
                    public void onStart() {
                        Log.e("数据，下载", "======" + Thread.currentThread().getName());
                        dialog.show();
                    }

                    @Override
                    public void onComplete(String path) {
                        Log.e("数据，下载完成，文件保存路径", "======" + path);
                        installAPK(path);
                        dialog.dismiss();
                    }

                    @Override
                    public void onError(final ApiException e) {
                        Log.e("数据，下载", "======" + Thread.currentThread().getName());
                        showToast(e.getMessage());
                        dialog.dismiss();
                    }
                });
    }


    /***
     * 安装apk
     * @param apkPath
     */
    public void installAPK(String apkPath) {
        File fileApk = new File(apkPath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(SettingActivity.this, "com.trueu.titigoface.fileProvider", fileApk);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(fileApk), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        startActivity(new Intent(this, RegisterAndRecognizeActivity.class));
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {

    }
}