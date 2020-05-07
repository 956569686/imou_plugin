package xl.honggv.cameraimouplugin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.lechange.common.configwifi.LCSmartConfig;
import com.lechange.opensdk.api.LCOpenSDK_Api;
import com.lechange.opensdk.api.bean.BindDevice;
import com.lechange.opensdk.api.bean.CheckDeviceBindOrNot;
import com.lechange.opensdk.api.bean.DeviceOnline;
import com.lechange.opensdk.api.bean.UnBindDevice;
import com.lechange.opensdk.api.bean.UnBindDeviceInfo;
import com.lechange.opensdk.api.client.BaseRequest;
import com.lechange.opensdk.api.client.BaseResponse;
import com.lechange.opensdk.configwifi.LCOpenSDK_ConfigWifi;
import com.lechange.opensdk.device.LCOpenSDK_DeviceInit;
import com.lechange.opensdk.media.DeviceInitInfo;

import java.util.List;

public class Business {

    private static class Instance {
        static Business instance = new Business();
    }

    public static Business getInstance() {
        return Instance.instance;
    }

    private String mToken = ""; // userToken或accessToken

    private String mHost = "openapi.lechange.cn:443";
    private String mAppId = "lc5bcb62d07bd146dd";
    private String mAppSecret = "8a4c3e270ce54cc881537f7b2cc8fb";

    private DeviceInitInfo deviceInitInfo;

    private static final int CONFIG_WIFI_TIMEOUT_TIME = 120 * 1000;
    private static final int CONFIG_SEARCH_DEVICE_TIME = 120 * 1000;

    /**
     * 设置AccessToken
     *
     * @param mToken
     */
    public void setToken(String mToken) {
        this.mToken = mToken;
    }

    /**
     * 管理员登陆设备
     */
    public void adminlogin(final Handler handler) {
        ///初始化client对象，配置client参数
        LCOpenSDK_Api.setHost(mHost);

        // 请求AccessToken
        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {
            @Override
            public void run() {
                OpenApiHelper.getAccessToken(mHost, "", mAppId, mAppSecret, handler);
            }
        });
    };

    /**
     * 为账号绑定设备
     *
     * @param deviceID
     *            序列号
     * @param handler
     */
    public void bindDevice(final String deviceID, final String key,final String mToken,
                           final Handler handler) {
        bindDevice(deviceID, key, key,mToken, handler);
    }

    public void bindDevice(final String deviceID, final String code,
                           final String key,final String mToken, final Handler handler) {

        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {

            @Override
            public void run() {
                BindDevice req = new BindDevice();
                req.data.token = mToken;
                req.data.deviceId = deviceID;
                req.data.code = code;
                //Log.e("*********WIRELESS: ", "Business:bindDevice()*********"+code+" "+deviceID+" "+mToken);
                // BindDevice.Response resp = null;
                RetObject retObject = null;
                retObject = request(req, 5 * 1000);
                handler.obtainMessage(retObject.mErrorCode, retObject)
                        .sendToTarget();
            }
        });
    }

    /**
     * 为账号解绑设备
     */
    public void unBindDevice(final String deviceID,final String mToken, final Handler handler) {

        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {
            @Override
            public void run() {
                UnBindDevice req = new UnBindDevice();
                req.data.token = mToken;
                req.data.deviceId = deviceID;
                // UnBindDevice.Response resp = null;
                RetObject retObject = null;
                retObject = request(req, 15 * 1000);
                handler.obtainMessage(retObject.mErrorCode, retObject)
                        .sendToTarget();
            }
        });
    }

    /**
     * 无线配对校验
     * @param deviceId
     * @param handler
     */
    public void checkBindOrNot(final String deviceId,final String token,final Handler handler) {
        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                CheckDeviceBindOrNot req = new CheckDeviceBindOrNot();
                mToken = token;
                req.data.token = token;
                req.data.deviceId = deviceId; // 设备id。
                // CheckDeviceBindOrNot.Response resp = null;
                RetObject retObject = null;
                retObject = request(req);

                // 将标示符和返回体发送给handle处理
                // retobject.resp = resp;
                handler.obtainMessage(retObject.mErrorCode, retObject)
                        .sendToTarget();
            }
        });
    }


    /**
     * 开启无线配网流程（权限检查，配对说明）
     */
    public void showWifiConfig(String ssid,String ssid_pwd,String deviceId,String token,Handler handler) {
        boolean isMinSDKM = Build.VERSION.SDK_INT < 23;
//        boolean isGranted = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
//        if (isMinSDKM) {
            mToken = token;
            startConfig(ssid,ssid_pwd,deviceId,handler);
            // 开启无线配对
//            return;
//        }
    }

    /**
     * 启动无线配对
     */
    private void startConfig(String ssid,String ssid_pwd,String deviceId,Handler handler) {
//        String mCapabilities = getWifiCapabilities(context,ssid);
        // 调用接口，开始通过smartConfig匹配 (频率由11000-->17000)
        LCOpenSDK_ConfigWifi.configWifiStart(deviceId, ssid, ssid_pwd, "WPA2", LCSmartConfig.ConfigType.LCConfigWifi_Type_ALL,true,11000,1);
        searchDevice(deviceId,CONFIG_SEARCH_DEVICE_TIME,handler);//搜索设备及超时时间
    }

    public void initDeviceByIP(final String mac, final String ip, final String key, final Handler handler){
        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real"){
            @Override
            public void run() {
                int ret = LCOpenSDK_DeviceInit.getInstance().initDeviceByIP(mac, ip, key);
                String msg = "";
                if(ret == 0){
                    msg = "Init success";
                }
                else if(ret == -1){
                    msg = "input param is empty";
                }
                else{//ret = error code
                    msg = "InitDevAccountByIP failed";
                }

                handler.obtainMessage(ret, msg).sendToTarget();
            }
        });
    }

    public void initDevice(final String mac, final String key, final Handler handler){
        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real"){
            @Override
            public void run() {
                int ret = LCOpenSDK_DeviceInit.getInstance().initDevice(mac, key);
                String msg = "";
                if(ret == 0){
                    msg = "Init success";
                }
                else if(ret == -1){
                    msg = "input param is empty";
                }
                else{//ret = error code
                    msg = "InitDevAccount failed";
                }

                handler.obtainMessage(ret, msg).sendToTarget();
            }
        });
    }

    /**
     * 设备在线状态
     *
     * @param deviceId
     * @param handler
     */
    public void checkOnline(final String deviceId, final String token, final Handler handler) {
        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                DeviceOnline req = new DeviceOnline();
                mToken = token;
                req.data.token = mToken;
                req.data.deviceId = deviceId; // 设备id。
                RetObject retObject = null;
                retObject = request(req);

                // 将标示符和返回体发送给handle处理
                // retobject.resp = resp;
                handler.obtainMessage(retObject.mErrorCode, retObject)
                        .sendToTarget();
            }
        });
    }

    public void unBindDeviceInfo(final String deviceID,final String mToken, final Handler handler){
        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                UnBindDeviceInfo req = new UnBindDeviceInfo();
                req.data.token = mToken;
                req.data.deviceId = deviceID; // 设备id。
                RetObject retObject = null;
                retObject = request(req);
                UnBindDeviceInfo.Response resp = (UnBindDeviceInfo.Response)retObject.resp;
                if(resp==null || resp.data == null || resp.data.ability == null){
                    Log.e("","unBindDeviceInfo response data is null");
                    handler.obtainMessage(1000, "unBindDeviceInfo response data is null").sendToTarget();
                    return;
                }

                retObject.resp = resp.data.ability;
                // 将标示符和返回体发送给handle处理
                handler.obtainMessage(retObject.mErrorCode, retObject.resp).sendToTarget();

            }
        });
    }

    /**
     * 获取wifi加密信息
     */
    private String getWifiCapabilities(Context context,String ssid) {
        String mCapabilities = null;
        ScanResult mScanResult = null;
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Activity.WIFI_SERVICE);
        if (mWifiManager != null) {
            WifiInfo mWifi = mWifiManager.getConnectionInfo();
            if (mWifi != null) {
                // 判断SSID是否�?��
                if (mWifi.getSSID() != null
                        && mWifi.getSSID().replaceAll("\"", "").equals(ssid)) {
                    List<ScanResult> mList = mWifiManager.getScanResults();
                    if (mList != null) {
                        for (ScanResult s : mList) {
                            if (s.SSID.replaceAll("\"", "").equals(ssid)) {
                                mScanResult = s;
                                break;
                            }
                        }
                    }
                }
            }
        }
        mCapabilities = mScanResult != null ? mScanResult.capabilities : null;
        return mCapabilities;
    }


    /**
     * 关闭无线配对
     */
    public void stopConfig() {
        LCOpenSDK_ConfigWifi.configWifiStop();// 调用smartConfig停止接口
        LCOpenSDK_DeviceInit.getInstance().stopSearchDeviceEx();
    }

    private void searchDevice(final String deviceId, final int timeout, final Handler handler) {

        TaskPoolHelper.addTask(new TaskPoolHelper.RunnableTask("real") {
            @Override
            public void run() {
                int ret = -1;
                String msg = "";
                deviceInitInfo = LCOpenSDK_DeviceInit.getInstance().searchDeviceInitInfo(deviceId, timeout);
                if (deviceInitInfo != null) {
                    if (deviceInitInfo.mStatus == 0) { // 不支持初始化操作
                        ret = 0;
                        msg = "device not support init";
                    } else if (deviceInitInfo.mStatus == 1) { // 未初始化
                        ret = 1;
                        msg = "device not init yet";
                    } else if(deviceInitInfo.mStatus == 2){ // 已初始化
                        ret = 2;
                        msg = "device already init";
                    }else {
                        ret = -2;
                        msg = "device not found";
                    }
                } else {
                    ret = -1;
                    msg = "StartSearchDevices failed";
                }
                handler.obtainMessage(ret, deviceInitInfo).sendToTarget();
            }
        });
    }


    /**
     * 发送网络请求，并对请求结果的错误码进行处理
     *
     * @param req
     * @return
     */
    private RetObject request(BaseRequest req) {
        return request(req, 15 * 1000);
    }

    /**
     * 发送网络请求，并对请求结果的错误码进行处理
     *
     * @param req
     * @param timeOut 访问dms接口时，超时时间设置长一点
     * @return
     * @throws Exception
     */
    private RetObject request(BaseRequest req, int timeOut) {
        // T t = LCOpenSDK_Api.request(req, timeOut);
        RetObject retObject = new RetObject();
        BaseResponse t = null;
        try {
            t = LCOpenSDK_Api.request(req, timeOut);
            // Log.d(tag, req.getBody());

            if (t.getCode() == HttpCode.SC_OK) {
                // 请求成功，则看服务器处理错误
                if (!t.getApiRetCode().equals("0"))
                    retObject.mErrorCode = 2; // 业务错误
                retObject.mMsg = "business errorcode: " + t.getApiRetCode()
                        + ", error msg: " + t.getApiRetMsg();
            } else {
                retObject.mErrorCode = 1; // http错误
                retObject.mMsg = "HTTP errorcode: " + t.getCode()
                        + ", error msg: " + t.getDesc();
            }
        } catch (Exception e) {
            // if timeout,return;
            e.printStackTrace();
            retObject.mErrorCode = -1000;
            retObject.mMsg = "inner errorcode : -1000, error msg: "
                    + e.getMessage();
            Log.d("乐橙sdk请求异常", req.getBody() + retObject.mMsg);
        }
        retObject.resp = t;
        return retObject;
    }


    public final class HttpCode {
        public static final int SC_OK = 200;// OK
        // （API调用成功，但是具体返回结果，由content中的code和desc描述）
        public static final int Bad_Request = 400;// Bad Request （API格式错误，无返回内容）
        public static final int Unauthorized = 401;// Unauthorized
        // （用户名密码认证失败，无返回内容）
        public static final int Forbidden = 403;// Forbidden （认证成功但是无权限，无返回内容）
        public static final int Not_Found = 404;// Not Found （请求的URI错误，无返回内容）
        public static final int Precondition_Failed = 412;// Precondition Failed
        // （先决条件失败，无返回内容。通常是由于客户端所带的x-hs-date时间与服务器时间相差过大。）
        public static final int Internal_Server_Error = 500;// Internal Server
        // Error
        // （服务器内部错误，无返回内容）
        public static final int Service_Unavailable = 503;// Service Unavailable
        // （服务不可用，无返回内容。这种情况一般是因为接口调用超出频率限制。）
    }

    public static class RetObject {
        public int mErrorCode = 0; // 错误码表示符 -1:返回体为null，0：成功，1：http错误， 2：业务错误
        public String mMsg;
        public Object resp;
    }
}
