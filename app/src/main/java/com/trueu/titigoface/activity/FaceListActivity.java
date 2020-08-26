package com.trueu.titigoface.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.trueu.titigoface.R;
import com.trueu.titigoface.database.AppDatabase;
import com.trueu.titigoface.database.UserEntity;
import com.trueu.titigoface.database.UserEntityCondition;
import com.trueu.titigoface.faceserver.FaceServer;
import com.trueu.titigoface.util.CustomScheculers;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;

import java.io.File;
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
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class FaceListActivity extends AppCompatActivity {

    @BindView(R.id.back_icon)
    ImageButton backIcon;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.search_btn)
    Button searchBtn;
    @BindView(R.id.user_search_edit)
    EditText userSearchEdit;
    @BindView(R.id.user_list)
    RecyclerView mRecyclerView;

    private CommonAdapter<UserEntity> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_list);
        ButterKnife.bind(this);

        toolbarTitle.setText("人脸列表");
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FaceServer.getInstance().init(FaceListActivity.this);//初始化人脸库

        requestDatabase(0, 20);

        //输入框监听 自动搜素
        userSearchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence.length() == 0) {
                        requestDatabase(0, 20);
                    } else if (charSequence.length() >= 4) {
                        selectByNo(userSearchEdit.getText().toString().trim());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }


    @OnClick({R.id.back_icon, R.id.search_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.back_icon:
                this.finish();
                break;
            case R.id.search_btn:
                if (userSearchEdit.getText().toString().trim().isEmpty()) requestDatabase(0, 20);
                else
                    selectByNo(userSearchEdit.getText().toString().trim());
                break;
        }
    }

    /***
     * 查询数据库人脸数据
     * @param page 页数
     * @param rows 条数
     */
    public void requestDatabase(final int page, final int rows) {

        Observable.create(new ObservableOnSubscribe<List<UserEntity>>() {
            @Override
            public void subscribe(ObservableEmitter<List<UserEntity>> list) {
                List<UserEntity> userEntities = AppDatabase.getDatabase(FaceListActivity.this).userDao().selectByPageRows(page, rows);
                list.onNext(userEntities);
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<UserEntity>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(final List<UserEntity> userEntities) {
                        refreshData(userEntities);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final long personCount = AppDatabase.getDatabase(FaceListActivity.this).userDao().count();
                                FaceListActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        toolbarTitle.setText("人脸列表 (" + personCount + "人)");
                                    }
                                });
                            }
                        }).start();

                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /***
     * 根据编号删除
     * @param userId
     */
    public void deleteById(final int userId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppDatabase.getDatabase(FaceListActivity.this).userDao().deleteById(userId);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("数据", "数据库人脸删除成功！");
                        Toast.makeText(FaceListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();

    }


    /***
     * 根据编号查询
     * @param no
     */
    public void selectByNo(final String no) {

        UserEntityCondition condition = new UserEntityCondition();
        condition.createCriteria()
                .andLike("userNo", no);
        //条件andLike与orLike不一样(大坑)

        Observable.just(condition)
                .map(new Function<UserEntityCondition, List<UserEntity>>() {
                    @Override
                    public List<UserEntity> apply(UserEntityCondition userEntityCondition) throws Exception {
                        return AppDatabase.getDatabase(FaceListActivity.this)
                                .userDao()
                                .selectByCondition(userEntityCondition.build());
                    }
                })
                .compose(CustomScheculers.<List<UserEntity>>iO2Main())
                .subscribe(new Observer<List<UserEntity>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(List<UserEntity> userEntities) {
                        refreshData(userEntities);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    /***
     * 更新列表
     * @param userEntities
     */
    public void refreshData(final List<UserEntity> userEntities) {
        mAdapter = new CommonAdapter<UserEntity>(FaceListActivity.this, R.layout.item_user, userEntities) {
            @Override
            protected void convert(ViewHolder holder, UserEntity userEntity, int position) {
                holder.setText(R.id.u_name, userEntity.getUserName());
                holder.setText(R.id.u_no, userEntity.getUserNo());
                holder.setText(R.id.reg_time, userEntity.getAddTime());
                holder.setText(R.id.u_address, userEntity.getUserAddress());

                ImageView headImg = holder.getConvertView().findViewById(R.id.u_img);

                File imgFile = new File(FaceListActivity.this.getFilesDir().getAbsolutePath() + File.separator + FaceServer.SAVE_IMG_DIR + File.separator +
                        userEntities.get(position).getUserNo() + "_" + userEntities.get(position).getUserName() + FaceServer.IMG_SUFFIX);
                Glide.with(FaceListActivity.this).load(imgFile).placeholder(R.mipmap.default_img).into(headImg);

            }
        };

        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, final int position) {

                new AlertDialog.Builder(FaceListActivity.this).setIcon(R.drawable.ic_baseline_warning_24).setTitle("警告")
                        .setMessage("确定要删除此人的注册信息吗？").setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        FaceServer.getInstance().clearFacesByUserNum(FaceListActivity.this, userEntities.get(position).getUserNo());//删除本地特征库
                        deleteById(userEntities.get(position).getId());//删除数据库操作
                        userEntities.remove(position);
                        mAdapter.notifyItemRemoved(position);
                        mAdapter.notifyItemRangeChanged(position, userEntities.size() - position);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();

                return false;
            }
        });
    }

}