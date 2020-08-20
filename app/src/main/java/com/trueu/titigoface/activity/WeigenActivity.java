package com.trueu.titigoface.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;

import com.common.pos.api.util.PosUtil;
import com.common.pos.api.util.ShellUtils;
import com.trueu.titigoface.R;

public class WeigenActivity extends Activity {

	TableLayout tableLayoutTPS967;
	private String mIDString="123456";
	String internalModel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.weigen_activity);
		internalModel = ShellUtils.execCommand("getprop ro.internal.model", false).successMsg;
		tableLayoutTPS967 = (TableLayout) findViewById(R.id.tableLayoutTPS967);
	}
	

    private static byte[] hexStringToBytes(String hexString) {
	    if (hexString == null || hexString.equals("")) {   
	        return null;   
	    }   
	    hexString = hexString.toUpperCase();   
	    int length = hexString.length() / 2;   
	    char[] hexChars = hexString.toCharArray();   
	    byte[] d = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;   
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
	    }   
	    return d;   
	}
    
    private static byte charToByte(char c) {   
    	return (byte) "0123456789ABCDEF".indexOf(c);   
    }
    
    private static String bytearray2Str(byte[] data, int start, int length, int targetLength) {
		long number = 0;
		if (data.length < start + length) {
			return "";
		}
		for (int i = 1; i <= length; i++) {
			number *= 0x100;
			number += (data[start + length - i] & 0xFF);
		}
		return String.format("%0" + targetLength + "d", number);
	}
    
    public static String toHexString(byte[] data) {
		if (data == null) {
			return "";
		}
		
		String string;
		StringBuilder stringBuilder = new StringBuilder();
		
		for (int i = 0; i < data.length; i++) {
			string = Integer.toHexString(data[i] & 0xFF);
			if (string.length() == 1) {
				stringBuilder.append("0");
			}
			
			stringBuilder.append(string.toUpperCase());
		}
		return stringBuilder.toString();
	}
    
    public void send26wiegand(View view){
    	if (mIDString != null){
			Toast.makeText(this, "send wg26 ret : " + PosUtil.getWg26Status(Long.parseLong(mIDString)), Toast.LENGTH_SHORT).show();
		}
    }
    
    public void send32wiegand(View view){
    	if (mIDString != null){
			Toast.makeText(this, "send wg32 ret : " + PosUtil.getWg32Status(Long.parseLong(mIDString)), Toast.LENGTH_SHORT).show();
		}
    }
    
    public void send34wiegand(View view){
    	if (mIDString != null){
			Toast.makeText(this, "send wg34 ret : " + PosUtil.getWg34Status(Long.parseLong(mIDString)), Toast.LENGTH_SHORT).show();
		}
    }
    
    public void setWGd0High(View view){
    	Toast.makeText(this, "" + PosUtil.setWGD0(1), Toast.LENGTH_SHORT).show();
    }
    
    public void setWGd0Low(View view){
    	Toast.makeText(this, "" + PosUtil.setWGD0(0), Toast.LENGTH_SHORT).show();
    }
    
    public void setWGd1High(View view){
    	Toast.makeText(this, "" + PosUtil.setWGD1(1), Toast.LENGTH_SHORT).show();
    }
    
    public void setWGd1Low(View view){
    	Toast.makeText(this, "" + PosUtil.setWGD1(0), Toast.LENGTH_SHORT).show();
    }
}
