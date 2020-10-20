package com.trueu.titigoface.common;

public class Constants {

    public static final String APP_ID = "g5P9cLkePoHV4qLZsMgpzfu1s1VqasZqGTK5e5pSNNK";
    public static final String SDK_KEY = "42CAmasKepAL4JkwmHC4shT6QL6GrFznY7jBBdRJoAgA";
    public static final String ADMIN_PASSWORD = "admin_password";
    public static final String DEFAULT_PASSWORD_VALUE = "111111";//默认密码
    public static String PLOT_ID = "PLOT_ID";

    public static String MSG_REG_STR="msg_regStr";

    //baseurl
    public static final String BASE_URL = "http://device.titigo.top";
    //设备控制器类
    public static final String DEVICE_CONTROLLER = "/elevator-web/device/regist";
    //人脸控制器类
    public static final String FACE_CONTROLLER = "/elevator-web/base/face/call";
    //app版本更新
    public static final String UPDATE_APP = "/elevator-web/app/upgrade";
    //二维码验证
    public static final String QRCODE_VERIFY = "/elevator-web/visit/license/verify";


    /**
     * IR预览数据相对于RGB预览数据的横向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int HORIZONTAL_OFFSET = 0;
    /**
     * IR预览数据相对于RGB预览数据的纵向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int VERTICAL_OFFSET = 0;
}

