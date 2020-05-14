package xl.honggv.cameraimouplugin;

import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.lechange.opensdk.api.bean.CheckDeviceBindOrNot;
import com.lechange.opensdk.api.bean.DeviceOnline;
import com.lechange.opensdk.media.DeviceInitInfo;

import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * CameraimoupluginPlugin
 */
public class CameraimoupluginPlugin implements MethodCallHandler {

    //初始化密匙
    String key = "hgwL123456";

    private EventChannel.EventSink eventSink = null;

    private EventChannel.StreamHandler streamHandler = new EventChannel.StreamHandler() {
        @Override
        public void onListen(Object arguments, EventChannel.EventSink sink) {
            eventSink = sink;
        }

        @Override
        public void onCancel(Object arguments) {
            eventSink = null;
        }
    };

    private CameraimoupluginPlugin(Registrar registrar, MethodChannel channel) {

        EventChannel eventChannel = new EventChannel(registrar.messenger(), "cameraimouplugin_event");
        eventChannel.setStreamHandler(streamHandler);
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(), "cameraimouplugin");
        channel.setMethodCallHandler(new CameraimoupluginPlugin(registrar, channel));

    }

    @Override
    public void onMethodCall(MethodCall call, final Result result) {
        if (call.method.equals("init")) {
            Business.getInstance().adminlogin(new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (0 == msg.what) {
                        String accessToken = (String) msg.obj;
//                        Business.getInstance().setToken(accessToken);
                        ///获取accessToken成功

                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "token");
                            params.putString("code", "0");
                            params.putString("value", accessToken);
                            eventSink.success(params.toMap());
                        }
                        Log.e("获取token成功：", accessToken);
                    } else {
                        ///获取accessToken失败

                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "token");
                            params.putString("code", "-1");
                            params.putString("value", "");
                            eventSink.success(params.toMap());
                        }
                        Log.e("获取token失败：", "");
                    }
                }
            });
        } else if (call.method.equals("bind_camera")) {
            ///获取参数
            String ssId = call.argument("ssId");
            String ssIdPwd = call.argument("ssIdPwd");
            String deviceId = call.argument("deviceId");
            String token = call.argument("token");
            ///无线配对校验
            if (TextUtils.isEmpty(ssId) || TextUtils.isEmpty(ssIdPwd) || TextUtils.isEmpty(deviceId) || TextUtils.isEmpty(token)) {
                if (eventSink != null) {
                    ConstraintMap params = new ConstraintMap();
                    params.putString("event", "checkBindOrNot");
                    params.putString("code", "-1");
                    params.putString("value", "无线配对校验参数不合法");
                    eventSink.success(params.toMap());
                }
            } else {
                checkBindOrNot(ssId.trim(), ssIdPwd.trim(), deviceId.trim(), token.trim());
            }
//            checkBindOrNot("hgwl", "hgwl1234567890", "5E04159PAJE23AE", "At_00005069d8ea210a49348026d1fe5489");
        } else if (call.method.equals("un_bind_camera")) {
            String deviceId = call.argument("deviceId");
            String token = call.argument("token");
            unBindDevice(deviceId, token);
        } else {
            result.notImplemented();
        }
    }

    ///无线配对校验
    private void checkBindOrNot(final String ssId, final String ssIdPwd, final String deviceId, final String token) {
        Business.getInstance().checkBindOrNot(deviceId, token, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                super.handleMessage(msg);
                Business.RetObject retObject = (Business.RetObject) msg.obj;
                if (msg.what == 0) {
                    CheckDeviceBindOrNot.Response resp = (CheckDeviceBindOrNot.Response) retObject.resp;
                    if (!resp.data.isBind) {
//                        result.success("无线配对校验成功");
                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "checkBindOrNot");
                            params.putString("code", "1");
                            params.putString("value", "无线配对校验成功");
                            eventSink.success(params.toMap());
                        }
                        ///开启设备查找绑定流程
                        showWifiConfig(ssId, ssIdPwd, deviceId, token);
                    } else if (resp.data.isBind && resp.data.isMine) {
                        ///设备已经被自己绑定
//                        result.error("-2","无线配对校验",retObject.mMsg);
                        Log.e("无线配对校验：", "无线配对校验设备已经被自己绑定");
                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "checkBindOrNot");
                            params.putString("code", "-1");
                            params.putString("value", "设备已经被自己绑定");
                            eventSink.success(params.toMap());
                        }
                    } else {
                        ///设备已经被别人绑定
//                        result.error("-2","无线配对校验",retObject.mMsg);
                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "checkBindOrNot");
                            params.putString("code", "-1");
                            params.putString("value", "设备已经被别人绑定");
                            eventSink.success(params.toMap());
                        }
                        Log.e("无线配对校验：", "设备已经被别人绑定");
                    }
                } else {
                    ///设备绑定异常 retObject.mMsg
//                    result.error("-2","无线配对校验",retObject.mMsg);
                    if (eventSink != null) {
                        ConstraintMap params = new ConstraintMap();
                        params.putString("event", "checkBindOrNot");
                        params.putString("code", "-1");
                        params.putString("value", "设备绑定异常:" + ssId+"\n"+ ssIdPwd+"\n"+ deviceId+"\n"+ token+"\n"+retObject.mMsg);
                        eventSink.success(params.toMap());
                    }
                    Log.e("无线配对校验：", retObject.mMsg);
                }
            }
        });
    }


    //解绑设备
    private void unBindDevice(String deviceId, final String token) {
        Business.getInstance().unBindDevice(deviceId, token, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Business.RetObject retObject = (Business.RetObject) msg.obj;
                if (msg.what == 0) {
//                    result.success("解绑设备成功");
                    if (eventSink != null) {
                        ConstraintMap params = new ConstraintMap();
                        params.putString("event", "unBindDevice");
                        params.putString("value", "解绑设备成功");
                        eventSink.success(params.toMap());
                    }
                    Log.e("解绑设备成功：", retObject.mMsg);
                } else {
//                    result.error("-8","解绑设备失败",retObject.mMsg);
                    if (eventSink != null) {
                        ConstraintMap params = new ConstraintMap();
                        params.putString("event", "unBindDevice");
                        params.putString("value", "解绑设备失败");
                        eventSink.success(params.toMap());
                    }
                    Log.e("解绑设备失败：", retObject.mMsg);
                }

            }
        });
    }

    private void showWifiConfig(String ssId, String ssIdPwd, final String deviceId, final String token) {
        Business.getInstance().showWifiConfig(ssId, ssIdPwd, deviceId, token, new Handler() {
            public void handleMessage(final Message msg) {
                if (msg.what < 0) {
                    if (msg.what == -2) {
                        ///未找到设备
//                        result.error("-3","未找到设备",null);
                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "checkBindOrNot");
                            params.putString("code", "-1");
                            params.putString("value", "未找到设备:\n" + msg.obj);
                            eventSink.success(params.toMap());
                        }
                        Log.e("无线添加设备：", "未找到设备");
                    } else {
                        ///设备查找失败
//                        result.error("-3","设备查找失败",null);
                        if (eventSink != null) {
                            ConstraintMap params = new ConstraintMap();
                            params.putString("event", "checkBindOrNot");
                            params.putString("code", "-1");
                            params.putString("value", "设备查找失败:\n" + msg.obj);
                            eventSink.success(params.toMap());
                        }
                        Log.e("无线添加设备：", "设备查找失败");
                    }
                    return;
                }
                ///设备查找成功
                ///mHandler.obtainMessage(DEVICE_SEARCH_SUCCESS, msg.obj).sendToTarget();
                Log.e("无线添加设备：", "设备查找成功");
                ///关闭无线配对
//                Business.getInstance().stopConfig();

                if (eventSink != null) {
                    ConstraintMap params = new ConstraintMap();
                    params.putString("event", "checkBindOrNot");
                    params.putString("code", "2");
                    params.putString("value", "设备查找成功");
                    eventSink.success(params.toMap());
                }
                ///查询设备是否在线
                initDevice((DeviceInitInfo) msg.obj, deviceId, token);
            }
        });
    }

    private void initDevice(DeviceInitInfo deviceInitInfo, final String deviceId, final String token) {
        final int status = deviceInitInfo.mStatus;
        //not support init
        if (status == 0) {
            ///检查设备是否在线
            checkOnline(deviceId, token);
        } else {
            if (status == 0 || status == 2) {
//                if (Business.getInstance().isOversea)
//                    checkPwdValidity(deviceId, deviceInitInfo.mIp, deviceInitInfo.mPort, key, mHandler);
//                else
                ///检查设备是否在线
                checkOnline(deviceId, token);
            } else if (status == 1) {
                Business.getInstance().initDevice(deviceInitInfo.mMac, key.trim(), new Handler() {
                    public void handleMessage(Message msg) {
                        String message = (String) msg.obj;
                        if (msg.what == 0) {
//                            result.success("设备初始化成功");
                            if (eventSink != null) {
                                ConstraintMap params = new ConstraintMap();
                                params.putString("event", "checkBindOrNot");
                                params.putString("code", "3");
                                params.putString("value", "设备初始化成功");
                                eventSink.success(params.toMap());
                            }
                            Log.e("设备初始化成功：", "设备初始化成功");
                            ///检查设备是否在线
                            checkOnline(deviceId, token);
                        } else {
                            if (eventSink != null) {
                                ConstraintMap params = new ConstraintMap();
                                params.putString("event", "checkBindOrNot");
                                params.putString("code", "-1");
                                params.putString("value", "初始化设备失败:\n"+message);
                                eventSink.success(params.toMap());
                            }
//                            result.error("-7","初始化设备失败",null);
                            Log.e("初始化设备失败：", "初始化设备失败");
                        }
                    }
                });
            }
        }
    }

    /**
     * 校验在线
     */
    private void checkOnline(final String deviceId, final String token) {
        Business.getInstance().checkOnline(deviceId, token,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Business.RetObject retObject = (Business.RetObject) msg.obj;
                        switch (msg.what) {
                            case 0:
                                if (((DeviceOnline.Response) retObject.resp).data.onLine.equals("1")) {
                                    Log.e("检查设备是否在线成功：", "设备在线");
                                    if (eventSink != null) {
                                        ConstraintMap params = new ConstraintMap();
                                        params.putString("event", "checkBindOrNot");
                                        params.putString("value", "正在检测设备是否在线");
                                        eventSink.success(params.toMap());
                                    }
//                                    result.success("检查设备是否在线成功");
                                    unBindDeviceInfo(deviceId, token);
                                } else {
//                                    result.error("-4","检查设备是否在线失败",null);
                                    if (eventSink != null) {
                                        ConstraintMap params = new ConstraintMap();
                                        params.putString("event", "checkBindOrNot");
                                        params.putString("value", "检查设备是否在线失败:\n"+retObject.mMsg+"\n"+((DeviceOnline.Response) retObject.resp).data.toString());
                                        eventSink.success(params.toMap());
                                    }
                                    Log.e("检查设备是否在线失败：", "检查设备是否在线失败");
                                }
                                break;
                            case -1000:
//                                result.error("-4","检查设备是否在线失败",null);
                                if (eventSink != null) {
                                    ConstraintMap params = new ConstraintMap();
                                    params.putString("event", "checkBindOrNot");
                                    params.putString("value", "检查设备是否在线失败:\n"+retObject.mMsg);
                                    eventSink.success(params.toMap());
                                }
                                Log.e("检查设备是否在线失败：", "检查设备是否在线失败");
                                break;
                            default:
//                                result.error("-4","检查设备是否在线失败",null);
                                if (eventSink != null) {
                                    ConstraintMap params = new ConstraintMap();
                                    params.putString("event", "checkBindOrNot");
                                    params.putString("value", "检查设备是否在线失败:\n"+retObject.mMsg);
                                    eventSink.success(params.toMap());
                                }
                                Log.e("default检查设备是否在线失败：", "检查设备是否在线失败");
                                break;
                        }
                    }
                });

    }

    private void unBindDeviceInfo(final String deviceId, final String mToken) {
        Business.getInstance().unBindDeviceInfo(deviceId, mToken, new Handler() {
            public void handleMessage(Message msg) {
                String message = (String) msg.obj;
                //				Log.d(tag, "unBindDeviceInfo,"+message);
                if (msg.what == 0) {
                    if (message.contains("Auth")) {
                        bindDevice(deviceId, mToken);
                    } else if (message.contains("RegCode")) {
                        bindDevice(deviceId, mToken);
                    } else {
                        bindDevice(deviceId, mToken);
                    }
                } else {
                    if (eventSink != null) {
                        ConstraintMap params = new ConstraintMap();
                        params.putString("event", "checkBindOrNot");
                        params.putString("value", "绑定设备失败 unBindDeviceInfo failed");
                        eventSink.success(params.toMap());
                    }
//                    result.error("-5","unBindDeviceInfo failed",null);
                    Log.e("unBindDeviceInfo：", "unBindDeviceInfo failed");
                }
            }
        });
    }

    /**
     * 绑定
     */
    private void bindDevice(String deviceId, final String token) {
        //设备绑定
        Business.getInstance().bindDevice(deviceId, key, token,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        Business.RetObject retObject = (Business.RetObject) msg.obj;
                        if (msg.what == 0) {
//                            result.success("设备绑定成功");
                            if (eventSink != null) {
                                ConstraintMap params = new ConstraintMap();
                                params.putString("event", "checkBindOrNot");
                                params.putString("value", "设备绑定成功");
                                eventSink.success(params.toMap());
                            }
                            Log.e("设备绑定成功：", "设备绑定成功");
                        } else {
                            if (eventSink != null) {
                                ConstraintMap params = new ConstraintMap();
                                params.putString("event", "checkBindOrNot");
                                params.putString("value", "设备绑定失败,请重置摄像机");
                                eventSink.success(params.toMap());
                            }
//                            result.error("-6","设备绑定失败",null);
                            Log.e("设备绑定失败：", retObject.mMsg);
                        }
                    }
                });
    }

}
