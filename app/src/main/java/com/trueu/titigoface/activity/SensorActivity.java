package com.trueu.titigoface.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.common.pos.api.util.PosUtil;
import com.common.pos.api.util.ShellUtils;
import com.trueu.titigoface.R;

public class SensorActivity extends AppCompatActivity {

    TextView sensorStatus;
    StringBuffer buffer = new StringBuffer();
    boolean isReading;
    String internalModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);

        sensorStatus = (TextView) findViewById(R.id.sensorStatus);
        internalModel = ShellUtils.execCommand("getprop ro.internal.model", false).successMsg;
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        isReading = true;
        startReadSensor();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        isReading = false;
    }

    private void startReadSensor(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                while(isReading){
                    buffer.setLength(0);
                    int ret = PosUtil.getPriximitySensorStatus(0);
                    if(ret == 1){
                        buffer.append("红外传感器[有人]\n");
                    }else if(ret == 0){
                        buffer.append("红外传感器[无人]\n");
                    }else{
                        buffer.append("红外传感器[失败]\n");
                    }
                    ret = PosUtil.getPriximitySensorStatus(1);
                    if(ret == 1){
                        buffer.append("拆除传感器[拆除]\n");
                    }else if(ret == 0){
                        buffer.append("拆除传感器[未拆除]\n");
                    }else{
                        buffer.append("拆除传感器[失败]\n");
                    }


                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            // TODO Auto-generated method stub
                            sensorStatus.setText(buffer.toString());
                        }
                    });

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

}
