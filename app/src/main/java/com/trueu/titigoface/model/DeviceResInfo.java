package com.trueu.titigoface.model;

/**
 * Created by Colin
 * on 2020/8/24
 * E-mail: hecanqi168@gmail.com
 */
public class DeviceResInfo {
    public String ipAddr;
    public int ipType;
    public String mac;
    public int plotDetailId;
    private static volatile DeviceResInfo deviceResInfo;


    public static DeviceResInfo getInstance() {
        if (deviceResInfo == null) {
            synchronized (DeviceResInfo.class) {
                if (deviceResInfo == null) {
                    deviceResInfo = new DeviceResInfo();
                }
            }
        }
        return deviceResInfo;
    }


    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getIpType() {
        return ipType;
    }

    public void setIpType(int ipType) {
        this.ipType = ipType;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getPlotDetailId() {
        return plotDetailId;
    }

    public void setPlotDetailId(int plotDetailId) {
        this.plotDetailId = plotDetailId;
    }
}
