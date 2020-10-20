package com.trueu.titigoface.faceserver;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.trueu.titigoface.activity.FaceAddActivity;
import com.trueu.titigoface.activity.RegisterAndRecognizeActivity;
import com.trueu.titigoface.database.AppDatabase;
import com.trueu.titigoface.database.UserEntity;
import com.trueu.titigoface.util.CustomScheculers;
import com.trueu.titigoface.util.GeneralUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import io.reactivex.observers.DefaultObserver;

import static com.trueu.titigoface.common.Constants.MSG_REG_STR;

/**
 * Created by Colin
 * on 2020/10/20
 * E-mail: hecanqi168@gmail.com
 */
public class PushRegServices extends IntentService {

    public Bitmap bitmap;

    private ExecutorService executorService;
    private String msgFaceStr, headImg, faceName;
    private int facePlotDetailId;

    public PushRegServices() {
        super("PushRegServices");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        msgFaceStr = intent.getStringExtra(MSG_REG_STR);

        executorService = Executors.newSingleThreadExecutor();
        FaceServer.getInstance().init(this);//初始化引擎

        pushMsgRegFace(msgFaceStr);

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
                boolean success = FaceServer.getInstance().registerBgr24(getApplicationContext(), bgr24, bitmap.getWidth(), bitmap.getHeight(),
                        no + "_" + name);
                if (!success) {
                    Log.e("数据", "注册失败");
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
                                .getDatabase(getApplicationContext())
                                .userDao()
                                .insert(userEntity);
                    }
                })
                .compose(CustomScheculers.<Long>iO2Main())
                .subscribe(new DefaultObserver<Long>() {
                    @Override
                    public void onNext(Long aLong) {
                        Log.e("数据", "人脸注册成功！");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e("数据", "插入数据库失败");
                    }

                    @Override
                    public void onComplete() {

                    }
                });


    }


    /***
     * 推送注册人脸
     * @param msgFaceStr
     */
    public void pushMsgRegFace(final String msgFaceStr) {
        if (!msgFaceStr.equals("none")) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(msgFaceStr);
                        headImg = jsonObject.optString("headImgUrl");
                        faceName = jsonObject.optString("name");
                        facePlotDetailId = jsonObject.optInt("plotDetailId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }).start();

            Glide.with(this).asBitmap().load(headImg).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                    if (resource.getHeight() > 100 && resource.getWidth() > 100) {
                        bitmap = Bitmap.createScaledBitmap(resource, 100, 100, true);
                    } else {
                        bitmap = resource;
                    }
                    if (bitmap != null) {
                        doRegister(faceName, "15811238977", "企业广场");
                    } else {
                        Log.e("数据", "推送bitmap为空");
                    }
                }
            });

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
