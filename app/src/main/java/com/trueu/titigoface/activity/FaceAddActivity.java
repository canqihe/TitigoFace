package com.trueu.titigoface.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.trueu.titigoface.R;
import com.trueu.titigoface.database.AppDatabase;
import com.trueu.titigoface.database.UserEntity;
import com.trueu.titigoface.faceserver.FaceServer;
import com.trueu.titigoface.util.CustomScheculers;
import com.trueu.titigoface.util.GeneralUtils;
import com.trueu.titigoface.util.GlideImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.jpush.android.api.JPushInterface;
import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DefaultObserver;

public class FaceAddActivity extends AppCompatActivity {


    //注册图所在的目录
    private static final String ROOT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "titigoface";
    private static final String REGISTER_DIR = ROOT_DIR + File.separator + "register";
    private static final String REGISTER_FAILED_DIR = ROOT_DIR + File.separator + "failed";

    @BindView(R.id.user_img)
    ImageView userImg;
    @BindView(R.id.user_name)
    EditText userName;
    @BindView(R.id.user_no)
    EditText userNo;
    @BindView(R.id.user_address)
    EditText userAddress;
    @BindView(R.id.submit_person_info)
    Button submitPersonInfo;
    @BindView(R.id.back_icon)
    ImageButton backIcon;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    private ImagePicker imagePicker;
    private int IMAGE_PICKER = 101;
    public Bitmap bitmap;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_add);
        ButterKnife.bind(this);

        imagePicker = ImagePicker.getInstance();
        imagePicker.setImageLoader(new GlideImageLoader());
        imagePicker.setMultiMode(false);
        imagePicker.setShowCamera(true);
        imagePicker.setCrop(false);

        toolbarTitle.setText("人脸注册");

        executorService = Executors.newSingleThreadExecutor();

        FaceServer.getInstance().init(this);//初始化引擎
    }


    @OnClick({R.id.user_img, R.id.submit_person_info, R.id.back_icon})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.user_img:
                Intent intent = new Intent(this, ImageGridActivity.class);
                startActivityForResult(intent, IMAGE_PICKER);
                break;
            case R.id.submit_person_info:
                if (TextUtils.isEmpty(userName.getText().toString()) || TextUtils.isEmpty(userNo.getText().toString())
                        || TextUtils.isEmpty(userAddress.getText().toString()) || bitmap == null) {
                    Toast.makeText(FaceAddActivity.this, "请检查个人信息与头像是否填写完整", Toast.LENGTH_SHORT).show();
                } else
                    doRegister(userName.getText().toString().trim(), userNo.getText().toString().trim(), userAddress.getText().toString().trim());
                break;
            case R.id.back_icon:
                this.finish();
                break;
        }
    }


    /***
     * 注册人脸（特征库）
     * @param name
     * @param no
     */
    private void doRegister(final String name, final String no, final String address) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                bitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
                byte[] bgr24 = ArcSoftImageUtil.createImageData(bitmap.getWidth(), bitmap.getHeight(), ArcSoftImageFormat.BGR24);
                ArcSoftImageUtil.bitmapToImageData(bitmap, bgr24, ArcSoftImageFormat.BGR24);
                boolean success = FaceServer.getInstance().registerBgr24(FaceAddActivity.this, bgr24, bitmap.getWidth(), bitmap.getHeight(),
                        no + "_" + name);
                if (!success) {
                    GeneralUtils.showToast(FaceAddActivity.this, "注册失败");
                } else {
                    addPerson(name, no, address);
                }
                Log.i("数据，线程池状态", "run: " + executorService.isShutdown());
            }
        });
    }


    //注册人脸（数据库）
    public void addPerson(String userName, String userNo, String userAddress) {

        UserEntity userEntity = new UserEntity();
        userEntity.setAddTime(GeneralUtils.getYMD() + " " + GeneralUtils.getTime());
        userEntity.setUserName(userName);
        userEntity.setUserNo(userNo);
        userEntity.setUserAddress(userAddress);
        Observable.just(userEntity)
                .map(new Function<UserEntity, Long>() {
                    @Override
                    public Long apply(UserEntity userEntity) throws Exception {
                        return AppDatabase
                                .getDatabase(FaceAddActivity.this)
                                .userDao()
                                .insert(userEntity);
                    }
                })
                .compose(CustomScheculers.<Long>iO2Main())
                .subscribe(new DefaultObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        Toast.makeText(FaceAddActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                        FaceAddActivity.this.finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(FaceAddActivity.this, "插入数据库失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == IMAGE_PICKER) {
                ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                bitmap = BitmapFactory.decodeFile(images.get(0).path);
                userImg.setImageBitmap(bitmap);
                if (bitmap == null) {
                    Log.d("数据", "decodeFile-失败!");
                }
            } else
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show();
        }
    }
}